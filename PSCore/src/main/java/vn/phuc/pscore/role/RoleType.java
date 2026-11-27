package vn.phuc.pscore.role;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum RoleType {
    ADMIN, STAFF, MEDIA, SPECTATOR, SYSTEM;

    public static Optional<RoleType> from(String input) {
        if (input == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(role -> role.name().equals(input.trim().toUpperCase(Locale.ROOT)))
                .findFirst();
    }
}
