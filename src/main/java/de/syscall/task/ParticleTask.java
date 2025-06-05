package de.syscall.task;

import de.syscall.AnarchySystem;
import de.syscall.data.HomeData;
import de.syscall.manager.ParticleManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleTask extends BukkitRunnable {

    private final AnarchySystem plugin;
    private final Player player;
    private final ParticleManager particleManager;
    private final Map<Integer, Double> homeRotationAngles;
    private final double rotationSpeed;

    public ParticleTask(AnarchySystem plugin, Player player, ParticleManager particleManager) {
        this.plugin = plugin;
        this.player = player;
        this.particleManager = particleManager;
        this.homeRotationAngles = new HashMap<>();
        this.rotationSpeed = plugin.getConfigManager().getParticleRotationSpeed();
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        if (!particleManager.isAvailable()) {
            cancel();
            return;
        }

        Location playerLocation = player.getLocation();
        double detectionRadius = plugin.getConfigManager().getParticleDetectionRadius();
        Map<Integer, HomeData> playerHomes = plugin.getHomesManager().getPlayerHomes(player);

        for (Map.Entry<Integer, HomeData> entry : playerHomes.entrySet()) {
            int homeNumber = entry.getKey();
            HomeData homeData = entry.getValue();
            Location homeLocation = homeData.toLocation();

            if (homeLocation == null) continue;
            if (!homeLocation.getWorld().equals(playerLocation.getWorld())) continue;

            double distance = homeLocation.distance(playerLocation);
            if (distance <= detectionRadius) {
                double currentAngle = homeRotationAngles.getOrDefault(homeNumber, 0.0);
                particleManager.sendCircleParticles(player, homeLocation, currentAngle);

                currentAngle += rotationSpeed;
                if (currentAngle >= 2 * Math.PI) {
                    currentAngle -= 2 * Math.PI;
                }
                homeRotationAngles.put(homeNumber, currentAngle);
            } else {
                homeRotationAngles.remove(homeNumber);
            }
        }
    }
}