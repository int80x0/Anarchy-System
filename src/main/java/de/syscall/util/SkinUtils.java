package de.syscall.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkinUtils {

    public static ItemStack getPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setOwner(playerName);
            head.setItemMeta(meta);
        }

        return head;
    }

    public static ItemStack createCustomHead(String playerName, String displayName) {
        ItemStack head = getPlayerHead(playerName);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            head.setItemMeta(meta);
        }

        return head;
    }
}