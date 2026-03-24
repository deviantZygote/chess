package client;

import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import config.ServerConfig;


public class ServerFacadeTests {

    private static Server server;
    private static int port = 0;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    private ServerFacade serverFacade;

    @BeforeEach
    public void setup() throws ResponseException {
        serverFacade = new ServerFacade("http://localhost:" + port);
        serverFacade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();

    }

    @AfterEach
    public void tearDown() throws ResponseException {
        serverFacade = new ServerFacade("http://localhost:" + port);
        serverFacade.clear();
    }


    @Test
    public void registerTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            RegisterResponse resp = serverFacade.register(registerRequest);
            Assertions.assertEquals("bob", resp.username);
            Assertions.assertNotNull(resp.authToken);

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void registerFailTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            RegisterResponse resp = serverFacade.register(registerRequest);

            RegisterRequest registerRequest2 = new RegisterRequest("bob", "bobPass", "bob@email.com");
            Assertions.assertThrows(ResponseException.class, () -> serverFacade.register(registerRequest2));

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void clearTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        Assertions.assertDoesNotThrow(() -> serverFacade.clear());
    }

    @Test
    public void loginTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            Assertions.assertNotNull(loginResp.authToken);
            Assertions.assertEquals("bob", loginResp.username);

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void loginFailTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobWrongPass");
            Assertions.assertThrows(ResponseException.class, () -> serverFacade.login(loginRequest));

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void logoutTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            Assertions.assertDoesNotThrow(() -> serverFacade.logout(loginResp.authToken));

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void logoutFailTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            Assertions.assertThrows(ResponseException.class, () -> serverFacade.logout("badToken123"));

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createGameTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            CreateGameResponse createGameResponse = serverFacade.createGame(new CreateGameRequest("newGame"), loginResp.authToken);
            Assertions.assertNotNull(createGameResponse);
            Assertions.assertTrue(createGameResponse.gameID > -1);

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createGameFailTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            Assertions.assertThrows(ResponseException.class,
                    () -> serverFacade.createGame(
                            new CreateGameRequest("newGame"),
                            "badLoginToken"));


        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getGamesTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            CreateGameResponse createGameResponse1 = serverFacade.createGame(new CreateGameRequest("newGame1"), loginResp.authToken);
            CreateGameResponse createGameResponse2 = serverFacade.createGame(new CreateGameRequest("newGame2"), loginResp.authToken);
            CreateGameResponse createGameResponse3 = serverFacade.createGame(new CreateGameRequest("newGame3"), loginResp.authToken);

            GetGamesResponse getGamesResponse = serverFacade.getGames(loginResp.authToken);

            Assertions.assertTrue(getGamesResponse.games.size() > 2);

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getGamesFailTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            CreateGameResponse createGameResponse1 = serverFacade.createGame(new CreateGameRequest("newGame1"), loginResp.authToken);
            CreateGameResponse createGameResponse2 = serverFacade.createGame(new CreateGameRequest("newGame2"), loginResp.authToken);
            CreateGameResponse createGameResponse3 = serverFacade.createGame(new CreateGameRequest("newGame3"), loginResp.authToken);

            Assertions.assertThrows(ResponseException.class, () -> serverFacade.getGames("BadToken123"));
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void joinGameTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            CreateGameResponse createGameResponse = serverFacade.createGame(new CreateGameRequest("newGame"), loginResp.authToken);

            JoinGameRequest joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResponse.gameID);
            Assertions.assertDoesNotThrow(() -> serverFacade.joinGame(joinGameRequest, loginResp.authToken));

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void joinGameFailTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            RegisterRequest registerRequest = new RegisterRequest("bob", "bobPass", "bob@email.com");
            serverFacade.register(registerRequest);

            RegisterRequest registerRequest2 = new RegisterRequest("bill", "billPass", "bill@email.com");
            serverFacade.register(registerRequest2);

            LoginRequest loginRequest = new LoginRequest("bob", "bobPass");
            LoginResponse loginResp = serverFacade.login(loginRequest);

            LoginRequest loginRequest2 = new LoginRequest("bill", "billPass");
            LoginResponse loginResp2 = serverFacade.login(loginRequest2);

            CreateGameResponse createGameResponse = serverFacade.createGame(new CreateGameRequest("newGame"), loginResp.authToken);

            JoinGameRequest joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResponse.gameID);
            serverFacade.joinGame(joinGameRequest, loginResp.authToken);

            JoinGameRequest joinGameRequest2 = new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResponse.gameID);
            Assertions.assertThrows(ResponseException.class, () -> serverFacade.joinGame(joinGameRequest2, loginResp2.authToken));

        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

}
