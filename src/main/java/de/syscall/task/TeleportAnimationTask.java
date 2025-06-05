package de.syscall.task;

import de.syscall.AnarchySystem;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportAnimationTask extends BukkitRunnable {

    private final AnarchySystem plugin;
    private final Player player;
    private final Location fromLocation;
    private final Location toLocation;
    private final Runnable onComplete;

    private int ticksElapsed = 0;
    private int totalTicks;
    private int soundInterval;
    private int lastSoundTick = -20;
    private final Location originalPlayerLocation;
    private final double movementThreshold;

    public TeleportAnimationTask(AnarchySystem plugin, Player player, Location fromLocation, Location toLocation, Runnable onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.fromLocation = fromLocation.clone();
        this.toLocation = toLocation.clone();
        this.onComplete = onComplete;
        this.totalTicks = plugin.getConfigManager().getTeleportAnimationDuration() * 10;
        this.soundInterval = 20;
        this.originalPlayerLocation = player.getLocation().clone();
        this.movementThreshold = plugin.getConfigManager().getTeleportAnimationMovementThreshold();
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead()) {
            cancelAnimation();
            return;
        }

        if (hasPlayerMoved()) {
            cancelAnimation();
            player.sendMessage(plugin.getConfigManager().getTeleportMessage("teleport-cancelled-movement").replace("&", "§"));
            return;
        }

        if (ticksElapsed >= totalTicks) {
            finishTeleport();
            return;
        }

        double progress = (double) ticksElapsed / totalTicks;
        double spiralHeight = plugin.getConfigManager().getTeleportAnimationHeight();

        spawnSpiralParticles(fromLocation, progress, true); 
        spawnSpiralParticles(toLocation, progress, false);
        spawnCircleParticles(fromLocation);
        spawnCircleParticles(toLocation);

        if (ticksElapsed - lastSoundTick >= soundInterval) {
            playSound();
            lastSoundTick = ticksElapsed;
        }

        ticksElapsed += 2;
    }

    private void spawnSpiralParticles(Location location, double progress, boolean upward) {
        double spiralHeight = plugin.getConfigManager().getTeleportAnimationHeight();
        double radius = plugin.getConfigManager().getTeleportAnimationRadius();
        int spiralParticleCount = plugin.getConfigManager().getTeleportAnimationSpiralParticleCount();
        double spiralTurns = plugin.getConfigManager().getTeleportAnimationSpiralTurns();

        for (int i = 0; i < spiralParticleCount; i++) {
            double particleProgress = progress + (i * (1.0 / spiralParticleCount)); // Gleichmäßiger Versatz

            if (particleProgress > 1.0) particleProgress -= 1.0;
            if (particleProgress < 0.0) particleProgress += 1.0;

            double currentProgress = upward ? particleProgress : 1.0 - particleProgress;

            double currentHeight;
            if (upward) {
                currentHeight = currentProgress * spiralHeight;
            } else {
                currentHeight = spiralHeight - (currentProgress * spiralHeight);
            }
            double currentAngle = currentProgress * spiralTurns * 2 * Math.PI;

            double x = location.getX() + radius * Math.cos(currentAngle);
            double y = location.getY() + currentHeight;
            double z = location.getZ() + radius * Math.sin(currentAngle);

            Location particleLocation = new Location(location.getWorld(), x, y, z);
            spawnSpiralParticle(particleLocation);
        }
    }

    private void spawnSpiralParticle(Location location) {
        if (location.getWorld() == null) return;

        String particleType = plugin.getConfigManager().getTeleportAnimationSpiralParticleType();
        String colorConfig = plugin.getConfigManager().getTeleportAnimationSpiralParticleColor();
        double viewDistance = plugin.getConfigManager().getTeleportAnimationViewDistance();

        try {
            Particle particle = Particle.valueOf(particleType.toUpperCase());

            for (Player nearbyPlayer : location.getWorld().getPlayers()) {
                if (nearbyPlayer.getLocation().distance(location) <= viewDistance) {

                    if (particle == Particle.DUST && !colorConfig.isEmpty()) {
                        org.bukkit.Color color = parseColor(colorConfig);
                        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
                        nearbyPlayer.spawnParticle(particle, location, 1, 0, 0, 0, 0, dustOptions, true);
                    } else if (particle == Particle.DUST_COLOR_TRANSITION && !colorConfig.isEmpty()) {
                        String[] colors = colorConfig.split("-");
                        org.bukkit.Color fromColor = parseColor(colors[0]);
                        org.bukkit.Color toColor = colors.length > 1 ? parseColor(colors[1]) : fromColor;
                        Particle.DustTransition dustTransition = new Particle.DustTransition(fromColor, toColor, 1.0f);
                        nearbyPlayer.spawnParticle(particle, location, 1, 0, 0, 0, 0, dustTransition, true);
                    } else {
                        nearbyPlayer.spawnParticle(particle, location, 1, 0, 0, 0, 0, null, true);
                    }
                }
            }
        } catch (Exception e) {
            for (Player nearbyPlayer : location.getWorld().getPlayers()) {
                if (nearbyPlayer.getLocation().distance(location) <= viewDistance) {
                    nearbyPlayer.spawnParticle(Particle.ELECTRIC_SPARK, location, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private boolean hasPlayerMoved() {
        Location currentLocation = player.getLocation();
        double distance = originalPlayerLocation.distance(currentLocation);
        return distance > movementThreshold;
    }

    private void cancelAnimation() {
        cancel();
        plugin.getTeleportAnimationManager().stopAnimation(player);
    }

    private void spawnCircleParticles(Location location) {
        double radius = plugin.getConfigManager().getTeleportAnimationRadius();
        int particleCount = plugin.getConfigManager().getTeleportAnimationParticleCount() * 2;
        double rotationSpeed = 0.1;
        double currentAngle = ticksElapsed * rotationSpeed;

        for (int i = 0; i < particleCount; i++) {
            double angle = currentAngle + (2 * Math.PI * i) / particleCount;
            double x = location.getX() + radius * Math.cos(angle);
            double z = location.getZ() + radius * Math.sin(angle);
            double y = location.getY() + 0.1;

            Location particleLocation = new Location(location.getWorld(), x, y, z);
            spawnWorldParticle(particleLocation);
        }
    }

    private void spawnWorldParticle(Location location) {
        if (location.getWorld() == null) return;

        String particleType = plugin.getConfigManager().getTeleportAnimationParticleType();
        String colorConfig = plugin.getConfigManager().getTeleportAnimationParticleColor();
        double viewDistance = plugin.getConfigManager().getTeleportAnimationViewDistance();

        try {
            Particle particle = Particle.valueOf(particleType.toUpperCase());

            for (Player nearbyPlayer : location.getWorld().getPlayers()) {
                if (nearbyPlayer.getLocation().distance(location) <= viewDistance) {

                    if (particle == Particle.DUST && !colorConfig.isEmpty()) {
                        org.bukkit.Color color = parseColor(colorConfig);
                        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
                        nearbyPlayer.spawnParticle(particle, location, 1, 0, 0, 0, 0, dustOptions, true);
                    } else if (particle == Particle.DUST_COLOR_TRANSITION && !colorConfig.isEmpty()) {
                        String[] colors = colorConfig.split("-");
                        org.bukkit.Color fromColor = parseColor(colors[0]);
                        org.bukkit.Color toColor = colors.length > 1 ? parseColor(colors[1]) : fromColor;
                        Particle.DustTransition dustTransition = new Particle.DustTransition(fromColor, toColor, 1.0f);
                        nearbyPlayer.spawnParticle(particle, location, 1, 0, 0, 0, 0, dustTransition, true);
                    } else {
                        nearbyPlayer.spawnParticle(particle, location, 1, 0, 0, 0, 0, null, true);
                    }
                }
            }
        } catch (Exception e) {
            for (Player nearbyPlayer : location.getWorld().getPlayers()) {
                if (nearbyPlayer.getLocation().distance(location) <= viewDistance) {
                    nearbyPlayer.spawnParticle(Particle.ELECTRIC_SPARK, location, 1, 0, 0, 0, 0);
                }
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

    private void playSound() {
        int currentSecond = (ticksElapsed / 20) + 1;
        int totalSeconds = plugin.getConfigManager().getTeleportAnimationDuration();

        String soundName;
        if (currentSecond == totalSeconds) {
            soundName = plugin.getConfigManager().getTeleportAnimationFinalSound();
        } else { 
            soundName = plugin.getConfigManager().getTeleportAnimationLoopSound();
        }

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = plugin.getConfigManager().getTeleportAnimationSoundVolume();
            float pitch = plugin.getConfigManager().getTeleportAnimationSoundPitch();

            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.5f, 1.0f);
        }
    }

    private void finishTeleport() {
        cancelAnimation();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }.runTask(plugin);
    }
}