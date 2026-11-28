
package vn.phuc.pscore.economy;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<AccountSnapshot> find(
            Connection connection,
            UUID playerId
    );

    void createIfAbsent(
            Connection connection,
            UUID playerId,
            String playerName,
            long timestamp
    );

    void updateBalance(
            Connection connection,
            UUID playerId,
            Money balance,
            long timestamp
    );

    boolean debit(
            Connection connection,
            UUID playerId,
            Money amount,
            long timestamp
    );

    void credit(
            Connection connection,
            UUID playerId,
            Money amount,
            long timestamp
    );

    List<BalanceTopEntry> findTopBalances(
            Connection connection,
            int limit
    );
}
