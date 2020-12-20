package me.skilled.parkourskills.commands;

import me.skilled.parkourskills.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.Console;

public class CommandHandler implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args)
    {
        Player player = (Player) sender;

        if( sender instanceof Player )
        {
            if (!player.isOp()) return true;

            if (args.length == 1)
            {
                if ( args[0].equalsIgnoreCase("reload") )
                {
                    reloadPlugin( player );
                    return true;
                }
            }

            player.sendMessage(  Main.PARKOUR_SKILLS + ChatColor.AQUA + "Use /pk reload");
        }

        return true;
    }

    private void reloadPlugin(Player player)
    {
        Main.getPlugin( Main.class ).getPluginLoader().disablePlugin( Main.getPlugin( Main.class ) );
        Main.getPlugin( Main.class ).getPluginLoader().enablePlugin( Main.getPlugin( Main.class ) );
        player.sendMessage( Main.PARKOUR_SKILLS + ChatColor.GREEN + "Reload complete!");
    }
}
