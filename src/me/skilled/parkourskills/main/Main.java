package me.skilled.parkourskills.main;

import me.skilled.parkourskills.utils.console.Logger;
import me.skilled.parkourskills.events.ParkourMovement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    private final int PLUGIN_ID = 9999;
    public static final String PARKOUR_SKILLS = ChatColor.LIGHT_PURPLE + "[" + ChatColor.LIGHT_PURPLE + "ParkourSkills" + ChatColor.LIGHT_PURPLE + "] ";

    @Override
    public void onEnable()
    {
        Logger.enablingClass("Parkour Movement");
        ParkourMovement parkourMovement =  new ParkourMovement();
        Logger.enabledClass("Parkour Movement");

        this.getServer().getPluginManager().registerEvents(parkourMovement, this);
        Logger.pluginEnabled();
        super.onEnable();
    }
}
