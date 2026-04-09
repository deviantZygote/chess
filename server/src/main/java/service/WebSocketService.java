package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.UnauthorizedException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.*;

import java.util.Collection;
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

    private void sendNotification(WsContext ctx, String message) {
        NotificationWS notificationWS =
                new NotificationWS(WSCommands.NOTIFICATION, message);
        ctx.send(gson.toJson(notificationWS));
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
            case WATCH:
                watchGameWs(ctx, json);
                break;
            case MAKE_MOVE:
                makeMoveWS(ctx, json);
                break;
        }
    }

    private void makeMoveWS(WsMessageContext ctx, String json) {
        MakeMoveWS makeMoveWS = gson.fromJson(json, MakeMoveWS.class);
        AuthData authData = getAuthData(ctx, makeMoveWS.authToken());

        if (authData == null) {
            ctx.closeSession();
            return;
        }

        GameData gameData;
        gameData = getGameData(ctx, makeMoveWS.gameID());
        if (gameData == null) {
            ctx.closeSession();
            return;
        }

        // check observer
        PlayerRole playerRole = getPlayerRole(gameData, authData.username());
        if (playerRole == PlayerRole.OBSERVER) {
            sendNotification(ctx,"Observer's can't make moves");
            return;
        }

        // reject if there aren't two players in the game
        if (gameData.getBlackUsername() == null || gameData.getWhiteUsername() == null) {
            sendNotification(ctx,"You need two players to start the game");
            return;
        }

        // check turn against player color
        if ((gameData.getChessGame().getTeamTurn() == ChessGame.TeamColor.WHITE) &&
                gameData.getBlackUsername().equals(authData.username())) {
            sendNotification(ctx,"It's not your turn");
            return;
        }

        if ((gameData.getChessGame().getTeamTurn() == ChessGame.TeamColor.BLACK) &&
                gameData.getWhiteUsername().equals(authData.username())) {
            sendNotification(ctx,"It's not your turn");
            return;
        }


        Collection<ChessMove> chessMoves =
                gameData.getChessGame().validMoves(makeMoveWS.move().getStartPosition());

        if (chessMoves == null || chessMoves.isEmpty()) {
            sendNotification(ctx,"Not a valid move");
            return;
        }

        if (chessMoves.contains(makeMoveWS.move())) {
            // make chess move on gameData object
            try {
                gameData.getChessGame().makeMove(makeMoveWS.move());
                dataAccess.updateGame(gameData);
            } catch (InvalidMoveException e) {
                sendNotification(ctx,"Invalid move");
                return;
            } catch (DataAccessException e) {
                sendNotification(ctx,"Error: Internal Server Error");
                return;
            }

            String moveMessage = authData.username() + " moved from " +
                    convertChessPositionToString(makeMoveWS.move().getStartPosition()) +
                    " to " +
                    convertChessPositionToString(makeMoveWS.move().getEndPosition());

            broadcastToGame(gameData.getGameID(), moveMessage, null);
            broadcastGameState(gameData.getGameID(), gameData.getChessGame());
        }

    }

    private void broadcastGameState(int gameID, ChessGame chessGame) {
        Set<WsContext> sockets = gameConnections.get(gameID);

        if (sockets == null || sockets.isEmpty()) {
            return;
        }

        LoadGameWS loadGameWS = new LoadGameWS(WSCommands.LOAD_GAME, chessGame);
        String json = gson.toJson(loadGameWS);

        for (WsContext socket : sockets) {
            socket.send(json);
        }
    }

    private String convertChessPositionToString(ChessPosition position) {
        if (position == null) {
            return null;
        }

        int row = position.getRow();       // 1–8
        int col = position.getColumn();    // 1–8

        // Validate just in case
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return null;
        }

        char file = (char) ('a' + col - 1);   // 1 → 'a', 8 → 'h'
        char rank = (char) ('0' + row);       // 1 → '1', 8 → '8'

        return "" + file + rank;
    }

    private void watchGameWs (WsMessageContext ctx, String json) {
        GameData gameData;

        WatchGameWS watchGameWS = gson.fromJson(json, WatchGameWS.class);
        AuthData authData = getAuthData(ctx, watchGameWS.authToken());

        if (authData == null) {
            ctx.closeSession();
            return;
        }

        gameData = getGameData(ctx, watchGameWS.gameID());
        if (gameData == null) {
            ctx.closeSession();
            return;
        }

        PlayerRole playerRole = getPlayerRole(gameData, authData.username());

        ConnectionData connectionData = new ConnectionData(authData.username(), watchGameWS.gameID(), playerRole);
        connections.put(ctx, connectionData);
        gameConnections
                .computeIfAbsent(watchGameWS.gameID(), k -> ConcurrentHashMap.newKeySet())
                .add(ctx);

        broadcastToGame(watchGameWS.gameID(), "\n\n" + authData.username() + " joined as observer\n\n", ctx);
        confirmJoinAsObvToPlayer(ctx, playerRole);
    }

    private void confirmJoinAsObvToPlayer (WsContext ctx, PlayerRole playerRole) {
        sendNotification(ctx,"\nYou joined the game as " + playerRole + "\n");
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
        sendNotification(ctx,"\nYou left the game\n");
    }

    private AuthData getAuthData (WsMessageContext ctx, String authToken) {
        try {
            return validateLogin(authToken, dataAccess);
        } catch (UnauthorizedException e) {
            sendNotification(ctx,"Error: Unauthorized");
            ctx.session.close();
            return null;
        }
    }

    private GameData getGameData (WsMessageContext ctx, int gameID) {
        GameData gameData = null;
        try {
            gameData = dataAccess.getGame(gameID);
        } catch (DataAccessException e) {
            sendNotification(ctx,"Error: Internal Server Error");
            ctx.session.close();
            return null;
        }

        if (gameData == null) {
            sendNotification(ctx,"Error: Bad Request");
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
        sendNotification(ctx,"\nYou joined the game as " + playerRole + "\n");
    }

    private void broadcastToGame(int gameID, String message, WsContext exclude) {
        Set<WsContext> sockets = gameConnections.get(gameID);

        if (sockets == null) return;

        for (WsContext socket : sockets) {
            if (socket != exclude) {
                sendNotification(socket, message);
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
