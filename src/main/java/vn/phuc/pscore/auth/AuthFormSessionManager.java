package vn.phuc.pscore.auth;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthFormSessionManager {

    private final Map<UUID, AuthFormType> activeForms =
            new ConcurrentHashMap<>();

    public boolean open(UUID playerId, AuthFormType type) {
        return activeForms.putIfAbsent(
                playerId,
                type
        ) == null;
    }

    public boolean isOpen(UUID playerId) {
        return activeForms.containsKey(playerId);
    }

    public Optional<AuthFormType> getType(UUID playerId) {
        return Optional.ofNullable(
                activeForms.get(playerId)
        );
    }

    public boolean finish(UUID playerId) {
        return activeForms.remove(playerId) != null;
    }

    public void cancel(UUID playerId) {
        activeForms.remove(playerId);
    }

    public void clear() {
        activeForms.clear();
    }
}
