package de.syscall.task;

import de.syscall.AnarchySystem;
import de.syscall.data.DeathData;
import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LootFountainTask extends BukkitRunnable {

    private final AnarchySystem plugin;
    private final Location spawnLocation;
    private final DeathData deathData;
    private final Player collector;
    private final List<Item> spawnedItems;
    private final List<ExperienceOrb> spawnedOrbs;
    private final double fountainHeight;
    private final double fountainRadius;
    private int ticksElapsed;

    public LootFountainTask(AnarchySystem plugin, Location location, DeathData deathData, Player collector) {
        this.plugin = plugin;
        this.spawnLocation = location.clone();
        this.deathData = deathData;
        this.collector = collector;
        this.spawnedItems = new ArrayList<>();
        this.spawnedOrbs = new ArrayList<>();
        this.fountainHeight = plugin.getConfigManager().getFountainHeight();
        this.fountainRadius = plugin.getConfigManager().getFountainRadius();
        this.ticksElapsed = 0;

        spawnLootItems();
        plugin.getDeathManager().playCollectSound(collector);
    }

    @Override
    public void run() {
        ticksElapsed++;

        if (ticksElapsed >= 100) {
            finishFountain();
            return;
        }

        collectNearbyItems();
    }

    private void spawnLootItems() {
        for (ItemStack itemStack : deathData.getItems()) {
            if (itemStack != null && itemStack.getType() != org.bukkit.Material.AIR) {
                Item item = spawnLocation.getWorld().dropItem(spawnLocation, itemStack);

                double angle = Math.random() * 2 * Math.PI;
                double radius = Math.random() * fountainRadius;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = fountainHeight * (0.5 + Math.random() * 0.5);

                Vector velocity = new Vector(x * 0.1, y, z * 0.1);
                item.setVelocity(velocity);
                item.setPickupDelay(20);

                spawnedItems.add(item);
            }
        }

        if (deathData.getExperience() > 0) {
            int orbCount = Math.min(5, (deathData.getExperience() / 10) + 1);
            int expPerOrb = deathData.getExperience() / orbCount;

            for (int i = 0; i < orbCount; i++) {
                ExperienceOrb orb = spawnLocation.getWorld().spawn(spawnLocation, ExperienceOrb.class);
                orb.setExperience(expPerOrb);

                double angle = Math.random() * 2 * Math.PI;
                double radius = Math.random() * fountainRadius;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = fountainHeight * (0.3 + Math.random() * 0.4);

                Vector velocity = new Vector(x * 0.08, y, z * 0.08);
                orb.setVelocity(velocity);

                spawnedOrbs.add(orb);
            }
        }
    }

    private void collectNearbyItems() {
        spawnedItems.removeIf(item -> {
            if (!item.isValid()) return true;

            if (item.getLocation().distance(collector.getLocation()) <= 1.5) {
                if (collector.getInventory().firstEmpty() != -1) {
                    collector.getInventory().addItem(item.getItemStack());
                } else {
                    collector.getWorld().dropItem(collector.getLocation(), item.getItemStack());
                }
                item.remove();
                return true;
            }
            return false;
        });

        spawnedOrbs.removeIf(orb -> {
            if (!orb.isValid()) return true;

            if (orb.getLocation().distance(collector.getLocation()) <= 1.5) {
                collector.giveExp(orb.getExperience());
                orb.remove();
                return true;
            }
            return false;
        });
    }

    private void finishFountain() {
        spawnedItems.forEach(item -> {
            if (item.isValid()) {
                if (collector.getInventory().firstEmpty() != -1) {
                    collector.getInventory().addItem(item.getItemStack());
                } else {
                    collector.getWorld().dropItem(collector.getLocation(), item.getItemStack());
                }
                item.remove();
            }
        });

        spawnedOrbs.forEach(orb -> {
            if (orb.isValid()) {
                collector.giveExp(orb.getExperience());
                orb.remove();
            }
        });

        cancel();
    }
}