package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    @Test
    public void clear_success() throws Exception {
        DataAccess data = new MemoryDataAccess();
        ClearService service = new ClearService(data);

        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));
        data.createAuth(new AuthData("token123", "bob"));

        assertNotNull(data.getUser("bob"));
        assertNotNull(data.getAuth("token123"));

        service.clear();

        assertNull(data.getUser("bob"));
        assertNull(data.getAuth("token123"));
    }
}
