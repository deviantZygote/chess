package dataaccess;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {
    @Test
    public void testConnection() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Error: Sql Failure");
        }
    }

    @Test
    public void userTableCreated() throws Exception {
        DatabaseManager.createDatabase();
        DatabaseDataAccess db = new DatabaseDataAccess();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                             SELECT table_name
                             FROM information_schema.tables
                             WHERE table_schema = DATABASE()
                             AND table_name = ?
                     """
             )) {

            stmt.setString(1, "users");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("users", rs.getString("table_name"));
        }
    }



}
