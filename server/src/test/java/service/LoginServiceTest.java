package service;

import dataaccess.*;
import exceptions.*;
import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {

    @Test
    public void login_success() throws DataAccessException {
            DataAccess data = new MemoryDataAccess();
            LoginService service = new LoginService(data);

            data.createUser(new UserData("bob", "pass", "bob@gmail.com"));

            LoginRequest req = new LoginRequest("bob", "pass");

            LoginResponse res = service.login(req);

            assertEquals("bob", res.username);
            assertNotNull(res.authToken);
            assertNotNull(data.getAuth(res.authToken));
        }

    @Test
    public void login_fail() throws DataAccessException {
        DataAccess data = new MemoryDataAccess();
        LoginService service = new LoginService(data);

        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));

        LoginRequest req = new LoginRequest("bob", "password");

        assertThrows(UnauthorizedException.class, () -> service.login(req));
    }
}
