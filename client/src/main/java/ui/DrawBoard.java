package ui;

import chess.ChessGame;
import chess.ChessPosition;

import java.util.Set;

public class DrawBoard {
    private ChessGame chessGame;
    private ChessGame.TeamColor playerColor;
    private Menu menu;

    public Set<ChessPosition> getHighlightedSquares() {
        return highlightedSquares;
    }

    public void setHighlightedSquares(Set<ChessPosition> highlightedSquares) {
        this.highlightedSquares = highlightedSquares;
    }

    Set<ChessPosition> highlightedSquares;


    public ChessGame getChessGame() {
        return chessGame;
    }

    public void setChessGame(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(ChessGame.TeamColor playerColor) {
        this.playerColor = playerColor;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public DrawBoard (ChessGame chessGame,
                      ChessGame.TeamColor playerColor,
                      Set<ChessPosition> highlightedSquares,
                      Menu menu) {

        if (chessGame == null) {
            menu.printToTerminal("\nNo game loaded!\n");
            return;
        }

        setMenu(menu);
        setChessGame(chessGame);
        setPlayerColor(playerColor);
        setHighlightedSquares(highlightedSquares);

        if (playerColor == ChessGame.TeamColor.WHITE) {
            drawWhiteOrientation();
        } else {
            drawBlackOrientation();
        }
    }

    private void drawWhiteOrientation() {
        var board = getChessGame().getBoard();
        System.out.print(EscapeSequences.ERASE_SCREEN);

        printColumnHeadersWhite();

        for (int row = 8; row >= 1; row--) {
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.print(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col);
            }
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.println(" " + row + " ");
        }

        printColumnHeadersWhite();
        resetColors();
    }

    private void drawBlackOrientation() {
        var board = getChessGame().getBoard();

        System.out.print(EscapeSequences.ERASE_SCREEN);

        printColumnHeadersBlack();

        for (int row = 1; row <= 8; row++) {
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.print(" " + row + " ");
            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col);
            }
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.println(" " + row + " ");
        }

        printColumnHeadersBlack();
        resetColors();
    }

    private void printSquare(chess.ChessBoard board, int row, int col) {
        ChessPosition currentPosition = new ChessPosition(row, col);
        boolean highlighted = getHighlightedSquares().contains(currentPosition);
        boolean lightSquare = (row + col) % 2 == 1;

        if (highlighted) {
            System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
        } else if (lightSquare) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        } else {
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
        }

        System.out.print(getPieceString(board, row, col));
    }

    private String getPieceString(chess.ChessBoard board, int row, int col) {
        chess.ChessPiece piece = board.getPiece(new chess.ChessPosition(row, col));

        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        String result = EscapeSequences.EMPTY;

        switch (piece.getTeamColor()) {
            case WHITE:
                switch (piece.getPieceType()) {
                    case KING:
                        result = EscapeSequences.WHITE_KING;
                        break;
                    case QUEEN:
                        result = EscapeSequences.WHITE_QUEEN;
                        break;
                    case BISHOP:
                        result = EscapeSequences.WHITE_BISHOP;
                        break;
                    case KNIGHT:
                        result = EscapeSequences.WHITE_KNIGHT;
                        break;
                    case ROOK:
                        result = EscapeSequences.WHITE_ROOK;
                        break;
                    case PAWN:
                        result = EscapeSequences.WHITE_PAWN;
                        break;
                    default:
                        result = EscapeSequences.EMPTY;
                        break;
                }
                break;

            case BLACK:
                switch (piece.getPieceType()) {
                    case KING:
                        result = EscapeSequences.BLACK_KING;
                        break;
                    case QUEEN:
                        result = EscapeSequences.BLACK_QUEEN;
                        break;
                    case BISHOP:
                        result = EscapeSequences.BLACK_BISHOP;
                        break;
                    case KNIGHT:
                        result = EscapeSequences.BLACK_KNIGHT;
                        break;
                    case ROOK:
                        result = EscapeSequences.BLACK_ROOK;
                        break;
                    case PAWN:
                        result = EscapeSequences.BLACK_PAWN;
                        break;
                    default:
                        result = EscapeSequences.EMPTY;
                        break;
                }
                break;

            default:
                result = EscapeSequences.EMPTY;
                break;
        }

        return result;
    }

    private void printColumnHeadersWhite() {
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
        System.out.print("   ");
        System.out.print(" a ");
        System.out.print(" b ");
        System.out.print(" c ");
        System.out.print(" d ");
        System.out.print(" e ");
        System.out.print(" f ");
        System.out.print(" g ");
        System.out.print(" h ");
        System.out.println();
    }

    private void printColumnHeadersBlack() {
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
        System.out.print("   ");
        System.out.print(" h ");
        System.out.print(" g ");
        System.out.print(" f ");
        System.out.print(" e ");
        System.out.print(" d ");
        System.out.print(" c ");
        System.out.print(" b ");
        System.out.print(" a ");
        System.out.println();
    }

    private void resetColors() {
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }
}
