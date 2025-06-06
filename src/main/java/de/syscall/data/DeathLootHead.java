package de.syscall.data;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class DeathLootHead {

    private final String playerName;
    private final ArmorStand armorStand;
    private final DeathData deathData;
    private final long spawnTime;
    private double currentRotation;

    public DeathLootHead(String playerName, ArmorStand armorStand, DeathData deathData) {
        this.playerName = playerName;
        this.armorStand = armorStand;
        this.deathData = deathData;
        this.spawnTime = System.currentTimeMillis();
        this.currentRotation = 0.0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public DeathData getDeathData() {
        return deathData;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public double getCurrentRotation() {
        return currentRotation;
    }

    public void setCurrentRotation(double rotation) {
        this.currentRotation = rotation;
    }

    public boolean isValid() {
        return armorStand != null && armorStand.isValid();
    }

    public void remove() {
        if (isValid()) {
            armorStand.remove();
        }
    }

    public boolean isExpired(long durationMs) {
        return System.currentTimeMillis() - spawnTime > durationMs;
    }

    public Location getLocation() {
        return isValid() ? armorStand.getLocation() : null;
    }
}