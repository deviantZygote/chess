package model;

import chess.ChessMove;

public record MakeMoveWS(
        WSCommands commandType,
        String authToken,
        int gameID,
        ChessMove move
) {}
