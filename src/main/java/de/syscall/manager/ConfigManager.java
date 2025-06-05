package de.syscall.manager;

import de.syscall.AnarchySystem;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final AnarchySystem plugin;
    private FileConfiguration config;

    public ConfigManager(AnarchySystem plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public boolean isModuleEnabled(String module) {
        return config.getBoolean("modules." + module + ".enabled", false);
    }

    public int getDefaultHomes() {
        return config.getInt("homes.default-homes", 1);
    }

    public int getMaxHomes() {
        return config.getInt("homes.max-homes", 10);
    }

    public boolean isLuckPermsIntegrationEnabled() {
        return config.getBoolean("homes.luckperms-integration", true);
    }

    public int getRankHomes(String rank) {
        return config.getInt("homes.ranks." + rank + ".homes", 0);
    }

    public String getRankLuckPermsGroup(String rank) {
        return config.getString("homes.ranks." + rank + ".luckperms-group", "");
    }

    public String getGuiTitle() {
        return config.getString("homes.gui.title", "Homes");
    }

    public List<String> getGuiLayout() {
        return config.getStringList("homes.gui.layout");
    }

    public String getGuiItemMaterial(String symbol) {
        return config.getString("homes.gui.items." + symbol + ".material", "GRAY_STAINED_GLASS_PANE");
    }

    public String getGuiItemName(String symbol) {
        return config.getString("homes.gui.items." + symbol + ".name", " ");
    }

    public String getHomeItemMaterial(int home) {
        return config.getString("homes.gui.homes." + home + ".material", "WHITE_BED");
    }

    public String getHomeItemName(int home) {
        return config.getString("homes.gui.homes." + home + ".name", "&fHome " + home);
    }

    public String getHomeItemRank(int home) {
        return config.getString("homes.gui.homes." + home + ".rank", "default");
    }

    public String getMessage(String key) {
        return config.getString("messages.commands." + key, "Message not found: " + key);
    }

    public String formatMessage(String key, String placeholder, String value) {
        return getMessage(key).replace("%" + placeholder + "%", value);
    }

    public List<String> getGuiHomeLore(String type) {
        return config.getStringList("messages.gui." + type + "-lore");
    }

    public String getGuiMessage(String key) {
        return config.getString("messages.gui.messages." + key, "GUI message not found: " + key);
    }

    public String getRankDisplayName(String rank) {
        return config.getString("messages.ranks." + rank, rank);
    }

    public String getRankPermission(String rank) {
        return config.getString("homes.ranks." + rank + ".permission", "anarchy.homes." + rank);
    }

    public int getParticleCirclePoints() {
        return config.getInt("packet-particles.circle-points", 16);
    }

    public double getParticleDetectionRadius() {
        return config.getDouble("packet-particles.detection-radius", 30.0);
    }

    public double getParticleCircleRadius() {
        return config.getDouble("packet-particles.circle-radius", 0.75);
    }

    public int getParticleCount() {
        return config.getInt("packet-particles.particle-count", 2);
    }

    public double getParticleYOffset() {
        return config.getDouble("packet-particles.y-offset", 1.0);
    }

    public double getParticleRotationSpeed() {
        return config.getDouble("packet-particles.rotation-speed", 0.19634954084936207);
    }

    public String getParticleType() {
        return config.getString("packet-particles.particle-type", "ELECTRIC_SPARK");
    }

    public String getParticleColor() {
        return config.getString("packet-particles.color", "0,255,255");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}