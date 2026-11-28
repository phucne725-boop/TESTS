
package vn.phuc.pscore.economy;

import java.util.UUID;

public final class DuplicateTransactionException extends EconomyException {

    private final UUID transactionId;

    public DuplicateTransactionException(UUID transactionId) {
        super("Transaction already exists: " + transactionId);
        this.transactionId = transactionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
}
