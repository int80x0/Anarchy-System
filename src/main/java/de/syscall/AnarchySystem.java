package de.syscall;

import de.syscall.command.*;
import de.syscall.manager.ConfigManager;
import de.syscall.manager.HomesManager;
import de.syscall.manager.LuckPermsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AnarchySystem extends JavaPlugin {

    private static AnarchySystem instance;
    private ConfigManager configManager;
    private HomesManager homesManager;
    private LuckPermsManager luckPermsManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.luckPermsManager = new LuckPermsManager(this);
        this.homesManager = new HomesManager(this);

        registerCommands();

        getLogger().info("Anarchy-System enabled!");
    }

    @Override
    public void onDisable() {
        if (homesManager != null) {
            homesManager.saveHomes();
        }
        getLogger().info("Anarchy-System disabled!");
    }

    private void registerCommands() {
        if (configManager.isModuleEnabled("homes")) {
            HomesTabCompleter tabCompleter = new HomesTabCompleter(this);

            getCommand("sethome").setExecutor(new SetHomeCommand(this));
            getCommand("sethome").setTabCompleter(tabCompleter);

            getCommand("home").setExecutor(new HomeCommand(this));
            getCommand("home").setTabCompleter(tabCompleter);

            getCommand("homes").setExecutor(new HomesCommand(this));

            getCommand("delhome").setExecutor(new DelHomeCommand(this));
            getCommand("delhome").setTabCompleter(tabCompleter);
        }

        AnarchyCommand anarchyCommand = new AnarchyCommand(this);
        getCommand("anarchy-system").setExecutor(anarchyCommand);
        getCommand("anarchy-system").setTabCompleter(anarchyCommand);
    }

    public void reload() {
        configManager.reload();
        if (configManager.isModuleEnabled("homes")) {
            homesManager.reload();
        }
    }

    public static AnarchySystem getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HomesManager getHomesManager() {
        return homesManager;
    }

    public LuckPermsManager getLuckPermsManager() {
        return luckPermsManager;
    }
}