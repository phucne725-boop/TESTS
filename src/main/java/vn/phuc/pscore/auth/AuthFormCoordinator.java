package vn.phuc.pscore.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class AuthFormCoordinator {

    private final Plugin plugin;
    private final AuthService authService;
    private final AuthRepository authRepository;
    private final AuthFormProviderRegistry providers;
    private final AuthFormSessionManager sessions;

    private final Consumer<AuthFormResult> resultLogger;

    public AuthFormCoordinator(
            Plugin plugin,
            AuthService authService,
            AuthRepository authRepository,
            AuthFormProviderRegistry providers,
            AuthFormSessionManager sessions,
            Consumer<AuthFormResult> resultLogger
    ) {
        this.plugin = Objects.requireNonNull(plugin);
        this.authService = Objects.requireNonNull(authService);
        this.authRepository = Objects.requireNonNull(authRepository);
        this.providers = Objects.requireNonNull(providers);
        this.sessions = Objects.requireNonNull(sessions);

        this.resultLogger = resultLogger == null
                ? result -> {
                }
                : resultLogger;
    }

    public boolean openInitialForm(Player player) {
        Objects.requireNonNull(player);

        UUID playerId = player.getUniqueId();

        if (authService.isAuthenticated(playerId)) {
            return false;
        }

        AuthFormType type = authRepository.exists(playerId)
                ? AuthFormType.LOGIN
                : AuthFormType.REGISTER;

        AuthFormDefinition definition =
                type == AuthFormType.LOGIN
                        ? AuthFormFactory.login()
                        : AuthFormFactory.register();

        return open(player, definition);
    }

    public boolean openChangePasswordForm(Player player) {
        Objects.requireNonNull(player);

        if (!authService.isAuthenticated(player.getUniqueId())) {
            sendResult(
                    player,
                    AuthFormResult.failure(
                            "Bạn cần đăng nhập trước."
                    )
            );

            return false;
        }

        return open(
                player,
                AuthFormFactory.changePassword()
        );
    }

    private boolean open(
            Player player,
            AuthFormDefinition definition
    ) {
        UUID playerId = player.getUniqueId();

        if (!sessions.open(playerId, definition.type())) {
            return false;
        }

        AuthFormProvider provider = providers.find(player)
                .orElse(null);

        if (provider == null) {
            sessions.cancel(playerId);

            sendResult(
                    player,
                    AuthFormResult.failure(
                            "Không tìm thấy giao diện xác thực phù hợp."
                    )
            );

            return false;
        }

        try {
            provider.open(
                    player,
                    definition,
                    submission -> runOnMainThread(() ->
                            handleSubmission(player, submission)
                    ),
                    () -> runOnMainThread(() ->
                            handleCancellation(player, provider)
                    )
            );

            return true;

        } catch (Exception exception) {
            sessions.cancel(playerId);

            plugin.getLogger().severe(
                    "Could not open authentication form: "
                            + exception.getMessage()
            );

            sendResult(
                    player,
                    AuthFormResult.failure(
                            "Không thể mở giao diện xác thực."
                    )
            );

            return false;
        }
    }

    private void handleSubmission(
            Player player,
            AuthFormSubmission submission
    ) {
        UUID playerId = player.getUniqueId();

        if (!player.isOnline()) {
            submission.close();
            sessions.cancel(playerId);
            return;
        }

        AuthFormType activeType = sessions.getType(playerId)
                .orElse(null);

        if (activeType == null
                || activeType != submission.getType()
                || !playerId.equals(submission.getPlayerId())) {
            submission.close();
            return;
        }

        sessions.finish(playerId);

        AuthFormResult result;

        try {
            result = executeSubmission(submission);

        } catch (AuthException exception) {
            result = AuthFormResult.failure(
                    exception.getMessage()
            );

        } catch (Exception exception) {
            plugin.getLogger().severe(
                    "Authentication form failed: "
                            + exception.getMessage()
            );

            result = AuthFormResult.failure(
                    "Có lỗi xảy ra. Vui lòng thử lại."
            );

        } finally {
            submission.close();
        }

        sendResult(player, result);
    }

    private AuthFormResult executeSubmission(
            AuthFormSubmission submission
    ) {
        UUID playerId = submission.getPlayerId();

        return switch (submission.getType()) {
            case REGISTER -> {
                char[] password =
                        submission.getSecret("password");

                char[] confirmation =
                        submission.getSecret("confirm");

                authService.register(
                        playerId,
                        Bukkit.getOfflinePlayer(playerId).getName(),
                        password,
                        confirmation
                );

                yield AuthFormResult.success(
                        "Đăng ký thành công!"
                );
            }

            case LOGIN -> {
                char[] password =
                        submission.getSecret("password");

                authService.login(playerId, password);

                yield AuthFormResult.success(
                        "Đăng nhập thành công!"
                );
            }

            case CHANGE_PASSWORD -> {
                char[] current =
                        submission.getSecret("current");

                char[] password =
                        submission.getSecret("password");

                char[] confirmation =
                        submission.getSecret("confirm");

                authService.changePassword(
                        playerId,
                        current,
                        password,
                        confirmation
                );

                yield AuthFormResult.success(
                        "Đổi mật khẩu thành công!"
                );
            }
        };
    }

    private void handleCancellation(
            Player player,
            AuthFormProvider provider
    ) {
        sessions.cancel(player.getUniqueId());
        provider.close(player);

        sendResult(
                player,
                AuthFormResult.cancelled(
                        "Bạn đã đóng giao diện xác thực."
                )
        );
    }

    private void sendResult(
            Player player,
            AuthFormResult result
    ) {
        resultLogger.accept(result);

        if (player.isOnline()) {
            player.sendMessage(result.message());
        }
    }

    private void runOnMainThread(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void cancel(UUID playerId) {
        sessions.cancel(playerId);
    }

    public void shutdown() {
        sessions.clear();
    }
          }
