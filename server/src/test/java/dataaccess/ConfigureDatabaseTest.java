package dataaccess;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigureDatabaseTest {
    @BeforeAll
    static void testConnection() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Error: Sql Failure");
        }
    }
}
