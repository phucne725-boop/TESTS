
package vn.phuc.pscore.stats;

public final class StatisticsRepositoryException
        extends StatisticsException {

    public StatisticsRepositoryException(String message) {
        super(message);
    }

    public StatisticsRepositoryException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
