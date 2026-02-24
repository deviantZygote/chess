package dataaccess;

import model.AuthData;
import java.util.concurrent.ConcurrentHashMap;

public class AuthDAO {
    private final ConcurrentHashMap<String, AuthData> authByToken = new ConcurrentHashMap<>();

    public void create(AuthData auth) {
        authByToken.put(auth.authToken, auth);
    }
}
