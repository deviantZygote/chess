package service;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.UnauthorizedException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static helpers.HelperFunctions.validateLogin;

public class WebSocketService {
    private final Gson gson = new Gson();
    private final DataAccess dataAccess;
    Map<WsContext, ConnectionData> connections = new ConcurrentHashMap<>();;
    Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();;

    public WebSocketService (DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void handleCommand (WsMessageContext ctx) {
        String json = ctx.message();

        BaseCommand baseCommand = gson.fromJson(json, BaseCommand.class);

        switch (baseCommand.commandType()) {
            case WSCommands.JOIN_GAME:
                joinGameWS(ctx, json);
                break;
            case LEAVE_GAME:
                leaveGameWS(ctx, json);
                break;
        }
    }

    private void leaveGameWS (WsMessageContext ctx, String json) {
        GameData gameData;

        LeaveGameWS leaveGameWS = gson.fromJson(json, LeaveGameWS.class);
        AuthData authData = getAuthData(ctx, leaveGameWS.authToken());

        if (authData == null) {
            ctx.closeSession();
            return;
        }

        gameData = getGameData(ctx, leaveGameWS.gameID());
        if (gameData == null) {
            ctx.closeSession();
            return;
        }

        broadcastToGame(
                leaveGameWS.gameID(),
                "\n\n" + authData.username() + " left the game\n\n",
                ctx
        );

        confirmLeaveToPlayer(ctx);
        ctx.closeSession();
    }

    private void confirmLeaveToPlayer (WsContext ctx) {
        ctx.send("\nYou left the game\n");
    }

    private AuthData getAuthData (WsMessageContext ctx, String authToken) {
        try {
            return validateLogin(authToken, dataAccess);
        } catch (UnauthorizedException e) {
            ctx.send("Error: Unauthorized");
            ctx.session.close();
            return null;
        }
    }

    private GameData getGameData (WsMessageContext ctx, int gameID) {
        GameData gameData = null;
        try {
            gameData = dataAccess.getGame(gameID);
        } catch (DataAccessException e) {
            ctx.send("Error: Internal Server Error");
            ctx.session.close();
            return null;
        }

        if (gameData == null) {
            ctx.send("Error: Bad Request");
            ctx.session.close();
            return null;
        }
        return gameData;
    }

    private void joinGameWS (WsMessageContext ctx, String json) {
        GameData gameData;
        JoinGameWS joinGameWS = gson.fromJson(json, JoinGameWS.class);

        AuthData authData = getAuthData(ctx, joinGameWS.authToken());
        if (authData == null) {
            ctx.closeSession();
            return;
        }

        gameData = getGameData(ctx, joinGameWS.gameID());
        if (gameData == null) {
            ctx.closeSession();
            return;
        }

        PlayerRole playerRole = getPlayerRole(gameData, authData.username());

        ConnectionData connectionData = new ConnectionData(authData.username(), joinGameWS.gameID(), playerRole);
        connections.put(ctx, connectionData);
        gameConnections
                .computeIfAbsent(joinGameWS.gameID(), k -> ConcurrentHashMap.newKeySet())
                .add(ctx);

        broadcastToGame(joinGameWS.gameID(), "\n\n" + authData.username() + " joined as " + playerRole + "\n\n", ctx);
        confirmJoinToPlayer(ctx, playerRole);

    }

    private void confirmJoinToPlayer (WsContext ctx, PlayerRole playerRole) {
        ctx.send("\nYou joined the game as " + playerRole + "\n");
    }

    private void broadcastToGame(int gameID, String message, WsContext exclude) {
        Set<WsContext> sockets = gameConnections.get(gameID);

        if (sockets == null) return;

        for (WsContext socket : sockets) {
            if (socket != exclude) {
                socket.send(message);
            }
        }
    }

    private PlayerRole getPlayerRole (GameData gameData, String username) {
        if (!(gameData.getWhiteUsername() == null) && gameData.getWhiteUsername().equals(username)) {
            return PlayerRole.WHITE;
        } else if (!(gameData.getBlackUsername() == null) &&
                gameData.getBlackUsername().equals(username)) {
            return PlayerRole.BLACK;
        } else {
            return PlayerRole.OBSERVER;
        }
    }

    public void closeConnection(WsCloseContext ctx) {
        ConnectionData connectionData = connections.remove(ctx);

        if (connectionData == null) {
            return;
        }

        int gameID = connectionData.gameID();

        Set<WsContext> connections = gameConnections.get(gameID);
        if (connections != null) {
            connections.remove(ctx);

            if (connections.isEmpty()) {
                gameConnections.remove(gameID);
            }
        }
    }
}
