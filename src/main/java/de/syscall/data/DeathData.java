package de.syscall.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DeathData {

    private final String playerName;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final List<ItemStack> items;
    private final int experience;
    private final long timestamp;

    public DeathData(String playerName, Location location, List<ItemStack> items, int experience) {
        this.playerName = playerName;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.items = items;
        this.experience = experience;
        this.timestamp = System.currentTimeMillis();
    }

    public Location toLocation() {
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public int getExperience() {
        return experience;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isExpired(long durationMs) {
        return System.currentTimeMillis() - timestamp > durationMs;
    }
}