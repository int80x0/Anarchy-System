package de.syscall.listener;

import de.syscall.AnarchySystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final AnarchySystem plugin;

    public PlayerListener(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getConfigManager().isModuleEnabled("packet-particles") &&
                plugin.getParticleManager().isAvailable()) {
            plugin.getParticleManager().startParticleTask(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getParticleManager() != null) {
            plugin.getParticleManager().cleanupPlayer(event.getPlayer());
        }
        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().cleanupPlayer(event.getPlayer());
        }
        if (plugin.getTeleportAnimationManager() != null) {
            plugin.getTeleportAnimationManager().cleanupPlayer(event.getPlayer());
        }
    }
}