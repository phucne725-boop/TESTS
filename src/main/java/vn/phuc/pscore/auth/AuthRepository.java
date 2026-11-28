
package vn.phuc.pscore.auth;

import java.util.Optional;
import java.util.UUID;

public interface AuthRepository {

    Optional<AuthAccount> findByPlayerId(UUID playerId);

    boolean exists(UUID playerId);

    void create(AuthAccount account);

    void updatePasswordHash(
            UUID playerId,
            PasswordHash passwordHash
    );
}
