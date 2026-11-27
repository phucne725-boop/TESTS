package vn.phuc.pscore.scoreboard;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.role.RoleService;

public final class PlayerPresentationListener implements Listener {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private final JavaPlugin plugin;
    private final RoleService roles;

    public PlayerPresentationListener(JavaPlugin plugin, RoleService roles) {
        this.plugin = plugin;
        this.roles = roles;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updatePlayerName(event.getPlayer());
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Player online : plugin.getServer().getOnlinePlayers()) updatePlayerName(online);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // The next scoreboard refresh removes the player automatically.
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) -> {
            Component prefix = LEGACY.deserialize(roles.getPrefix(source.getUniqueId()));
            return prefix.append(Component.text(source.getName(), NamedTextColor.WHITE))
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(message);
        }));
    }

    private void updatePlayerName(Player player) {
        String prefix = roles.getPrefix(player.getUniqueId());
        Component display = LEGACY.deserialize(prefix)
                .append(Component.text(player.getName(), NamedTextColor.WHITE));
        player.playerListName(display);
        player.displayName(display);
    }
}
