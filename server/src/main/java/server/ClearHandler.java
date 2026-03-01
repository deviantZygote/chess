package server;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.*;
import service.ClearService;
import exceptions.*;

import static helpers.HelperFunctions.catchHandlerExceptions;

public class ClearHandler {
    private final Gson gson = new Gson();
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }


    public void handle(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
                } catch (Exception e) {
            catchHandlerExceptions(e, ctx, gson);
        }
    }
}
