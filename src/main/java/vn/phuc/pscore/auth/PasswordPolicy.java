
package vn.phuc.pscore.auth;

public final class PasswordPolicy {

    private final int minimumLength;
    private final int maximumLength;

    public PasswordPolicy(int minimumLength, int maximumLength) {
        if (minimumLength < 1 || maximumLength < minimumLength) {
            throw new IllegalArgumentException("Invalid password policy.");
        }

        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
    }

    public void validate(char[] password) {
        if (password == null) {
            throw new AuthException("Mật khẩu không được để trống.");
        }

        if (password.length < minimumLength) {
            throw new AuthException(
                    "Mật khẩu phải có ít nhất "
                            + minimumLength + " ký tự."
            );
        }

        if (password.length > maximumLength) {
            throw new AuthException(
                    "Mật khẩu không được vượt quá "
                            + maximumLength + " ký tự."
            );
        }
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public int getMaximumLength() {
        return maximumLength;
    }
}
