package de.syscall;

import de.syscall.command.*;
import de.syscall.listener.DeathListener;
import de.syscall.listener.PlayerListener;
import de.syscall.listener.TeleportListener;
import de.syscall.manager.*;
import de.syscall.util.MigrationUtility;
import org.bukkit.plugin.java.JavaPlugin;

public class AnarchySystem extends JavaPlugin {

    private static AnarchySystem instance;
    private ConfigManager configManager;
    private HomesManagerCracked homesManager;
    private LuckPermsManager luckPermsManager;
    private ParticleManagerCracked particleManager;
    private TeleportManagerCracked teleportManager;
    private TeleportAnimationManagerCracked teleportAnimationManager;
    private HintManager hintManager;
    private MigrationUtility migrationUtility;
    private DeathManager deathManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.luckPermsManager = new LuckPermsManager(this);
        this.migrationUtility = new MigrationUtility(this);
        this.deathManager = new DeathManager(this);

        if (getConfig().getBoolean("migrate-from-uuid", false)) {
            getLogger().info("Starting migration from UUID to player names...");
            if (migrationUtility.migrateFromUuidToNames()) {
                getLogger().info("Migration completed successfully!");
                getConfig().set("migrate-from-uuid", false);
                saveConfig();
            } else {
                getLogger().warning("Migration failed!");
            }
        }

        this.homesManager = new HomesManagerCracked(this);
        this.particleManager = new ParticleManagerCracked(this);
        this.teleportManager = new TeleportManagerCracked(this);
        this.teleportAnimationManager = new TeleportAnimationManagerCracked(this);
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
        if (deathManager != null) {
            deathManager.shutdown();
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
        if (configManager.isModuleEnabled("death-system")) {
            getServer().getPluginManager().registerEvents(new DeathListener(this), this);
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

        if (particleManager != null && configManager.isModuleEnabled("home-particles")) {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                particleManager.startParticleTask(player);
            }
        }

        if (teleportAnimationManager != null) {
            teleportAnimationManager.shutdown();
        }

        if (deathManager != null) {
            deathManager.shutdown();
            this.deathManager = new DeathManager(this);
        }

        getLogger().info("Plugin reload completed!");
    }

    public static AnarchySystem getInstance() {
        return instance;
    }

    public DeathManager getDeathManager() {
        return deathManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HomesManagerCracked getHomesManager() {
        return homesManager;
    }

    public LuckPermsManager getLuckPermsManager() {
        return luckPermsManager;
    }

    public ParticleManagerCracked getParticleManager() {
        return particleManager;
    }

    public TeleportManagerCracked getTeleportManager() {
        return teleportManager;
    }

    public TeleportAnimationManagerCracked getTeleportAnimationManager() {
        return teleportAnimationManager;
    }

    public HintManager getHintManager() {
        return hintManager;
    }

    public MigrationUtility getMigrationUtility() {
        return migrationUtility;
    }
}