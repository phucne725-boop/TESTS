
package vn.phuc.pscore.stats;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PlaytimeSession {

    private final UUID playerId;
    private final Instant startedAt;

    public PlaytimeSession(UUID playerId, Instant startedAt) {
        this.playerId = Objects.requireNonNull(playerId);
        this.startedAt = Objects.requireNonNull(startedAt);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public long elapsedSeconds(Instant now) {
        Objects.requireNonNull(now);

        if (now.isBefore(startedAt)) {
            return 0;
        }

        return now.getEpochSecond() - startedAt.getEpochSecond();
    }
}
