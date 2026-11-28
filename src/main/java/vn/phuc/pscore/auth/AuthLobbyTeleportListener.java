package vn.phuc.pscore.auth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;
import java.util.function.Predicate;

public final class AuthLobbyTeleportListener implements Listener {

    private final AuthLobbyWorldManager worldManager;
    private final Predicate<UUID> authenticated;

    public AuthLobbyTeleportListener(
            AuthLobbyWorldManager worldManager,
            Predicate<UUID> authenticated
    ) {
        this.worldManager = worldManager;
        this.authenticated = authenticated;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (worldManager.isAuthLobby(event.getPlayer().getWorld())
                && !authenticated.test(event.getPlayer().getUniqueId())
                && !worldManager.isAuthLobby(event.getTo().getWorld())) {
            event.setCancelled(true);
        }
    }
}
