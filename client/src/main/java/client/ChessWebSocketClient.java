package client;

import jakarta.websocket.*;
import model.JoinGameWS;

import com.google.gson.Gson;
import model.WSCommands;
import ui.Menu;

@ClientEndpoint
public class ChessWebSocketClient {

    private Session session;
    private String authToken = "";
    private int gameID = -1;
    private Gson gson = null;
    Menu menu = null;

    public ChessWebSocketClient (JoinGameWS joinGameWS, Menu menu) {
        setGameID(joinGameWS.gameID());
        setAuthToken(joinGameWS.authToken());
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
        JoinGameWS joinGameWS = new JoinGameWS(WSCommands.JOIN_GAME, this.getAuthToken(), getGameID());
        send(joinGameWS);
    }

    @OnMessage
    public void onMessage(String message) {
        menu.printAsyncMessage(message);
        if (message.contains("Unauthorized")) {
            menu.handleUnauthorized();
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
