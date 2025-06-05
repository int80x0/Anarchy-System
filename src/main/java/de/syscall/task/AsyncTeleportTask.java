package de.syscall.task;

import de.syscall.AnarchySystem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncTeleportTask extends BukkitRunnable {

    private final AnarchySystem plugin;
    private final Player player;
    private final Location location;
    private final String message;

    public AsyncTeleportTask(AnarchySystem plugin, Player player, Location location, String message) {
        this.plugin = plugin;
        this.player = player;
        this.location = location;
        this.message = message;
    }

    @Override
    public void run() {
        if (!player.isOnline()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(location);
                if (message != null && !message.isEmpty()) {
                    player.sendMessage(message);
                }
            }
        }.runTask(plugin);
    }
}