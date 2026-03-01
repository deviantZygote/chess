package service;

import chess.ChessGame;
import dataaccess.*;
import exceptions.*;
import model.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;


public class JoinGameServiceTest {
    @Test
    public void join_game_success() throws DataAccessException {
        DataAccess data = new MemoryDataAccess();
        CreateGameService createGameService = new CreateGameService(data);
        LoginService loginService = new LoginService(data);
        JoinGameService joinGameService = new JoinGameService(data);


        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));
        UserData userData = data.getUser("bob");
        LoginRequest req = new LoginRequest(userData.username(), userData.password());

        LoginResponse res = loginService.login(req);

        data.createUser(new UserData("bill", "pass", "bill@gmail.com"));
        UserData userData2 = data.getUser("bill");
        LoginRequest req2 = new LoginRequest(userData2.username(), userData2.password());

        LoginResponse res2 = loginService.login(req2);

        CreateGameResponse createGameResponse = createGameService.createGame(res.authToken, new CreateGameRequest("newGame"));

        joinGameService.joinGame(res.authToken, new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResponse.gameID));
        joinGameService.joinGame(res2.authToken, new JoinGameRequest(ChessGame.TeamColor.BLACK, createGameResponse.gameID));

        GameData gameData = data.getGame(createGameResponse.gameID);

        assertEquals(gameData.getWhiteUsername(), userData.username());
        assertEquals(gameData.getBlackUsername(), userData2.username());

    }

    @Test
    public void join_game_fail() throws DataAccessException {
        DataAccess data = new MemoryDataAccess();
        CreateGameService createGameService = new CreateGameService(data);
        LoginService loginService = new LoginService(data);
        JoinGameService joinGameService = new JoinGameService(data);


        data.createUser(new UserData("bob", "pass", "bob@gmail.com"));
        UserData userData = data.getUser("bob");
        LoginRequest req = new LoginRequest(userData.username(), userData.password());
        LoginResponse res = loginService.login(req);

        data.createUser(new UserData("bill", "pass", "bill@gmail.com"));
        UserData userData2 = data.getUser("bill");
        LoginRequest req2 = new LoginRequest(userData2.username(), userData2.password());
        LoginResponse res2 = loginService.login(req2);

        data.createUser(new UserData("jane", "pass", "jane@gmail.com"));
        UserData userData3 = data.getUser("jane");
        LoginRequest req3 = new LoginRequest(userData3.username(), userData3.password());
        LoginResponse res3 = loginService.login(req3);

        CreateGameResponse createGameResponse = createGameService.createGame(res.authToken, new CreateGameRequest("newGame"));

        joinGameService.joinGame(res.authToken, new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResponse.gameID));
        joinGameService.joinGame(res2.authToken, new JoinGameRequest(ChessGame.TeamColor.BLACK, createGameResponse.gameID));

        assertThrows(AlreadyTakenException.class, () -> joinGameService.joinGame(res3.authToken, new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResponse.gameID)));
    }
}
