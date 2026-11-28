
package vn.phuc.pscore.stats;

import vn.phuc.pscore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public final class StatisticsService {

    private final DatabaseManager database;
    private final PlayerStatisticsRepository repository;

    public StatisticsService(
            DatabaseManager database,
            PlayerStatisticsRepository repository
    ) {
        this.database = database;
        this.repository = repository;
    }

    public PlayerStatistics getOrCreate(UUID playerId) {
        return database.getTransactions().execute(connection -> {
            repository.createIfAbsent(connection, playerId);

            return repository.find(connection, playerId)
                    .orElseThrow(() -> new StatisticsException(
                            "Could not create player statistics."
                    ));
        });
    }

    public void recordKill(UUID playerId) {
        database.getTransactions().execute(connection -> {
            repository.createIfAbsent(connection, playerId);
            repository.addKill(connection, playerId);
            return null;
        });
    }

    public void recordDeath(UUID playerId) {
        database.getTransactions().execute(connection -> {
            repository.createIfAbsent(connection, playerId);
            repository.addDeath(connection, playerId);
            return null;
        });
    }

    public void addPlaytime(UUID playerId, long seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException(
                    "Playtime must be greater than zero."
            );
        }

        database.getTransactions().execute(connection -> {
            repository.createIfAbsent(connection, playerId);
            repository.addPlaytime(connection, playerId, seconds);
            return null;
        });
    }

    public List<PlayerStatistics> getTopPlaytime(int limit) {
        validateLimit(limit);

        try (Connection connection = database.getConnection()) {
            return repository.findTopPlaytime(connection, limit);
        } catch (SQLException exception) {
            throw new StatisticsException(
                    "Could not load playtime leaderboard.",
                    exception
            );
        }
    }

    public List<PlayerStatistics> getTopKills(int limit) {
        validateLimit(limit);

        try (Connection connection = database.getConnection()) {
            return repository.findTopKills(connection, limit);
        } catch (SQLException exception) {
            throw new StatisticsException(
                    "Could not load kills leaderboard.",
                    exception
            );
        }
    }

    private void validateLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 100."
            );
        }
    }
}
