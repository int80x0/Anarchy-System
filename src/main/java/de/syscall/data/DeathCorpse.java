package de.syscall.data;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class DeathCorpse {

    private final String playerName;
    private final ArmorStand armorStand;
    private final Location originalLocation;
    private final long spawnTime;
    private boolean sinking;

    public DeathCorpse(String playerName, ArmorStand armorStand, Location location) {
        this.playerName = playerName;
        this.armorStand = armorStand;
        this.originalLocation = location.clone();
        this.spawnTime = System.currentTimeMillis();
        this.sinking = false;
    }

    public String getPlayerName() {
        return playerName;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public boolean isSinking() {
        return sinking;
    }

    public void setSinking(boolean sinking) {
        this.sinking = sinking;
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
}