package vn.phuc.pscore.auth;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record AuthFormDefinition(
        AuthFormType type,
        String title,
        String description,
        List<AuthFormField> fields,
        String submitLabel,
        String cancelLabel
) {

    public AuthFormDefinition {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(fields, "fields");
        Objects.requireNonNull(submitLabel, "submitLabel");

        if (title.isBlank() || submitLabel.isBlank()) {
            throw new IllegalArgumentException(
                    "Form title and submit label cannot be blank."
            );
        }

        fields = List.copyOf(fields);

        Set<String> ids = new HashSet<>();

        for (AuthFormField field : fields) {
            if (!ids.add(field.id())) {
                throw new IllegalArgumentException(
                        "Duplicate form field: " + field.id()
                );
            }
        }

        description = description == null ? "" : description;
        cancelLabel = cancelLabel == null ? "" : cancelLabel;
    }
}
