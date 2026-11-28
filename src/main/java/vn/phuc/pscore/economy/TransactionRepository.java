
package vn.phuc.pscore.economy;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    void insert(
            Connection connection,
            EconomyTransaction transaction
    );

    void updateStatus(
            Connection connection,
            UUID transactionId,
            TransactionStatus status,
            long updatedAt
    );

    Optional<EconomyTransaction> find(
            Connection connection,
            UUID transactionId
    );
}
