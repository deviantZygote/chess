package chess;

import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board;

    public ChessBoard() {
     this.board = new ChessPiece[8][8];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true;}
        if (o == null) {return false;}
        if (this.getClass() != o.getClass()) {return false;}

        ChessBoard board = (ChessBoard) o;

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition position = new ChessPosition(i, j);
                if (Objects.equals(this.getPiece(position), board.getPiece(position)) ) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (ChessPiece[] row : this.board){
            for (ChessPiece piece : row) {
                hash = 37 * hash + (piece == null ? 0 : piece.hashCode() );
            }
        }
        return hash;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece)
    {
        int row = position.getRow();
        int col = position.getColumn();

        board[row - 1][col - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (this.isOutOfBounds(position)) {
            return null;
        }
        int row = position.getRow();
        int col = position.getColumn();
        return this.board[row-1][col-1];
    }

    public boolean isOutOfBounds(ChessPosition position) {
        if (position.getRow() > 8 || position.getRow() < 1 || position.getColumn() > 8 || position.getColumn() < 1) {
            return true;
        }
        return false;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.clearBoard();
        this.setUpWhite();
        this.setUpBlack();
    }

    /**
     * Resets each ChessPiece to null in the board
     */
    private void clearBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++){
                this.board[row][col] = null;
            }
        }
    }

    /**
     * Sets up all the black pieces
     */
    private void setUpBlack () {
        this.setUpBlackPawns();
        this.setUpBlackRooks();
        this.setUpBlackKnights();
        this.setUpBlackBishops();
        this.setUpBlackQueen();
        this.setUpBlackKing();
    }

    private void setUpBlackPawns() {
        int row = 7;
        ChessPiece piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        for (int i = 1; i < 9; i++) {
            ChessPosition position = new ChessPosition(row, i);
            this.addPiece(position, piece);
        }
    }

    private void setUpBlackRooks() {
        int row = 8;
        int [] cols = {1, 8};
        ChessPiece rook = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);

        for (int col : cols) {
            ChessPosition position = new ChessPosition(row, col);
            this.addPiece(position, rook);
        }
    }

    private void setUpBlackKnights() {
        int row = 8;
        int[] cols = {2, 7};
        ChessPiece knight = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);

        for (int col : cols) {
            ChessPosition position = new ChessPosition(row, col);
            this.addPiece(position, knight);
        }
    }

    private void setUpBlackBishops() {
        int row = 8;
        int [] cols = {3, 6};
        ChessPiece bishop = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);

        for (int col : cols) {
            ChessPosition position = new ChessPosition(row, col);
            this.addPiece(position, bishop);
        }
    }

    private void setUpBlackQueen() {
        int row = 8;
        int col = 4;
        ChessPiece queen = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        ChessPosition position = new ChessPosition(row, col);
        this.addPiece(position, queen);
    }

    private void setUpBlackKing() {
        int row = 8;
        int col = 5;
        ChessPiece king = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        ChessPosition position = new ChessPosition(row, col);
        this.addPiece(position, king);
    }

    /**
     * sets up all the white pieces
     */
    private void setUpWhite() {
        this.setUpWhitePawns();
        this.setUpWhiteRooks();
        this.setUpWhiteKnights();
        this.setUpWhiteBishops();
        this.setUpWhiteQueen();
        this.setUpWhiteKing();
    }

    private void setUpWhitePawns() {
        int row = 2;
        ChessPiece piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        for (int i = 1; i < 9; i++) {
            ChessPosition position = new ChessPosition(row, i);
            this.addPiece(position, piece);
        }
    }

    private void setUpWhiteRooks() {
        int row = 1;
        int [] cols = {1, 8};
        ChessPiece rook = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);

        for (int col : cols) {
            ChessPosition position = new ChessPosition(row, col);
            this.addPiece(position, rook);
        }
    }

    private void setUpWhiteBishops() {
        int row = 1;
        int [] cols = {3, 6};
        ChessPiece bishop = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);

        for (int col : cols) {
            ChessPosition position = new ChessPosition(row, col);
            this.addPiece(position, bishop);
        }
    }

    private void setUpWhiteKnights() {
        int row = 1;
        int[] cols = {2, 7};
        ChessPiece knight = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);

        for (int col : cols) {
            ChessPosition position = new ChessPosition(row, col);
            this.addPiece(position, knight);
        }
    }

    private void setUpWhiteQueen() {
        int row = 1;
        int col = 4;
        ChessPiece queen = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        ChessPosition position = new ChessPosition(row, col);
        this.addPiece(position, queen);
        }

    private void setUpWhiteKing() {
        int row = 1;
        int col = 5;
        ChessPiece king = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        ChessPosition position = new ChessPosition(row, col);
        this.addPiece(position, king);
    }
}
