package service;

import dataaccess.*;
import exceptions.*;
import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTest {

    @Test
    public void registerSuccess() {
        DataAccess data = new MemoryDataAccess();
        RegisterService service = new RegisterService(data);

        RegisterRequest req = new RegisterRequest("bob", "pass", "bob@gmail.com");

        RegisterResponse res = service.register(req);

        assertEquals("bob", res.username);
        assertNotNull(res.authToken);
    }

    @Test
    public void registerDuplicateUserThrows() {
        DataAccess data = new MemoryDataAccess();
        RegisterService service = new RegisterService(data);

        RegisterRequest req = new RegisterRequest("bob", "pass", "bob@gmail.com");

        service.register(req);

        assertThrows(AlreadyTakenException.class, () -> { service.register(req); });
    }

    @Test
    public void registerMissingFieldThrowsBadRequest() {
        DataAccess data = new MemoryDataAccess();
        RegisterService service = new RegisterService(data);

        RegisterRequest req = new RegisterRequest("bob", "", "bob@gmail.com");

        assertThrows(BadRequestException.class, () -> { service.register(req); });
    }
}