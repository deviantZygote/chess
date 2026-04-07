package service;

import com.google.gson.Gson;
import model.BaseCommand;
import model.WSCommands;

public class WebSocketService {
    private final Gson gson = new Gson();

    public WebSocketService () {
    }

    // join game
    public void handleCommand (String json) {
        BaseCommand baseCommand = gson.fromJson(json, BaseCommand.class);
        if (baseCommand.commandType() == WSCommands.JOIN_GAME) {
            System.out.println("\n\nWe're joining a game\n\n");
        }
    }
}
