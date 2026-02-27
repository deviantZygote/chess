package service;

import dataaccess.*;
import exceptions.*;
import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTest {

    @Test
    public void create_success() throws DataAccessException {
        DataAccess data = new MemoryDataAccess();
        CreateGameService createGameService = new CreateGameService(data);
        LoginService loginService = new LoginService(data);


        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));
        UserData userData = data.getUser("bob");
        LoginRequest req = new LoginRequest(userData.username(), userData.password());

        LoginResponse res = loginService.login(req);

        CreateGameResponse createGameResponse = createGameService.createGame(res.authToken, new CreateGameRequest("newGame"));

        GameData newGameData = data.getGame(createGameResponse.gameID);

        assertNotNull(createGameResponse);
        assertTrue(createGameResponse.gameID > 0);
        assertNotNull(newGameData);
        assertEquals("newGame", newGameData.getGameName());

    }

    @Test
    public void create_failure() throws DataAccessException {
        DataAccess data = new MemoryDataAccess();
        CreateGameService createGameService = new CreateGameService(data);
        LoginService loginService = new LoginService(data);


        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));
        UserData userData = data.getUser("bob");
        LoginRequest req = new LoginRequest(userData.username(), userData.password());

        LoginResponse res = loginService.login(req);

        assertThrows(UnauthorizedException.class, () -> createGameService.createGame("WRONGTOKEN", new CreateGameRequest("newGame")));
        assertThrows(BadRequestException.class, () -> createGameService.createGame(res.authToken, new CreateGameRequest(null)));
    }
}
