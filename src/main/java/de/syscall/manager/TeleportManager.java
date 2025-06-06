package de.syscall.manager;

import de.syscall.AnarchySystem;
import de.syscall.data.TeleportRequest;
import de.syscall.data.LastLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    private final AnarchySystem plugin;
    private final Map<UUID, TeleportRequest> pendingRequests;
    private final Map<UUID, Set<UUID>> blockedPlayers;
    private final Map<UUID, Boolean> tpaToggleStatus;
    private final Map<UUID, Long> lastTeleportTime;
    private final Map<UUID, LastLocation> lastLocations;
    private final Map<UUID, BukkitTask> timeoutTasks;

    public TeleportManager(AnarchySystem plugin) {
        this.plugin = plugin;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.blockedPlayers = new ConcurrentHashMap<>();
        this.tpaToggleStatus = new ConcurrentHashMap<>();
        this.lastTeleportTime = new ConcurrentHashMap<>();
        this.lastLocations = new ConcurrentHashMap<>();
        this.timeoutTasks = new ConcurrentHashMap<>();
    }

    public boolean sendTeleportRequest(Player requester, Player target, TeleportRequest.TeleportType type) {
        if (!tpaToggleStatus.getOrDefault(target.getUniqueId(), true)) {
            requester.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-disabled").replace("&", "§").replace("%player%", target.getName()));
            return false;
        }

        if (isBlocked(target, requester)) {
            requester.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-blocked").replace("&", "§").replace("%player%", target.getName()));
            return false;
        }

        long cooldown = plugin.getConfigManager().getTpaCooldown() * 1000L;
        if (isOnCooldown(requester, cooldown)) {
            long remaining = getRemainingCooldown(requester, cooldown);
            requester.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-cooldown").replace("&", "§").replace("%time%", String.valueOf(remaining / 1000)));
            return false;
        }

        UUID targetId = target.getUniqueId();
        if (pendingRequests.containsKey(targetId)) {
            requester.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-pending").replace("&", "§").replace("%player%", target.getName()));
            return false;
        }

        TeleportRequest request = new TeleportRequest(requester, target, type);
        pendingRequests.put(targetId, request);

        BukkitTask timeoutTask = timeoutTasks.remove(targetId);
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }

        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                pendingRequests.remove(targetId);
                timeoutTasks.remove(targetId);
                if (requester.isOnline()) {
                    requester.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-timeout").replace("&", "§").replace("%player%", target.getName()));
                }
                if (target.isOnline()) {
                    target.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-timeout-target").replace("&", "§").replace("%player%", requester.getName()));
                }
            }
        }.runTaskLater(plugin, plugin.getConfigManager().getTpaTimeout() * 20L);

        timeoutTasks.put(targetId, timeoutTask);

        String messageKey = type == TeleportRequest.TeleportType.TPA ? "tpa-sent" : "tpahere-sent";
        requester.sendMessage(plugin.getConfigManager().getTeleportMessage(messageKey).replace("&", "§").replace("%player%", target.getName()));

        messageKey = type == TeleportRequest.TeleportType.TPA ? "tpa-received" : "tpahere-received";
        String message = plugin.getConfigManager().getTeleportMessage(messageKey).replace("%player%", requester.getName());

        Component acceptComponent = Component.text("[ACCEPT]")
                .color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/tpaccept"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to accept")));

        Component denyComponent = Component.text("[DENY]")
                .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/tpdeny"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to deny")));

        target.sendMessage(Component.text(message.replace("&", "§"))
                .append(Component.text(" "))
                .append(acceptComponent)
                .append(Component.text(" "))
                .append(denyComponent));

        lastTeleportTime.put(requester.getUniqueId(), System.currentTimeMillis());
        return true;
    }

    public boolean acceptRequest(Player target) {
        UUID targetId = target.getUniqueId();
        TeleportRequest request = pendingRequests.remove(targetId);

        BukkitTask timeoutTask = timeoutTasks.remove(targetId);
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }

        if (request == null) {
            target.sendMessage(plugin.getConfigManager().getTeleportMessage("no-pending-request").replace("&", "§"));
            return false;
        }

        Player requester = request.getRequester();
        if (!requester.isOnline()) {
            target.sendMessage(plugin.getConfigManager().getTeleportMessage("requester-offline").replace("&", "§"));
            return false;
        }

        if (request.getType() == TeleportRequest.TeleportType.TPA) {
            saveLastLocation(requester);
            Location fromLocation = requester.getLocation();
            Location toLocation = target.getLocation();

            plugin.getTeleportAnimationManager().startTeleportAnimation(requester, fromLocation, toLocation, () -> {
                requester.teleport(toLocation);
            });
        } else {
            saveLastLocation(target);
            Location fromLocation = target.getLocation();
            Location toLocation = requester.getLocation();

            plugin.getTeleportAnimationManager().startTeleportAnimation(target, fromLocation, toLocation, () -> {
                target.teleport(toLocation);
            });
        }

        plugin.getHintManager().sendTeleportMessageWithHint(requester, "tpa-accepted", "teleport-animation", "player", target.getName());

        return true;
    }

    public boolean denyRequest(Player target) {
        UUID targetId = target.getUniqueId();
        TeleportRequest request = pendingRequests.remove(targetId);

        BukkitTask timeoutTask = timeoutTasks.remove(targetId);
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }

        if (request == null) {
            target.sendMessage(plugin.getConfigManager().getTeleportMessage("no-pending-request").replace("&", "§"));
            return false;
        }

        Player requester = request.getRequester();
        if (requester.isOnline()) {
            requester.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-denied").replace("&", "§").replace("%player%", target.getName()));
        }
        target.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-denied-target").replace("&", "§").replace("%player%", requester.getName()));

        return true;
    }

    public void toggleTpa(Player player) {
        UUID playerId = player.getUniqueId();
        boolean current = tpaToggleStatus.getOrDefault(playerId, true);
        tpaToggleStatus.put(playerId, !current);

        String messageKey = current ? "tpa-disabled-self" : "tpa-enabled-self";
        player.sendMessage(plugin.getConfigManager().getTeleportMessage(messageKey).replace("&", "§"));
    }

    public boolean teleportBack(Player player) {
        UUID playerId = player.getUniqueId();
        LastLocation lastLoc = lastLocations.get(playerId);

        if (lastLoc == null) {
            player.sendMessage(plugin.getConfigManager().getTeleportMessage("no-back-location").replace("&", "§"));
            return false;
        }

        long cooldown = plugin.getConfigManager().getBackCooldown() * 1000L;
        if (isOnCooldown(player, cooldown)) {
            long remaining = getRemainingCooldown(player, cooldown);
            player.sendMessage(plugin.getConfigManager().getTeleportMessage("back-cooldown").replace("&", "§").replace("%time%", String.valueOf(remaining / 1000)));
            return false;
        }

        Location location = lastLoc.toLocation();
        if (location == null) {
            player.sendMessage(plugin.getConfigManager().getTeleportMessage("back-world-not-found").replace("&", "§"));
            return false;
        }

        saveLastLocation(player);
        player.teleport(location);
        player.sendMessage(plugin.getConfigManager().getTeleportMessage("back-teleported").replace("&", "§"));
        lastTeleportTime.put(playerId, System.currentTimeMillis());

        return true;
    }

    public void saveLastLocation(Player player) {
        lastLocations.put(player.getUniqueId(), new LastLocation(player.getLocation()));
    }

    private boolean isOnCooldown(Player player, long cooldownMs) {
        if (player.hasPermission("anarchy.teleport.bypass-cooldown")) {
            return false;
        }

        Long lastTime = lastTeleportTime.get(player.getUniqueId());
        if (lastTime == null) return false;

        return System.currentTimeMillis() - lastTime < cooldownMs;
    }

    private long getRemainingCooldown(Player player, long cooldownMs) {
        Long lastTime = lastTeleportTime.get(player.getUniqueId());
        if (lastTime == null) return 0;

        long elapsed = System.currentTimeMillis() - lastTime;
        return Math.max(0, cooldownMs - elapsed);
    }

    private boolean isBlocked(Player target, Player requester) {
        Set<UUID> blocked = blockedPlayers.get(target.getUniqueId());
        return blocked != null && blocked.contains(requester.getUniqueId());
    }

    public void cleanupPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        pendingRequests.remove(playerId);
        BukkitTask task = timeoutTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public void shutdown() {
        timeoutTasks.values().forEach(task -> {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        });
        timeoutTasks.clear();
        pendingRequests.clear();
    }
}