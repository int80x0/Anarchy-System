package de.syscall.command;

import de.syscall.AnarchySystem;
import de.syscall.data.TeleportRequest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaHereCommand implements CommandExecutor {

    private final AnarchySystem plugin;

    public TpaHereCommand(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("anarchy.teleport.tpahere")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission").replace("&", "§"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getTeleportMessage("tpahere-usage").replace("&", "§"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getConfigManager().getTeleportMessage("player-not-found").replace("&", "§").replace("%player%", args[0]));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getConfigManager().getTeleportMessage("tpa-self").replace("&", "§"));
            return true;
        }

        plugin.getTeleportManager().sendTeleportRequest(player, target, TeleportRequest.TeleportType.TPAHERE);
        return true;
    }
}