package me.skilled.parkourskills.events;


import javafx.util.Pair;
import me.skilled.parkourskills.configuration.ParkourConfig;
import me.skilled.parkourskills.configuration.ParkourPerms;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class ParkourMovement implements Listener
{
    // Store if player is holding to any wall
    private final HashMap<Player, Location> playerHoldingToWall = new HashMap<>();
    // Store last holding position to check later if is the same wall as before
    private final HashMap<Player, Location> playerLastPosition = new HashMap<>();
    // Store all tasks id to once dropped, stop it
    private final HashMap<Player, Integer> playerTaskIDs = new HashMap<>();
    // Store if player rolled
    private final HashMap<Player, Boolean> playerRolled = new HashMap<>();

    private Plugin plugin;

    private final double WALLRUNNING_SPEED = .35f;

    //TODO: PARKOUR ONLY WORKS ON SURVIVAL MODE

    // Check parkour actions
    public void checkParkour()
    {
        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run()
            {
                plugin.getServer().getOnlinePlayers().forEach(player -> {

                    if( !player.hasPermission( ParkourPerms.canParkour )) return;

                    // Check if player is wearing elytra
                    if( player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType().equals( Material. ELYTRA ) )
                        return;


                    if( ( (LivingEntity) player).isOnGround() && playerRolled.containsKey( player ))
                        playerRoll( player, false);


                    // When player jumps and it's not flying
                    if(!((LivingEntity) player).isOnGround() && !player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR))
                    {
                        // If player shift
                        if (player.isSneaking())
                        {
                            // If player's on the same wall as before
                            if( !ParkourConfig.canClimbSameWall )
                                if( checkIfIsSameWall(player) ) return;

                            if( ParkourConfig.canSlideWall )
                                if( isPlayerHolding(player)  && !playerTaskIDs.containsKey(player) ) startSliding(player);


                            // There's a excluded block in front of player
                            for(Material material: ParkourConfig.excludedBlocks)
                            {
                                if(checkTargetBlock(player, material))
                                {
                                    dropPlayerFromHolding(player, true);
                                    return;
                                }
                            }
                            dropPlayerFromHolding(player, false);

                            // If player move to much we drop from holding place
                            if( isPlayerHolding(player) )
                            {
                                if(player.getLocation().distance(playerHoldingToWall.get(player)) > 0f)
                                {
                                    dropPlayerFromHolding(player, true);
                                }
                            }
                        }

                        // On release sneak, we will make another jump
                        if (!player.isSneaking() && isPlayerHolding(player))
                        {
                            doSecondJump(player);
                            dropPlayerFromHolding(player, true);
                        }
                    }

                    // If player is touching the ground in survival mode
                    if(((LivingEntity)player).isOnGround() && player.getGameMode().equals(GameMode.SURVIVAL))
                    {
                        restartParkourCounter(player);
                    }

                });

            }
        }, 1, 1);
    }

    @EventHandler
    private void playerDisconnectEvent(PlayerQuitEvent event)
    {
        restartParkourCounter( event.getPlayer() );
        playerFall( event.getPlayer(), true );
    }

    @EventHandler
    private void playerShiftEvent(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();
        // If player hasn't perms of rolling
        if( !player.hasPermission( ParkourPerms.canRoll ) ) return;

        if( event.isSneaking() && !(( LivingEntity ) player).isOnGround())
        {
            Location belowPlayer = new Location( player.getWorld(), player.getLocation().getX(), player.getLocation().getY() - 2, player.getLocation().getZ() );
            if( !player.getWorld().getBlockAt( belowPlayer ).getType().equals( Material.AIR ) )
            {
               // Player roll effect
               playerRoll( player, true);
            }
        }
    }


    @EventHandler
    private void playerMoveEvent( PlayerMoveEvent event )
    {
        Player player = event.getPlayer();

        // User has to have permissions or op
        if( !player.hasPermission( ParkourPerms.canWallRun ) && !player.isOp() ) return;
        // Gamemode has to be Survival
        if( !player.getGameMode().equals( GameMode.SURVIVAL ) ) return;

        // Has to be sprinting
        if( !player.isSprinting() )
        {
            playerFall( player, true);
            return;
        }

        // Cannot be on ground
        if( ((LivingEntity)player).isOnGround()) return;

        // Check that there's a block on a side
        if(  getLeftAndRightBlocks( player ).get(0).getType().equals( Material.AIR ) && getLeftAndRightBlocks( player ).get(1).getType().equals( Material.AIR ))
        {
            playerFall( player, true);
            return;
        }

        // Check excluded blocks
        for( Material mat : ParkourConfig.excludedBlocks )
        {
            // Check if null because of config.yml
            if( mat == null) continue;
            // Air checking was before, we won't have problems with this
            if( mat.equals( Material.AIR ) ) continue;

            // If user has an excluded block on a side, will fall
            if(  getLeftAndRightBlocks( player ).get(0).getType().equals( mat ) || getLeftAndRightBlocks( player ).get(1).getType().equals( mat ))
            {
                playerFall( player, true);
                return;
            }

        }

        // Player wall-run
        playerFall( player, false);

    }

    private ArrayList<Block> getLeftAndRightBlocks(Player player )
    {
        double distance = ParkourConfig.wallRunDistanceFromWall;
        // Right
        final float rightNewZ = (float)(player.getLocation().getZ() + ( -distance * Math.sin(Math.toRadians(player.getLocation().getYaw()))));
        final float rightNewX = (float)(player.getLocation().getX() + ( -distance * Math.cos(Math.toRadians(player.getLocation().getYaw()))));

        // Left
        final float leftNewZ = (float)(player.getLocation().getZ() + ( distance * Math.sin(Math.toRadians(player.getLocation().getYaw()))));
        final float leftNewX = (float)(player.getLocation().getX() + ( distance * Math.cos(Math.toRadians(player.getLocation().getYaw()))));

        Block rightBlock = player.getWorld().getBlockAt( new Location( player.getWorld(), rightNewX, player.getLocation().getY() + 1, rightNewZ ) );
        Block leftBlock = player.getWorld().getBlockAt( new Location( player.getWorld(), leftNewX, player.getLocation().getY() + 1, leftNewZ ) );


        return new ArrayList<>(Arrays.asList(leftBlock, rightBlock));
    }

    // Make player fall
    private void playerFall( Player player, boolean fall )
    {
        player.setGravity( fall );
        player.setAllowFlight( !fall );

        // Add velocity because of gravity disabled player will go slowly
        if( !fall ) player.setVelocity( new Vector( player.getLocation().getDirection().getX() * WALLRUNNING_SPEED, -ParkourConfig.wallRunGravity, player.getLocation().getDirection().getZ() * WALLRUNNING_SPEED));

    }

    // Player roll effect
    private void playerRoll( Player player, boolean rolled )
    {
        // If player get roll
        if( rolled )
        {
            playerRolled.put( player, true );
            player.setInvulnerable( true );
            return;
        }

        if( playerRolled.containsKey( player ) )
        {
            player.setInvulnerable( false );
            playerRolled.remove( player );
        }

    }

    // Check players target block
    private boolean checkTargetBlock(Player player, Material material)
    {
        return player.getTargetBlock(null, 1).getType().equals(material);
    }

    // Drop or not a Player holding to a wall
    private void dropPlayerFromHolding(Player player, boolean drop)
    {
        player.setGravity(drop);
        player.setAllowFlight(!drop);

        if(!drop)
        {
            player.setVelocity(new Vector().zero());
            playerHoldingToWall.put(player, player.getLocation());
        }
        else
        {
            if(playerLastPosition.containsKey(player)) playerLastPosition.replace(player, player.getLocation());
            else playerLastPosition.put(player, player.getLocation());

            player.spawnParticle(Particle.BLOCK_DUST, player.getLocation(), 5, player.getTargetBlock(null, 1).getType().createBlockData());

            playerHoldingToWall.remove(player);
            cancelTasks(player);
        }
    }

    // Start sliding on X time
    private void startSliding(Player player)
    {
        int id = plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run()
            {
                if(isPlayerHolding(player))
                {
                    dropPlayerFromHolding(player, true);
                }
            }
        }, ParkourConfig.startSlidingTime * 20).getTaskId();

        playerTaskIDs.put(player, id);
    }

    // Return if player is holding
    private boolean isPlayerHolding(Player player)
    {
        return playerHoldingToWall.containsKey(player);
    }

    // Check if player's on the same wall as before
    private boolean checkIfIsSameWall(Player player)
    {
        if(playerLastPosition.containsKey(player))
        {
            double maxX = Math.max(playerLastPosition.get(player).getX(), player.getLocation().getX());
            double minX = Math.min(playerLastPosition.get(player).getX(), player.getLocation().getX());

            double maxZ = Math.max(playerLastPosition.get(player).getZ(), player.getLocation().getZ());
            double minZ = Math.min(playerLastPosition.get(player).getZ(), player.getLocation().getZ());

            if(maxX - minX < 0.5f &&  maxZ - minZ < 0.5f )
            {
                dropPlayerFromHolding(player, true);
                return true;
            }
        }
        return false;
    }

    // Restart all HashMaps
    private void restartParkourCounter(Player player)
    {
        playerHoldingToWall.remove(player);
        playerLastPosition.remove(player);

        cancelTasks(player);

    }

    // Do the second jump when stop holding
    private void doSecondJump(Player player)
    {
        Vector secondJump = player.getVelocity().add( new Vector(0, ParkourConfig.doubleJumpForce, 0) );
        player.setVelocity( secondJump );
    }

    private void cancelTasks(Player player)
    {
        if( playerTaskIDs.containsKey(player) )
        {
            plugin.getServer().getScheduler().cancelTask( playerTaskIDs.get(player) );
            playerTaskIDs.remove(player);
        }
    }

    public void setPlugin(Plugin plugin){ this.plugin = plugin; }

}
