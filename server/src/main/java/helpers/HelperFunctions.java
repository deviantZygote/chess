package helpers;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.AlreadyTakenException;
import exceptions.BadRequestException;
import exceptions.UnauthorizedException;
import model.*;
import io.javalin.http.Context;
import com.google.gson.Gson;

public class HelperFunctions {
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static AuthData validateLogin(String authToken, DataAccess dataAccess) {
        try {
            if (isBlank(authToken)) {
                throw new UnauthorizedException("Error: unauthorized");
            }

            AuthData authData = dataAccess.getAuth(authToken);
            if (authData == null) {
                throw new UnauthorizedException("Error: unauthorized");
            } else {
                return authData;
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void catchHandlerExceptions(Exception e, Context ctx, Gson gson) {
        if (e instanceof BadRequestException) {
            ctx.status(400);
            ctx.contentType("application/json");
            ctx.result(gson.toJson( new ErrorResponse( e.getMessage() )));
        } else if (e instanceof com.google.gson.JsonSyntaxException) {
            ctx.status(400).contentType("application/json").result(gson.toJson(new ErrorResponse("Error: bad request")));
        } else if (e instanceof UnauthorizedException) {
            ctx.status(401);
            ctx.contentType("application/json");
            ctx.result(gson.toJson( new ErrorResponse( e.getMessage() )));
        } else if (e instanceof AlreadyTakenException) {
            ctx.status(403);
            ctx.contentType("application/json");
            ctx.result(gson.toJson( new ErrorResponse( e.getMessage() )));
        } else {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }
}
