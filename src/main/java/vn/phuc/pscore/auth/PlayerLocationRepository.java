package vn.phuc.pscore.auth;

import java.util.Optional;
import java.util.UUID;

public interface PlayerLocationRepository {

    Optional<PlayerLocationSnapshot> find(UUID playerId);

    void save(UUID playerId, PlayerLocationSnapshot location);

    void delete(UUID playerId);
}
