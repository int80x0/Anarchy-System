package de.syscall.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.syscall.AnarchySystem;
import de.syscall.data.HomeData;
import de.syscall.util.HomeDataAdapter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomesManager {

    private final AnarchySystem plugin;
    private final File homesFile;
    private final Gson gson;
    private Map<UUID, Map<Integer, HomeData>> playerHomes;

    public HomesManager(AnarchySystem plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), "homes.json");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(HomeData.class, new HomeDataAdapter())
                .create();
        this.playerHomes = new HashMap<>();
        loadHomes();
    }

    public void loadHomes() {
        if (!homesFile.exists()) {
            playerHomes = new HashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(homesFile)) {
            Type type = new TypeToken<Map<UUID, Map<Integer, HomeData>>>() {}.getType();
            Map<UUID, Map<Integer, HomeData>> loaded = gson.fromJson(reader, type);
            playerHomes = loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load homes: " + e.getMessage());
            playerHomes = new HashMap<>();
        }
    }

    public void saveHomes() {
        try {
            if (!homesFile.getParentFile().exists()) {
                homesFile.getParentFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(homesFile)) {
                gson.toJson(playerHomes, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save homes: " + e.getMessage());
        }
    }

    public boolean setHome(Player player, int homeNumber, Location location) {
        if (!canSetHome(player, homeNumber)) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        playerHomes.computeIfAbsent(uuid, k -> new HashMap<>());
        playerHomes.get(uuid).put(homeNumber, new HomeData(location));
        saveHomes();
        return true;
    }

    public Location getHome(Player player, int homeNumber) {
        UUID uuid = player.getUniqueId();
        Map<Integer, HomeData> homes = playerHomes.get(uuid);
        if (homes == null || !homes.containsKey(homeNumber)) {
            return null;
        }

        return homes.get(homeNumber).toLocation();
    }

    public boolean deleteHome(Player player, int homeNumber) {
        UUID uuid = player.getUniqueId();
        Map<Integer, HomeData> homes = playerHomes.get(uuid);
        if (homes == null || !homes.containsKey(homeNumber)) {
            return false;
        }

        homes.remove(homeNumber);
        saveHomes();
        return true;
    }

    public Map<Integer, HomeData> getPlayerHomes(Player player) {
        return playerHomes.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    public int getPlayerMaxHomes(Player player) {
        if (plugin.getLuckPermsManager().isAvailable()) {
            return plugin.getLuckPermsManager().getPlayerHomesByGroup(player);
        }

        ConfigManager config = plugin.getConfigManager();
        String[] ranks = {"legendary", "hero", "ultra", "epic"};

        for (String rank : ranks) {
            if (player.hasPermission("anarchy.homes." + rank)) {
                return config.getRankHomes(rank);
            }
        }

        return config.getDefaultHomes();
    }

    public boolean canSetHome(Player player, int homeNumber) {
        if (homeNumber < 1 || homeNumber > plugin.getConfigManager().getMaxHomes()) {
            return false;
        }

        int maxHomes = getPlayerMaxHomes(player);
        if (homeNumber > maxHomes) {
            return false;
        }

        return homeNumber == 1 || player.hasPermission("anarchy.homes.multiple");
    }

    public void reload() {
        loadHomes();
    }
}