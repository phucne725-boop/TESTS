
package vn.phuc.pscore.economy;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MoneyParser {

    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("\\d+(?:\\.\\d+)?");

    private static final Pattern GROUPED_NUMBER_PATTERN =
            Pattern.compile("\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?");

    private MoneyParser() {
    }

    public static Money parsePositive(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Amount cannot be empty.");
        }

        String value = input.trim().toLowerCase(Locale.ROOT)
                .replace(" ", "");

        if (value.endsWith("$")) {
            value = value.substring(0, value.length() - 1);
        }

        long multiplier = 1;

        if (value.endsWith("k")) {
            multiplier = 1_000L;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("m")) {
            multiplier = 1_000_000L;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("b")) {
            multiplier = 1_000_000_000L;
            value = value.substring(0, value.length() - 1);
        }

        boolean valid = NUMBER_PATTERN.matcher(value).matches()
                || GROUPED_NUMBER_PATTERN.matcher(value).matches();

        if (!valid) {
            throw new IllegalArgumentException("Invalid money format.");
        }

        try {
            BigDecimal parsed = new BigDecimal(value.replace(",", ""));
            BigDecimal result = parsed.multiply(BigDecimal.valueOf(multiplier));

            long amount = result.longValueExact();

            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive.");
            }

            return Money.of(amount);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Amount is too large or invalid.", exception);
        }
    }
}
