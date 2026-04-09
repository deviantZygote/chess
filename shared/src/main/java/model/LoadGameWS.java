package model;

import chess.ChessGame;

public record LoadGameWS(
        ServerMessage.ServerMessageType serverMessageType,
        ChessGame game
) {}
