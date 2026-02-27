package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.LogoutService;
import exceptions.*;
import static helpers.HelperFunctions.isBlank;

public class LogoutHandler {
    private final Gson gson = new Gson();
    private final LogoutService logoutService;

    public LogoutHandler(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (isBlank(authToken)) {
                throw new UnauthorizedException("Error: unauthorized");
            }
            logoutService.logout(authToken);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
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
