package client;

import jakarta.websocket.*;
import model.ConnectGameWS;
import model.JoinGameWS;

import com.google.gson.Gson;
import model.WSCommands;
import model.WatchGameWS;
import ui.Menu;

@ClientEndpoint
public class ChessWebSocketClient {

    private Session session;
    private String authToken = "";
    private int gameID = -1;
    private WSCommands joinRole = null;
    private Gson gson = null;
    Menu menu = null;

    public ChessWebSocketClient (ConnectGameWS connectGameWS, Menu menu) {
        setGameID(connectGameWS.gameID());
        setAuthToken(connectGameWS.authToken());
        setJoinRole(connectGameWS.commandType());
        this.gson = new Gson();
        this.menu = menu;
    }

    public void setJoinRole(WSCommands joinRole) {
        this.joinRole = joinRole;
    }

    public WSCommands getJoinRole () {
        return this.joinRole;
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
        if (getJoinRole() == WSCommands.JOIN_GAME) {
            JoinGameWS joinGameWS = new JoinGameWS(WSCommands.JOIN_GAME, this.getAuthToken(), getGameID());
            send(joinGameWS);
        } else /* watch */ {
            WatchGameWS watchGameWS = new WatchGameWS(WSCommands.WATCH, this.getAuthToken(), getGameID());
            send(watchGameWS);
        }
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
