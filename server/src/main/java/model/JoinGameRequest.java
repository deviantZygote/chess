package model;

import chess.ChessGame;

public class JoinGameRequest {
    public JoinGameRequest (ChessGame.TeamColor color, int gameID ) {
        this.playerColor = color;
        this.gameID = gameID;
    }
    public ChessGame.TeamColor playerColor;
    public int gameID;
}
