
package vn.phuc.pscore.auth;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class AuthSession {

    private final UUID playerId;
    private final Instant authenticatedAt;

    public AuthSession(UUID playerId, Instant authenticatedAt) {
        this.playerId = Objects.requireNonNull(playerId);
        this.authenticatedAt = Objects.requireNonNull(authenticatedAt);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Instant getAuthenticatedAt() {
        return authenticatedAt;
    }
}
