import server.Server;
import config.ServerConfig;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(ServerConfig.PORT);

        System.out.println("♕ 240 Chess Server");
    }
}