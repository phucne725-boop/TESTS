package vn.phuc.pscore.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import vn.phuc.pscore.economy.EconomyService;
import vn.phuc.pscore.player.PlayerDataService;
import vn.phuc.pscore.role.RoleService;
import vn.phuc.pscore.util.PlaytimeFormatter;
import vn.phuc.pscore.util.Text;

import java.sql.SQLException;

public final class ScoreboardService {
    private final JavaPlugin plugin;
    private final PlayerDataService playerData;
    private final EconomyService economy;
    private final RoleService roles;

    public ScoreboardService(JavaPlugin plugin, PlayerDataService playerData,
                             EconomyService economy, RoleService roles) {
        this.plugin = plugin;
        this.playerData = playerData;
        this.economy = economy;
        this.roles = roles;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) return;
        long period = Math.max(20L, plugin.getConfig().getLong("scoreboard.update-ticks", 40L));
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 1L, period);
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) update(player);
    }

    public void update(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) return;
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        String title = plugin.getConfig().getString("scoreboard.title", "&b&lPHUTSMILE SMP");
        Objective objective = board.registerNewObjective("pscore", "dummy", Text.component(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        String rolePrefix = roles.getPrefix(player.getUniqueId());
        player.playerListName(Text.component(rolePrefix + player.getName()));

        int score = 8;
        addLine(objective, "&7Xin chao, &f" + player.getName(), score--);
        if (plugin.getConfig().getBoolean("scoreboard.show-role", true)) {
            String role = roles.getPrefix(player.getUniqueId());
            addLine(objective, "&fVai tro: " + (role.isBlank() ? "&7Nguoi choi" : role.trim()), score--);
        }
        if (plugin.getConfig().getBoolean("scoreboard.show-balance", true)) {
            String balance = "&cKhong kha dung";
            try {
                if (economy.isAvailable()) balance = "&a" + economy.format(economy.getBalance(player));
            } catch (RuntimeException ignored) { }
            addLine(objective, "&fTien: " + balance, score--);
        }
        if (plugin.getConfig().getBoolean("scoreboard.show-playtime", true)) {
            try {
                PlayerDataService.PlayerStats stats = playerData.getStats(player.getUniqueId());
                String time = stats == null ? "0s" : PlaytimeFormatter.format(stats.playtimeSeconds());
                addLine(objective, "&fThoi gian: &b" + time, score--);
            } catch (SQLException exception) {
                addLine(objective, "&fThoi gian: &cLoi du lieu", score--);
            }
        }
        if (plugin.getConfig().getBoolean("scoreboard.show-server-address", true)) {
            String address = plugin.getConfig().getString("server.address", "");
            addLine(objective, "&fServer: &b" + address, score--);
        }
        player.setScoreboard(board);
    }

    private void addLine(Objective objective, String text, int score) {
        String line = ChatColor.translateAlternateColorCodes('&', text);
        if (line.length() > 64) line = line.substring(0, 64);
        objective.getScore(line).setScore(score);
    }
}
