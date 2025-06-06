package de.syscall.manager;

import com.comphenix.protocol.ProtocolLibrary;
import de.syscall.AnarchySystem;
import de.syscall.task.ParticleTaskCracked;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class ParticleManagerCracked {

    private final AnarchySystem plugin;
    private final Map<String, BukkitTask> activeTasks;
    private boolean protocolLibAvailable;

    public ParticleManagerCracked(AnarchySystem plugin) {
        this.plugin = plugin;
        this.activeTasks = new HashMap<>();
        try {
            ProtocolLibrary.getProtocolManager();
            this.protocolLibAvailable = true;
        } catch (Exception e) {
            this.protocolLibAvailable = false;
            plugin.getLogger().warning("ProtocolLib not found, particles disabled");
        }
    }

    public boolean isAvailable() {
        return protocolLibAvailable && plugin.getConfigManager().isModuleEnabled("home-particles");
    }

    public void startParticleTask(Player player) {
        if (!isAvailable()) return;

        stopParticleTask(player);

        ParticleTaskCracked task = new ParticleTaskCracked(plugin, player, this);
        BukkitTask bukkitTask = task.runTaskTimerAsynchronously(plugin, 0L, 2L);
        activeTasks.put(player.getName().toLowerCase(), bukkitTask);
    }

    public void stopParticleTask(Player player) {
        BukkitTask task = activeTasks.remove(player.getName().toLowerCase());
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void sendCircleParticles(Player player, Location center, double rotationAngle) {
        if (!isAvailable()) return;

        try {
            double radius = plugin.getConfigManager().getParticleCircleRadius();
            double yOffset = plugin.getConfigManager().getParticleYOffset();
            int particleCount = plugin.getConfigManager().getParticleCount();

            Location circleCenter = center.clone().add(0.0, yOffset, 0.0);

            for (int i = 0; i < particleCount; i++) {
                double angleOffset = (2 * Math.PI * i) / particleCount;
                double currentAngle = rotationAngle + angleOffset;

                double x = circleCenter.getX() + radius * Math.cos(currentAngle);
                double z = circleCenter.getZ() + radius * Math.sin(currentAngle);
                double y = circleCenter.getY();

                sendParticlePacket(player, x, y, z);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send particle packet: " + e.getMessage());
        }
    }

    private void sendParticlePacket(Player player, double x, double y, double z) {
        try {
            String particleType = plugin.getConfigManager().getParticleType();
            String particleColor = plugin.getConfigManager().getParticleColor();

            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType.toUpperCase());

            if (particle == org.bukkit.Particle.DUST && !particleColor.isEmpty()) {
                org.bukkit.Color color = parseColor(particleColor);
                org.bukkit.Particle.DustOptions dustOptions = new org.bukkit.Particle.DustOptions(color, 1.0f);
                player.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0, dustOptions, true);
            } else if (particle == org.bukkit.Particle.DUST_COLOR_TRANSITION && !particleColor.isEmpty()) {
                String[] colors = particleColor.split("-");
                org.bukkit.Color fromColor = parseColor(colors[0]);
                org.bukkit.Color toColor = colors.length > 1 ? parseColor(colors[1]) : fromColor;
                org.bukkit.Particle.DustTransition dustTransition = new org.bukkit.Particle.DustTransition(fromColor, toColor, 1.0f);
                player.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0, dustTransition, true);
            } else if (particle == org.bukkit.Particle.ENTITY_EFFECT && !particleColor.isEmpty()) {
                org.bukkit.Color color = parseColor(particleColor);
                player.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0, color, true);
            } else {
                player.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0, null, true);
            }
        } catch (Exception e) {
            try {
                player.spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to send particle to " + player.getName() + ": " + ex.getMessage());
            }
        }
    }

    private org.bukkit.Color parseColor(String colorConfig) {
        try {
            String[] rgb = colorConfig.trim().split(",");
            int r = Integer.parseInt(rgb[0].trim());
            int g = Integer.parseInt(rgb[1].trim());
            int b = Integer.parseInt(rgb[2].trim());
            return org.bukkit.Color.fromRGB(r, g, b);
        } catch (Exception e) {
            return org.bukkit.Color.AQUA;
        }
    }

    public void cleanupPlayer(Player player) {
        stopParticleTask(player);
    }

    public void reload() {
        Map<String, Player> playersToRestart = new HashMap<>();

        for (String playerName : activeTasks.keySet()) {
            Player player = plugin.getServer().getPlayer(playerName);
            if (player != null && player.isOnline()) {
                playersToRestart.put(playerName, player);
            }
        }

        shutdown();

        for (Player player : playersToRestart.values()) {
            startParticleTask(player);
        }
    }

    public void shutdown() {
        activeTasks.values().forEach(task -> {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        });
        activeTasks.clear();
    }
}