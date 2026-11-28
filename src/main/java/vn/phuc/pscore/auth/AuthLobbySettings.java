package vn.phuc.pscore.auth;

import org.bukkit.configuration.file.FileConfiguration;
import vn.phuc.pscore.PSCore;

public final class AuthLobbySettings {

    private final String worldName;
    private final int spawnX;
    private final int spawnY;
    private final int spawnZ;
    private final int platformRadius;
    private final int timeoutSeconds;

    public AuthLobbySettings(PSCore plugin) {
        FileConfiguration config = plugin.getConfig();

        this.worldName = config.getString(
                "auth-lobby.world",
                "auth_lobby"
        );

        this.spawnX = config.getInt("auth-lobby.spawn.x", 0);
        this.spawnY = config.getInt("auth-lobby.spawn.y", 100);
        this.spawnZ = config.getInt("auth-lobby.spawn.z", 0);

        this.platformRadius = config.getInt(
                "auth-lobby.platform-radius", 4
        );

        this.timeoutSeconds = config.getInt(
                "auth-lobby.timeout-seconds", 120
        );

        validate();
    }

    private void validate() {
        if (worldName == null || worldName.isBlank()) {
            throw new IllegalArgumentException(
                    "Auth Lobby world name cannot be empty."
            );
        }

        if (spawnY < 2 || spawnY > 250) {
            throw new IllegalArgumentException(
                    "Auth Lobby spawn Y must be between 2 and 250."
            );
        }

        if (platformRadius < 2 || platformRadius > 16) {
            throw new IllegalArgumentException(
                    "Auth Lobby platform radius must be between 2 and 16."
            );
        }

        if (timeoutSeconds < 10 || timeoutSeconds > 600) {
            throw new IllegalArgumentException(
                    "Auth Lobby timeout must be between 10 and 600 seconds."
            );
        }
    }

    public String getWorldName() {
        return worldName;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public int getSpawnZ() {
        return spawnZ;
    }

    public int getPlatformRadius() {
        return platformRadius;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
