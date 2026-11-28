
package vn.phuc.pscore.economy;

import java.time.Instant;
import java.util.UUID;

public final class EconomyTransactionFactory {

    private EconomyTransactionFactory() {
    }

    public static EconomyTransaction create(
            UUID source,
            UUID target,
            Money amount,
            TransactionType type,
            String description
    ) {
        if (amount == null || amount.isZero()) {
            throw new InvalidAmountException("Transaction amount must be positive.");
        }

        return new EconomyTransaction(
                UUID.randomUUID(),
                source,
                target,
                amount,
                type,
                TransactionStatus.PENDING,
                description,
                Instant.now()
        );
    }
}
