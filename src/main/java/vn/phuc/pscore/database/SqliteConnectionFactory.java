
package vn.phuc.pscore.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class SqliteConnectionFactory {

    private final DatabaseConfig config;
    private final String jdbcUrl;

    public SqliteConnectionFactory(DatabaseConfig config) {
        this.config = config;

        this.jdbcUrl = "jdbc:sqlite:"
                + config.getDatabasePath().toAbsolutePath();
    }

    public Connection openConnection() {
        try {
            Path parent = config.getDatabasePath()
                    .toAbsolutePath()
                    .getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Connection connection =
                    DriverManager.getConnection(jdbcUrl);

            configure(connection);

            return connection;
        } catch (Exception exception) {
            throw new DatabaseException(
                    "Could not open SQLite connection.",
                    exception
            );
        }
    }

    private void configure(Connection connection)
            throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "PRAGMA busy_timeout = "
                            + config.getBusyTimeout()
            );

            statement.execute(
                    "PRAGMA foreign_keys = "
                            + (config.isForeignKeysEnabled() ? "ON" : "OFF")
            );

            statement.execute(
                    "PRAGMA synchronous = "
                            + config.getSynchronousMode()
            );
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
