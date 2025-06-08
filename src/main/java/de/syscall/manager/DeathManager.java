package de.syscall.manager;

import de.syscall.AnarchySystem;
import de.syscall.data.DeathCorpse;
import de.syscall.data.DeathData;
import de.syscall.data.DeathLootHead;
import de.syscall.task.CorpseSinkTask;
import de.syscall.task.HeadRotationTask;
import de.syscall.task.LootFountainTask;
import de.syscall.util.SkinUtils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DeathManager {

    private final AnarchySystem plugin;
    private final Map<String, DeathCorpse> activeCorpses;
    private final Map<String, DeathLootHead> activeLootHeads;
    private final Map<String, BukkitTask> corpseTasks;
    private final Map<String, BukkitTask> headTasks;

    public DeathManager(AnarchySystem plugin) {
        this.plugin = plugin;
        this.activeCorpses = new ConcurrentHashMap<>();
        this.activeLootHeads = new ConcurrentHashMap<>();
        this.corpseTasks = new ConcurrentHashMap<>();
        this.headTasks = new ConcurrentHashMap<>();
        startCleanupTask();
    }

    public void handlePlayerDeath(Player player, Location deathLocation) {
        if (!plugin.getConfigManager().isDeathSystemEnabled()) {
            return;
        }

        String playerName = player.getName().toLowerCase();
        List<ItemStack> items = new ArrayList<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
            }
        }

        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
            }
        }

        int experience = calculateExperience(player.getLevel(), player.getExp());
        DeathData deathData = new DeathData(player.getName(), deathLocation, items, experience);

        if (plugin.getConfigManager().isCorpseEnabled()) {
            spawnRealisticCorpse(player, deathLocation, deathData);
        } else {
            spawnLootHead(deathData);
        }
    }

    private void spawnRealisticCorpse(Player player, Location location, DeathData deathData) {
        String playerName = player.getName().toLowerCase();

        cleanupPlayerCorpse(playerName);

        Location corpseLocation = location.getBlock().getLocation().add(0.5, -0.3, 0.5);
        corpseLocation.setYaw(location.getYaw());
        corpseLocation.setPitch(0);

        ArmorStand corpse = (ArmorStand) location.getWorld().spawnEntity(corpseLocation, EntityType.ARMOR_STAND);
        corpse.setVisible(true);
        corpse.setGravity(false);
        corpse.setCanPickupItems(false);
        corpse.setMarker(false);
        corpse.setSmall(false);
        corpse.setBasePlate(false);
        corpse.setArms(true);
        corpse.setCustomNameVisible(false);
        corpse.setInvulnerable(true);

        setupRealisticLyingPose(corpse);
        applyRealisticBodyToCorpse(corpse, player);

        DeathCorpse deathCorpse = new DeathCorpse(player.getName(), corpse, corpseLocation);
        activeCorpses.put(playerName, deathCorpse);

        CorpseSinkTask sinkTask = new CorpseSinkTask(plugin, deathCorpse, deathData);
        BukkitTask task = sinkTask.runTaskTimer(plugin,
                plugin.getConfigManager().getCorpseDisplayDuration() * 20L,
                plugin.getConfigManager().getCorpseSinkSpeed());
        corpseTasks.put(playerName, task);
    }

    public Map<String, DeathLootHead> getActiveLootHeads() {
        return activeLootHeads;
    }

    public Map<String, DeathCorpse> getActiveCorpses() {
        return activeCorpses;
    }

    public void spawnLootHead(DeathData deathData) {
        String playerName = deathData.getPlayerName().toLowerCase();
        Location location = deathData.toLocation();
        if (location == null) return;

        cleanupPlayerLootHead(playerName);

        location = location.getBlock().getLocation().add(0.5, 0, 0.5);

        ArmorStand headStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        headStand.setVisible(false);
        headStand.setGravity(false);
        headStand.setCanPickupItems(false);
        headStand.setMarker(false);
        headStand.setSmall(true);
        headStand.setBasePlate(false);
        headStand.setArms(false);
        headStand.setInvulnerable(true);

        String displayText = plugin.getConfigManager().getDeathLootText()
                .replace("%player%", deathData.getPlayerName());
        headStand.setCustomName(displayText.replace("&", "§"));
        headStand.setCustomNameVisible(true);

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwner(deathData.getPlayerName());
            playerHead.setItemMeta(skullMeta);
        }
        headStand.getEquipment().setHelmet(playerHead);

        DeathLootHead lootHead = new DeathLootHead(deathData.getPlayerName(), headStand, deathData);
        activeLootHeads.put(playerName, lootHead);

        HeadRotationTask rotationTask = new HeadRotationTask(plugin, lootHead);
        BukkitTask task = rotationTask.runTaskTimer(plugin, 0L, 2L);
        headTasks.put(playerName, task);

        scheduleHeadRemoval(playerName);
    }

    public void collectLoot(Player player, String targetPlayerName) {
        String targetKey = targetPlayerName.toLowerCase();
        DeathLootHead lootHead = activeLootHeads.get(targetKey);

        if (lootHead == null || !lootHead.isValid()) {
            return;
        }

        DeathData deathData = lootHead.getDeathData();
        Location location = lootHead.getLocation();

        cleanupPlayerLootHead(targetKey);

        if (plugin.getConfigManager().isLootFountainEnabled()) {
            LootFountainTask fountainTask = new LootFountainTask(plugin, location, deathData, player);
            fountainTask.runTaskTimer(plugin, 0L, 2L);
        } else {
            giveItemsDirectly(player, deathData);
            playCollectSound(player);
        }
    }

    private void giveItemsDirectly(Player player, DeathData deathData) {
        for (ItemStack item : deathData.getItems()) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        if (deathData.getExperience() > 0) {
            ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
            orb.setExperience(deathData.getExperience());
        }
    }

    public void playCollectSound(Player player) {
        String soundName = plugin.getConfigManager().getLootCollectSound();
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = plugin.getConfigManager().getLootCollectSoundVolume();
            float pitch = plugin.getConfigManager().getLootCollectSoundPitch();
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        }
    }

    private void setupRealisticLyingPose(ArmorStand corpse) {
        corpse.setHeadPose(new EulerAngle(Math.toRadians(-85), Math.toRadians(5), Math.toRadians(-2)));
        corpse.setBodyPose(new EulerAngle(Math.toRadians(-90), 0, 0));
        corpse.setLeftArmPose(new EulerAngle(Math.toRadians(-85), Math.toRadians(-20), Math.toRadians(-15)));
        corpse.setRightArmPose(new EulerAngle(Math.toRadians(-95), Math.toRadians(20), Math.toRadians(15)));
        corpse.setLeftLegPose(new EulerAngle(Math.toRadians(-88), Math.toRadians(-8), Math.toRadians(-3)));
        corpse.setRightLegPose(new EulerAngle(Math.toRadians(-92), Math.toRadians(8), Math.toRadians(3)));
    }

    private void applyRealisticBodyToCorpse(ArmorStand corpse, Player player) {
        try {
            ItemStack playerHead = SkinUtils.getPlayerHead(player.getName());
            corpse.getEquipment().setHelmet(playerHead);

            if (player.getInventory().getChestplate() != null) {
                corpse.getEquipment().setChestplate(player.getInventory().getChestplate().clone());
            } else {
                ItemStack defaultChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                corpse.getEquipment().setChestplate(defaultChestplate);
            }

            if (player.getInventory().getLeggings() != null) {
                corpse.getEquipment().setLeggings(player.getInventory().getLeggings().clone());
            } else {
                ItemStack defaultLeggings = new ItemStack(Material.LEATHER_LEGGINGS);
                corpse.getEquipment().setLeggings(defaultLeggings);
            }

            if (player.getInventory().getBoots() != null) {
                corpse.getEquipment().setBoots(player.getInventory().getBoots().clone());
            } else {
                ItemStack defaultBoots = new ItemStack(Material.LEATHER_BOOTS);
                corpse.getEquipment().setBoots(defaultBoots);
            }

            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                corpse.getEquipment().setItemInMainHand(player.getInventory().getItemInMainHand().clone());
            }
            if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                corpse.getEquipment().setItemInOffHand(player.getInventory().getItemInOffHand().clone());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply realistic body to corpse for player: " + player.getName());
        }
    }

    private int calculateExperience(int level, float exp) {
        int totalExp = 0;

        for (int i = 1; i <= level; i++) {
            if (i <= 16) {
                totalExp += 2 * i + 7;
            } else if (i <= 31) {
                totalExp += 5 * i - 38;
            } else {
                totalExp += 9 * i - 158;
            }
        }

        if (level <= 16) {
            totalExp += (int) (exp * (2 * level + 7));
        } else if (level <= 31) {
            totalExp += (int) (exp * (5 * level - 38));
        } else {
            totalExp += (int) (exp * (9 * level - 158));
        }

        return Math.min(totalExp, plugin.getConfigManager().getMaxExperienceDrop());
    }

    private void scheduleHeadRemoval(String playerName) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            cleanupPlayerLootHead(playerName);
        }, plugin.getConfigManager().getLootHeadDuration() * 20L);
    }

    public void cleanupPlayerCorpse(String playerName) {
        String key = playerName.toLowerCase();

        DeathCorpse corpse = activeCorpses.remove(key);
        if (corpse != null) {
            corpse.remove();
        }

        BukkitTask task = corpseTasks.remove(key);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void cleanupPlayerLootHead(String playerName) {
        String key = playerName.toLowerCase();

        DeathLootHead lootHead = activeLootHeads.remove(key);
        if (lootHead != null) {
            lootHead.remove();
        }

        BukkitTask task = headTasks.remove(key);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            long corpseMaxAge = plugin.getConfigManager().getCorpseDisplayDuration() * 1000L + 30000L;
            long headMaxAge = plugin.getConfigManager().getLootHeadDuration() * 1000L + 30000L;

            activeCorpses.entrySet().removeIf(entry -> {
                DeathCorpse corpse = entry.getValue();
                if (!corpse.isValid() || corpse.isExpired(corpseMaxAge)) {
                    cleanupPlayerCorpse(entry.getKey());
                    return true;
                }
                return false;
            });

            activeLootHeads.entrySet().removeIf(entry -> {
                DeathLootHead lootHead = entry.getValue();
                if (!lootHead.isValid() || lootHead.isExpired(headMaxAge)) {
                    cleanupPlayerLootHead(entry.getKey());
                    return true;
                }
                return false;
            });
        }, 1200L, 1200L);
    }

    public boolean hasLootHead(String playerName) {
        return activeLootHeads.containsKey(playerName.toLowerCase());
    }

    public DeathLootHead getLootHead(String playerName) {
        return activeLootHeads.get(playerName.toLowerCase());
    }

    public void cleanupPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        cleanupPlayerCorpse(playerName);
        cleanupPlayerLootHead(playerName);
    }

    public void shutdown() {
        corpseTasks.values().forEach(task -> {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        });

        headTasks.values().forEach(task -> {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        });

        activeCorpses.values().forEach(DeathCorpse::remove);
        activeLootHeads.values().forEach(DeathLootHead::remove);

        corpseTasks.clear();
        headTasks.clear();
        activeCorpses.clear();
        activeLootHeads.clear();
    }
}