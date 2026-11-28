package vn.phuc.pscore.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class AuthLobbyVisibilityService {

    private final Plugin plugin;
    private final AuthLobbyWorldManager worldManager;

    public AuthLobbyVisibilityService(
            Plugin plugin,
            AuthLobbyWorldManager worldManager
    ) {
        this.plugin = Objects.requireNonNull(plugin);
        this.worldManager = Objects.requireNonNull(worldManager);
    }

    public void refresh() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (viewer.equals(target)) {
                    continue;
                }

                boolean shouldHide =
                        worldManager.isAuthLobby(viewer.getWorld())
                        || worldManager.isAuthLobby(target.getWorld());

                if (shouldHide) {
                    viewer.hidePlayer(plugin, target);
                } else {
                    viewer.showPlayer(plugin, target);
                }
            }
        }
    }

    public void refresh(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        refresh();
    }
}
