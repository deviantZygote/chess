package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.RegisterService;
import exceptions.*;

import static helpers.HelperFunctions.catchHandlerExceptions;

public class RegisterHandler {
    private final Gson gson = new Gson();
    private final RegisterService registerService;

    public RegisterHandler(RegisterService registerService) {
        this.registerService = registerService;
    }


    public void handle(Context ctx) {
        try {
            RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
            if (req == null) {
                throw new BadRequestException("Error: bad request");
            }
            RegisterResponse res = registerService.register(req);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(res));
        }catch (Exception e) {
            catchHandlerExceptions(e, ctx, gson);
        }



    }
}
