package de.syscall.command;

import de.syscall.AnarchySystem;
import de.syscall.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final AnarchySystem plugin;

    public HomeCommand(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("anarchy.homes.use")) {
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

        Location home = plugin.getHomesManager().getHome(player, homeNumber);
        if (home == null) {
            player.sendMessage(plugin.getConfigManager().formatMessage("home-not-found", "home", String.valueOf(homeNumber)).replace("&", "§"));
            return true;
        }

        player.teleport(home);
        player.sendMessage(plugin.getConfigManager().formatMessage("home-teleported", "home", String.valueOf(homeNumber)).replace("&", "§"));

        return true;
    }
}