package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.RegisterService;

public class RegisterHandler {
    private final Gson gson = new Gson();
    private final RegisterService registerService;

    public RegisterHandler(RegisterService registerService) {
        this.registerService = registerService;
    }


    public void handle(Context ctx) {
        String body = ctx.body();
        RegisterRequest req = gson.fromJson(body, RegisterRequest.class);
        RegisterResponse res = registerService.register(req);


        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(res)); // echo back as JSON (for now)
    }
}
