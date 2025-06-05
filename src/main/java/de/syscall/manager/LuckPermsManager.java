package de.syscall.manager;

import de.syscall.AnarchySystem;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

public class LuckPermsManager {

    private final AnarchySystem plugin;
    private LuckPerms luckPerms;
    private boolean available;

    public LuckPermsManager(AnarchySystem plugin) {
        this.plugin = plugin;
        try {
            this.luckPerms = LuckPermsProvider.get();
            this.available = true;
        } catch (IllegalStateException e) {
            this.available = false;
        }
    }

    public boolean isAvailable() {
        return available && plugin.getConfigManager().isLuckPermsIntegrationEnabled();
    }

    public String getPlayerGroup(Player player) {
        if (!isAvailable()) return null;

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;

        return user.getPrimaryGroup();
    }

    public boolean hasGroup(Player player, String group) {
        if (!isAvailable()) return false;

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return false;

        return user.getInheritedGroups(user.getQueryOptions()).stream()
                .anyMatch(g -> g.getName().equalsIgnoreCase(group));
    }

    public int getPlayerHomesByGroup(Player player) {
        if (!isAvailable()) {
            return getPlayerHomesByPermissions(player);
        }

        ConfigManager config = plugin.getConfigManager();
        String[] ranks = {"legendary", "hero", "ultra", "epic"};

        for (String rank : ranks) {
            String groupName = config.getRankLuckPermsGroup(rank);
            if (hasGroup(player, groupName)) {
                return config.getRankHomes(rank);
            }
        }

        return config.getDefaultHomes();
    }

    private int getPlayerHomesByPermissions(Player player) {
        ConfigManager config = plugin.getConfigManager();
        String[] ranks = {"legendary", "hero", "ultra", "epic"};

        for (String rank : ranks) {
            String permission = config.getRankPermission(rank);
            if (player.hasPermission(permission)) {
                return config.getRankHomes(rank);
            }
        }

        return config.getDefaultHomes();
    }
}