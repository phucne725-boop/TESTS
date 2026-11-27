
package vn.phuc.pscore.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import vn.phuc.pscore.player.PlayerDataService;

public final class PlayerListener implements Listener {

    private final PlayerDataService playerData;

    public PlayerListener(PlayerDataService playerData) {
        this.playerData = playerData;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerData.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerData.onQuit(event.getPlayer());
    }
}
