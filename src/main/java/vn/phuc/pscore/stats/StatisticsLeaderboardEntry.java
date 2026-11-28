
package vn.phuc.pscore.stats;

import java.util.Objects;
import java.util.UUID;

public final class StatisticsLeaderboardEntry {

    private final int position;
    private final UUID playerId;
    private final String playerName;
    private final long value;

    public StatisticsLeaderboardEntry(
            int position,
            UUID playerId,
            String playerName,
            long value
    ) {
        if (position < 1 || value < 0) {
            throw new IllegalArgumentException(
                    "Invalid leaderboard entry."
            );
        }

        this.position = position;
        this.playerId = Objects.requireNonNull(playerId);
        this.playerName = Objects.requireNonNull(playerName);
        this.value = value;
    }

    public int getPosition() {
        return position;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getValue() {
        return value;
    }
}
