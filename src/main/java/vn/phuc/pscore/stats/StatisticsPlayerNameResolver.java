
package vn.phuc.pscore.stats;

import java.util.UUID;

@FunctionalInterface
public interface StatisticsPlayerNameResolver {

    String resolveName(UUID playerId);
}
