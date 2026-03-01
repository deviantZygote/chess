package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.LoginRequest;
import model.LoginResponse;
import model.UserData;
import org.junit.jupiter.api.Test;
import exceptions.*;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutServiceTest {

    @Test
    public void logoutSuccess() throws DataAccessException {
        DataAccess data = new MemoryDataAccess();
        LoginService loginService = new LoginService(data);
        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));
        LoginRequest req = new LoginRequest("bob", "pass");
        LoginResponse res = loginService.login(req);

        LogoutService logoutService = new LogoutService(data);
        assertDoesNotThrow(() -> logoutService.logout(res.authToken));
        assertNull(data.getAuth(res.authToken));
    }

    @Test
    public void logoutInvalidTokenThrowsUnauthorized() {
        DataAccess data = new MemoryDataAccess();
        LogoutService logoutService = new LogoutService(data);

        assertThrows(UnauthorizedException.class, () -> logoutService.logout("not-a-real-token"));
    }
}
