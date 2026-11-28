
package vn.phuc.pscore.economy;

import java.util.UUID;

public final class AccountNotFoundException extends EconomyException {

    private final UUID playerId;

    public AccountNotFoundException(UUID playerId) {
        super("Account not found: " + playerId);
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }
}
