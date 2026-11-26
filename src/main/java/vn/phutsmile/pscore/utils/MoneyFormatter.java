
package vn.phutsmile.pscore.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols symbols =
            DecimalFormatSymbols.getInstance(Locale.US);

        FORMAT = new DecimalFormat("#,##0.##", symbols);
        FORMAT.setGroupingUsed(true);
    }

    private MoneyFormatter() {
    }

    public static synchronized String format(double amount) {
        if (!Double.isFinite(amount)) {
            return "0";
        }

        return FORMAT.format(amount);
    }

    public static String formatWithSymbol(double amount) {
        return "$" + format(amount);
    }
}
