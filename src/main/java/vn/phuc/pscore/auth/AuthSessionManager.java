
package vn.phuc.pscore.auth;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthSessionManager {

    private final Map<UUID, AuthSession> sessions =
            new ConcurrentHashMap<>();

    public void authenticate(UUID playerId) {
        sessions.put(
                playerId,
                new AuthSession(playerId, Instant.now())
        );
    }

    public boolean isAuthenticated(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public Optional<AuthSession> getSession(UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public boolean logout(UUID playerId) {
        return sessions.remove(playerId) != null;
    }

    public void clear() {
        sessions.clear();
    }

    public int getAuthenticatedCount() {
        return sessions.size();
    }
}
