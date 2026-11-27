package vn.phuc.pscore.util;

public final class PlaytimeFormatter {
    private PlaytimeFormatter() { }

    public static String format(long totalSeconds) {
        long seconds = Math.max(0, totalSeconds);
        long days = seconds / 86_400;
        long hours = (seconds % 86_400) / 3_600;
        long minutes = (seconds % 3_600) / 60;
        long remainder = seconds % 60;
        if (days > 0) return days + "d " + hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + remainder + "s";
        return remainder + "s";
    }
}
