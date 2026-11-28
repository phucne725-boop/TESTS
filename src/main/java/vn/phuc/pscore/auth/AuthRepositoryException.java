package vn.phuc.pscore.auth;

public final class AuthRepositoryException extends AuthException {

    public AuthRepositoryException(String message) {
        super(message);
    }

    public AuthRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
