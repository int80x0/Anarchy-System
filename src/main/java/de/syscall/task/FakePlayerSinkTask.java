package de.syscall.task;

import de.syscall.AnarchySystem;
import de.syscall.data.DeathData;
import de.syscall.util.FakePlayerCorpse;  // ← Korrigierter Import
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FakePlayerSinkTask extends BukkitRunnable {

    private final AnarchySystem plugin;
    private final FakePlayerCorpse fakeCorpse;
    private final DeathData deathData;
    private final double sinkRate;
    private final double maxSinkDepth;
    private double currentSinkDepth;

    public FakePlayerSinkTask(AnarchySystem plugin, FakePlayerCorpse fakeCorpse, DeathData deathData) {
        this.plugin = plugin;
        this.fakeCorpse = fakeCorpse;
        this.deathData = deathData;
        this.sinkRate = plugin.getConfigManager().getCorpseSinkRate();
        this.maxSinkDepth = plugin.getConfigManager().getCorpseSinkDepth();
        this.currentSinkDepth = 0.0;
    }

    @Override
    public void run() {
        if (fakeCorpse.getViewers().isEmpty()) {
            finishCorpse();
            return;
        }

        currentSinkDepth += sinkRate;

        if (currentSinkDepth >= maxSinkDepth) {
            finishCorpse();
            return;
        }

        Location currentLocation = fakeCorpse.getCurrentLocation();
        Location newLocation = currentLocation.clone().subtract(0, sinkRate, 0);
        fakeCorpse.teleport(newLocation);  // ← Verwende die teleport() Methode
    }

    private void finishCorpse() {
        cancel();
        String playerName = fakeCorpse.getPlayerName().toLowerCase();
        plugin.getDeathManager().cleanupPlayerCorpse(playerName);
        plugin.getDeathManager().spawnLootHead(deathData);
    }
}