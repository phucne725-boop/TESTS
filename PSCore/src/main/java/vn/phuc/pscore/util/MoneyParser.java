
package vn.phuc.pscore.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class MoneyParser {

    private MoneyParser() {
    }

    public static double parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Hay nhap so tien.");
        }

        String value = input.trim()
                .toLowerCase(Locale.ROOT)
                .replace(",", "")
                .replace("_", "");

        BigDecimal multiplier = BigDecimal.ONE;

        if (value.endsWith("k")) {
            multiplier = BigDecimal.valueOf(1_000);
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("m")) {
            multiplier = BigDecimal.valueOf(1_000_000);
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("b")) {
            multiplier = BigDecimal.valueOf(1_000_000_000);
            value = value.substring(0, value.length() - 1);
        }

        try {
            BigDecimal amount = new BigDecimal(value)
                    .multiply(multiplier)
                    .setScale(2, RoundingMode.HALF_UP);

            if (amount.signum() <= 0) {
                throw new IllegalArgumentException(
                        "So tien phai lon hon 0."
                );
            }

            double result = amount.doubleValue();

            if (!Double.isFinite(result)) {
                throw new IllegalArgumentException("So tien qua lon.");
            }

            return result;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("So tien khong hop le.");
        }
    }
}
