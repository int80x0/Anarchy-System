package de.syscall.command;

import de.syscall.AnarchySystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {

    private final AnarchySystem plugin;

    public BackCommand(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("anarchy.teleport.back")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission").replace("&", "§"));
            return true;
        }

        boolean success = plugin.getTeleportManager().teleportBack(player);
        if (success) {
            plugin.getHintManager().sendTeleportMessageWithHint(player, "back-teleported", "death-locations");
        }

        return true;
    }
}