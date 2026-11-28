
package vn.phuc.pscore.auth;

public interface PasswordHasher {

    PasswordHash hash(char[] password);

    boolean verify(char[] password, PasswordHash passwordHash);
}
