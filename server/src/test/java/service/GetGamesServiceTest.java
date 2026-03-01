package service;

import dataaccess.*;
import exceptions.*;
import model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class GetGamesServiceTest {
    @Test
    public void listGamesSuccess() throws DataAccessException {
        DataAccess data = new MemoryDataAccess();
        CreateGameService createGameService = new CreateGameService(data);
        LoginService loginService = new LoginService(data);


        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));
        UserData userData = data.getUser("bob");
        LoginRequest req = new LoginRequest(userData.username(), userData.password());

        LoginResponse res = loginService.login(req);

        CreateGameResponse createGameResponse = createGameService.createGame(res.authToken, new CreateGameRequest("newGame"));
        CreateGameResponse createGameResponse1 = createGameService.createGame(res.authToken, new CreateGameRequest("newGame1"));
        CreateGameResponse createGameResponse2 = createGameService.createGame(res.authToken, new CreateGameRequest("newGame2"));

        ArrayList<GameData> gamesData = new ArrayList<>();
        gamesData.addAll(data.getGames());

        assertEquals(3, gamesData.size());

    }

    @Test
    public void listGamesFail() throws DataAccessException {
        DataAccess data = new MemoryDataAccess();
        CreateGameService createGameService = new CreateGameService(data);
        LoginService loginService = new LoginService(data);


        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));
        UserData userData = data.getUser("bob");
        LoginRequest req = new LoginRequest(userData.username(), userData.password());

        LoginResponse res = loginService.login(req);

        ArrayList<GameData> gamesData = new ArrayList<>();
        gamesData.addAll(data.getGames());

        assertEquals(0, gamesData.size());

    }
}
