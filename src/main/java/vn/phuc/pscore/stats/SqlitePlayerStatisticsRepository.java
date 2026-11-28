
package vn.phuc.pscore.stats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SqlitePlayerStatisticsRepository
        implements PlayerStatisticsRepository {

    @Override
    public void createIfAbsent(Connection connection, UUID playerId) {
        String sql = """
                INSERT OR IGNORE INTO player_stats
                (uuid, playtime_seconds, kills, deaths)
                VALUES (?, 0, 0, 0)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new StatisticsRepositoryException(
                    "Could not create player statistics.",
                    exception
            );
        }
    }

    @Override
    public Optional<PlayerStatistics> find(
            Connection connection,
            UUID playerId
    ) {
        String sql = """
                SELECT uuid, kills, deaths, playtime_seconds
                FROM player_stats
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
            throw new StatisticsRepositoryException(
                    "Could not load player statistics.",
                    exception
            );
        }
    }

    @Override
    public void addKill(Connection connection, UUID playerId) {
        increment(connection, playerId, "kills", 1);
    }

    @Override
    public void addDeath(Connection connection, UUID playerId) {
        increment(connection, playerId, "deaths", 1);
    }

    @Override
    public void addPlaytime(
            Connection connection,
            UUID playerId,
            long seconds
    ) {
        if (seconds <= 0) {
            throw new IllegalArgumentException(
                    "Seconds must be greater than zero."
            );
        }

        increment(connection, playerId, "playtime_seconds", seconds);
    }

    @Override
    public List<PlayerStatistics> findTopPlaytime(
            Connection connection,
            int limit
    ) {
        return findTop(connection, "playtime_seconds", limit);
    }

    @Override
    public List<PlayerStatistics> findTopKills(
            Connection connection,
            int limit
    ) {
        return findTop(connection, "kills", limit);
    }

    private void increment(
            Connection connection,
            UUID playerId,
            String column,
            long amount
    ) {
        if (!column.equals("kills")
                && !column.equals("deaths")
                && !column.equals("playtime_seconds")) {
            throw new IllegalArgumentException("Unsupported statistics column.");
        }

        String sql = """
                UPDATE player_stats
                SET %s = %s + ?
                WHERE uuid = ?
                """.formatted(column, column);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, amount);
            statement.setString(2, playerId.toString());

            if (statement.executeUpdate() != 1) {
                throw new StatisticsException(
                        "Player statistics do not exist."
                );
            }
        } catch (SQLException exception) {
            throw new StatisticsRepositoryException(
                    "Could not update player statistics.",
                    exception
            );
        }
    }

    private List<PlayerStatistics> findTop(
            Connection connection,
            String column,
            int limit
    ) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 100."
            );
        }

        if (!column.equals("kills")
                && !column.equals("playtime_seconds")) {
            throw new IllegalArgumentException("Unsupported leaderboard column.");
        }

        String sql = """
                SELECT uuid, kills, deaths, playtime_seconds
                FROM player_stats
                ORDER BY %s DESC, uuid ASC
                LIMIT ?
                """.formatted(column);

        List<PlayerStatistics> entries = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    entries.add(map(result));
                }
            }

            return List.copyOf(entries);
        } catch (SQLException exception) {
            throw new StatisticsRepositoryException(
                    "Could not load statistics leaderboard.",
                    exception
            );
        }
    }

    private PlayerStatistics map(ResultSet result) throws SQLException {
        return new PlayerStatistics(
                UUID.fromString(result.getString("uuid")),
                result.getLong("kills"),
                result.getLong("deaths"),
                result.getLong("playtime_seconds")
        );
    }
}
