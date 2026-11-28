
package vn.phuc.pscore.economy;

public final class InsufficientFundsException extends EconomyException {

    private final Money balance;
    private final Money required;

    public InsufficientFundsException(Money balance, Money required) {
        super("Insufficient funds. Balance: " + balance
                + ", required: " + required);

        this.balance = balance;
        this.required = required;
    }

    public Money getBalance() {
        return balance;
    }

    public Money getRequired() {
        return required;
    }
}
