package de.syscall.util;

import de.syscall.AnarchySystem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigValidator {

    private final AnarchySystem plugin;

    public ConfigValidator(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    public boolean validateConfig() {
        FileConfiguration config = plugin.getConfig();
        boolean valid = true;

        if (!validateModules(config)) valid = false;
        if (!validateHomes(config)) valid = false;
        if (!validateGui(config)) valid = false;

        return valid;
    }

    private boolean validateModules(FileConfiguration config) {
        if (!config.contains("modules.homes.enabled")) {
            plugin.getLogger().warning("Missing modules.homes.enabled in config");
            return false;
        }
        return true;
    }

    private boolean validateHomes(FileConfiguration config) {
        boolean valid = true;

        int defaultHomes = config.getInt("homes.default-homes", -1);
        if (defaultHomes < 1 || defaultHomes > 10) {
            plugin.getLogger().warning("Invalid homes.default-homes: must be 1-10");
            valid = false;
        }

        int maxHomes = config.getInt("homes.max-homes", -1);
        if (maxHomes < 1 || maxHomes > 10) {
            plugin.getLogger().warning("Invalid homes.max-homes: must be 1-10");
            valid = false;
        }

        String[] ranks = {"epic", "ultra", "hero", "legendary"};
        for (String rank : ranks) {
            int homes = config.getInt("homes.ranks." + rank + ".homes", -1);
            if (homes < 1 || homes > 10) {
                plugin.getLogger().warning("Invalid homes for rank " + rank + ": must be 1-10");
                valid = false;
            }
        }

        return valid;
    }

    private boolean validateGui(FileConfiguration config) {
        boolean valid = true;

        for (int i = 1; i <= 10; i++) {
            String material = config.getString("homes.gui.items.home-" + i + ".material");
            if (material != null) {
                try {
                    Material.valueOf(material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material for home-" + i + ": " + material);
                    valid = false;
                }
            }
        }

        return valid;
    }
}