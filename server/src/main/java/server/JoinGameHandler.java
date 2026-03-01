package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.JoinGameService;
import exceptions.*;

import static helpers.HelperFunctions.catchHandlerExceptions;
import static helpers.HelperFunctions.isBlank;

public class JoinGameHandler {
    private final Gson gson = new Gson();
    private final JoinGameService joinGameService;

    public JoinGameHandler(JoinGameService joinGameService) {
        this.joinGameService = joinGameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (isBlank(authToken)) {
                throw new UnauthorizedException("Error: unauthorized");
            }
            JoinGameRequest req = gson.fromJson(ctx.body(), JoinGameRequest.class);
            if (req == null) {
                throw new BadRequestException("Error: bad request");
            }
            joinGameService.joinGame(authToken, req);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
        } catch (Exception e) {
            catchHandlerExceptions(e, ctx, gson);
        }
    }


}
