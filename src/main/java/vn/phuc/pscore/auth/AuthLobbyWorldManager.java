package vn.phuc.pscore.auth;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import vn.phuc.pscore.PSCore;

import java.util.Objects;

public final class AuthLobbyWorldManager {

    private final PSCore plugin;
    private final AuthLobbySettings settings;

    private World world;
    private Location spawn;

    public AuthLobbyWorldManager(
            PSCore plugin,
            AuthLobbySettings settings
    ) {
        this.plugin = Objects.requireNonNull(plugin);
        this.settings = Objects.requireNonNull(settings);
    }

    public synchronized void initialize() {
        World existing = Bukkit.getWorld(settings.getWorldName());

        if (existing != null) {
            world = existing;
        } else {
            WorldCreator creator =
                    new WorldCreator(settings.getWorldName());

            creator.type(WorldType.NORMAL);
            creator.generator(new VoidChunkGenerator());
            creator.generateStructures(false);

            world = creator.createWorld();
        }

        if (world == null) {
            throw new IllegalStateException(
                    "Không thể tạo hoặc tải Auth Lobby Void World."
            );
        }

        configureWorld();
        createSafePlatform();

        spawn = new Location(
                world,
                settings.getSpawnX() + 0.5,
                settings.getSpawnY() + 1.0,
                settings.getSpawnZ() + 0.5
        );

        world.setSpawnLocation(
                settings.getSpawnX(),
                settings.getSpawnY() + 1,
                settings.getSpawnZ()
        );

        plugin.getLogger().info(
                "Auth Lobby loaded: " + settings.getWorldName()
        );
    }

    private void configureWorld() {
        world.setPVP(false);
        world.setStorm(false);
        world.setThundering(false);
        world.setTime(6000L);

        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_TILE_DROPS, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

    private void createSafePlatform() {
        int centerX = settings.getSpawnX();
        int centerY = settings.getSpawnY();
        int centerZ = settings.getSpawnZ();
        int radius = settings.getPlatformRadius();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                world.getBlockAt(x, centerY, z)
                        .setType(Material.SMOOTH_STONE, false);
            }
        }

        world.getBlockAt(centerX, centerY + 1, centerZ)
                .setType(Material.AIR, false);

        world.getBlockAt(centerX, centerY + 2, centerZ)
                .setType(Material.AIR, false);
    }

    public World getWorld() {
        if (world == null) {
            throw new IllegalStateException(
                    "Auth Lobby has not been initialized."
            );
        }

        return world;
    }

    public Location getSpawn() {
        if (spawn == null) {
            throw new IllegalStateException(
                    "Auth Lobby spawn has not been initialized."
            );
        }

        return spawn.clone();
    }

    public boolean isAuthLobby(World candidate) {
        return candidate != null
                && candidate.getName().equalsIgnoreCase(
                        settings.getWorldName()
                );
    }

    public AuthLobbySettings getSettings() {
        return settings;
    }
          }
