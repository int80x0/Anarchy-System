package de.syscall.command;

import de.syscall.AnarchySystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpDenyCommand implements CommandExecutor {

    private final AnarchySystem plugin;

    public TpDenyCommand(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("anarchy.teleport.deny")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission").replace("&", "§"));
            return true;
        }

        plugin.getTeleportManager().denyRequest(player);
        return true;
    }
}