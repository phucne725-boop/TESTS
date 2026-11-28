
package vn.phuc.pscore.database;

import org.bukkit.configuration.file.FileConfiguration;
import vn.phuc.pscore.PSCore;

import java.io.File;
import java.nio.file.Path;

public final class DatabaseConfig {

    private final Path databasePath;
    private final int busyTimeout;
    private final boolean foreignKeys;
    private final String journalMode;
    private final String synchronousMode;
    private final boolean recoveryEnabled;
    private final boolean backupEnabled;
    private final int backupRetention;

    public DatabaseConfig(PSCore plugin) {
        FileConfiguration main = plugin.getConfig();
        FileConfiguration database =
                plugin.getConfigManager().getDatabaseConfig();

        String configuredPath = main.getString(
                "database.file",
                "data/pscore.db"
        );

        if (configuredPath == null || configuredPath.isBlank()) {
            throw new DatabaseException(
                    "Database path cannot be empty."
            );
        }

        Path path = plugin.getDataFolder()
                .toPath()
                .resolve(configuredPath)
                .normalize();

        if (!path.startsWith(plugin.getDataFolder().toPath().normalize())) {
            throw new DatabaseException(
                    "Database path cannot escape plugin data folder."
            );
        }

        this.databasePath = path;

        this.busyTimeout = database.getInt(
                "sqlite.busy-timeout-ms",
                main.getInt("database.busy-timeout-ms", 5000)
        );

        this.foreignKeys = database.getBoolean(
                "sqlite.foreign-keys",
                true
        );

        this.journalMode = database.getString(
                "sqlite.journal-mode",
                "WAL"
        );

        this.synchronousMode = database.getString(
                "sqlite.synchronous",
                "FULL"
        );

        this.recoveryEnabled = database.getBoolean(
                "transactions.recovery-on-startup",
                true
        );

        this.backupEnabled = database.getBoolean(
                "backups.enabled",
                true
        );

        this.backupRetention = database.getInt(
                "backups.keep-last",
                10
        );

        validate();
    }

    private void validate() {
        if (busyTimeout < 0 || busyTimeout > 60000) {
            throw new DatabaseException(
                    "Invalid SQLite busy timeout."
            );
        }

        if (!"WAL".equalsIgnoreCase(journalMode)
                && !"DELETE".equalsIgnoreCase(journalMode)) {
            throw new DatabaseException(
                    "Unsupported SQLite journal mode."
            );
        }

        if (!"FULL".equalsIgnoreCase(synchronousMode)
                && !"NORMAL".equalsIgnoreCase(synchronousMode)
                && !"EXTRA".equalsIgnoreCase(synchronousMode)) {
            throw new DatabaseException(
                    "Unsupported SQLite synchronous mode."
            );
        }

        if (backupRetention < 1 || backupRetention > 1000) {
            throw new DatabaseException(
                    "Invalid backup retention."
            );
        }
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    public int getBusyTimeout() {
        return busyTimeout;
    }

    public boolean isForeignKeysEnabled() {
        return foreignKeys;
    }

    public String getJournalMode() {
        return journalMode;
    }

    public String getSynchronousMode() {
        return synchronousMode;
    }

    public boolean isRecoveryEnabled() {
        return recoveryEnabled;
    }

    public boolean isBackupEnabled() {
        return backupEnabled;
    }

    public int getBackupRetention() {
        return backupRetention;
    }
}
