package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;
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
            createGameDataTable(conn);
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

    private void createGameDataTable(Connection conn) throws SQLException {
        String statement = """
        CREATE TABLE IF NOT EXISTS game (
            gameID INT AUTO_INCREMENT PRIMARY KEY,
            gameName VARCHAR(255) NOT NULL,
            whiteUsername VARCHAR(100),
            blackUsername VARCHAR(100),
            game TEXT NOT NULL
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
            clearGameTable(conn);
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

    private void clearGameTable(Connection conn) throws SQLException {
        String statement = """
        DELETE FROM game;
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
        if (auth == null)  {
            throw new DataAccessException("Error: SQL Auth Null");
        }
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
        String sql = """
        DELETE
        FROM auth
        WHERE authToken = ?
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new DataAccessException("Error: authToken not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to retrieve authData", e);
        }

    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        Gson gson = new Gson();
        ChessGame chessGame = new ChessGame();
        String gameJson = gson.toJson(chessGame);

        String sql = """
        INSERT INTO game (gameName, whiteUsername, blackUsername, game)
        VALUES (?, NULL, NULL, ?)
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, gameName);
            stmt.setString(2, gameJson);

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            int gameID = rs.getInt(1);

            return new GameData(gameID, gameName, null, null, chessGame);
        } catch (SQLException e) {
            throw new DataAccessException("Error: SQL Game Insert Fail");
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        Gson gson = new Gson();

        String sql = """
        SELECT gameID, gameName, whiteUsername, blackUsername, game
        FROM game
        WHERE gameID = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ChessGame chessGame = gson.fromJson(rs.getString("game"), ChessGame.class);

                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("gameName"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            chessGame
                    );
                }
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to retrieve gameData", e);
        }
    }

    @Override
    public Collection<GameData> getGames() throws DataAccessException {
        Gson gson = new Gson();
        Collection<GameData> games = new ArrayList<>();

        String sql = """
        SELECT gameID, gameName, whiteUsername, blackUsername, game
        FROM game
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ChessGame chessGame = gson.fromJson(rs.getString("game"), ChessGame.class);

                GameData game = new GameData(
                        rs.getInt("gameID"),
                        rs.getString("gameName"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        chessGame
                );

                games.add(game);
            }

            return games;

        } catch (SQLException e) {
            throw new DataAccessException("Error: SQL error getting games", e);
        }
    }

    @Override
    public void assignGamePlayer(ChessGame.TeamColor teamColor,
                                 int gameID,
                                 String username) throws DataAccessException {

        GameData targetGame = getGame(gameID);
        if (targetGame == null) {
            throw new DataAccessException("Error: game not found");
        }

        String sql;

        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (targetGame.getWhiteUsername() != null ) {
                throw new DataAccessException("Error: Position already taken!");
            }
            sql = """
                UPDATE game
                SET whiteUsername = ?
                WHERE gameID = ?
                """;
        } else if (teamColor == ChessGame.TeamColor.BLACK) {
            if (targetGame.getBlackUsername() != null ) {
                throw new DataAccessException("Error: Position already taken!");
            }
            sql = """
                UPDATE game
                SET blackUsername = ?
                WHERE gameID = ?
                """;
        } else {
            throw new DataAccessException("Error: invalid team color");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, gameID);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new DataAccessException("Error: game not found");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to assign player", e);
        }
    }


}
