package server;

import io.javalin.*;
import dataaccess.*;
import service.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        DataAccess dataAccess = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dataAccess);
        RegisterHandler registerHandler = new RegisterHandler(registerService);
        javalin.post("/user", registerHandler::handle);

        ClearService clearService = new ClearService(dataAccess);
        ClearHandler clearHandler = new ClearHandler(clearService);
        javalin.delete("/db", clearHandler::handle);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
