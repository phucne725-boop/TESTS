
package vn.phuc.pscore;

import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.config.ConfigManager;

import java.util.logging.Level;

public final class PSCore extends JavaPlugin {

    private ConfigManager configManager;

    private static PSCore instance;

    @Override
    public void onEnable() {
        instance = this;

        try {
            saveDefaultConfig();
            saveResourceIfMissing("messages.yml");
            saveResourceIfMissing("database.yml");

            this.configManager = new ConfigManager(this);
            this.configManager.load();

            getLogger().info("================================");
            getLogger().info("       PSCore is starting       ");
            getLogger().info("       PhutSmileSmp Beta 0.1    ");
            getLogger().info("================================");

            getLogger().info("Configuration loaded.");
            getLogger().info("Server address: "
                    + configManager.getServerAddress());

            getLogger().info("PSCore enabled successfully.");
        } catch (Exception exception) {
            getLogger().log(
                    Level.SEVERE,
                    "PSCore could not start safely.",
                    exception
            );

            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("PSCore is shutting down.");

        instance = null;
    }

    private void saveResourceIfMissing(String resourceName) {
        if (!getDataFolder().exists()
                && !getDataFolder().mkdirs()) {
            throw new IllegalStateException(
                    "Could not create plugin data folder."
            );
        }

        saveResource(resourceName, false);
    }

    public static PSCore getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "PSCore is not initialized."
            );
        }

        return instance;
    }

    public ConfigManager getConfigManager() {
        if (configManager == null) {
            throw new IllegalStateException(
                    "ConfigManager is not initialized."
            );
        }

        return configManager;
    }
}
