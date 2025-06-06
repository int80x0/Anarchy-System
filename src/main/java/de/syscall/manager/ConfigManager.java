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

    public String getConfirmationGuiTitle() {
        return config.getString("homes.confirmation-gui.title", "&cDelete Home %home%?");
    }

    public List<String> getConfirmationGuiLayout() {
        return config.getStringList("homes.confirmation-gui.layout");
    }

    public String getConfirmationGuiItemMaterial(String symbol) {
        return config.getString("homes.confirmation-gui.items." + symbol + ".material", "GRAY_STAINED_GLASS_PANE");
    }

    public String getConfirmationGuiItemName(String symbol) {
        return config.getString("homes.confirmation-gui.items." + symbol + ".name", " ");
    }

    public List<String> getConfirmationGuiItemLore(String symbol) {
        return config.getStringList("homes.confirmation-gui.items." + symbol + ".lore");
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

    public boolean isDeathSystemEnabled() {
        return config.getBoolean("death-system.enabled", true);
    }

    public boolean isCorpseEnabled() {
        return config.getBoolean("death-system.corpse.enabled", true);
    }

    public int getCorpseDisplayDuration() {
        return config.getInt("death-system.corpse.display-duration", 4);
    }

    public double getCorpseSinkRate() {
        return config.getDouble("death-system.corpse.sink-rate", 0.05);
    }

    public double getCorpseSinkDepth() {
        return config.getDouble("death-system.corpse.sink-depth", 2.0);
    }

    public int getCorpseSinkSpeed() {
        return config.getInt("death-system.corpse.sink-speed", 2);
    }

    public int getLootHeadDuration() {
        return config.getInt("death-system.loot-head.duration", 180);
    }

    public double getHeadRotationSpeed() {
        return config.getDouble("death-system.loot-head.rotation-speed", 2.0);
    }

    public String getDeathLootText() {
        return config.getString("death-system.loot-head.display-text", "&6Loot of &f%player%");
    }

    public boolean isLootFountainEnabled() {
        return config.getBoolean("death-system.loot-fountain.enabled", true);
    }

    public double getFountainHeight() {
        return config.getDouble("death-system.loot-fountain.height", 1.5);
    }

    public double getFountainRadius() {
        return config.getDouble("death-system.loot-fountain.radius", 1.0);
    }

    public String getLootCollectSound() {
        return config.getString("death-system.loot-fountain.collect-sound", "ENTITY_ITEM_PICKUP");
    }

    public float getLootCollectSoundVolume() {
        return (float) config.getDouble("death-system.loot-fountain.sound-volume", 1.0);
    }

    public float getLootCollectSoundPitch() {
        return (float) config.getDouble("death-system.loot-fountain.sound-pitch", 1.0);
    }

    public boolean shouldClearDropsOnDeath() {
        return config.getBoolean("death-system.clear-drops", true);
    }

    public boolean canCollectOwnLoot() {
        return config.getBoolean("death-system.allow-self-collect", false);
    }

    public int getMaxExperienceDrop() {
        return config.getInt("death-system.max-experience-drop", 100);
    }

    public String getDeathMessage(String key) {
        return config.getString("messages.death." + key, "Death message not found: " + key);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}