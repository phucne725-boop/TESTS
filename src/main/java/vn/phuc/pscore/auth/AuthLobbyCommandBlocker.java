package vn.phuc.pscore.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;
import java.util.function.Predicate;

public final class AuthLobbyCommandBlocker implements Listener {

    private final AuthLobbyWorldManager worldManager;
    private final Predicate<UUID> authenticated;

    public AuthLobbyCommandBlocker(
            AuthLobbyWorldManager worldManager,
            Predicate<UUID> authenticated
    ) {
        this.worldManager = worldManager;
        this.authenticated = authenticated;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!worldManager.isAuthLobby(player.getWorld())) {
            return;
        }

        if (!authenticated.test(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(
                    "§cBạn cần đăng nhập hoặc đăng ký trước khi sử dụng lệnh."
            );
        }
    }
}
