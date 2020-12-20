package me.skilled.parkourskills.configuration;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class ConfigLoader
{
    private Plugin plugin;

    public ConfigLoader(Plugin plugin)
    {
        this.plugin = plugin;
    }

    public void loadConfig()
    {
        ParkourConfig.doubleJumpForce = plugin.getConfig().getDouble("second_jump_force");
        ParkourConfig.wallRunGravity = plugin.getConfig().getDouble("wall_run_gravity");
        ParkourConfig.wallRunDistanceFromWall = plugin.getConfig().getDouble("wall_run_distance_from_wall");
        ParkourConfig.canClimbSameWall = plugin.getConfig().getBoolean("can_climb_same_wall");
        ParkourConfig.canSlideWall = plugin.getConfig().getBoolean("slide_wall");
        ParkourConfig.startSlidingTime = plugin.getConfig().getInt("holding_time");

        String excludedBlocks = plugin.getConfig().getString("excluded_blocks");
        String[] excludedBlocksArray = Objects.requireNonNull(excludedBlocks).split(", ");

        for(String block : excludedBlocksArray)
        {
            ParkourConfig.excludedBlocks.add(Material.getMaterial(block));
        }
    }

}
