package me.skilled.parkourskills.configuration;


import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;

public class ParkourConfig
{
    public static double doubleJumpForce = 2;
    public static double wallRunGravity = 0.03;
    public static double wallRunDistanceFromWall = 0.50;
    public static boolean canClimbSameWall = false;
    public static boolean canSlideWall = true;
    public static int startSlidingTime = 2; // Seconds
    public static ArrayList<Material> excludedBlocks = new ArrayList<>(Arrays.asList
                        ( Material.AIR, Material.WATER, Material.LAVA, Material.LADDER, Material.GRASS, Material.TALL_GRASS,
                        Material.LILAC, Material.TALL_GRASS, Material.PEONY, Material.ROSE_BUSH, Material.SUNFLOWER ) );
}
