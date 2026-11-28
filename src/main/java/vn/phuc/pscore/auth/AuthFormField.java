package vn.phuc.pscore.auth;

import java.util.Objects;

public record AuthFormField(
        String id,
        String label,
        String placeholder,
        boolean required,
        boolean secret,
        int maxLength
) {

    public AuthFormField {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(label, "label");

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "Field id cannot be blank."
            );
        }

        if (maxLength < 1) {
            throw new IllegalArgumentException(
                    "Maximum length must be positive."
            );
        }

        placeholder = placeholder == null ? "" : placeholder;
    }
}
