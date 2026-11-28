package vn.phuc.pscore.auth;

import vn.phuc.pscore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class SqlitePlayerLocationRepository
        implements PlayerLocationRepository {

    private final DatabaseManager database;

    public SqlitePlayerLocationRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public Optional<PlayerLocationSnapshot> find(UUID playerId) {
        String sql = """
                SELECT world_name, x, y, z, yaw, pitch
                FROM player_last_locations
                WHERE player_uuid = ?
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerId.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(new PlayerLocationSnapshot(
                        result.getString("world_name"),
                        result.getDouble("x"),
                        result.getDouble("y"),
                        result.getDouble("z"),
                        result.getFloat("yaw"),
                        result.getFloat("pitch")
                ));
            }
        } catch (SQLException exception) {
            throw new AuthRepositoryException(
                    "Không thể đọc vị trí thoát của người chơi.", exception
            );
        }
    }

    @Override
    public void save(UUID playerId, PlayerLocationSnapshot location) {
        String sql = """
                INSERT INTO player_last_locations
                    (player_uuid, world_name, x, y, z, yaw, pitch, saved_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    world_name = excluded.world_name,
                    x = excluded.x,
                    y = excluded.y,
                    z = excluded.z,
                    yaw = excluded.yaw,
                    pitch = excluded.pitch,
                    saved_at = excluded.saved_at
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerId.toString());
            statement.setString(2, location.getWorldName());
            statement.setDouble(3, location.getX());
            statement.setDouble(4, location.getY());
            statement.setDouble(5, location.getZ());
            statement.setFloat(6, location.getYaw());
            statement.setFloat(7, location.getPitch());
            statement.setLong(8, System.currentTimeMillis());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new AuthRepositoryException(
                    "Không thể lưu vị trí thoát của người chơi.", exception
            );
        }
    }

    @Override
    public void delete(UUID playerId) {
        String sql = """
                DELETE FROM player_last_locations
                WHERE player_uuid = ?
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new AuthRepositoryException(
                    "Không thể xóa vị trí thoát của người chơi.", exception
            );
        }
    }
                  }
