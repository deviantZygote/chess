package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.LoginService;
import exceptions.*;
import service.RegisterService;

public class LoginHandler {
    private final Gson gson = new Gson();
    private final LoginService loginService;

    public LoginHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    public void handle(Context ctx) {
        try {
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
            if (req == null) {
                throw new BadRequestException("Error: bad request");
            }
            LoginResponse res = loginService.login(req);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(res));
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
        } catch (Exception e) {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }



}
