package vn.phuc.pscore.auth;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface AuthFormProvider {

    String getProviderId();

    int getPriority();

    boolean supports(Player player);

    void open(
            Player player,
            AuthFormDefinition definition,
            Consumer<AuthFormSubmission> onSubmit,
            Runnable onCancel
    );

    default void close(Player player) {
    }
}
