
package vn.phuc.pscore.auth;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class AuthAccount {

    private final UUID playerId;
    private final String username;
    private final PasswordHash passwordHash;
    private final Instant createdAt;
    private final Instant updatedAt;

    public AuthAccount(
            UUID playerId,
            String username,
            PasswordHash passwordHash,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.playerId = Objects.requireNonNull(playerId);
        this.username = Objects.requireNonNull(username);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public AuthAccount withPasswordHash(
            PasswordHash newHash,
            Instant updatedAt
    ) {
        return new AuthAccount(
                playerId,
                username,
                newHash,
                createdAt,
                updatedAt
        );
    }
}
