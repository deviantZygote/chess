package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    public void gameDataTableCreateTest() throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                             SELECT table_name
                             FROM information_schema.tables
                             WHERE table_schema = DATABASE()
                             AND table_name = ?
                     """
             )) {

            stmt.setString(1, "game");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("game", rs.getString("table_name"));
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
    public void getUserFailTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        UserData expectedUser = new UserData("testUser", "testPassword", "test@email.com");
        UserData returnedUser = db.getUser(expectedUser.username());

        assertNull(returnedUser);
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
    public void createAuthFailTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        assertThrows(DataAccessException.class, () -> db.createAuth(null));
    }

    @Test
    public void getAuthTest() throws Exception {
        createAuthTest();
    }

    @Test
    public void getAuthFailTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        AuthData exampleAuthData = new AuthData("112345678911234567891123456789", "username");
        AuthData returnedAuthData = db.getAuth(exampleAuthData.authToken());

        assertNull(returnedAuthData);

    }

    @Test
    public void deleteAuthTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        AuthData expectedAuthData = new AuthData("112345678911234567891123456789", "username");
        db.createAuth(expectedAuthData);
        assertNotNull(db.getAuth(expectedAuthData.authToken()));
        db.deleteAuth(expectedAuthData.authToken());
        assertNull(db.getAuth(expectedAuthData.authToken()) );
    }

    @Test
    public void deleteAuthFailTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        AuthData exampleAuthData = new AuthData("112345678911234567891123456789", "username");
        assertThrows(DataAccessException.class, () -> db.deleteAuth(exampleAuthData.authToken()));
    }

    @Test
    public void createGameTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        String gameName = "aChessGame";
        GameData chessGame = db.createGame(gameName);

        assertEquals(gameName, chessGame.getGameName());
    }

    @Test
    public void createGameFailTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        assertThrows(DataAccessException.class, () -> db.createGame(null));
    }

    @Test
    public void getGameTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        String gameName = "aChessGame";
        GameData expectedChessGame = db.createGame(gameName);

        GameData returnedChessGame = db.getGame(expectedChessGame.getGameID());

        assertEquals(expectedChessGame.getGameID(), returnedChessGame.getGameID());
        assertEquals(expectedChessGame.getGameName(), returnedChessGame.getGameName());
        assertEquals(expectedChessGame.getWhiteUsername(), returnedChessGame.getWhiteUsername());
        assertEquals(expectedChessGame.getBlackUsername(), returnedChessGame.getBlackUsername());
        assertEquals(expectedChessGame, returnedChessGame);

    }

    @Test
    public void getGameFailTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        assertNull(db.getGame(-1));
    }

    @Test
    public void getGamesTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        String chessName1 = "chessGame1";
        String chessName2 = "chessGame2";
        String chessName3 = "chessGame3";

        GameData chessGame1 = db.createGame(chessName1);
        GameData chessGame2 = db.createGame(chessName2);
        GameData chessGame3 = db.createGame(chessName3);


        Collection<GameData> gamesData = db.getGames();

        Set<String> names = new HashSet<>();

        for (GameData game : gamesData) {
            names.add(game.getGameName());
        }

        assertTrue(names.contains(chessName1));
        assertTrue(names.contains(chessName2));
        assertTrue(names.contains(chessName3));
    }

    @Test
    public void getGamesFailTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        Collection<GameData> gamesData = db.getGames();
        assertTrue(gamesData.isEmpty());
    }

    @Test
    public void assignUserTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        String gameName = "aChessGame";
        GameData expectedChessGame = db.createGame(gameName);
        UserData expectedUser = new UserData("testUser", "testPassword", "test@email.com");

        db.assignGamePlayer(ChessGame.TeamColor.WHITE, expectedChessGame.getGameID(), expectedUser.username());

        GameData returnedChessGame = db.getGame(expectedChessGame.getGameID());

        assertEquals(expectedUser.username(), returnedChessGame.getWhiteUsername());
    }

    @Test
    public void assignUserFailTest() throws Exception {
        DatabaseDataAccess db = new DatabaseDataAccess();
        String gameName = "aChessGame";
        GameData chessGame = db.createGame(gameName);
        UserData firstUser = new UserData("testUser", "testPassword", "test@email.com");
        UserData secondUser = new UserData("testUser2", "testPassword", "test@email.com");

        db.assignGamePlayer(ChessGame.TeamColor.WHITE,
                chessGame.getGameID(),
                firstUser.username());

        assertThrows(DataAccessException.class,
                () -> db.assignGamePlayer(ChessGame.TeamColor.WHITE,
                        chessGame.getGameID(),
                        secondUser.username()));
    }



}
