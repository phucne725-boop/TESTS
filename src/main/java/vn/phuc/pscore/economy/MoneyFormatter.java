
package vn.phuc.pscore.economy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String format(Money money) {
        long amount = money.getAmount();

        if (amount < 1_000) {
            return amount + "$";
        }

        if (amount < 1_000_000) {
            return compact(amount, 1_000, "k");
        }

        if (amount < 1_000_000_000) {
            return compact(amount, 1_000_000, "m");
        }

        return compact(amount, 1_000_000_000, "b");
    }

    private static String compact(long amount, long divisor, String suffix) {
        BigDecimal value = BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(divisor), 2, RoundingMode.DOWN)
                .stripTrailingZeros();

        return value.toPlainString() + suffix + "$";
    }

    public static String formatFull(Money money) {
        return String.format("%,d$", money.getAmount());
    }
}
