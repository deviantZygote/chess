package client;

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
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void RegisterTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        try {
            serverFacade.register("bob", "bobPass", "bob@email.com");
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(true);
    }

    @Test
    public void ClearTest() {
        ServerFacade serverFacade = new ServerFacade(ServerConfig.SERVER_URL + port);
        Assertions.assertDoesNotThrow(() -> serverFacade.clear());
    }

}
