
package vn.phutsmile.pscore.database;

import vn.phutsmile.pscore.PSCore;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private final PSCore plugin;
    private Connection connection;

    public DatabaseManager(PSCore plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        File folder = plugin.getDataFolder();

        if (!folder.exists() && !folder.mkdirs()) {
            throw new SQLException("Cannot create plugin data folder.");
        }

        String fileName = plugin.getConfig().getString(
            "database.file",
            "database.db"
        );

        File databaseFile = new File(folder, fileName);

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("SQLite driver is missing.", exception);
        }

        connection = DriverManager.getConnection(
            "jdbc:sqlite:" + databaseFile.getAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.execute("PRAGMA journal_mode = WAL;");
            statement.execute("PRAGMA busy_timeout = 5000;");
        }

        plugin.getLogger().info(
            "SQLite connected: " + databaseFile.getName()
        );
    }

    public void initializeTables() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Database is not connected.");
        }

        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid TEXT PRIMARY KEY,
                    player_name TEXT NOT NULL,
                    playtime_seconds INTEGER NOT NULL DEFAULT 0,
                    first_join INTEGER NOT NULL DEFAULT 0,
                    last_seen INTEGER NOT NULL DEFAULT 0
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auction_listings (
                    id TEXT PRIMARY KEY,
                    seller_uuid TEXT NOT NULL,
                    item_data TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    price REAL NOT NULL,
                    created_at INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'ACTIVE'
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auction_transactions (
                    id TEXT PRIMARY KEY,
                    listing_id TEXT NOT NULL,
                    buyer_uuid TEXT NOT NULL,
                    seller_uuid TEXT NOT NULL,
                    item_data TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    price REAL NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS staff_roles (
                    player_uuid TEXT PRIMARY KEY,
                    player_name TEXT NOT NULL,
                    role_name TEXT NOT NULL,
                    granted_at INTEGER NOT NULL,
                    expires_at INTEGER NOT NULL,
                    granted_by TEXT NOT NULL
                )
                """);
        }

        plugin.getLogger().info("Database tables initialized.");
    }

    public synchronized Connection getConnection() {
        return connection;
    }

    public synchronized void close() {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.close();
            }

            plugin.getLogger().info("SQLite disconnected.");
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                "Cannot close SQLite: " + exception.getMessage()
            );
        }
    }
}
