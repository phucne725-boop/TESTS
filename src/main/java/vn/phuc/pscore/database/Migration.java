
package vn.phuc.pscore.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {

    int getVersion();

    String getDescription();

    void migrate(Connection connection) throws SQLException;
}
