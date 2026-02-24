package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.RegisterRequest;

public class RegisterHandler {
    private final Gson gson = new Gson();

    public void handle(Context ctx) {
        String body = ctx.body();
        RegisterRequest req = gson.fromJson(body, RegisterRequest.class);
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(req)); // echo back as JSON (for now)
    }
}
