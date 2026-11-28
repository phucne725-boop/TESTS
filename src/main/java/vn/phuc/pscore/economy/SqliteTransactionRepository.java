
package vn.phuc.pscore.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class SqliteTransactionRepository implements TransactionRepository {

    @Override
    public void insert(
            Connection connection,
            EconomyTransaction transaction
    ) {
        String sql = """
                INSERT INTO transaction_journal
                (transaction_id, transaction_type, status,
                 actor_uuid, target_uuid, amount, payload,
                 created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getTransactionId().toString());
            statement.setString(2, transaction.getType().name());
            statement.setString(3, transaction.getStatus().name());

            statement.setString(
                    4,
                    transaction.getSourcePlayer() == null
                            ? null
                            : transaction.getSourcePlayer().toString()
            );

            statement.setString(
                    5,
                    transaction.getTargetPlayer() == null
                            ? null
                            : transaction.getTargetPlayer().toString()
            );

            statement.setLong(6, transaction.getAmount().getAmount());
            statement.setString(7, transaction.getDescription());
            statement.setLong(8, transaction.getCreatedAt().getEpochSecond());
            statement.setLong(9, transaction.getCreatedAt().getEpochSecond());

            statement.executeUpdate();
        } catch (SQLException exception) {
            if (exception.getMessage() != null
                    && exception.getMessage().toLowerCase().contains("unique")) {
                throw new DuplicateTransactionException(
                        transaction.getTransactionId()
                );
            }

            throw new EconomyException("Could not save transaction.", exception);
        }
    }

    @Override
    public void updateStatus(
            Connection connection,
            UUID transactionId,
            TransactionStatus status,
            long updatedAt
    ) {
        String sql = """
                UPDATE transaction_journal
                SET status = ?, updated_at = ?
                WHERE transaction_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, updatedAt);
            statement.setString(3, transactionId.toString());

            if (statement.executeUpdate() != 1) {
                throw new EconomyException("Transaction record was not found.");
            }
        } catch (SQLException exception) {
            throw new EconomyException("Could not update transaction status.", exception);
        }
    }

    @Override
    public Optional<EconomyTransaction> find(
            Connection connection,
            UUID transactionId
    ) {
        String sql = """
                SELECT transaction_id, transaction_type, status,
                       actor_uuid, target_uuid, amount, payload, created_at
                FROM transaction_journal
                WHERE transaction_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transactionId.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                String source = result.getString("actor_uuid");
                String target = result.getString("target_uuid");

                return Optional.of(new EconomyTransaction(
                        UUID.fromString(result.getString("transaction_id")),
                        source == null ? null : UUID.fromString(source),
                        target == null ? null : UUID.fromString(target),
                        Money.of(result.getLong("amount")),
                        TransactionType.valueOf(result.getString("transaction_type")),
                        TransactionStatus.valueOf(result.getString("status")),
                        result.getString("payload"),
                        Instant.ofEpochSecond(result.getLong("created_at"))
                ));
            }
        } catch (SQLException | IllegalArgumentException exception) {
            throw new EconomyException("Could not load transaction.", exception);
        }
    }
}
