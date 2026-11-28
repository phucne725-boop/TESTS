package vn.phuc.pscore.auth;

import java.util.Objects;

public record AuthFormResult(
        Status status,
        String message
) {

    public AuthFormResult {
        Objects.requireNonNull(status);
        Objects.requireNonNull(message);
    }

    public static AuthFormResult success(String message) {
        return new AuthFormResult(
                Status.SUCCESS,
                message
        );
    }

    public static AuthFormResult failure(String message) {
        return new AuthFormResult(
                Status.FAILURE,
                message
        );
    }

    public static AuthFormResult cancelled(String message) {
        return new AuthFormResult(
                Status.CANCELLED,
                message
        );
    }

    public enum Status {
        SUCCESS,
        FAILURE,
        CANCELLED
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
