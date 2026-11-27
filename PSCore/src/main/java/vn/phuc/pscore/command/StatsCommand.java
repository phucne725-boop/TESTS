package vn.phuc.pscore.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vn.phuc.pscore.PSCore;
import vn.phuc.pscore.player.PlayerDataService;
import vn.phuc.pscore.player.PlayerDataService.PlayerStats;
import vn.phuc.pscore.util.PlaytimeFormatter;
import vn.phuc.pscore.util.Text;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class StatsCommand implements CommandExecutor {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private final JavaPlugin plugin;
    private final PlayerDataService data;

    public StatsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.data = ((PSCore) plugin).getPlayerData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            Text.sendPrefixed(plugin, sender, "Su dung: /stats [player]");
            return true;
        }
        OfflinePlayer target;
        if (args.length == 1) {
            target = Bukkit.getOfflinePlayer(args[0]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            Text.sendPrefixed(plugin, sender, "Console can dung /stats <player>.");
            return true;
        }
        try {
            PlayerStats stats = data.getStats(target.getUniqueId());
            if (stats == null) {
                Text.sendPrefixed(plugin, sender, "Chua co du lieu cua nguoi choi nay.");
                return true;
            }
            Text.sendPrefixed(plugin, sender, "Thong tin nguoi choi &e" + stats.name());
            Text.send(sender, "&7Thoi gian choi: &f" + PlaytimeFormatter.format(stats.playtimeSeconds()));
            Text.send(sender, "&7Lan dau tham gia: &f" + DATE.format(Instant.ofEpochMilli(stats.firstJoin())));
            Text.send(sender, "&7Hoat dong gan nhat: &f" + DATE.format(Instant.ofEpochMilli(stats.lastSeen())));
        } catch (SQLException exception) {
            plugin.getLogger().severe("Khong doc duoc stats: " + exception.getMessage());
            Text.sendPrefixed(plugin, sender, "Khong the doc thong tin nguoi choi luc nay.");
        }
        return true;
    }
}
