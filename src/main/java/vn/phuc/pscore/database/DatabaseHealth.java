
package vn.phuc.pscore.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DatabaseHealth {

    private final SqliteConnectionFactory connectionFactory;

    public DatabaseHealth(
            SqliteConnectionFactory connectionFactory
    ) {
        this.connectionFactory = connectionFactory;
    }

    public boolean isHealthy() {
        try (Connection connection =
                     connectionFactory.openConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA quick_check")) {

            return result.next()
                    && "ok".equalsIgnoreCase(result.getString(1));

        } catch (Exception exception) {
            return false;
        }
    }

    public void verify() {
        try (Connection connection =
                     connectionFactory.openConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA quick_check")) {

            if (!result.next()
                    || !"ok".equalsIgnoreCase(result.getString(1))) {
                throw new DatabaseException(
                        "SQLite integrity check failed."
                );
            }

        } catch (DatabaseException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DatabaseException(
                    "Could not verify database integrity.",
                    exception
            );
        }
    }
}
