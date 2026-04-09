package model;

import chess.ChessMove;

public record MakeMoveWS(
        UserGameCommand.CommandType commandType,
        String authToken,
        Integer gameID,
        ChessMove move
) {}
