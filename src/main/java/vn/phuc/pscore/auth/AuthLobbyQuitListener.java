package vn.phuc.pscore.auth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class AuthLobbyQuitListener implements Listener {

    private final AuthLobbyStateManager stateManager;
    private final AuthLobbyTimeoutManager timeoutManager;

    public AuthLobbyQuitListener(
            AuthLobbyStateManager stateManager,
            AuthLobbyTimeoutManager timeoutManager
    ) {
        this.stateManager = stateManager;
        this.timeoutManager = timeoutManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var playerId = event.getPlayer().getUniqueId();

        timeoutManager.cancel(playerId);
        stateManager.remove(playerId);
    }
}
