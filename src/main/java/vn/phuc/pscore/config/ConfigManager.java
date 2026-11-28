
package vn.phuc.pscore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vn.phuc.pscore.PSCore;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class ConfigManager {

    private final PSCore plugin;

    private FileConfiguration messages;
    private FileConfiguration database;

    public ConfigManager(PSCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();

        this.messages = loadYaml("messages.yml");
        this.database = loadYaml("database.yml");

        validateConfiguration();
    }

    private FileConfiguration loadYaml(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            throw new IllegalStateException(
                    "Missing configuration file: " + fileName
            );
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    private void validateConfiguration() {
        String databaseType = plugin.getConfig()
                .getString("database.type", "sqlite");

        if (!"sqlite".equalsIgnoreCase(databaseType)) {
            throw new IllegalStateException(
                    "Only SQLite is supported in this version."
            );
        }

        int timeout = plugin.getConfig()
                .getInt("database.busy-timeout-ms", 5000);

        if (timeout < 0 || timeout > 60000) {
            throw new IllegalStateException(
                    "Invalid database busy timeout."
            );
        }

        int combatDuration = plugin.getConfig()
                .getInt("combat.duration-seconds", 20);

        if (combatDuration < 1 || combatDuration > 3600) {
            throw new IllegalStateException(
                    "Invalid combat duration."
            );
        }

        int rtpCooldown = plugin.getConfig()
                .getInt("teleport.rtp-cooldown-seconds", 60);

        if (rtpCooldown < 0) {
            throw new IllegalStateException(
                    "Invalid RTP cooldown."
            );
        }
    }

    public void reload() {
        load();
    }

    public String getServerName() {
        return plugin.getConfig().getString(
                "server.name",
                "PS-Smp"
        );
    }

    public String getServerAddress() {
        return plugin.getConfig().getString(
                "server.address",
                "ps-smp.play.jumphosting.cloud"
        );
    }

    public String getLanguage() {
        return plugin.getConfig().getString(
                "general.language",
                "vi"
        );
    }

    public boolean isDebugEnabled() {
        return plugin.getConfig().getBoolean(
                "general.debug",
                false
        );
    }

    public int getRtpCooldownSeconds() {
        return plugin.getConfig().getInt(
                "teleport.rtp-cooldown-seconds",
                60
        );
    }

    public int getCombatDurationSeconds() {
        return plugin.getConfig().getInt(
                "combat.duration-seconds",
                20
        );
    }

    public String getMessage(String path) {
        return messages.getString(
                path,
                "&cMissing message: " + path
        );
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getDatabaseConfig() {
        return database;
    }

    public void saveMessages() {
        File file = new File(
                plugin.getDataFolder(),
                "messages.yml"
        );

        try {
            messages.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Could not save messages.yml.",
                    exception
            );
        }
    }
}
