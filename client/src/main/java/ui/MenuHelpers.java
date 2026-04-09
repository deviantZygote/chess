package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GetGamesResponse;

public class MenuHelpers {
    public boolean makeMoveDataValidation(
            ChessPosition startPos,
            ChessPosition endPos,
            ChessGame.TeamColor playerColor,
            ChessGame.TeamColor gameTurnColor,
            ChessPiece chessPiece,
            Menu menu
            ) {
        if (startPos == null || endPos == null) {
            menu.printToTerminal("\nInvalid coordinates.\n");
            return false;
        }

        if (playerColor == null) {
            menu.printToTerminal("\nObservers can't make moves.\n");
            return false;
        }

        if (playerColor != gameTurnColor) {
            menu.printToTerminal("\nIt's not your turn. Please wait.\n");
            return false;
        }

        if (chessPiece == null) {
            menu.printToTerminal("\nNo piece at that position.\n");
            return false;
        }

        if (chessPiece.getTeamColor() != playerColor) {
            menu.printToTerminal("\nYou can only move your own pieces.\n");
            return false;
        }
        return true;
    }

    public void matchAndGetGame (String stringGameId, GetGamesResponse gamesResponse, Menu menu) {
        int clientDisplayId = Integer.parseInt(stringGameId);

        if (gamesResponse == null) {
            menu.printToTerminal("\n\nFirst List the available games\n\nPress h for commands:\n\n");
            return;
        }
        try {
            menu.setTargetGameData(gamesResponse.games.get(clientDisplayId - 1));
            menu.setChessGame(menu.getTargetGameData().getChessGame());
        } catch (IndexOutOfBoundsException e) {
            menu.printToTerminal(String.format("\n\nCheck your game Number: %s\nPress h for commands:\n\n",
                    clientDisplayId));
        }
    }
}
