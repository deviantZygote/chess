package client;

import jakarta.websocket.*;
import model.*;

import com.google.gson.Gson;
import ui.Menu;

@ClientEndpoint
public class ChessWebSocketClient {

    private Session session;
    private String authToken = "";
    private int gameID = -1;
    private UserGameCommand.CommandType commandType;
    private Gson gson = null;
    Menu menu = null;

    public ChessWebSocketClient(ConnectWS connectWS, Menu menu) {
        setGameID(connectWS.gameID());
        setAuthToken(connectWS.authToken());
        setCommandType(connectWS.commandType());
        this.gson = new Gson();
        this.menu = menu;
    }

    public void setCommandType(UserGameCommand.CommandType commandType) {
        this.commandType = commandType;
    }

    public UserGameCommand.CommandType getCommandType () {
        return this.commandType;
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

        ConnectWS connectWS = new ConnectWS(
                this.getCommandType(),
                this.getAuthToken(),
                this.getGameID()
        );

        send(connectWS);
    }

    @OnMessage
    public void onMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

        if (serverMessage == null || serverMessage.serverMessageType() == null) {
            menu.printAsyncMessage(message);
            return;
        }

        switch (serverMessage.serverMessageType()) {
            case LOAD_GAME:
                LoadGameWS loadGameWS = gson.fromJson(message, LoadGameWS.class);
                menu.updateGame(loadGameWS.game());
                break;
            case ERROR:
                ErrorWS errorWS = gson.fromJson(message, ErrorWS.class);
                menu.printAsyncMessage(errorWS.errorMessage());
                if (errorWS.errorMessage().contains("Unauthorized")) {
                    menu.handleUnauthorized();
                }
                break;
            case NOTIFICATION:
                NotificationWS notificationWS = gson.fromJson(message, NotificationWS.class);
                menu.printAsyncMessage(notificationWS.message());
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
