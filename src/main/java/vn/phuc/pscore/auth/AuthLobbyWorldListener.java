package vn.phuc.pscore.auth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

public final class AuthLobbyWorldListener implements Listener {

    private final AuthLobbyWorldManager worldManager;

    public AuthLobbyWorldListener(AuthLobbyWorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (worldManager.isAuthLobby(event.getLocation().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (worldManager.isAuthLobby(event.getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (worldManager.isAuthLobby(event.getWorld())) {
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }
}
