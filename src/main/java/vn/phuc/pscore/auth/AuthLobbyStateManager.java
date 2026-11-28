package vn.phuc.pscore.auth;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthLobbyStateManager {

    private final Set<UUID> pendingPlayers = ConcurrentHashMap.newKeySet();

    public void markPending(UUID playerId) {
        pendingPlayers.add(playerId);
    }

    public void markAuthenticated(UUID playerId) {
        pendingPlayers.remove(playerId);
    }

    public void remove(UUID playerId) {
        pendingPlayers.remove(playerId);
    }

    public boolean isPending(UUID playerId) {
        return pendingPlayers.contains(playerId);
    }

    public int getPendingCount() {
        return pendingPlayers.size();
    }
}
