package model;

import chess.ChessGame;

public record LoadGameWS(
        WSCommands commandType,
        ChessGame chessGame
) {}
