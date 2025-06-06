package de.syscall.gui;

import de.syscall.AnarchySystem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationGui {

    private final AnarchySystem plugin;
    private final Player player;
    private final int homeNumber;
    private final HomesGui parentGui;

    public ConfirmationGui(AnarchySystem plugin, Player player, int homeNumber, HomesGui parentGui) {
        this.plugin = plugin;
        this.player = player;
        this.homeNumber = homeNumber;
        this.parentGui = parentGui;
    }

    public void open() {
        Gui gui = Gui.normal()
                .setStructure("#Y#N#")
                .addIngredient('#', new BorderItem())
                .addIngredient('Y', new ConfirmItem())
                .addIngredient('N', new CancelItem())
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle(plugin.getConfigManager().getConfirmationGuiTitle().replace("%home%", String.valueOf(homeNumber)))
                .setGui(gui)
                .build();

        window.open();
    }

    private String colorize(String text) {
        return text != null ? text.replace("&", "§") : "";
    }

    private class BorderItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            String materialName = plugin.getConfigManager().getConfirmationGuiItemMaterial("#");
            String name = colorize(plugin.getConfigManager().getConfirmationGuiItemName("#"));
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.GRAY_STAINED_GLASS_PANE;
            }
            return new ItemBuilder(material).setDisplayName(name);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {}
    }

    private class ConfirmItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            String materialName = plugin.getConfigManager().getConfirmationGuiItemMaterial("Y");
            String name = colorize(plugin.getConfigManager().getConfirmationGuiItemName("Y"));
            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfigManager().getConfirmationGuiItemLore("Y")) {
                lore.add(colorize(line.replace("%home%", String.valueOf(homeNumber))));
            }

            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.GREEN_CONCRETE;
            }
            return new ItemBuilder(material).setDisplayName(name).setLegacyLore(lore);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            player.closeInventory();

            if (plugin.getHomesManager().deleteHome(player, homeNumber)) {
                plugin.getHintManager().sendMessageWithHint(player, "home-deleted", "homes-gui", "home", String.valueOf(homeNumber));
                parentGui.open();
            } else {
                player.sendMessage(colorize(plugin.getConfigManager().formatMessage("home-not-found", "home", String.valueOf(homeNumber))));
            }
        }
    }

    private class CancelItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            String materialName = plugin.getConfigManager().getConfirmationGuiItemMaterial("N");
            String name = colorize(plugin.getConfigManager().getConfirmationGuiItemName("N"));
            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfigManager().getConfirmationGuiItemLore("N")) {
                lore.add(colorize(line));
            }

            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.RED_CONCRETE;
            }
            return new ItemBuilder(material).setDisplayName(name).setLegacyLore(lore);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            player.closeInventory();
            parentGui.open();
        }
    }
}