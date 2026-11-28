
package vn.phuc.pscore.economy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class AccountSnapshot {

    private final UUID playerId;
    private final String playerName;
    private final Money balance;
    private final Instant createdAt;
    private final Instant updatedAt;

    public AccountSnapshot(
            UUID playerId,
            String playerName,
            Money balance,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.playerId = Objects.requireNonNull(playerId);
        this.playerName = Objects.requireNonNull(playerName);
        this.balance = Objects.requireNonNull(balance);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Money getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
