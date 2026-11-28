
package vn.phuc.pscore.database;

import vn.phuc.pscore.PSCore;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DatabaseManager {

    private final PSCore plugin;
    private final DatabaseConfig config;
    private final SqliteConnectionFactory connectionFactory;
    private final MigrationRunner migrationRunner;
    private final DatabaseHealth health;
    private final DatabaseTransactionRunner transactions;
    private final DatabaseBackupService backups;

    private final AtomicBoolean initialized =
            new AtomicBoolean(false);

    public DatabaseManager(PSCore plugin) {
        this.plugin = plugin;

        this.config = new DatabaseConfig(plugin);

        this.connectionFactory =
                new SqliteConnectionFactory(config);

        this.migrationRunner = new MigrationRunner(
                List.of(new SchemaMigrationV1())
        );

        this.health = new DatabaseHealth(connectionFactory);

        this.transactions =
                new DatabaseTransactionRunner(connectionFactory);

        this.backups = new DatabaseBackupService(
                config,
                connectionFactory
        );
    }

    public synchronized void initialize() {
        if (initialized.get()) {
            return;
        }

        try (Connection connection =
                     connectionFactory.openConnection()) {

            migrationRunner.run(connection);

            health.verify();

            initialized.set(true);

            plugin.getLogger().info(
                    "SQLite database initialized: "
                            + config.getDatabasePath()
            );

        } catch (Exception exception) {
            throw new DatabaseException(
                    "PSCore database initialization failed. "
                            + "Plugin startup must not continue.",
                    exception
            );
        }
    }

    public Connection getConnection() {
        ensureInitialized();

        return connectionFactory.openConnection();
    }

    public DatabaseTransactionRunner getTransactions() {
        ensureInitialized();
        return transactions;
    }

    public DatabaseHealth getHealth() {
        ensureInitialized();
        return health;
    }

    public DatabaseBackupService getBackups() {
        ensureInitialized();
        return backups;
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    private void ensureInitialized() {
        if (!initialized.get()) {
            throw new IllegalStateException(
                    "Database has not been initialized."
            );
        }
    }

    public synchronized void shutdown() {
        initialized.set(false);
    }
}
