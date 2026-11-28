
package vn.phuc.pscore.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StatisticsProfileFormatter {

    private StatisticsProfileFormatter() {
    }

    public static List<String> format(
            String playerName,
            PlayerStatistics statistics
    ) {
        Objects.requireNonNull(playerName);
        Objects.requireNonNull(statistics);

        List<String> lines = new ArrayList<>();

        lines.add("§e§lTHỐNG KÊ NGƯỜI CHƠI");
        lines.add("§7Tên: §f" + playerName);
        lines.add("§7Kills: §a" + statistics.getKills());
        lines.add("§7Deaths: §c" + statistics.getDeaths());
        lines.add(
                "§7K/D: §b"
                        + StatisticsFormatter.formatKdr(statistics)
        );
        lines.add(
                "§7Playtime: §e"
                        + StatisticsFormatter.formatPlaytime(
                                statistics.getPlaytimeSeconds()
                        )
        );

        return List.copyOf(lines);
    }
}
