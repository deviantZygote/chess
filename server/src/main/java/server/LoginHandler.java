package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.LoginService;
import exceptions.*;

import static helpers.HelperFunctions.catchHandlerExceptions;

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
        } catch (Exception e) {
            catchHandlerExceptions(e, ctx, gson);
        }
    }



}
