package vn.phuc.pscore.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class AuthMigrationV2 implements Migration {

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public String getDescription() {
        return "Authentication accounts and last player locations";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS auth_accounts (
                        player_uuid TEXT PRIMARY KEY,
                        username TEXT NOT NULL,
                        password_hash TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_last_locations (
                        player_uuid TEXT PRIMARY KEY,
                        world_name TEXT NOT NULL,
                        x REAL NOT NULL,
                        y REAL NOT NULL,
                        z REAL NOT NULL,
                        yaw REAL NOT NULL,
                        pitch REAL NOT NULL,
                        saved_at INTEGER NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_auth_username
                    ON auth_accounts(username)
                    """);
        }
    }
}
