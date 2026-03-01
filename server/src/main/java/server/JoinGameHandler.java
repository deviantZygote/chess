package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.JoinGameService;
import exceptions.*;
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
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.contentType("application/json");
            ctx.result(gson.toJson( new ErrorResponse( e.getMessage() )));
        } catch (com.google.gson.JsonSyntaxException e) {
            ctx.status(400).contentType("application/json").result(gson.toJson(new ErrorResponse("Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.contentType("application/json");
            ctx.result(gson.toJson( new ErrorResponse( e.getMessage() )));
        } catch (AlreadyTakenException e) {
            ctx.status(403);
            ctx.contentType("application/json");
            ctx.result(gson.toJson( new ErrorResponse( e.getMessage() )));
        } catch (Exception e) {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }


}
