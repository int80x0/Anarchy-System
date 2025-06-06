package de.syscall.listener;

import de.syscall.AnarchySystem;
import de.syscall.data.DeathLootHead;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class DeathListener implements Listener {

    private final AnarchySystem plugin;

    public DeathListener(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().isDeathSystemEnabled()) {
            return;
        }

        Player player = event.getEntity();

        if (plugin.getConfigManager().shouldClearDropsOnDeath()) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }

        plugin.getDeathManager().handlePlayerDeath(player, player.getLocation());
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        for (DeathLootHead lootHead : plugin.getDeathManager().getActiveLootHeads().values()) {
            if (lootHead.getArmorStand().equals(armorStand)) {
                event.setCancelled(true);

                String targetPlayerName = lootHead.getPlayerName();
                if (!plugin.getConfigManager().canCollectOwnLoot() &&
                        player.getName().equalsIgnoreCase(targetPlayerName)) {
                    String message = plugin.getConfigManager().getDeathMessage("cannot-collect-own-loot");
                    player.sendMessage(message.replace("&", "§"));
                    return;
                }

                plugin.getDeathManager().collectLoot(player, targetPlayerName);

                String collectorMessage = plugin.getConfigManager().getDeathMessage("loot-collected")
                        .replace("%player%", targetPlayerName);
                player.sendMessage(collectorMessage.replace("&", "§"));

                return;
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }

        ArmorStand armorStand = (ArmorStand) event.getEntity();

        for (DeathLootHead lootHead : plugin.getDeathManager().getActiveLootHeads().values()) {
            if (lootHead.getArmorStand().equals(armorStand)) {
                event.setCancelled(true);
                return;
            }
        }

        for (de.syscall.data.DeathCorpse corpse : plugin.getDeathManager().getActiveCorpses().values()) {
            if (corpse.getArmorStand().equals(armorStand)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}