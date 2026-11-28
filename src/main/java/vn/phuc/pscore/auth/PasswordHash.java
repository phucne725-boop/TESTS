
package vn.phuc.pscore.auth;

import java.util.Objects;

public final class PasswordHash {

    private final String encoded;

    public PasswordHash(String encoded) {
        this.encoded = Objects.requireNonNull(encoded);

        if (encoded.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be blank.");
        }
    }

    public String getEncoded() {
        return encoded;
    }

    @Override
    public String toString() {
        return "PasswordHash[PROTECTED]";
    }
}
