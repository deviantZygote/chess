package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class DatabaseDataAccess implements DataAccess {

    public DatabaseDataAccess () throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase () throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            createUserTable(conn);
            createAuthTable(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Error: SQL error");
        }
    }

    private void createUserTable(Connection conn) throws SQLException {
        String statement = """
        CREATE TABLE IF NOT EXISTS users (
            username VARCHAR(50) PRIMARY KEY,
            password VARCHAR(100) NOT NULL,
            email VARCHAR(100) NOT NULL
        )
    """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        }
    }

    private void createAuthTable(Connection conn) throws SQLException {
        String statement = """
        CREATE TABLE IF NOT EXISTS auth (
            authToken VARCHAR(200) PRIMARY KEY,
            username VARCHAR(100) NOT NULL
        )
    """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            clearUserTable(conn);
            clearAuthTable(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Error: Failed to Clear DB");
        }

    }

    private void clearUserTable(Connection conn) throws SQLException {

        String statement = """
        DELETE FROM users;
    """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        }
    }

    private void clearAuthTable(Connection conn) throws SQLException {

        String statement = """
        DELETE FROM auth;
    """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        }
    }


    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = """
        INSERT INTO users (username, password, email)
        VALUES (?, ?, ?)
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: SQL User Insert Fail");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {

        String sql = """
        SELECT username, password, email
        FROM users
        WHERE username = ?
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to retrieve user", e);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = """
        INSERT INTO auth (authToken, username)
        VALUES (?, ?)
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: SQL Auth Insert Fail");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = """
        SELECT authToken, username
        FROM auth
        WHERE authToken = ?
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new AuthData(
                        rs.getString("authToken"),
                        rs.getString("username")
                );
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to retrieve authData", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        // remove authToken from db
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        // create a game in the db
        return new GameData(1234, "fakeGame", "whiteUsername", "blackUsername", new ChessGame());
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        // return existing game
        return new GameData(1234, "fakeGame", "whiteUsername", "blackUsername", new ChessGame());
    }

    @Override
    public Collection<GameData> getGames() throws DataAccessException {
        // return all games
        Collection<GameData> games = new ArrayList<>();
        return games;
    }

    @Override
    public void assignGamePlayer(ChessGame.TeamColor teamColor, int gameID, String username) throws DataAccessException {
        // update player in db
    }


}
