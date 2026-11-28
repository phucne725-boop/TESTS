
package vn.phuc.pscore.auth;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public final class AuthService {

    private final AuthRepository repository;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final AuthSessionManager sessionManager;

    public AuthService(
            AuthRepository repository,
            PasswordHasher passwordHasher,
            PasswordPolicy passwordPolicy,
            AuthSessionManager sessionManager
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
        this.sessionManager = Objects.requireNonNull(sessionManager);
    }

    public AuthAccount register(
            UUID playerId,
            String username,
            char[] password,
            char[] confirmation
    ) {
        Objects.requireNonNull(playerId);
        Objects.requireNonNull(username);

        try {
            passwordPolicy.validate(password);

            if (!Arrays.equals(password, confirmation)) {
                throw new AuthException("Mật khẩu xác nhận không khớp.");
            }

            if (repository.exists(playerId)) {
                throw new AuthException("Tài khoản đã được đăng ký.");
            }

            PasswordHash hash = passwordHasher.hash(password);
            Instant now = Instant.now();

            AuthAccount account = new AuthAccount(
                    playerId,
                    username,
                    hash,
                    now,
                    now
            );

            repository.create(account);
            sessionManager.authenticate(playerId);

            return account;
        } finally {
            clear(password);
            clear(confirmation);
        }
    }

    public AuthAccount login(UUID playerId, char[] password) {
        Objects.requireNonNull(playerId);

        try {
            AuthAccount account = repository.findByPlayerId(playerId)
                    .orElseThrow(() -> new AuthException(
                            "Tài khoản chưa được đăng ký."
                    ));

            if (!passwordHasher.verify(password, account.getPasswordHash())) {
                throw new AuthException("Mật khẩu không chính xác.");
            }

            sessionManager.authenticate(playerId);

            return account;
        } finally {
            clear(password);
        }
    }

    public void changePassword(
            UUID playerId,
            char[] currentPassword,
            char[] newPassword,
            char[] confirmation
    ) {
        try {
            if (!sessionManager.isAuthenticated(playerId)) {
                throw new AuthException("Bạn chưa đăng nhập.");
            }

            passwordPolicy.validate(newPassword);

            if (!Arrays.equals(newPassword, confirmation)) {
                throw new AuthException("Mật khẩu xác nhận không khớp.");
            }

            AuthAccount account = repository.findByPlayerId(playerId)
                    .orElseThrow(() -> new AuthException(
                            "Không tìm thấy tài khoản."
                    ));

            if (!passwordHasher.verify(
                    currentPassword,
                    account.getPasswordHash()
            )) {
                throw new AuthException("Mật khẩu hiện tại không đúng.");
            }

            PasswordHash newHash = passwordHasher.hash(newPassword);

            repository.updatePasswordHash(playerId, newHash);
        } finally {
            clear(currentPassword);
            clear(newPassword);
            clear(confirmation);
        }
    }

    public boolean logout(UUID playerId) {
        return sessionManager.logout(playerId);
    }

    public boolean isAuthenticated(UUID playerId) {
        return sessionManager.isAuthenticated(playerId);
    }

    private void clear(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
