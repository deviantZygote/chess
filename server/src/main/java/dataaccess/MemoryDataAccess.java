package dataaccess;

import model.*;
import chess.ChessGame;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collection;

public class MemoryDataAccess implements DataAccess {

    private final ConcurrentHashMap<String, UserData> usersByUsername = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AuthData> authByToken = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, GameData> chessGamesByID = new ConcurrentHashMap<>();
    private final AtomicInteger gameId = new AtomicInteger(1);

    @Override
    public void clear() throws DataAccessException {
        usersByUsername.clear();
        authByToken.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null) throw new DataAccessException("user is null");
        if (user.username() == null) throw new DataAccessException("username is null");

        if (usersByUsername.putIfAbsent(user.username(), user) != null) {
            throw new DataAccessException("duplicate user");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null) throw new DataAccessException("username is null");
        return usersByUsername.get(username);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null) throw new DataAccessException("auth is null");
        if (auth.authToken() == null) throw new DataAccessException("authToken is null");
        authByToken.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authToken == null) throw new DataAccessException("authToken is null");
        return authByToken.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null) throw new DataAccessException("authToken is null");
        authByToken.remove(authToken);
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        if (gameName == null) {
            throw new DataAccessException("gameName is null");
        }
        int tempId = gameId.getAndIncrement();
        GameData gameData = new GameData(tempId, gameName, null, null, new ChessGame());
        chessGamesByID.put(tempId, gameData);
        return gameData;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData gameData = chessGamesByID.get(gameID);
        if (gameData == null) {
           throw new DataAccessException("Error: Game doesn't exist");
       }
        return gameData;
    }

    @Override
    public Collection<GameData> getGames() throws DataAccessException {
        ArrayList<GameData> gameDatas = new ArrayList<>();
        gameDatas.addAll(chessGamesByID.values());
        return gameDatas;
    }

    @Override
    public void assignGamePlayer(ChessGame.TeamColor teamColor, int gameID, String username) throws DataAccessException {
        GameData targetGame = getGame(gameID);
        if (targetGame == null || teamColor == null || username == null) {
            throw new DataAccessException("Error: bad data sent");
        }
        switch (teamColor) {
            case WHITE:
                targetGame.setWhiteUsername(username);
                break;
            case BLACK:
                targetGame.setBlackUsername(username);
                break;
        }

        chessGamesByID.replace(gameID, targetGame);
    }

}
