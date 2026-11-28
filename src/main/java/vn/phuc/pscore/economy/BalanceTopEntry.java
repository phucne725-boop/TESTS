
package vn.phuc.pscore.economy;

import java.util.Objects;
import java.util.UUID;

public final class BalanceTopEntry {

    private final UUID playerId;
    private final String playerName;
    private final Money balance;

    public BalanceTopEntry(
            UUID playerId,
            String playerName,
            Money balance
    ) {
        this.playerId = Objects.requireNonNull(playerId);
        this.playerName = Objects.requireNonNull(playerName);
        this.balance = Objects.requireNonNull(balance);
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
}
