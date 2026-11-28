
package vn.phuc.pscore.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StatisticsLeaderboardService {

    private final StatisticsService statisticsService;
    private final StatisticsPlayerNameResolver nameResolver;

    public StatisticsLeaderboardService(
            StatisticsService statisticsService,
            StatisticsPlayerNameResolver nameResolver
    ) {
        this.statisticsService = Objects.requireNonNull(statisticsService);
        this.nameResolver = Objects.requireNonNull(nameResolver);
    }

    public List<StatisticsLeaderboardEntry> getLeaderboard(
            StatisticsLeaderboardType type,
            int limit
    ) {
        Objects.requireNonNull(type);

        List<PlayerStatistics> statistics = switch (type) {
            case PLAYTIME -> statisticsService.getTopPlaytime(limit);
            case KILLS -> statisticsService.getTopKills(limit);
        };

        List<StatisticsLeaderboardEntry> entries =
                new ArrayList<>(statistics.size());

        int position = 1;

        for (PlayerStatistics entry : statistics) {
            String name = nameResolver.resolveName(entry.getPlayerId());

            if (name == null || name.isBlank()) {
                name = entry.getPlayerId().toString();
            }

            long value = type == StatisticsLeaderboardType.PLAYTIME
                    ? entry.getPlaytimeSeconds()
                    : entry.getKills();

            entries.add(new StatisticsLeaderboardEntry(
                    position++,
                    entry.getPlayerId(),
                    name,
                    value
            ));
        }

        return List.copyOf(entries);
    }
}
