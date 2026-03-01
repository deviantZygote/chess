package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.CreateGameService;
import exceptions.*;

import static helpers.HelperFunctions.catchHandlerExceptions;
import static helpers.HelperFunctions.isBlank;

public class CreateGameHandler {
    private final Gson gson = new Gson();
    private final CreateGameService createGameService;

    public CreateGameHandler(CreateGameService createGameService) {
        this.createGameService = createGameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (isBlank(authToken)) {
                throw new UnauthorizedException("Error: unauthorized");
            }

            CreateGameRequest req = gson.fromJson(ctx.body(), CreateGameRequest.class);
            if (req == null || req.gameName == null) {
                throw new BadRequestException("Error: bad request");
            }
            CreateGameResponse res = createGameService.createGame(authToken, req);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(res));
        } catch (Exception e) {
            catchHandlerExceptions(e, ctx, gson);
        }
    }

}
