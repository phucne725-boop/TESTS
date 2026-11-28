
package vn.phuc.pscore.stats;

import vn.phuc.pscore.database.DatabaseManager;

import java.util.Objects;
import java.util.UUID;

public final class StatisticsManager {

    private final StatisticsService statisticsService;
    private final PlaytimeTracker playtimeTracker;
    private final PlaytimePersistenceService playtimePersistence;
    private final StatisticsLeaderboardService leaderboardService;

    public StatisticsManager(
            DatabaseManager database,
            StatisticsPlayerNameResolver nameResolver
    ) {
        Objects.requireNonNull(database);
        Objects.requireNonNull(nameResolver);

        PlayerStatisticsRepository repository =
                new SqlitePlayerStatisticsRepository();

        this.statisticsService = new StatisticsService(
                database,
                repository
        );

        this.playtimeTracker = new PlaytimeTracker();

        this.playtimePersistence = new PlaytimePersistenceService(
                playtimeTracker,
                statisticsService
        );

        this.leaderboardService = new StatisticsLeaderboardService(
                statisticsService,
                nameResolver
        );
    }

    public PlayerStatistics getStatistics(UUID playerId) {
        return statisticsService.getOrCreate(playerId);
    }

    public void recordKill(UUID playerId) {
        statisticsService.recordKill(playerId);
    }

    public void recordDeath(UUID playerId) {
        statisticsService.recordDeath(playerId);
    }

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    public PlaytimeTracker getPlaytimeTracker() {
        return playtimeTracker;
    }

    public PlaytimePersistenceService getPlaytimePersistence() {
        return playtimePersistence;
    }

    public StatisticsLeaderboardService getLeaderboardService() {
        return leaderboardService;
    }
}
