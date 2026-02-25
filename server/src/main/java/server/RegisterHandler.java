package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.RegisterService;
import exceptions.*;

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
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.contentType("application/json");
            ctx.result(gson.toJson( new ErrorResponse( e.getMessage() )));
        } catch (com.google.gson.JsonSyntaxException e) {
            ctx.status(400).contentType("application/json").result(gson.toJson(new ErrorResponse("Error: bad request")));
        } catch (AlreadyTakenException e) {
            ctx.status(403);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse(e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }



    }
}
