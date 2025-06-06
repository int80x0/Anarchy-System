package de.syscall.command;

import de.syscall.AnarchySystem;
import de.syscall.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {

    private final AnarchySystem plugin;

    public DelHomeCommand(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("anarchy.homes.set")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission").replace("&", "§"));
            return true;
        }

        int homeNumber = 1;
        if (args.length > 0) {
            try {
                homeNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-number").replace("&", "§"));
                return true;
            }
        }

        if (plugin.getHomesManager().deleteHome(player, homeNumber)) {
            plugin.getHintManager().sendMessageWithHint(player, "home-deleted", "homes-gui", "home", String.valueOf(homeNumber));
        } else {
            player.sendMessage(plugin.getConfigManager().formatMessage("home-not-found", "home", String.valueOf(homeNumber)).replace("&", "§"));
        }

        return true;
    }
}