
package vn.phuc.pscore.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vn.phuc.pscore.database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PlayerDataService {

    private final DatabaseManager database;

    private final ConcurrentMap<UUID, Long> sessions =
            new ConcurrentHashMap<>();

    public PlayerDataService(DatabaseManager database) {
        this.database = database;
    }

    public void initialize() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            onJoin(player);
        }
    }

    public void onJoin(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        String sql = """
            INSERT INTO players
                (uuid, name, first_join, last_seen, playtime_seconds)
            VALUES (?, ?, ?, ?, 0)
            ON CONFLICT(uuid) DO UPDATE SET
                name = excluded.name,
                last_seen = excluded.last_seen
        """;

        try (PreparedStatement statement =
                     database.getConnection().prepareStatement(sql)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, player.getName());
            statement.setLong(3, now);
            statement.setLong(4, now);
            statement.executeUpdate();

            sessions.put(uuid, now);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Khong the luu du lieu nguoi choi.", exception
            );
        }
    }

    public void onQuit(Player player) {
        saveSession(player.getUniqueId());
    }

    public void saveAllOnlinePlayers() {
        for (UUID uuid : sessions.keySet()) {
            saveSession(uuid);
        }
    }

    private synchronized void saveSession(UUID uuid) {
        Long started = sessions.remove(uuid);

        if (started == null) return;

        long now = System.currentTimeMillis();
        long seconds = Math.max(0, (now - started) / 1000);

        String sql = """
            UPDATE players
            SET last_seen = ?,
                playtime_seconds = playtime_seconds + ?
            WHERE uuid = ?
        """;

        try (PreparedStatement statement =
                     database.getConnection().prepareStatement(sql)) {

            statement.setLong(1, now);
            statement.setLong(2, seconds);
            statement.setString(3, uuid.toString());
            statement.executeUpdate();

        } catch (SQLException exception) {
            sessions.putIfAbsent(uuid, started);
            throw new IllegalStateException(
                    "Khong the cap nhat thoi gian choi.", exception
            );
        }
    }

    public PlayerStats getStats(UUID uuid) throws SQLException {
        String sql = """
            SELECT name, first_join, last_seen, playtime_seconds
            FROM players WHERE uuid = ?
        """;

        try (PreparedStatement statement =
                     database.getConnection().prepareStatement(sql)) {

            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;

                long playtime = result.getLong("playtime_seconds");
                Long started = sessions.get(uuid);

                if (started != null) {
                    playtime += Math.max(
                            0, (System.currentTimeMillis() - started) / 1000
                    );
                }

                return new PlayerStats(
                        result.getString("name"),
                        result.getLong("first_join"),
                        result.getLong("last_seen"),
                        playtime
                );
            }
        }
    }

    public record PlayerStats(
            String name,
            long firstJoin,
            long lastSeen,
            long playtimeSeconds
    ) {
    }
}
