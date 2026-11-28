
package vn.phuc.pscore.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaMigrationV1 implements Migration {

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Initial PSCore persistent data schema";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_accounts (
                        uuid TEXT PRIMARY KEY,
                        username TEXT NOT NULL,
                        balance INTEGER NOT NULL DEFAULT 0
                            CHECK(balance >= 0),
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        uuid TEXT PRIMARY KEY,
                        playtime_seconds INTEGER NOT NULL DEFAULT 0
                            CHECK(playtime_seconds >= 0),
                        kills INTEGER NOT NULL DEFAULT 0
                            CHECK(kills >= 0),
                        deaths INTEGER NOT NULL DEFAULT 0
                            CHECK(deaths >= 0),
                        FOREIGN KEY(uuid)
                            REFERENCES player_accounts(uuid)
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_homes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        owner_uuid TEXT NOT NULL,
                        home_name TEXT NOT NULL,
                        world_name TEXT NOT NULL,
                        x REAL NOT NULL,
                        y REAL NOT NULL,
                        z REAL NOT NULL,
                        yaw REAL NOT NULL,
                        pitch REAL NOT NULL,
                        created_at INTEGER NOT NULL,
                        UNIQUE(owner_uuid, home_name),
                        FOREIGN KEY(owner_uuid)
                            REFERENCES player_accounts(uuid)
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS transaction_journal (
                        transaction_id TEXT PRIMARY KEY,
                        transaction_type TEXT NOT NULL,
                        status TEXT NOT NULL,
                        actor_uuid TEXT,
                        target_uuid TEXT,
                        amount INTEGER NOT NULL DEFAULT 0,
                        payload TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS item_escrow (
                        escrow_id TEXT PRIMARY KEY,
                        transaction_id TEXT NOT NULL,
                        owner_uuid TEXT NOT NULL,
                        item_data BLOB NOT NULL,
                        item_state TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        FOREIGN KEY(transaction_id)
                            REFERENCES transaction_journal(transaction_id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS auction_listings (
                        listing_id TEXT PRIMARY KEY,
                        seller_uuid TEXT NOT NULL,
                        item_data BLOB NOT NULL,
                        quantity INTEGER NOT NULL CHECK(quantity > 0),
                        price INTEGER NOT NULL CHECK(price > 0),
                        status TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_orders (
                        order_id TEXT PRIMARY KEY,
                        owner_uuid TEXT NOT NULL,
                        material_key TEXT NOT NULL,
                        quantity_total INTEGER NOT NULL
                            CHECK(quantity_total > 0),
                        quantity_fulfilled INTEGER NOT NULL DEFAULT 0
                            CHECK(quantity_fulfilled >= 0),
                        unit_price INTEGER NOT NULL CHECK(unit_price > 0),
                        status TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        CHECK(quantity_fulfilled <= quantity_total)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS transaction_events (
                        event_id TEXT PRIMARY KEY,
                        transaction_id TEXT NOT NULL,
                        event_type TEXT NOT NULL,
                        details TEXT,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(transaction_id)
                            REFERENCES transaction_journal(transaction_id)
                    )
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_auction_status_created
                    ON auction_listings(status, created_at)
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_order_status_created
                    ON player_orders(status, created_at)
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_escrow_owner_state
                    ON item_escrow(owner_uuid, item_state)
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_transaction_status
                    ON transaction_journal(status, updated_at)
                    """);
        }
    }
}
