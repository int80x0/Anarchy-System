package de.syscall.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.syscall.AnarchySystem;
import de.syscall.data.HomeData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MigrationUtility {

    private final AnarchySystem plugin;
    private final Gson gson;

    public MigrationUtility(AnarchySystem plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(HomeData.class, new HomeDataAdapter())
                .create();
    }

    public boolean migrateFromUuidToNames() {
        File oldHomesFile = new File(plugin.getDataFolder(), "homes.json");
        File backupFile = new File(plugin.getDataFolder(), "homes_uuid_backup.json");
        File newHomesFile = new File(plugin.getDataFolder(), "homes_names.json");

        if (!oldHomesFile.exists()) {
            plugin.getLogger().info("No UUID-based homes file found to migrate");
            return false;
        }

        try {
            if (oldHomesFile.renameTo(backupFile)) {
                plugin.getLogger().info("Created backup of UUID homes file");
            }

            Map<UUID, Map<Integer, HomeData>> uuidHomes = loadUuidHomes(backupFile);
            Map<String, Map<Integer, HomeData>> nameHomes = convertToNameBased(uuidHomes);
            saveNameHomes(newHomesFile, nameHomes);

            plugin.getLogger().info("Successfully migrated " + nameHomes.size() + " player homes from UUID to names");
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to migrate homes: " + e.getMessage());
            if (backupFile.exists() && !oldHomesFile.exists()) {
                backupFile.renameTo(oldHomesFile);
                plugin.getLogger().info("Restored original homes file from backup");
            }
            return false;
        }
    }

    private Map<UUID, Map<Integer, HomeData>> loadUuidHomes(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<UUID, Map<Integer, HomeData>>>() {}.getType();
            Map<UUID, Map<Integer, HomeData>> loaded = gson.fromJson(reader, type);
            return loaded != null ? loaded : new HashMap<>();
        }
    }

    private Map<String, Map<Integer, HomeData>> convertToNameBased(Map<UUID, Map<Integer, HomeData>> uuidHomes) {
        Map<String, Map<Integer, HomeData>> nameHomes = new HashMap<>();
        int convertedCount = 0;
        int skippedCount = 0;

        for (Map.Entry<UUID, Map<Integer, HomeData>> entry : uuidHomes.entrySet()) {
            UUID uuid = entry.getKey();
            Map<Integer, HomeData> homes = entry.getValue();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String playerName = offlinePlayer.getName();

            if (playerName != null && !playerName.isEmpty()) {
                nameHomes.put(playerName.toLowerCase(), homes);
                convertedCount++;
                plugin.getLogger().info("Migrated homes for player: " + playerName + " (UUID: " + uuid + ")");
            } else {
                skippedCount++;
                plugin.getLogger().warning("Skipped UUID " + uuid + " - no player name found");
            }
        }

        plugin.getLogger().info("Migration summary: " + convertedCount + " converted, " + skippedCount + " skipped");
        return nameHomes;
    }

    private void saveNameHomes(File file, Map<String, Map<Integer, HomeData>> nameHomes) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(nameHomes, writer);
        }
    }

    public boolean migrateFromNamesToUuid() {
        File oldHomesFile = new File(plugin.getDataFolder(), "homes_names.json");
        File backupFile = new File(plugin.getDataFolder(), "homes_names_backup.json");
        File newHomesFile = new File(plugin.getDataFolder(), "homes.json");

        if (!oldHomesFile.exists()) {
            plugin.getLogger().info("No name-based homes file found to migrate");
            return false;
        }

        try {
            if (oldHomesFile.renameTo(backupFile)) {
                plugin.getLogger().info("Created backup of name-based homes file");
            }

            Map<String, Map<Integer, HomeData>> nameHomes = loadNameHomes(backupFile);
            Map<UUID, Map<Integer, HomeData>> uuidHomes = convertToUuidBased(nameHomes);
            saveUuidHomes(newHomesFile, uuidHomes);

            plugin.getLogger().info("Successfully migrated " + uuidHomes.size() + " player homes from names to UUID");
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to migrate homes: " + e.getMessage());
            if (backupFile.exists() && !oldHomesFile.exists()) {
                backupFile.renameTo(oldHomesFile);
                plugin.getLogger().info("Restored original homes file from backup");
            }
            return false;
        }
    }

    private Map<String, Map<Integer, HomeData>> loadNameHomes(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Map<Integer, HomeData>>>() {}.getType();
            Map<String, Map<Integer, HomeData>> loaded = gson.fromJson(reader, type);
            return loaded != null ? loaded : new HashMap<>();
        }
    }

    private Map<UUID, Map<Integer, HomeData>> convertToUuidBased(Map<String, Map<Integer, HomeData>> nameHomes) {
        Map<UUID, Map<Integer, HomeData>> uuidHomes = new HashMap<>();
        int convertedCount = 0;
        int skippedCount = 0;

        for (Map.Entry<String, Map<Integer, HomeData>> entry : nameHomes.entrySet()) {
            String playerName = entry.getKey();
            Map<Integer, HomeData> homes = entry.getValue();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                UUID uuid = offlinePlayer.getUniqueId();
                uuidHomes.put(uuid, homes);
                convertedCount++;
                plugin.getLogger().info("Migrated homes for player: " + playerName + " (UUID: " + uuid + ")");
            } else {
                skippedCount++;
                plugin.getLogger().warning("Skipped player " + playerName + " - never played on server");
            }
        }

        plugin.getLogger().info("Migration summary: " + convertedCount + " converted, " + skippedCount + " skipped");
        return uuidHomes;
    }

    private void saveUuidHomes(File file, Map<UUID, Map<Integer, HomeData>> uuidHomes) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(uuidHomes, writer);
        }
    }
}