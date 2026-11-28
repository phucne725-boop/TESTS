
package vn.phuc.pscore.economy;

import java.util.Objects;

public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    private final long amount;

    private Money(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Money cannot be negative.");
        }
        this.amount = amount;
    }

    public static Money of(long amount) {
        return amount == 0 ? ZERO : new Money(amount);
    }

    public long getAmount() {
        return amount;
    }

    public boolean isZero() {
        return amount == 0;
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other");
        return Money.of(Math.addExact(amount, other.amount));
    }

    public Money subtract(Money other) {
        Objects.requireNonNull(other, "other");

        if (other.amount > amount) {
            throw new IllegalArgumentException("Insufficient money.");
        }

        return Money.of(amount - other.amount);
    }

    public boolean isAtLeast(Money other) {
        return compareTo(other) >= 0;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(amount, other.amount);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Money money)) return false;
        return amount == money.amount;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(amount);
    }

    @Override
    public String toString() {
        return MoneyFormatter.format(this);
    }
}
