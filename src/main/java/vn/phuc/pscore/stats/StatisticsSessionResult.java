
package vn.phuc.pscore.stats;

import java.util.Objects;
import java.util.UUID;

public final class StatisticsSessionResult {

    private final UUID playerId;
    private final long recordedSeconds;

    public StatisticsSessionResult(
            UUID playerId,
            long recordedSeconds
    ) {
        this.playerId = Objects.requireNonNull(playerId);

        if (recordedSeconds < 0) {
            throw new IllegalArgumentException(
                    "Recorded seconds cannot be negative."
            );
        }

        this.recordedSeconds = recordedSeconds;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getRecordedSeconds() {
        return recordedSeconds;
    }

    public boolean hasRecordedTime() {
        return recordedSeconds > 0;
    }
}
