package service;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.UnauthorizedException;
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
        if (baseCommand.commandType() == WSCommands.JOIN_GAME) {
            AuthData authData;
            GameData gameData;
            JoinGameWS joinGameWS = gson.fromJson(json, JoinGameWS.class);
            try {
                authData = validateLogin(joinGameWS.authToken(), dataAccess);
            } catch (UnauthorizedException e) {
                ctx.send("Error: Unauthorized");
                ctx.session.close();
                return;
            }

            try {
                gameData = dataAccess.getGame(joinGameWS.gameID());
            } catch (DataAccessException e) {
                ctx.send("Error: Internal Server Error");
                ctx.session.close();
                return;
            }

            if (gameData == null) {
                ctx.send("Error: Bad Request");
                ctx.session.close();
                return;
            }


            PlayerRole playerRole = getPlayerRole(gameData, authData.username());

            ConnectionData connectionData = new ConnectionData(authData.username(), joinGameWS.gameID(), playerRole);
            connections.put(ctx, connectionData);
            gameConnections
                    .computeIfAbsent(joinGameWS.gameID(), k -> ConcurrentHashMap.newKeySet())
                    .add(ctx);

            broadcastToGame(joinGameWS.gameID(), authData.username() + " joined as " + playerRole, ctx);
            confirmJoinToPlayer(ctx, playerRole);
        }
    }

    private void confirmJoinToPlayer (WsContext ctx, PlayerRole playerRole) {
        ctx.send("You joined the game as " + playerRole);
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
}
