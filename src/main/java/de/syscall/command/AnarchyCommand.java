package de.syscall.command;

import de.syscall.AnarchySystem;
import de.syscall.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class AnarchyCommand implements CommandExecutor, TabCompleter {

    private final AnarchySystem plugin;

    public AnarchyCommand(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtil.colorize("&6Anarchy-System &7v1.0"));
            sender.sendMessage(ChatUtil.colorize("&7Use &6/" + label + " reload &7to reload the configuration"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("anarchy.reload")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission").replace("&", "§"));
                return true;
            }

            plugin.reload();
            sender.sendMessage(plugin.getConfigManager().getMessage("config-reloaded").replace("&", "§"));
            return true;
        }

        sender.sendMessage(ChatUtil.colorize("&cUnknown subcommand!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("anarchy.reload")) {
            return Arrays.asList("reload");
        }
        return null;
    }
}