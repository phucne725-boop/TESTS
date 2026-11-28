
package vn.phuc.pscore.database;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseTransactionRunner {

    private final SqliteConnectionFactory connectionFactory;

    public DatabaseTransactionRunner(
            SqliteConnectionFactory connectionFactory
    ) {
        this.connectionFactory = connectionFactory;
    }

    public <T> T execute(SqlWork<T> work) {
        try (Connection connection =
                     connectionFactory.openConnection()) {

            connection.setAutoCommit(false);

            try {
                T result = work.execute(connection);

                connection.commit();

                return result;

            } catch (Exception exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                if (exception instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new DatabaseException(
                        "Database transaction failed and was rolled back.",
                        exception
                );
            }

        } catch (DatabaseException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new DatabaseException(
                    "Could not complete database transaction.",
                    exception
            );
        }
    }

    @FunctionalInterface
    public interface SqlWork<T> {
        T execute(Connection connection) throws Exception;
    }
}
