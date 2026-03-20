package client;

import model.RegisterRequest;
import model.RegisterResponse;
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
    public void RegisterTest() {
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
    public void ClearTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        Assertions.assertDoesNotThrow(() -> serverFacade.clear());
    }

}
