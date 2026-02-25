package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryDataAccess implements DataAccess {

    private final ConcurrentHashMap<String, UserData> usersByUsername = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AuthData> authByToken = new ConcurrentHashMap<>();

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
}
