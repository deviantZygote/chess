package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    @Override
    public void clear() throws DataAccessException {
        // clear the db
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // add user to db
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        // query db for userData
        return new UserData("dummyUser", "dummyPass", "bob@gmail.com");
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        // store auth data
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        // retrive authToken from db
        return new AuthData("fakeAuthToken", "fakeUsername");
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
