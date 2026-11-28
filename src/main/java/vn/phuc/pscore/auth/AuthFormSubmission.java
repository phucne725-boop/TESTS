package vn.phuc.pscore.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AuthFormSubmission implements AutoCloseable {

    private final UUID playerId;
    private final AuthFormType type;

    private final Map<String, char[]> values =
            new HashMap<>();

    private boolean closed;

    public AuthFormSubmission(
            UUID playerId,
            AuthFormType type,
            Map<String, char[]> values
    ) {
        this.playerId = Objects.requireNonNull(playerId);
        this.type = Objects.requireNonNull(type);

        Objects.requireNonNull(values);

        values.forEach((key, value) -> {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);

            this.values.put(key, value.clone());
        });
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public AuthFormType getType() {
        return type;
    }

    public synchronized char[] getSecret(String fieldId) {
        ensureOpen();

        char[] value = values.get(fieldId);

        return value == null ? new char[0] : value.clone();
    }

    public synchronized boolean hasField(String fieldId) {
        ensureOpen();
        return values.containsKey(fieldId);
    }

    public synchronized void clear() {
        if (closed) {
            return;
        }

        values.values().forEach(value ->
                Arrays.fill(value, '\0')
        );

        values.clear();
        closed = true;
    }

    @Override
    public void close() {
        clear();
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "Form submission has already been cleared."
            );
        }
    }
}
