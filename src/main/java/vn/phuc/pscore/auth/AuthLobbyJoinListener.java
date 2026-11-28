package vn.phuc.pscore.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;
import java.util.function.Predicate;

public final class AuthLobbyJoinListener implements Listener {

    private final AuthLobbyWorldManager worldManager;
    private final AuthLobbyStateManager stateManager;
    private final AuthLobbyTimeoutManager timeoutManager;
    private final AuthLobbyVisibilityService visibilityService;
    private final Predicate<UUID> authenticated;

    public AuthLobbyJoinListener(
            AuthLobbyWorldManager worldManager,
            AuthLobbyStateManager stateManager,
            AuthLobbyTimeoutManager timeoutManager,
            AuthLobbyVisibilityService visibilityService,
            Predicate<UUID> authenticated
    ) {
        this.worldManager = worldManager;
        this.stateManager = stateManager;
        this.timeoutManager = timeoutManager;
        this.visibilityService = visibilityService;
        this.authenticated = authenticated;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (authenticated.test(playerId)) {
            return;
        }

        stateManager.markPending(playerId);

        player.teleport(worldManager.getSpawn());

        timeoutManager.start(player);
        visibilityService.refresh();

        player.sendMessage(
                "§eBạn có 2 phút để đăng ký hoặc đăng nhập."
        );
    }
}
