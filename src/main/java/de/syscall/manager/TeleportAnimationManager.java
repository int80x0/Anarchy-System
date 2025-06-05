package de.syscall.manager;

import de.syscall.AnarchySystem;
import de.syscall.task.TeleportAnimationTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportAnimationManager {

    private final AnarchySystem plugin;
    private final Map<UUID, BukkitTask> activeAnimations;

    public TeleportAnimationManager(AnarchySystem plugin) {
        this.plugin = plugin;
        this.activeAnimations = new ConcurrentHashMap<>();
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().isTeleportAnimationEnabled();
    }

    public void startTeleportAnimation(Player player, Location fromLocation, Location toLocation, Runnable onComplete) {
        if (!isEnabled()) {
            onComplete.run();
            return;
        }

        stopAnimation(player);

        TeleportAnimationTask task = new TeleportAnimationTask(plugin, player, fromLocation, toLocation, onComplete);
        BukkitTask bukkitTask = task.runTaskTimer(plugin, 0L, 2L);
        activeAnimations.put(player.getUniqueId(), bukkitTask);
    }

    public void stopAnimation(Player player) {
        BukkitTask task = activeAnimations.remove(player.getUniqueId());
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void cleanupPlayer(Player player) {
        stopAnimation(player);
    }

    public void shutdown() {
        activeAnimations.values().forEach(task -> {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        });
        activeAnimations.clear();
    }
}