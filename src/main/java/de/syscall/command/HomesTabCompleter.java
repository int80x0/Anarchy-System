package de.syscall.command;

import de.syscall.AnarchySystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomesTabCompleter implements TabCompleter {

    private final AnarchySystem plugin;

    public HomesTabCompleter(AnarchySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player player)) {
            return completions;
        }

        if (args.length == 1) {
            int maxHomes = plugin.getHomesManager().getPlayerMaxHomes(player);
            for (int i = 1; i <= maxHomes; i++) {
                String homeNum = String.valueOf(i);
                if (homeNum.startsWith(args[0])) {
                    completions.add(homeNum);
                }
            }
        }

        return completions;
    }
}