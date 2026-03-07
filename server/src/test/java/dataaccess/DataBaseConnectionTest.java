package dataaccess;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DataBaseConnectionTest {
    @Test
    public void testConnection() throws Exception {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection()) {
            assertNotNull(conn);
        }
    }

}
