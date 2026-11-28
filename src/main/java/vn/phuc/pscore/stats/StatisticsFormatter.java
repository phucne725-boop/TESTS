
package vn.phuc.pscore.stats;

import java.util.Locale;

public final class StatisticsFormatter {

    private StatisticsFormatter() {
    }

    public static String formatPlaytime(long seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException(
                    "Playtime cannot be negative."
            );
        }

        long days = seconds / 86_400;
        long hours = (seconds % 86_400) / 3_600;
        long minutes = (seconds % 3_600) / 60;
        long remainingSeconds = seconds % 60;

        if (days > 0) {
            return String.format(
                    Locale.ROOT,
                    "%dd %dh %dm",
                    days,
                    hours,
                    minutes
            );
        }

        if (hours > 0) {
            return String.format(
                    Locale.ROOT,
                    "%dh %dm",
                    hours,
                    minutes
            );
        }

        if (minutes > 0) {
            return String.format(
                    Locale.ROOT,
                    "%dm %ds",
                    minutes,
                    remainingSeconds
            );
        }

        return remainingSeconds + "s";
    }

    public static String formatKdr(PlayerStatistics statistics) {
        return String.format(
                Locale.ROOT,
                "%.2f",
                statistics.getKdr()
        );
    }
}
