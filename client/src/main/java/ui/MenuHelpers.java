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

    public void printHelp(STATE state, Menu menu) {
        menu.printToTerminal("\nAvailable Commands:\n");

        switch (state) {
            case LOGGED_OUT -> {
                menu.printToTerminal("help : h\n");
                menu.printToTerminal("quit : q\n");
                menu.printToTerminal("login : li\n");
                menu.printToTerminal("register : r\n");
            }
            case LOGGED_IN -> {
                menu.printToTerminal("help : h\n");
                menu.printToTerminal("logout : lo\n");
                menu.printToTerminal("create game : cg <gameName>\n");
                menu.printToTerminal("list games : lg\n");
                menu.printToTerminal("join game : jg <gameId> <color|c>\n");
                menu.printToTerminal("watch game : wg <gameId>\n");
            }
            case IN_GAME -> {
                menu.printToTerminal("show piece moves : spm <positionX> (ie. spm e4)\n");
                menu.printToTerminal("move piece : mp <positionX> <positionY> (ie. mp e4 e5)\n");
                menu.printToTerminal("show player turn : spt\n");
                menu.printToTerminal("draw board : db\n");
                menu.printToTerminal("leave match : lm\n");
                menu.printToTerminal("logout : lo\n");
            }
            case WATCH_GAME -> {
                menu.printToTerminal("draw board : db\n");
                menu.printToTerminal("show player turn : spt\n");
                menu.printToTerminal("show piece moves : spm <positionX> (ie. spm e4)\n");
                menu.printToTerminal("leave match : lm\n");
                menu.printToTerminal("logout : lo\n");
            }
        }
    }

    public ChessPosition convertStringToChessPosition(String input) {
        if (input == null) {
            return null;
        }

        input = input.trim().toLowerCase();

        if (input.length() != 2) {
            return null;
        }

        char file = input.charAt(0);
        char rank = input.charAt(1);

        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }

        int column = file - 'a' + 1;
        int row = rank - '0';

        return new ChessPosition(row, column);
    }

    public String gatherPassword(Menu menu) {
        String password = "";
        String confirmPassword = "";
        do {
            menu.printToTerminal("Enter a new password\n");
            password = menu.scanner.nextLine();
            menu.printToTerminal("re-enter your password\n");
            confirmPassword = menu.scanner.nextLine();

            if (!confirmPassword.equals(password) ||
                    password.isEmpty() ||
                    confirmPassword.isEmpty()) {
                menu.printToTerminal("Your passwords aren't matching or are empty.\nTry again.\n");
            }
        } while (!confirmPassword.equals(password) ||
                password.isEmpty() ||
                confirmPassword.isEmpty());

        return password;
    }

    public void printPromptText (STATE state) {
        switch (state) {
            case LOGGED_OUT:
                System.out.print("\nSTART MENU: ");
                break;
            case LOGGED_IN:
                System.out.print("\nGAME BROWSER: ");
                break;
            case IN_GAME:
                System.out.print("\nGAME MENU: ");
                break;
            case WATCH_GAME:
                System.out.print("\nOBSERVER MENU: ");
                break;
        }
    }
}
