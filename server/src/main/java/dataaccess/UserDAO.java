package dataaccess;

import model.UserData;
import java.util.concurrent.ConcurrentHashMap;

public class UserDAO {
    private final ConcurrentHashMap<String, UserData> usersByUsername = new ConcurrentHashMap<>();

    public boolean exists(String username) {
        return usersByUsername.containsKey(username);
    }

    public void create(UserData user) {
        usersByUsername.put(user.username(), user);
    }
}
