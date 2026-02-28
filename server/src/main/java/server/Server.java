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

        LoginService loginService = new LoginService(dataAccess);
        LoginHandler loginHandler = new LoginHandler(loginService);
        javalin.post("/session", loginHandler::handle);

        LogoutService logoutService = new LogoutService(dataAccess);
        LogoutHandler logoutHandler = new LogoutHandler(logoutService);
        javalin.delete("/session", logoutHandler::handle);

        CreateGameService createGameService = new CreateGameService(dataAccess);
        CreateGameHandler createGameHandler = new CreateGameHandler(createGameService);
        javalin.post("/game", createGameHandler::handle);

        GetGamesService getGamesService = new GetGamesService(dataAccess);
        GetGamesHandler getGamesHandler = new GetGamesHandler(getGamesService);
        javalin.get("/game", getGamesHandler::handle);

//        JoinGameService joinGameService = new joinGameService(dataAccess);
//        JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService);
//        javalin.put("/game", joinGameHandler::handle);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
