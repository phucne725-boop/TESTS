
package vn.phuc.pscore.economy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class EconomyTransaction {

    private final UUID transactionId;
    private final UUID sourcePlayer;
    private final UUID targetPlayer;
    private final Money amount;
    private final TransactionType type;
    private final TransactionStatus status;
    private final String description;
    private final Instant createdAt;

    public EconomyTransaction(
            UUID transactionId,
            UUID sourcePlayer,
            UUID targetPlayer,
            Money amount,
            TransactionType type,
            TransactionStatus status,
            String description,
            Instant createdAt
    ) {
        this.transactionId = Objects.requireNonNull(transactionId);
        this.sourcePlayer = sourcePlayer;
        this.targetPlayer = targetPlayer;
        this.amount = Objects.requireNonNull(amount);
        this.type = Objects.requireNonNull(type);
        this.status = Objects.requireNonNull(status);
        this.description = description == null ? "" : description;
        this.createdAt = Objects.requireNonNull(createdAt);

        if (amount.isZero()) {
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }

        if (sourcePlayer == null && targetPlayer == null) {
            throw new IllegalArgumentException(
                    "A transaction must have a source or target player."
            );
        }
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getSourcePlayer() {
        return sourcePlayer;
    }

    public UUID getTargetPlayer() {
        return targetPlayer;
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public EconomyTransaction withStatus(TransactionStatus newStatus) {
        return new EconomyTransaction(
                transactionId,
                sourcePlayer,
                targetPlayer,
                amount,
                type,
                newStatus,
                description,
                createdAt
        );
    }
}
