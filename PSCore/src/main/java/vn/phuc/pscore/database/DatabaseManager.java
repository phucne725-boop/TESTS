
package vn.phuc.pscore.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void initialize() throws SQLException {
        File folder = plugin.getDataFolder();

        if (!folder.exists() && !folder.mkdirs()) {
            throw new SQLException("Khong tao duoc thu muc plugin.");
        }

        String fileName = plugin.getConfig().getString(
                "database.file", "database.db"
        );

        File file = new File(folder, fileName);

        connection = DriverManager.getConnection(
                "jdbc:sqlite:" + file.getAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA busy_timeout = 5000");

            statement.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    first_join INTEGER NOT NULL,
                    last_seen INTEGER NOT NULL,
                    playtime_seconds INTEGER NOT NULL DEFAULT 0
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    uuid TEXT PRIMARY KEY,
                    role TEXT NOT NULL,
                    expires_at INTEGER NOT NULL DEFAULT 0
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS auctions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller_uuid TEXT NOT NULL,
                    seller_name TEXT NOT NULL,
                    item BLOB NOT NULL,
                    price REAL NOT NULL,
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    created_at INTEGER NOT NULL,
                    expires_at INTEGER NOT NULL DEFAULT 0,
                    buyer_uuid TEXT,
                    buyer_name TEXT,
                    sold_at INTEGER
                )
            """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_auctions_status
                ON auctions(status, created_at)
            """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_auctions_seller
                ON auctions(seller_uuid, status)
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS auction_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    auction_id INTEGER NOT NULL,
                    seller_uuid TEXT NOT NULL,
                    seller_name TEXT NOT NULL,
                    buyer_uuid TEXT NOT NULL,
                    buyer_name TEXT NOT NULL,
                    item BLOB NOT NULL,
                    price REAL NOT NULL,
                    completed_at INTEGER NOT NULL
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS item_claims (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_uuid TEXT NOT NULL,
                    source TEXT NOT NULL,
                    source_id TEXT NOT NULL,
                    item BLOB NOT NULL,
                    created_at INTEGER NOT NULL,
                    claimed_at INTEGER
                )
            """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_item_claims_owner
                ON item_claims(owner_uuid, claimed_at)
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS auction_purchases (
                    auction_id INTEGER PRIMARY KEY,
                    buyer_uuid TEXT NOT NULL,
                    buyer_name TEXT NOT NULL,
                    price REAL NOT NULL,
                    state TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS auction_payouts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    auction_id INTEGER NOT NULL,
                    seller_uuid TEXT NOT NULL,
                    amount REAL NOT NULL,
                    state TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    paid_at INTEGER
                )
            """);
        }
    }

    public synchronized Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database chua khoi tao.");
        }
        return connection;
    }

    public synchronized void close() {
        if (connection == null) return;

        try {
            connection.close();
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Loi dong database: " + exception.getMessage()
            );
        } finally {
            connection = null;
        }
    }
}
