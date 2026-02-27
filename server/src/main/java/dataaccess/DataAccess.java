package dataaccess;

import model.*;
import chess.ChessGame;
import java.util.Collection;

public interface DataAccess {
    void clear() throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    GameData createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> getGames() throws DataAccessException;
    void assignGamePlayer(ChessGame.TeamColor teamColor, int gameID, String username) throws DataAccessException;

}
