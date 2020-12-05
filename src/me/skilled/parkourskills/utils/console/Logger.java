package me.skilled.parkourskills.utils.console;

import me.skilled.parkourskills.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    public static void enablingClass(String className)
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.PARKOUR_SKILLS + "&bEnabling " + className + "..."));
    }

    public static void enabledClass(String className)
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.PARKOUR_SKILLS +  "&b" + className + " successfully enabled!"));
    }

    public static void errorEnablingClass(String className, String err)
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.PARKOUR_SKILLS +  "&cCouldn't enable " + className + ". Error: " + err));
    }

    public static void pluginEnabled()
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.PARKOUR_SKILLS +  "&bPlugin Enabled!"));
    }

    public static void sendMessage(String message)
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.PARKOUR_SKILLS +  message));
    }

}
