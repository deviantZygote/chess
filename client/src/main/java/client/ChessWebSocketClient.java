package client;

import jakarta.websocket.*;
import model.JoinGameWS;

import com.google.gson.Gson;

@ClientEndpoint
public class ChessWebSocketClient {

    private Session session;
    private String authToken = "";
    private int gameID = -1;
    private Gson gson = null;

    public ChessWebSocketClient (JoinGameWS joinGameWS) {
        setGameID(joinGameWS.gameID());
        setAuthToken(joinGameWS.authToken());
        this.gson = new Gson();
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
        JoinGameWS joinGameWS = new JoinGameWS(getAuthToken(), getGameID());
        send(gson.toJson(joinGameWS));
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Message from server: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket Closed: " + reason);
    }

    public void send(String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
