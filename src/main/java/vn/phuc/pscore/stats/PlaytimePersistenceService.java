
package vn.phuc.pscore.stats;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlaytimePersistenceService {

    private final PlaytimeTracker tracker;
    private final StatisticsService statisticsService;

    private final Set<UUID> activePlayers =
            ConcurrentHashMap.newKeySet();

    public PlaytimePersistenceService(
            PlaytimeTracker tracker,
            StatisticsService statisticsService
    ) {
        this.tracker = Objects.requireNonNull(tracker);
        this.statisticsService = Objects.requireNonNull(statisticsService);
    }

    public synchronized boolean startSession(UUID playerId) {
        Objects.requireNonNull(playerId);

        boolean started = tracker.start(playerId);

        if (started) {
            activePlayers.add(playerId);
        }

        return started;
    }

    public synchronized StatisticsSessionResult stopSession(UUID playerId) {
        Objects.requireNonNull(playerId);

        long seconds = tracker.stop(playerId);
        activePlayers.remove(playerId);

        if (seconds > 0) {
            statisticsService.addPlaytime(playerId, seconds);
        }

        return new StatisticsSessionResult(playerId, seconds);
    }

    public synchronized StatisticsSessionResult checkpoint(UUID playerId) {
        Objects.requireNonNull(playerId);

        long seconds = tracker.stop(playerId);

        if (seconds > 0) {
            statisticsService.addPlaytime(playerId, seconds);
        }

        tracker.start(playerId);
        activePlayers.add(playerId);

        return new StatisticsSessionResult(playerId, seconds);
    }

    public synchronized void flushAll() {
        for (UUID playerId : Set.copyOf(activePlayers)) {
            stopSession(playerId);
        }
    }

    public boolean isActive(UUID playerId) {
        return tracker.isTracking(playerId);
    }

    public int getActivePlayerCount() {
        return activePlayers.size();
    }
}
