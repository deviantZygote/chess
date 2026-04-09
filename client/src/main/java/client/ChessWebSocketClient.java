package client;

import jakarta.websocket.*;
import model.*;

import com.google.gson.Gson;
import ui.Menu;
import websocket.commands.ConnectCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

@ClientEndpoint
public class ChessWebSocketClient {

    private Session session;
    private String authToken = "";
    private int gameID = -1;
    private Gson gson = null;
    Menu menu = null;

    public ChessWebSocketClient(ConnectCommand connectCommand, Menu menu) {
        setGameID(connectCommand.getGameID());
        setAuthToken(connectCommand.getAuthToken());
        this.gson = new Gson();
        this.menu = menu;
    }

    public String getAuthToken() {
        return this.authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public int getGameID() {
        return this.gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;

        ConnectCommand connectCommand = new ConnectCommand(
                this.getAuthToken(),
                this.getGameID()
        );

        send(connectCommand);
    }

    @OnMessage
    public void onMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

        if (serverMessage == null || serverMessage.getServerMessageType() == null) {
            menu.printAsyncMessage(message);
            return;
        }

        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameMessage loadGameMessage = gson.fromJson(message, LoadGameMessage.class);
                menu.updateGame(loadGameMessage.getGame());
                break;
            case ERROR:
                ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
                menu.printAsyncMessage(errorMessage.getErrorMessage());
                if (errorMessage.getErrorMessage().contains("Unauthorized")) {
                    menu.handleUnauthorized();
                }
                break;
            case NOTIFICATION:
                NotificationMessage notificationMessage = gson.fromJson(message, NotificationMessage.class);
                menu.printAsyncMessage(notificationMessage.getMessage());
                break;
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        menu.closeSocket("\n\nLost connection to game: " + reason + "\n");
    }

    public void send(Object o) {
        try {
            session.getBasicRemote().sendText(gson.toJson(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
