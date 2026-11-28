
package vn.phuc.pscore.economy;

import java.util.Objects;
import java.util.UUID;

public final class EconomyOperationResult {

    private final UUID transactionId;
    private final Money amount;
    private final Money sourceBalance;
    private final Money targetBalance;

    public EconomyOperationResult(
            UUID transactionId,
            Money amount,
            Money sourceBalance,
            Money targetBalance
    ) {
        this.transactionId = Objects.requireNonNull(transactionId);
        this.amount = Objects.requireNonNull(amount);
        this.sourceBalance = sourceBalance;
        this.targetBalance = targetBalance;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public Money getAmount() {
        return amount;
    }

    public Money getSourceBalance() {
        return sourceBalance;
    }

    public Money getTargetBalance() {
        return targetBalance;
    }
}
