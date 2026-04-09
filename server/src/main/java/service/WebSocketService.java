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
import websocket.commands.*;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static helpers.HelperFunctions.validateLogin;

public class WebSocketService {
    private final Gson gson = new Gson();
    private final DataAccess dataAccess;
    private final Map<WsContext, ConnectionData> connections = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Connection>> gameConnections = new ConcurrentHashMap<>();

    public WebSocketService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void handleCommand(WsMessageContext ctx) {
        String json = ctx.message();
        UserGameCommand command = gson.fromJson(json, UserGameCommand.class);

        if (command == null || command.getCommandType() == null) {
            sendError(ctx, "Error: bad request");
            return;
        }

        switch (command.getCommandType()) {
            case CONNECT:
                connectWS(ctx, json);
                break;
            case MAKE_MOVE:
                makeMoveWS(ctx, json);
                break;
            case LEAVE:
                leaveWS(ctx, json);
                break;
            case RESIGN:
                resignWS(ctx, json);
                break;
            default:
                sendError(ctx, "Error: bad request");
                break;
        }
    }

    private void sendNotification(WsContext ctx, String message) {
        NotificationMessage notificationMessage = new NotificationMessage(message);
        ctx.send(gson.toJson(notificationMessage));
    }

    private void sendLoadGame(WsContext ctx, ChessGame game) {
        LoadGameMessage loadGameMessage = new LoadGameMessage(game);
        ctx.send(gson.toJson(loadGameMessage));
    }

    private void sendError(WsContext ctx, String errorMessage) {
        ErrorMessage errorWS = new ErrorMessage(errorMessage);
        ctx.send(gson.toJson(errorWS));
    }

    private void connectWS(WsMessageContext ctx, String json) {
        UserGameCommand connectCommand = gson.fromJson(json, UserGameCommand.class);

        AuthData authData = getAuthData(ctx, connectCommand.getAuthToken());
        if (authData == null) {
            ctx.closeSession();
            return;
        }

        GameData gameData = getGameData(ctx, connectCommand.getGameID());
        if (gameData == null) {
            ctx.closeSession();
            return;
        }

        PlayerRole playerRole = getPlayerRole(gameData, authData.username());

        ConnectionData connectionData =
                new ConnectionData(authData.username(), connectCommand.getGameID(), playerRole);

        connections.put(ctx, connectionData);
        gameConnections
                .computeIfAbsent(connectCommand.getGameID(), k -> ConcurrentHashMap.newKeySet())
                .add(new Connection(authData.username(), ctx));

        sendLoadGame(ctx, gameData.getChessGame());
        broadcastToGame(
                connectCommand.getGameID(),
                authData.username() + " joined as " + playerRole,
                authData.username()
        );
    }

    private void leaveWS(WsMessageContext ctx, String json) {
        UserGameCommand leaveCommand = gson.fromJson(json, UserGameCommand.class);

        AuthData authData = getAuthData(ctx, leaveCommand.getAuthToken());
        if (authData == null) {
            ctx.closeSession();
            return;
        }

        GameData gameData = getGameData(ctx, leaveCommand.getGameID());
        if (gameData == null) {
            ctx.closeSession();
            return;
        }

        String username = authData.username();

        if (!gameData.getChessGame().isGameOver()) {
            String white = gameData.getWhiteUsername();
            String black = gameData.getBlackUsername();

            if (username.equals(white)) {
                white = null;
            } else if (username.equals(black)) {
                black = null;
            }

            GameData updatedGame = new GameData(
                    gameData.getGameID(),
                    gameData.getGameName(),
                    white,
                    black,
                    gameData.getChessGame()
            );

            try {
                dataAccess.updateGame(updatedGame);
            } catch (DataAccessException e) {
                sendError(ctx, "Error: internal server error");
                return;
            }
        }

        connections.remove(ctx);

        Set<Connection> gameSet = gameConnections.get(leaveCommand.getGameID());
        if (gameSet != null) {
            gameSet.removeIf(connection -> connection.ws.equals(ctx));

            if (gameSet.isEmpty()) {
                gameConnections.remove(leaveCommand.getGameID());
            }
        }

        broadcastToGame(
                leaveCommand.getGameID(),
                username + " left the game",
                username
        );

        ctx.closeSession();
    }

    private void resignWS(WsMessageContext ctx, String json) {
        UserGameCommand resignCommand = gson.fromJson(json, UserGameCommand.class);

        AuthData authData = getAuthData(ctx, resignCommand.getAuthToken());
        if (authData == null) {
            ctx.closeSession();
            return;
        }

        GameData gameData = getGameData(ctx, resignCommand.getGameID());
        if (gameData == null) {
            ctx.closeSession();
            return;
        }

        PlayerRole playerRole = getPlayerRole(gameData, authData.username());
        if (playerRole == PlayerRole.OBSERVER) {
            sendError(ctx, "Error: observers cannot resign");
            return;
        }

        if (gameData.getChessGame().isGameOver()) {
            sendError(ctx, "Error: game is over");
            return;
        }

        gameData.getChessGame().setGameOver(true);
        gameData.getChessGame().setResignedUsername(authData.username());

        try {
            dataAccess.updateGame(gameData);
        } catch (DataAccessException e) {
            sendError(ctx, "Error: internal server error");
            return;
        }

        broadcastToGame(
                resignCommand.getGameID(),
                authData.username() + " resigned",
                null
        );
    }

    private void makeMoveWS(WsMessageContext ctx, String json) {
        MakeMoveCommand makeMoveCommand = gson.fromJson(json, MakeMoveCommand.class);

        AuthData authData = getAuthData(ctx, makeMoveCommand.getAuthToken());
        if (authData == null) {
            ctx.closeSession();
            return;
        }

        GameData gameData = getGameData(ctx, makeMoveCommand.getGameID());
        if (gameData == null) {
            ctx.closeSession();
            return;
        } else if (gameData.getChessGame().isGameOver()) {
            sendError(ctx, "Error: game is over");
            return;
        }

        PlayerRole playerRole = getPlayerRole(gameData, authData.username());
        if (playerRole == PlayerRole.OBSERVER) {
            sendError(ctx, "Error: observers can't make moves");
            return;
        }

        if (gameData.getBlackUsername() == null || gameData.getWhiteUsername() == null) {
            sendError(ctx, "Error: two players are required to start the game");
            return;
        }

        if (gameData.getChessGame().getTeamTurn() == ChessGame.TeamColor.WHITE
                && gameData.getBlackUsername().equals(authData.username())) {
            sendError(ctx, "Error: it's not your turn");
            return;
        }

        if (gameData.getChessGame().getTeamTurn() == ChessGame.TeamColor.BLACK
                && gameData.getWhiteUsername().equals(authData.username())) {
            sendError(ctx, "Error: it's not your turn");
            return;
        }

        Collection<ChessMove> chessMoves =
                gameData.getChessGame().validMoves(makeMoveCommand.getMove().getStartPosition());

        if (chessMoves == null || chessMoves.isEmpty()) {
            sendError(ctx, "Error: invalid move");
            return;
        }

        if (!chessMoves.contains(makeMoveCommand.getMove())) {
            sendError(ctx, "Error: invalid move");
            return;
        }

        String moveMessage = authData.username() + " moved from "
                + convertChessPositionToString(makeMoveCommand.getMove().getStartPosition())
                + " to "
                + convertChessPositionToString(makeMoveCommand.getMove().getEndPosition());

        if (makeMoveCommand.getMove().getPromotionPiece() != null) {
            moveMessage += " and promoted to " + makeMoveCommand.getMove().getPromotionPiece();
        }

        try {
            gameData.getChessGame().makeMove(makeMoveCommand.getMove());

            ChessGame.TeamColor currentTurn = gameData.getChessGame().getTeamTurn();

            if (gameData.getChessGame().isInCheckmate(currentTurn)) {
                gameData.getChessGame().setGameOver(true);
            }

            dataAccess.updateGame(gameData);
        } catch (InvalidMoveException e) {
            sendError(ctx, "Error: invalid move");
            return;
        } catch (DataAccessException e) {
            sendError(ctx, "Error: internal server error");
            return;
        }

        ChessGame.TeamColor currentTurn = gameData.getChessGame().getTeamTurn();

        broadcastGameState(gameData.getGameID(), gameData.getChessGame());
        broadcastToGame(gameData.getGameID(), moveMessage, authData.username());

        if (gameData.getChessGame().isInCheckmate(currentTurn)) {
            String checkmateUser = "";
            if (currentTurn == ChessGame.TeamColor.WHITE) {
                checkmateUser = gameData.getBlackUsername();
            } else {
                checkmateUser = gameData.getWhiteUsername();
            }
            broadcastToGame(
                    gameData.getGameID(),
                    checkmateUser + " is in checkmate",
                    null
            );
        } else if (gameData.getChessGame().isInCheck(currentTurn)) {
            String checkUser = "";
            if (currentTurn == ChessGame.TeamColor.WHITE) {
                checkUser = gameData.getBlackUsername();
            } else {
                checkUser = gameData.getWhiteUsername();
            }
            broadcastToGame(
                    gameData.getGameID(),
                    checkUser + " is in check",
                    null
            );
        }
    }

    private void broadcastGameState(int gameID, ChessGame game) {
        Set<Connection> connections = gameConnections.get(gameID);
        if (connections == null || connections.isEmpty()) {
            return;
        }

        LoadGameMessage loadGameMessage = new LoadGameMessage(game);

        String json = gson.toJson(loadGameMessage);

        for (Connection connection : connections) {
            connection.ws.send(json);
        }
    }

    private void broadcastToGame(int gameID, String message, String excludeUsername) {
        Set<Connection> connections = gameConnections.get(gameID);

        if (connections == null) {
            return;
        }

        for (Connection connection : connections) {
            if (!connection.username.equals(excludeUsername)) {
                sendNotification(connection.ws, message);
            }
        }
    }

    private String convertChessPositionToString(ChessPosition position) {
        if (position == null) {
            return null;
        }

        int row = position.getRow();
        int col = position.getColumn();

        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return null;
        }

        char file = (char) ('a' + col - 1);
        char rank = (char) ('0' + row);

        return "" + file + rank;
    }

    private AuthData getAuthData(WsMessageContext ctx, String authToken) {
        try {
            return validateLogin(authToken, dataAccess);
        } catch (UnauthorizedException e) {
            sendError(ctx, "Error: unauthorized");
            ctx.session.close();
            return null;
        }
    }

    private GameData getGameData(WsMessageContext ctx, int gameID) {
        try {
            GameData gameData = dataAccess.getGame(gameID);

            if (gameData == null) {
                sendError(ctx, "Error: bad request");
                ctx.session.close();
                return null;
            }

            return gameData;
        } catch (DataAccessException e) {
            sendError(ctx, "Error: internal server error");
            ctx.session.close();
            return null;
        }
    }

    private PlayerRole getPlayerRole(GameData gameData, String username) {
        if (gameData.getWhiteUsername() != null
                && gameData.getWhiteUsername().equals(username)) {
            return PlayerRole.WHITE;
        } else if (gameData.getBlackUsername() != null
                && gameData.getBlackUsername().equals(username)) {
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

        Set<Connection> gameSet = gameConnections.get(gameID);
        if (gameSet != null) {
            gameSet.removeIf(connection -> connection.ws.equals(ctx));

            if (gameSet.isEmpty()) {
                gameConnections.remove(gameID);
            }
        }
    }
}