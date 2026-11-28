
package vn.phuc.pscore.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class DatabaseBackupService {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final DatabaseConfig config;
    private final SqliteConnectionFactory connectionFactory;

    public DatabaseBackupService(
            DatabaseConfig config,
            SqliteConnectionFactory connectionFactory
    ) {
        this.config = config;
        this.connectionFactory = connectionFactory;
    }

    public Path createBackup() {
        if (!config.isBackupEnabled()) {
            throw new DatabaseException(
                    "Database backups are disabled."
            );
        }

        Path source = config.getDatabasePath().toAbsolutePath();

        Path backupDirectory = source.getParent()
                .resolve("backups");

        try {
            Files.createDirectories(backupDirectory);

            checkpointWal();

            String timestamp = LocalDateTime.now().format(FORMAT);

            Path destination = backupDirectory.resolve(
                    "pscore-" + timestamp + ".db"
            );

            Files.copy(
                    source,
                    destination,
                    StandardCopyOption.COPY_ATTRIBUTES
            );

            pruneOldBackups(backupDirectory);

            return destination;

        } catch (IOException exception) {
            throw new DatabaseException(
                    "Could not create database backup.",
                    exception
            );
        }
    }

    private void checkpointWal() {
        try (var connection = connectionFactory.openConnection();
             var statement = connection.createStatement()) {

            statement.execute("PRAGMA wal_checkpoint(FULL)");

        } catch (Exception exception) {
            throw new DatabaseException(
                    "Could not checkpoint SQLite WAL before backup.",
                    exception
            );
        }
    }

    private void pruneOldBackups(Path directory)
            throws IOException {

        List<Path> backups;

        try (Stream<Path> stream = Files.list(directory)) {
            backups = stream
                    .filter(path -> path.getFileName()
                            .toString().startsWith("pscore-"))
                    .filter(path -> path.getFileName()
                            .toString().endsWith(".db"))
                    .sorted(Comparator.comparingLong(
                            this::lastModified
                    ).reversed())
                    .toList();
        }

        for (int index = config.getBackupRetention();
             index < backups.size();
             index++) {

            Files.deleteIfExists(backups.get(index));
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException exception) {
            return 0L;
        }
    }
}
