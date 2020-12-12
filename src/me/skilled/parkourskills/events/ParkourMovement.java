package me.skilled.parkourskills.events;


import me.skilled.parkourskills.configuration.ParkourConfig;
import me.skilled.parkourskills.configuration.ParkourPerms;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashMap;


public class ParkourMovement implements Listener
{
    // Store if player is holding to any wall
    private final HashMap<Player, Location> playerHoldingToWall = new HashMap<>();
    // Store last holding position to check later if is the same wall as before
    private final HashMap<Player, Location> playerLastPosition = new HashMap<>();
    // Store all tasks id to once dropped, stop it
    private final HashMap<Player, Integer> playerTaskIDs = new HashMap<>();

    private Plugin plugin;

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
        Vector playerLocationNormalized = player.getLocation().toVector().normalize();
        Vector secondJump = playerLocationNormalized.multiply(new Vector(0, ParkourConfig.doubleJumpForce,0));

        player.setVelocity(secondJump);
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
