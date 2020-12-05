package me.skilled.parkourskills.events;


import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;


public class ParkourMovement implements Listener
{
    HashMap<Player, Boolean> playerHold = new HashMap<>();

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event)
    {
        LivingEntity playerEntity = event.getPlayer();
        Player player = event.getPlayer();

        // When player jumps and it's not flying
        if(!playerEntity.isOnGround() && !player.isFlying() && !player.getGameMode().equals(GameMode.CREATIVE))
        {
            // If player shift
            if(player.isSneaking())
            {
                // There's a block in front of player
                if(!player.getTargetBlock(null, 1).getType().equals(Material.AIR))
                {
                    freezePlayer(player, true);
                    playerHold.put(player, true);
                }
            }
            else if(!player.isSneaking() && playerHold.containsKey(player))
            {
                Vector vector = player.getLocation().getDirection().multiply(new Vector(0, -1, 0));
                player.setVelocity(vector);
                playerHold.remove(player);
            }
            else
            {
                if(!playerHold.containsKey(player))
                {
                    freezePlayer(player, false);
                }
            }



        }
    }

    private void freezePlayer(Player player, boolean freeze)
    {
        player.setGravity(!freeze);

        if(freeze) player.setVelocity(new Vector().zero());
    }

}
