package me.skilled.parkourskills.main;

import me.skilled.parkourskills.configuration.ConfigLoader;
import me.skilled.parkourskills.utils.console.Logger;
import me.skilled.parkourskills.events.ParkourMovement;
import me.skilled.parkourskills.utils.metrics.Metrics;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    private final int PLUGIN_ID = 9570;
    public static final String PARKOUR_SKILLS = ChatColor.LIGHT_PURPLE + "[" + ChatColor.LIGHT_PURPLE + "ParkourSkills" + ChatColor.LIGHT_PURPLE + "] ";
    public Plugin plugin = this;

    @Override
    public void onEnable()
    {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        ConfigLoader configLoader = new ConfigLoader(this);

        Logger.enablingClass("Parkour Advanced Movement");
        ParkourMovement parkourMovement =  new ParkourMovement();
        Logger.enabledClass("Parkour Advanced Movement");

        Logger.enablingClass("Metrics");
        Metrics metrics = new Metrics(this, PLUGIN_ID);

        parkourMovement.setPlugin(this);
        parkourMovement.checkParkour();

        this.getServer().getPluginManager().registerEvents(parkourMovement, this);
        configLoader.loadConfig();
        Logger.pluginEnabled();
        super.onEnable();
    }
}
