
package vn.phuc.pscore.stats;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlaytimeTracker {

    private final Map<UUID, PlaytimeSession> sessions =
            new ConcurrentHashMap<>();

    public boolean start(UUID playerId) {
        PlaytimeSession session = new PlaytimeSession(
                playerId,
                Instant.now()
        );

        return sessions.putIfAbsent(playerId, session) == null;
    }

    public long stop(UUID playerId) {
        PlaytimeSession session = sessions.remove(playerId);

        if (session == null) {
            return 0;
        }

        return session.elapsedSeconds(Instant.now());
    }

    public long getCurrentSessionSeconds(UUID playerId) {
        PlaytimeSession session = sessions.get(playerId);

        if (session == null) {
            return 0;
        }

        return session.elapsedSeconds(Instant.now());
    }

    public boolean isTracking(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public void clear() {
        sessions.clear();
    }
}
