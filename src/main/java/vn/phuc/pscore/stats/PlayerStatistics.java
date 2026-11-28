
package vn.phuc.pscore.stats;

import java.util.Objects;
import java.util.UUID;

public final class PlayerStatistics {

    private final UUID playerId;
    private final long kills;
    private final long deaths;
    private final long playtimeSeconds;

    public PlayerStatistics(
            UUID playerId,
            long kills,
            long deaths,
            long playtimeSeconds
    ) {
        this.playerId = Objects.requireNonNull(playerId);

        if (kills < 0 || deaths < 0 || playtimeSeconds < 0) {
            throw new IllegalArgumentException("Statistics cannot be negative.");
        }

        this.kills = kills;
        this.deaths = deaths;
        this.playtimeSeconds = playtimeSeconds;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getKills() {
        return kills;
    }

    public long getDeaths() {
        return deaths;
    }

    public long getPlaytimeSeconds() {
        return playtimeSeconds;
    }

    public double getKdr() {
        if (deaths == 0) {
            return kills;
        }

        return (double) kills / deaths;
    }

    public PlayerStatistics addKill() {
        return new PlayerStatistics(
                playerId,
                Math.addExact(kills, 1),
                deaths,
                playtimeSeconds
        );
    }

    public PlayerStatistics addDeath() {
        return new PlayerStatistics(
                playerId,
                kills,
                Math.addExact(deaths, 1),
                playtimeSeconds
        );
    }

    public PlayerStatistics addPlaytime(long seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds cannot be negative.");
        }

        return new PlayerStatistics(
                playerId,
                kills,
                deaths,
                Math.addExact(playtimeSeconds, seconds)
        );
    }
}
