package de.syscall.task;

import de.syscall.AnarchySystem;
import de.syscall.data.DeathCorpse;
import de.syscall.data.DeathData;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

public class CorpseSinkTask extends BukkitRunnable {

    private final AnarchySystem plugin;
    private final DeathCorpse deathCorpse;
    private final DeathData deathData;
    private final double sinkRate;
    private final double maxSinkDepth;
    private double currentSinkDepth;
    private boolean hasStartedSinking;

    public CorpseSinkTask(AnarchySystem plugin, DeathCorpse deathCorpse, DeathData deathData) {
        this.plugin = plugin;
        this.deathCorpse = deathCorpse;
        this.deathData = deathData;
        this.sinkRate = plugin.getConfigManager().getCorpseSinkRate();
        this.maxSinkDepth = plugin.getConfigManager().getCorpseSinkDepth();
        this.currentSinkDepth = 0.0;
        this.hasStartedSinking = false;
    }

    @Override
    public void run() {
        if (!deathCorpse.isValid()) {
            finishCorpse();
            return;
        }

        ArmorStand corpse = deathCorpse.getArmorStand();

        if (!hasStartedSinking) {
            hasStartedSinking = true;
            deathCorpse.setSinking(true);
        }

        currentSinkDepth += sinkRate;

        if (currentSinkDepth >= maxSinkDepth) {
            finishCorpse();
            return;
        }

        Location currentLocation = corpse.getLocation();
        Location newLocation = currentLocation.clone().subtract(0, sinkRate, 0);
        corpse.teleport(newLocation);
    }

    private void finishCorpse() {
        cancel();
        String playerName = deathCorpse.getPlayerName().toLowerCase();
        plugin.getDeathManager().cleanupPlayerCorpse(playerName);
        plugin.getDeathManager().spawnLootHead(deathData);
    }
}