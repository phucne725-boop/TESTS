
package vn.phuc.pscore.stats;

import java.util.Objects;
import java.util.UUID;

public final class StatisticsUpdate {

    public enum Type {
        KILL,
        DEATH,
        PLAYTIME
    }

    private final UUID playerId;
    private final Type type;
    private final long amount;

    public StatisticsUpdate(
            UUID playerId,
            Type type,
            long amount
    ) {
        this.playerId = Objects.requireNonNull(playerId);
        this.type = Objects.requireNonNull(type);

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Update amount must be positive."
            );
        }

        if (type != Type.PLAYTIME && amount != 1) {
            throw new IllegalArgumentException(
                    "Kill and death updates must have amount 1."
            );
        }

        this.amount = amount;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Type getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }
}
