package server;

import io.javalin.*;
import dataaccess.*;
import service.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new UserDAO();
        AuthDAO authDAO = new AuthDAO();
        RegisterService registerService = new RegisterService(userDAO, authDAO);

        RegisterHandler registerHandler = new RegisterHandler(registerService);
        javalin.post("/user", registerHandler::handle);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
