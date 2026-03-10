package model;

import chess.ChessGame;

import java.util.Objects;

public class GameData {
    private int gameID;
    private String gameName;
    private String whiteUsername;
    private String blackUsername;
    private ChessGame chessGame;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameData gameData = (GameData) o;
        return getGameID() == gameData.getGameID() &&
                Objects.equals(getGameName(), gameData.getGameName()) &&
                Objects.equals(getWhiteUsername(), gameData.getWhiteUsername()) &&
                Objects.equals(getBlackUsername(), gameData.getBlackUsername()) &&
                Objects.equals(getChessGame(), gameData.getChessGame());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGameID(), getGameName(), getWhiteUsername(), getBlackUsername(), getChessGame());
    }

    public GameData(int gameID, String gameName, String whiteUsername, String blackUsername, ChessGame chessGame) {
        setGameID(gameID);
        setGameName(gameName);
        setWhiteUsername(whiteUsername);
        setBlackUsername(blackUsername);
        setChessGame(chessGame);
    }

    public int getGameID() {
        return this.gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

    public ChessGame getChessGame() {
        return this.chessGame;
    }

    public void setChessGame(ChessGame chessGame) { this.chessGame = chessGame;}

}
