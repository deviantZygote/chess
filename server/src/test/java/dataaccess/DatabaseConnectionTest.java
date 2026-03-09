package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @BeforeEach
    public void setup() throws Exception {
        DatabaseManager.createDatabase();
        DatabaseDataAccess db = new DatabaseDataAccess();
        db.clear();
    }

    @Test
    public void dbConnectTest() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Error: Sql Failure");
        }
    }

    @Test
    public void userTableCreateTest() throws Exception {
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

    @Test
    public void authTableCreateTest() throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                             SELECT table_name
                             FROM information_schema.tables
                             WHERE table_schema = DATABASE()
                             AND table_name = ?
                     """
             )) {

            stmt.setString(1, "auth");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("auth", rs.getString("table_name"));
        }
    }

    @Test
    public void getUserTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        UserData expectedUser = new UserData("testUser", "testPassword", "test@email.com");
        db.createUser(expectedUser);
        UserData returnedUser = db.getUser(expectedUser.username());

        assertEquals(expectedUser, returnedUser);
    }

    @Test
    public void createAuthTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        AuthData expectedAuthData = new AuthData("112345678911234567891123456789", "username");
        db.createAuth(expectedAuthData);
        AuthData returnedAuthData = db.getAuth(expectedAuthData.authToken());

        assertEquals(expectedAuthData.username(), returnedAuthData.username());
    }

    @Test
    public void deleteAuthTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        AuthData expectedAuthData = new AuthData("112345678911234567891123456789", "username");
        db.createAuth(expectedAuthData);
        AuthData returnedAuthData = db.getAuth(expectedAuthData.authToken());
        assertNotNull(db.getAuth(expectedAuthData.authToken()));
        db.deleteAuth(expectedAuthData.authToken());
        assertNull(db.getAuth(expectedAuthData.authToken()) );
    }


}
