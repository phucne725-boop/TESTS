package vn.phuc.pscore.database;

import java.util.List;

public final class AuthMigrationCatalog {

    private AuthMigrationCatalog() {
    }

    public static List<Migration> create() {
        return List.of(
                new SchemaMigrationV1(),
                new AuthMigrationV2()
        );
    }
}
