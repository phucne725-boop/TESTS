
package vn.phuc.pscore.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final SecureRandom random = new SecureRandom();

    @Override
    public PasswordHash hash(char[] password) {
        validatePassword(password);

        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        byte[] derived = derive(password, salt, ITERATIONS);

        try {
            return new PasswordHash(
                    ITERATIONS + ":"
                            + Base64.getEncoder().encodeToString(salt)
                            + ":"
                            + Base64.getEncoder().encodeToString(derived)
            );
        } finally {
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(derived, (byte) 0);
        }
    }

    @Override
    public boolean verify(char[] password, PasswordHash passwordHash) {
        validatePassword(password);

        String[] parts = passwordHash.getEncoded().split(":");

        if (parts.length != 3) {
            return false;
        }

        byte[] salt = null;
        byte[] expected = null;
        byte[] actual = null;

        try {
            int iterations = Integer.parseInt(parts[0]);

            if (iterations < 100_000 || iterations > 1_000_000) {
                return false;
            }

            salt = Base64.getDecoder().decode(parts[1]);
            expected = Base64.getDecoder().decode(parts[2]);

            if (salt.length != SALT_LENGTH || expected.length != KEY_LENGTH / 8) {
                return false;
            }

            actual = derive(password, salt, iterations);

            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException exception) {
            return false;
        } finally {
            if (salt != null) {
                Arrays.fill(salt, (byte) 0);
            }

            if (expected != null) {
                Arrays.fill(expected, (byte) 0);
            }

            if (actual != null) {
                Arrays.fill(actual, (byte) 0);
            }
        }
    }

    private byte[] derive(
            char[] password,
            byte[] salt,
            int iterations
    ) {
        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                iterations,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new AuthException(
                    "Password hashing failed.",
                    exception
            );
        } finally {
            spec.clearPassword();
        }
    }

    private void validatePassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
    }
}
