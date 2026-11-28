package vn.phuc.pscore.auth;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AuthFormProviderRegistry {

    private final List<AuthFormProvider> providers =
            new ArrayList<>();

    public synchronized void register(
            AuthFormProvider provider
    ) {
        Objects.requireNonNull(provider);

        boolean duplicate = providers.stream()
                .anyMatch(existing ->
                        existing.getProviderId().equalsIgnoreCase(
                                provider.getProviderId()
                        )
                );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Auth form provider already registered: "
                            + provider.getProviderId()
            );
        }

        providers.add(provider);

        providers.sort(
                Comparator.comparingInt(
                        AuthFormProvider::getPriority
                ).reversed()
        );
    }

    public synchronized boolean unregister(String providerId) {
        return providers.removeIf(provider ->
                provider.getProviderId().equalsIgnoreCase(providerId)
        );
    }

    public synchronized Optional<AuthFormProvider> find(
            Player player
    ) {
        Objects.requireNonNull(player);

        return providers.stream()
                .filter(AuthFormProvider::isAvailable)
                .filter(provider -> provider.supports(player))
                .findFirst();
    }

    public synchronized List<String> getProviderIds() {
        return providers.stream()
                .map(AuthFormProvider::getProviderId)
                .toList();
    }

    public synchronized void clear() {
        providers.clear();
    }
}
