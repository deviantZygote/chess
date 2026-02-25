package service;

import dataaccess.*;
import exceptions.*;
import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTest {

    @Test
    public void register_success() {
        DataAccess data = new MemoryDataAccess();
        RegisterService service = new RegisterService(data);

        RegisterRequest req = new RegisterRequest("bob", "pass", "bob@gmail.com");

        RegisterResponse res = service.register(req);

        assertEquals("bob", res.username);
        assertNotNull(res.authToken);
    }

    @Test
    public void register_duplicateUser_throws() {
        DataAccess data = new MemoryDataAccess();
        RegisterService service = new RegisterService(data);

        RegisterRequest req = new RegisterRequest("bob", "pass", "bob@gmail.com");

        service.register(req);

        assertThrows(AlreadyTakenException.class, () -> { service.register(req); });
    }

    @Test
    public void register_missingField_throwsBadRequest() {
        DataAccess data = new MemoryDataAccess();
        RegisterService service = new RegisterService(data);

        RegisterRequest req = new RegisterRequest("bob", "", "bob@gmail.com");

        assertThrows(BadRequestException.class, () -> { service.register(req); });
    }
}