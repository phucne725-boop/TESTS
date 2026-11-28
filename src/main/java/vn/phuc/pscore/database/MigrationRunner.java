
package vn.phuc.pscore.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MigrationRunner {

    private final List<Migration> migrations;

    public MigrationRunner(List<Migration> migrations) {
        this.migrations = new ArrayList<>(migrations);

        this.migrations.sort(
                Comparator.comparingInt(Migration::getVersion)
        );

        validateVersions();
    }

    private void validateVersions() {
        int previous = 0;

        for (Migration migration : migrations) {
            if (migration.getVersion() <= previous) {
                throw new DatabaseException(
                        "Migration versions must be unique and positive."
                );
            }

            previous = migration.getVersion();
        }
    }

    public void run(Connection connection) throws SQLException {
        createVersionTable(connection);

        int currentVersion = getCurrentVersion(connection);

        for (Migration migration : migrations) {
            if (migration.getVersion() <= currentVersion) {
                continue;
            }

            boolean previousAutoCommit = connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                migration.migrate(connection);

                saveVersion(connection, migration);

                connection.commit();

                currentVersion = migration.getVersion();
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private void createVersionTable(Connection connection)
            throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS schema_migrations (
                    version INTEGER PRIMARY KEY,
                    description TEXT NOT NULL,
                    applied_at INTEGER NOT NULL
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private int getCurrentVersion(Connection connection)
            throws SQLException {

        String sql = """
                SELECT COALESCE(MAX(version), 0)
                FROM schema_migrations
                """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            return result.next() ? result.getInt(1) : 0;
        }
    }

    private void saveVersion(
            Connection connection,
            Migration migration
    ) throws SQLException {

        String sql = """
                INSERT INTO schema_migrations
                    (version, description, applied_at)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, migration.getVersion());
            statement.setString(2, migration.getDescription());
            statement.setLong(3,
                    System.currentTimeMillis());

            statement.executeUpdate();
        }
    }
}
