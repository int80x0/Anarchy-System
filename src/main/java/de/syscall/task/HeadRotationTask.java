package de.syscall.task;

import de.syscall.AnarchySystem;
import de.syscall.data.DeathLootHead;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

public class HeadRotationTask extends BukkitRunnable {

    private final AnarchySystem plugin;
    private final DeathLootHead lootHead;
    private final double rotationSpeed;

    public HeadRotationTask(AnarchySystem plugin, DeathLootHead lootHead) {
        this.plugin = plugin;
        this.lootHead = lootHead;
        this.rotationSpeed = plugin.getConfigManager().getHeadRotationSpeed();
    }

    @Override
    public void run() {
        if (!lootHead.isValid()) {
            cancel();
            return;
        }

        ArmorStand head = lootHead.getArmorStand();
        Location currentLocation = head.getLocation();

        double newRotation = lootHead.getCurrentRotation() + rotationSpeed;
        if (newRotation >= 360.0) {
            newRotation -= 360.0;
        }

        lootHead.setCurrentRotation(newRotation);

        Location newLocation = currentLocation.clone();
        newLocation.setYaw((float) newRotation);
        head.teleport(newLocation);
    }
}