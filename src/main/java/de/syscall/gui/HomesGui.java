package de.syscall.gui;

import de.syscall.AnarchySystem;
import de.syscall.data.HomeData;
import org.bukkit.Location;
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
import java.util.Map;

public class HomesGui {

    private final AnarchySystem plugin;
    private final Player player;
    private int currentPage = 0;
    private final List<Integer> homeSlots = new ArrayList<>();
    private Gui gui;
    private Window window;

    public HomesGui(AnarchySystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        List<String> layout = plugin.getConfigManager().getGuiLayout();
        homeSlots.clear();
        findHomeSlots(layout);

        this.gui = Gui.normal()
                .setStructure(layout.toArray(new String[0]))
                .addIngredient('#', new BorderItem())
                .addIngredient('X', new CloseItem())
                .build();

        updateContent();

        this.window = Window.single()
                .setViewer(player)
                .setTitle(colorize(plugin.getConfigManager().getGuiTitle()))
                .setGui(gui)
                .build();

        window.open();
    }

    private void updateContent() {
        fillHomeItems();
        updateNavigationItems();
    }

    private void findHomeSlots(List<String> layout) {
        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                if (line.charAt(col) == '.') {
                    homeSlots.add(row * 9 + col);
                }
            }
        }
    }

    private void fillHomeItems() {
        Map<Integer, HomeData> playerHomes = plugin.getHomesManager().getPlayerHomes(player);
        int playerMaxHomes = plugin.getHomesManager().getPlayerMaxHomes(player);
        int maxHomes = plugin.getConfigManager().getMaxHomes();

        int itemsPerPage = homeSlots.size();
        int startHome = currentPage * itemsPerPage + 1;

        for (int i = 0; i < homeSlots.size(); i++) {
            int homeNumber = startHome + i;
            if (homeNumber <= maxHomes) {
                HomeData homeData = playerHomes.get(homeNumber);
                boolean hasAccess = homeNumber <= playerMaxHomes;
                gui.setItem(homeSlots.get(i), new HomeItem(homeNumber, homeData, hasAccess));
            } else {
                gui.setItem(homeSlots.get(i), new BorderItem());
            }
        }
    }

    private void updateNavigationItems() {
        List<String> layout = plugin.getConfigManager().getGuiLayout();
        int leftArrowSlot = findCharSlot('<', layout);
        int rightArrowSlot = findCharSlot('>', layout);

        if (leftArrowSlot != -1) {
            gui.setItem(leftArrowSlot, canGoPrevious() ? new PreviousPageItem() : new BorderItem());
        }
        if (rightArrowSlot != -1) {
            gui.setItem(rightArrowSlot, canGoNext() ? new NextPageItem() : new BorderItem());
        }
    }

    private int findCharSlot(char c, List<String> layout) {
        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                if (line.charAt(col) == c) {
                    return row * 9 + col;
                }
            }
        }
        return -1;
    }

    private boolean canGoNext() {
        int itemsPerPage = homeSlots.size();
        int maxHomes = plugin.getConfigManager().getMaxHomes();
        int lastHomeOnCurrentPage = (currentPage + 1) * itemsPerPage;
        return lastHomeOnCurrentPage < maxHomes;
    }

    private boolean canGoPrevious() {
        return currentPage > 0;
    }

    private void nextPage() {
        if (canGoNext()) {
            currentPage++;
            updateContent();
        }
    }

    private void previousPage() {
        if (canGoPrevious()) {
            currentPage--;
            updateContent();
        }
    }

    private String colorize(String text) {
        return text != null ? text.replace("&", "§") : "";
    }

    private class BorderItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            String materialName = plugin.getConfigManager().getGuiItemMaterial("#");
            String name = colorize(plugin.getConfigManager().getGuiItemName("#"));
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

    private class PreviousPageItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            String materialName = plugin.getConfigManager().getGuiItemMaterial("<");
            String name = colorize(plugin.getConfigManager().getGuiItemName("<"));
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.ARROW;
            }
            return new ItemBuilder(material).setDisplayName(name);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            previousPage();
        }
    }

    private class NextPageItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            String materialName = plugin.getConfigManager().getGuiItemMaterial(">");
            String name = colorize(plugin.getConfigManager().getGuiItemName(">"));
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.ARROW;
            }
            return new ItemBuilder(material).setDisplayName(name);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            nextPage();
        }
    }

    private class CloseItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            String materialName = plugin.getConfigManager().getGuiItemMaterial("X");
            String name = colorize(plugin.getConfigManager().getGuiItemName("X"));
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.BARRIER;
            }
            return new ItemBuilder(material).setDisplayName(name);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            player.closeInventory();
        }
    }

    private class HomeItem extends AbstractItem {
        private final int homeNumber;
        private final HomeData homeData;
        private final boolean hasAccess;

        public HomeItem(int homeNumber, HomeData homeData, boolean hasAccess) {
            this.homeNumber = homeNumber;
            this.homeData = homeData;
            this.hasAccess = hasAccess;
        }

        @Override
        public ItemProvider getItemProvider() {
            String materialName = plugin.getConfigManager().getHomeItemMaterial(homeNumber);
            String name = colorize(plugin.getConfigManager().getHomeItemName(homeNumber));
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.WHITE_BED;
            }

            List<String> lore = new ArrayList<>();

            if (!hasAccess) {
                String requiredRank = getRequiredRank();
                String rankDisplayName = colorize(plugin.getConfigManager().getRankDisplayName(requiredRank));
                for (String line : plugin.getConfigManager().getGuiHomeLore("home-locked")) {
                    lore.add(colorize(line.replace("%rank%", rankDisplayName)));
                }
            } else if (homeData != null) {
                for (String line : plugin.getConfigManager().getGuiHomeLore("home-set")) {
                    lore.add(colorize(line
                            .replace("%world%", homeData.getWorldName())
                            .replace("%x%", String.valueOf(Math.round(homeData.getX())))
                            .replace("%y%", String.valueOf(Math.round(homeData.getY())))
                            .replace("%z%", String.valueOf(Math.round(homeData.getZ())))));
                }
            } else {
                for (String line : plugin.getConfigManager().getGuiHomeLore("home-empty")) {
                    lore.add(colorize(line.replace("%number%", String.valueOf(homeNumber))));
                }
            }

            return new ItemBuilder(material).setDisplayName(name).setLegacyLore(lore);
        }

        private String getRequiredRank() {
            int defaultHomes = plugin.getConfigManager().getDefaultHomes();
            int epicHomes = plugin.getConfigManager().getRankHomes("epic");
            int ultraHomes = plugin.getConfigManager().getRankHomes("ultra");
            int heroHomes = plugin.getConfigManager().getRankHomes("hero");

            if (homeNumber <= defaultHomes) return "default";
            if (homeNumber <= epicHomes) return "epic";
            if (homeNumber <= ultraHomes) return "ultra";
            if (homeNumber <= heroHomes) return "hero";
            return "legendary";
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (!hasAccess) {
                player.sendMessage(colorize(plugin.getConfigManager().getGuiMessage("no-access")));
                return;
            }

            if (homeData == null) {
                player.closeInventory();
                List<String> emptyLore = plugin.getConfigManager().getGuiHomeLore("home-empty");
                if (emptyLore.size() >= 3) {
                    String message = colorize(emptyLore.get(2).replace("%number%", String.valueOf(homeNumber)));
                    player.sendMessage(message);
                }
                return;
            }

            if (clickType.isLeftClick()) {
                Location location = homeData.toLocation();
                if (location != null) {
                    player.closeInventory();
                    player.teleport(location);
                    player.sendMessage(colorize(plugin.getConfigManager().formatMessage("home-teleported", "home", String.valueOf(homeNumber))));
                } else {
                    player.sendMessage(colorize(plugin.getConfigManager().getGuiMessage("world-not-found")));
                }
            } else if (clickType.isRightClick()) {
                if (plugin.getHomesManager().deleteHome(player, homeNumber)) {
                    player.sendMessage(colorize(plugin.getConfigManager().formatMessage("home-deleted", "home", String.valueOf(homeNumber))));
                    updateContent();
                }
            }
        }
    }
}