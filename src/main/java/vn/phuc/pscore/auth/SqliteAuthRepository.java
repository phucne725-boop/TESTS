package vn.phuc.pscore.auth;

import vn.phuc.pscore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class SqliteAuthRepository implements AuthRepository {

    private final DatabaseManager database;

    public SqliteAuthRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public Optional<AuthAccount> findByPlayerId(UUID playerId) {
        String sql = """
                SELECT player_uuid, username, password_hash,
                       created_at, updated_at
                FROM auth_accounts
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

                return Optional.of(new AuthAccount(
                        UUID.fromString(result.getString("player_uuid")),
                        result.getString("username"),
                        new PasswordHash(result.getString("password_hash")),
                        Instant.ofEpochMilli(result.getLong("created_at")),
                        Instant.ofEpochMilli(result.getLong("updated_at"))
                ));
            }
        } catch (SQLException | IllegalArgumentException exception) {
            throw new AuthRepositoryException(
                    "Không thể đọc tài khoản xác thực.", exception
            );
        }
    }

    @Override
    public boolean exists(UUID playerId) {
        String sql = """
                SELECT 1 FROM auth_accounts
                WHERE player_uuid = ? LIMIT 1
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerId.toString());

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException exception) {
            throw new AuthRepositoryException(
                    "Không thể kiểm tra tài khoản xác thực.", exception
            );
        }
    }

    @Override
    public void create(AuthAccount account) {
        String sql = """
                INSERT INTO auth_accounts
                    (player_uuid, username, password_hash,
                     created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, account.getPlayerId().toString());
            statement.setString(2, account.getUsername());
            statement.setString(3, account.getPasswordHash().getEncoded());
            statement.setLong(4, account.getCreatedAt().toEpochMilli());
            statement.setLong(5, account.getUpdatedAt().toEpochMilli());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new AuthRepositoryException(
                    "Không thể tạo tài khoản xác thực.", exception
            );
        }
    }

    @Override
    public void updatePasswordHash(
            UUID playerId,
            PasswordHash passwordHash
    ) {
        String sql = """
                UPDATE auth_accounts
                SET password_hash = ?, updated_at = ?
                WHERE player_uuid = ?
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash.getEncoded());
            statement.setLong(2, System.currentTimeMillis());
            statement.setString(3, playerId.toString());

            if (statement.executeUpdate() != 1) {
                throw new AuthRepositoryException(
                        "Không tìm thấy tài khoản để đổi mật khẩu."
                );
            }
        } catch (SQLException exception) {
            throw new AuthRepositoryException(
                    "Không thể cập nhật mật khẩu.", exception
            );
        }
    }
    }
