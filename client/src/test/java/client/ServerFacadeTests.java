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

    @AfterAll
    static void stopServer() {
        server.stop();
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

}
