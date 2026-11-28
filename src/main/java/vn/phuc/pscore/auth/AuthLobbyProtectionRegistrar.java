package vn.phuc.pscore.auth;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.function.Predicate;

public final class AuthLobbyProtectionRegistrar {

    private AuthLobbyProtectionRegistrar() {
    }

    public static void register(
            Plugin plugin,
            AuthLobbyWorldManager worldManager,
            AuthLobbyStateManager stateManager,
            AuthLobbyTimeoutManager timeoutManager,
            AuthLobbyVisibilityService visibilityService,
            Predicate<UUID> authenticated
    ) {
        Bukkit.getPluginManager().registerEvents(
                new AuthLobbyProtectionListener(
                        worldManager,
                        authenticated
                ),
                plugin
        );

        Bukkit.getPluginManager().registerEvents(
                new AuthLobbyCommandBlocker(
                        worldManager,
                        authenticated
                ),
                plugin
        );

        Bukkit.getPluginManager().registerEvents(
                new AuthLobbyJoinListener(
                        worldManager,
                        stateManager,
                        timeoutManager,
                        visibilityService,
                        authenticated
                ),
                plugin
        );

        Bukkit.getPluginManager().registerEvents(
                new AuthLobbyQuitListener(
                        stateManager,
                        timeoutManager
                ),
                plugin
        );

        Bukkit.getPluginManager().registerEvents(
                new AuthLobbyWorldListener(worldManager),
                plugin
        );

        Bukkit.getPluginManager().registerEvents(
                new AuthLobbyTeleportListener(
                        worldManager,
                        authenticated
                ),
                plugin
        );
    }
}
