
package vn.phuc.pscore.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StatisticsLeaderboardFormatter {

    private StatisticsLeaderboardFormatter() {
    }

    public static List<String> format(
            StatisticsLeaderboardType type,
            List<StatisticsLeaderboardEntry> entries
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(entries);

        List<String> lines = new ArrayList<>();

        String title = type == StatisticsLeaderboardType.PLAYTIME
                ? "§e§lTOP PLAYTIME"
                : "§e§lTOP KILLS";

        lines.add(title);

        for (StatisticsLeaderboardEntry entry : entries) {
            String value = type == StatisticsLeaderboardType.PLAYTIME
                    ? StatisticsFormatter.formatPlaytime(entry.getValue())
                    : Long.toString(entry.getValue());

            lines.add(
                    "§7#" + entry.getPosition()
                            + " §f" + entry.getPlayerName()
                            + " §8- §a" + value
            );
        }

        if (entries.isEmpty()) {
            lines.add("§7Chưa có dữ liệu.");
        }

        return List.copyOf(lines);
    }
}
