package vn.phuc.pscore.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class AuthLobbyTimeoutManager {

    private final Plugin plugin;
    private final AuthLobbySettings settings;
    private final AuthLobbyStateManager stateManager;
    private final Predicate<UUID> authenticated;

    private final Map<UUID, BukkitTask> tasks = new ConcurrentHashMap<>();

    public AuthLobbyTimeoutManager(
            Plugin plugin,
            AuthLobbySettings settings,
            AuthLobbyStateManager stateManager,
            Predicate<UUID> authenticated
    ) {
        this.plugin = plugin;
        this.settings = settings;
        this.stateManager = stateManager;
        this.authenticated = authenticated;
    }

    public void start(Player player) {
        cancel(player.getUniqueId());

        UUID playerId = player.getUniqueId();
        long delay = settings.getTimeoutSeconds() * 20L;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    tasks.remove(playerId);

                    Player current = Bukkit.getPlayer(playerId);

                    if (current == null || !current.isOnline()) {
                        stateManager.remove(playerId);
                        return;
                    }

                    if (authenticated.test(playerId)) {
                        stateManager.markAuthenticated(playerId);
                        return;
                    }

                    if (stateManager.isPending(playerId)) {
                        current.kickPlayer(
                                "§cBạn đã hết 2 phút để đăng ký hoặc đăng nhập!"
                        );
                        stateManager.remove(playerId);
                    }
                },
                delay
        );

        tasks.put(playerId, task);
    }

    public void cancel(UUID playerId) {
        BukkitTask task = tasks.remove(playerId);

        if (task != null) {
            task.cancel();
        }
    }

    public void cancelAll() {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }

        tasks.clear();
    }
}
