
package vn.phuc.pscore.stats;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerStatisticsRepository {

    void createIfAbsent(Connection connection, UUID playerId);

    Optional<PlayerStatistics> find(
            Connection connection,
            UUID playerId
    );

    void addKill(Connection connection, UUID playerId);

    void addDeath(Connection connection, UUID playerId);

    void addPlaytime(
            Connection connection,
            UUID playerId,
            long seconds
    );

    List<PlayerStatistics> findTopPlaytime(
            Connection connection,
            int limit
    );

    List<PlayerStatistics> findTopKills(
            Connection connection,
            int limit
    );
}
