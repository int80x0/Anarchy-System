package de.syscall;

import de.syscall.command.*;
import de.syscall.listener.PlayerListener;
import de.syscall.listener.TeleportListener;
import de.syscall.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

public class AnarchySystem extends JavaPlugin {

    private static AnarchySystem instance;
    private ConfigManager configManager;
    private HomesManager homesManager;
    private LuckPermsManager luckPermsManager;
    private ParticleManager particleManager;
    private TeleportManager teleportManager;
    private TeleportAnimationManager teleportAnimationManager;
    private HintManager hintManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.luckPermsManager = new LuckPermsManager(this);
        this.homesManager = new HomesManager(this);
        this.particleManager = new ParticleManager(this);
        this.teleportManager = new TeleportManager(this);
        this.teleportAnimationManager = new TeleportAnimationManager(this);
        this.hintManager = new HintManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("Anarchy-System enabled!");
    }

    @Override
    public void onDisable() {
        if (particleManager != null) {
            particleManager.shutdown();
        }
        if (teleportManager != null) {
            teleportManager.shutdown();
        }
        if (homesManager != null) {
            homesManager.saveHomes();
        }
        if (teleportAnimationManager != null) {
            teleportAnimationManager.shutdown();
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

        if (configManager.isModuleEnabled("teleport")) {
            getCommand("tpa").setExecutor(new TpaCommand(this));
            getCommand("tpahere").setExecutor(new TpaHereCommand(this));
            getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
            getCommand("tpdeny").setExecutor(new TpDenyCommand(this));
            getCommand("tpatoggle").setExecutor(new TpaToggleCommand(this));
            getCommand("back").setExecutor(new BackCommand(this));
        }

        AnarchyCommand anarchyCommand = new AnarchyCommand(this);
        getCommand("anarchy-system").setExecutor(anarchyCommand);
        getCommand("anarchy-system").setTabCompleter(anarchyCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        if (configManager.isModuleEnabled("teleport")) {
            getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        }
    }

    public void reload() {
        getLogger().info("Starting plugin reload...");

        if (particleManager != null) {
            particleManager.shutdown();
        }
        if (teleportManager != null) {
            teleportManager.shutdown();
        }

        configManager.reload();

        if (configManager.isModuleEnabled("homes")) {
            homesManager.reload();
        }

        if (particleManager != null && configManager.isModuleEnabled("packet-particles")) {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                particleManager.startParticleTask(player);
            }
        }

        if (teleportAnimationManager != null) {
            teleportAnimationManager.shutdown();
        }

        getLogger().info("Plugin reload completed!");
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

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public TeleportAnimationManager getTeleportAnimationManager() {
        return teleportAnimationManager;
    }

    public HintManager getHintManager() {
        return hintManager;
    }
}