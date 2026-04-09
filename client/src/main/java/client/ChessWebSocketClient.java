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
        BaseCommand baseCommand = gson.fromJson(message, BaseCommand.class);

        if (baseCommand == null || baseCommand.commandType() == null) {
            menu.printAsyncMessage(message);
            return;
        }

        switch (baseCommand.commandType()) {
            case LOAD_GAME:
                LoadGameWS loadGameWS = gson.fromJson(message, LoadGameWS.class);
                menu.updateGame(loadGameWS.chessGame());
                break;

            case NOTIFICATION:
                NotificationWS notificationWS = gson.fromJson(message, NotificationWS.class);
                menu.printAsyncMessage(notificationWS.message());

                if (notificationWS.message().contains("Unauthorized")) {
                    menu.handleUnauthorized();
                }
                break;

            default:
                menu.printAsyncMessage(message);
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
