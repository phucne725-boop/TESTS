
package vn.phuc.pscore.stats;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class StatisticsSnapshot {

    private final UUID playerId;
    private final String playerName;
    private final PlayerStatistics statistics;
    private final Instant capturedAt;

    public StatisticsSnapshot(
            UUID playerId,
            String playerName,
            PlayerStatistics statistics,
            Instant capturedAt
    ) {
        this.playerId = Objects.requireNonNull(playerId);
        this.playerName = Objects.requireNonNull(playerName);
        this.statistics = Objects.requireNonNull(statistics);
        this.capturedAt = Objects.requireNonNull(capturedAt);

        if (!playerId.equals(statistics.getPlayerId())) {
            throw new IllegalArgumentException(
                    "Player ID does not match statistics."
            );
        }
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public PlayerStatistics getStatistics() {
        return statistics;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }
}
