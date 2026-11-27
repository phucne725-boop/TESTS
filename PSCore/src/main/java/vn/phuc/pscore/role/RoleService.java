package vn.phuc.pscore.role;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.database.DatabaseManager;
import vn.phuc.pscore.util.Text;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Persistent role assignments. Expiration timestamps are Unix milliseconds; zero means permanent. */
public final class RoleService {
    private final JavaPlugin plugin;
    private final DatabaseManager database;
    private final Map<UUID, Assignment> assignments = new ConcurrentHashMap<>();

    public RoleService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.database = ((vn.phuc.pscore.PSCore) plugin).getDatabase();
    }

    public void load() {
        assignments.clear();
        long now = System.currentTimeMillis();
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "SELECT uuid, role, expires_at FROM roles");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(result.getString("uuid"));
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                String roleName = result.getString("role");
                long expiresAt = result.getLong("expires_at");
                RoleType role = RoleType.from(roleName).orElse(null);
                if (role == null || (expiresAt > 0 && expiresAt <= now)) {
                    deleteExpired(uuid);
                    continue;
                }
                assignments.put(uuid, new Assignment(role, expiresAt));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the tai danh sach role.", exception);
        }
    }

    public synchronized void setRole(OfflinePlayer target, RoleType role, long expiresAt) throws SQLException {
        String sql = "INSERT INTO roles(uuid, role, expires_at) VALUES(?, ?, ?) "
                + "ON CONFLICT(uuid) DO UPDATE SET role=excluded.role, expires_at=excluded.expires_at";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, target.getUniqueId().toString());
            statement.setString(2, role.name());
            statement.setLong(3, expiresAt);
            statement.executeUpdate();
        }
        assignments.put(target.getUniqueId(), new Assignment(role, expiresAt));
    }

    public synchronized void removeRole(UUID uuid) throws SQLException {
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "DELETE FROM roles WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        }
        assignments.remove(uuid);
    }

    public RoleType getRole(UUID uuid) {
        Assignment assignment = assignments.get(uuid);
        if (assignment == null) return null;
        if (assignment.expiresAt > 0 && assignment.expiresAt <= System.currentTimeMillis()) {
            // Keep this accessor read-only: it is also called from Paper's async chat renderer.
            assignments.remove(uuid, assignment);
            return null;
        }
        return assignment.role;
    }

    public String getPrefix(UUID uuid) {
        RoleType role = getRole(uuid);
        if (role == null) return "";
        String prefix = plugin.getConfig().getString("roles." + role.name() + ".prefix", "&7[" + role.name() + "]");
        return prefix + " &r";
    }

    public long getExpiry(UUID uuid) {
        Assignment assignment = assignments.get(uuid);
        return assignment == null ? 0L : assignment.expiresAt;
    }

    private void deleteExpired(UUID uuid) {
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "DELETE FROM roles WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().warning("Khong the don role loi/het han: " + exception.getMessage());
        }
    }

    private record Assignment(RoleType role, long expiresAt) { }
}
