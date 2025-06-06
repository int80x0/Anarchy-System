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
        return config.getInt("home-particles.circle-points", 16);
    }

    public double getParticleDetectionRadius() {
        return config.getDouble("home-particles.detection-radius", 30.0);
    }

    public double getParticleCircleRadius() {
        return config.getDouble("home-particles.circle-radius", 0.75);
    }

    public int getParticleCount() {
        return config.getInt("home-particles.particle-count", 2);
    }

    public double getParticleYOffset() {
        return config.getDouble("home-particles.y-offset", 1.0);
    }

    public double getParticleRotationSpeed() {
        return config.getDouble("home-particles.rotation-speed", 0.19634954084936207);
    }

    public String getParticleType() {
        return config.getString("home-particles.particle-type", "ELECTRIC_SPARK");
    }

    public String getParticleColor() {
        return config.getString("home-particles.color", "0,255,255");
    }

    public String getTeleportMessage(String key) {
        return config.getString("messages.teleport." + key, "Teleport message not found: " + key);
    }

    public int getTpaTimeout() {
        return config.getInt("teleport.tpa-timeout", 60);
    }

    public int getTpaCooldown() {
        return config.getInt("teleport.tpa-cooldown", 30);
    }

    public int getBackCooldown() {
        return config.getInt("teleport.back-cooldown", 10);
    }

    public boolean isTeleportAnimationEnabled() {
        return config.getBoolean("teleport-animation.enabled", true);
    }

    public int getTeleportAnimationDuration() {
        return config.getInt("teleport-animation.duration", 6);
    }

    public double getTeleportAnimationHeight() {
        return config.getDouble("teleport-animation.spiral.height", 2.0);
    }

    public double getTeleportAnimationRadius() {
        return config.getDouble("teleport-animation.radius", 1.0);
    }

    public double getTeleportAnimationRotationSpeed() {
        return config.getDouble("teleport-animation.circle.rotation-speed", 0.19634954084936207);
    }

    public boolean isSpiralAnimationEnabled() {
        return config.getBoolean("teleport-animation.spiral.enabled", true);
    }

    public boolean isCircleAnimationEnabled() {
        return config.getBoolean("teleport-animation.circle.enabled", true);
    }

    public boolean isDestinationAnimationEnabled() {
        return config.getBoolean("teleport-animation.destination.enabled", true);
    }

    public int getTeleportAnimationParticleCount() {
        return config.getInt("teleport-animation.circle.particle-count", 3);
    }

    public int getTeleportAnimationSpiralParticleCount() {
        return config.getInt("teleport-animation.spiral.particle-count", 6);
    }

    public String getTeleportAnimationParticleType() {
        return config.getString("teleport-animation.circle.particle-type", "ELECTRIC_SPARK");
    }

    public String getTeleportAnimationSpiralParticleType() {
        return config.getString("teleport-animation.spiral.particle-type", "DUST_COLOR_TRANSITION");
    }

    public String getTeleportAnimationParticleColor() {
        return config.getString("teleport-animation.circle.particle-color", "0,255,255");
    }

    public double getTeleportAnimationCircleYOffset() {
        return config.getDouble("teleport-animation.circle.y-offset", 0.1);
    }

    public double getTeleportAnimationSpiralYOffset() {
        return config.getDouble("teleport-animation.spiral.y-offset", 0.0);
    }

    public String getTeleportAnimationSpiralParticleColor() {
        return config.getString("teleport-animation.spiral.particle-color", "255,215,0-255,69,0");
    }

    public double getTeleportAnimationViewDistance() {
        return config.getDouble("teleport-animation.view-distance", 40.0);
    }

    public String getTeleportAnimationLoopSound() {
        return config.getString("teleport-animation.sounds.loop", "ENTITY_BAT_TAKEOFF");
    }

    public String getTeleportAnimationFinalSound() {
        return config.getString("teleport-animation.sounds.final", "ITEM_CHORUS_FRUIT_TELEPORT");
    }

    public float getTeleportAnimationSoundVolume() {
        return (float) config.getDouble("teleport-animation.sounds.volume", 0.5);
    }

    public float getTeleportAnimationSoundPitch() {
        return (float) config.getDouble("teleport-animation.sounds.pitch", 1.0);
    }

    public double getTeleportAnimationMovementThreshold() {
        return config.getDouble("teleport-animation.movement-threshold", 0.5);
    }

    public double getTeleportAnimationSpiralTurns() {
        return config.getDouble("teleport-animation.spiral.turns", 3.0);
    }

    public boolean isHintsEnabled() {
        return config.getBoolean("hints.enabled", true);
    }

    public String getHintText() {
        return config.getString("hints.text", "(hint)");
    }

    public String getHintMessage(String key) {
        return config.getString("hints.messages." + key, "Hint not found: " + key);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}