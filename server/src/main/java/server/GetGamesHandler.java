package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.GetGamesService;
import exceptions.*;

import static helpers.HelperFunctions.isBlank;


public class GetGamesHandler {
    private final Gson gson = new Gson();
    private final GetGamesService getGamesService;

    public GetGamesHandler(GetGamesService getGamesService) {
        this.getGamesService = getGamesService;
    }

    public void handle(Context ctx) {
    try {
            String authToken = ctx.header("authorization");
            if (isBlank(authToken)) {
                throw new UnauthorizedException("Error: unauthorized");
            }
            GetGamesResponse getGamesResponse = getGamesService.getGames(authToken);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(getGamesResponse));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.contentType("application/json");
            ctx.result(gson.toJson( new ErrorResponse( e.getMessage() )));
        } catch (Exception e) {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }

}
