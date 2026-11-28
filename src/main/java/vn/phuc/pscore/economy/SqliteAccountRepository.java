
package vn.phuc.pscore.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SqliteAccountRepository implements AccountRepository {

    @Override
    public Optional<AccountSnapshot> find(
            Connection connection,
            UUID playerId
    ) {
        String sql = """
                SELECT uuid, username, balance, created_at, updated_at
                FROM player_accounts
                WHERE uuid = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(map(result));
            }
        } catch (SQLException exception) {
            throw new EconomyException("Could not load player account.", exception);
        }
    }

    @Override
    public void createIfAbsent(
            Connection connection,
            UUID playerId,
            String playerName,
            long timestamp
    ) {
        String sql = """
                INSERT OR IGNORE INTO player_accounts
                (uuid, username, balance, created_at, updated_at)
                VALUES (?, ?, 0, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, playerName);
            statement.setLong(3, timestamp);
            statement.setLong(4, timestamp);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new EconomyException("Could not create player account.", exception);
        }
    }

    @Override
    public void updateBalance(
            Connection connection,
            UUID playerId,
            Money balance,
            long timestamp
    ) {
        String sql = """
                UPDATE player_accounts
                SET balance = ?, updated_at = ?
                WHERE uuid = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, balance.getAmount());
            statement.setLong(2, timestamp);
            statement.setString(3, playerId.toString());

            if (statement.executeUpdate() != 1) {
                throw new AccountNotFoundException(playerId);
            }
        } catch (SQLException exception) {
            throw new EconomyException("Could not update player balance.", exception);
        }
    }

    @Override
    public boolean debit(
            Connection connection,
            UUID playerId,
            Money amount,
            long timestamp
    ) {
        String sql = """
                UPDATE player_accounts
                SET balance = balance - ?, updated_at = ?
                WHERE uuid = ? AND balance >= ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, amount.getAmount());
            statement.setLong(2, timestamp);
            statement.setString(3, playerId.toString());
            statement.setLong(4, amount.getAmount());

            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new EconomyException("Could not debit player balance.", exception);
        }
    }

    @Override
    public void credit(
            Connection connection,
            UUID playerId,
            Money amount,
            long timestamp
    ) {
        AccountSnapshot account = find(connection, playerId)
                .orElseThrow(() -> new AccountNotFoundException(playerId));

        Money newBalance;

        try {
            newBalance = account.getBalance().add(amount);
        } catch (ArithmeticException exception) {
            throw new InvalidAmountException("Player balance limit exceeded.", exception);
        }

        updateBalance(connection, playerId, newBalance, timestamp);
    }

    @Override
    public List<BalanceTopEntry> findTopBalances(
            Connection connection,
            int limit
    ) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100.");
        }

        String sql = """
                SELECT uuid, username, balance
                FROM player_accounts
                ORDER BY balance DESC, username COLLATE NOCASE ASC
                LIMIT ?
                """;

        List<BalanceTopEntry> entries = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    entries.add(new BalanceTopEntry(
                            UUID.fromString(result.getString("uuid")),
                            result.getString("username"),
                            Money.of(result.getLong("balance"))
                    ));
                }
            }

            return List.copyOf(entries);
        } catch (SQLException exception) {
            throw new EconomyException("Could not load balance leaderboard.", exception);
        }
    }

    private AccountSnapshot map(ResultSet result) throws SQLException {
        return new AccountSnapshot(
                UUID.fromString(result.getString("uuid")),
                result.getString("username"),
                Money.of(result.getLong("balance")),
                Instant.ofEpochSecond(result.getLong("created_at")),
                Instant.ofEpochSecond(result.getLong("updated_at"))
        );
    }
}
