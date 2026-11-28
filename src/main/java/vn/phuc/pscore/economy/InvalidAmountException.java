
package vn.phuc.pscore.economy;

public final class InvalidAmountException extends EconomyException {

    public InvalidAmountException(String message) {
        super(message);
    }

    public InvalidAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
