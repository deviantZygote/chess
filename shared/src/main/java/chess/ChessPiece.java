package chess;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor teamColor;
    private ChessPiece.PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.setTeamColor(pieceColor);
        this.setPieceType(type);
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if ( this.getClass() != o.getClass()) {
            return false;
        }

        ChessPiece piece = (ChessPiece) o;

        return piece.getTeamColor() == this.getTeamColor() && piece.getPieceType() == this.getPieceType();
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 37 * hash + this.getPieceType().hashCode();
        hash = 37 * hash + this.getTeamColor().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "teamColor=" + teamColor +
                ", pieceType=" + pieceType +
                '}';
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.teamColor;
    }

    private void setTeamColor(ChessGame.TeamColor teamColor) {
        this.teamColor = teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    private void setPieceType(ChessPiece.PieceType pieceType) {
        this.pieceType = pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> legalMoves = new ArrayList<>();
        switch (this.getPieceType()) {
            case PieceType.PAWN:
                legalMoves = pawnMoves(board, myPosition);
                break;
            case PieceType.ROOK:
                // method to rook
                break;
            case PieceType.KNIGHT:
                // method to knight
                break;
            case PieceType.BISHOP:
                // method to bishop
                break;
            case PieceType.QUEEN:
                // method to queen
                break;
            case PieceType.KING:
                // method to king
                legalMoves = kingMoves(board, myPosition);
                break;
        }
        return legalMoves;
    }

    private ArrayList<ChessMove> kingMoves (ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();
        ChessMove chessMove;
        ChessPosition endPosition;

        // checking 1 up
        endPosition = new ChessPosition(position.getRow() + 1 , position.getColumn());
        chessMove = new ChessMove(position, endPosition, null);
        legalMoves.add(chessMove);

        return legalMoves;
    }

    private ArrayList<ChessMove> pawnMoves (ChessBoard board, ChessPosition position) {

        ArrayList<ChessMove> pawnMoves = new ArrayList<ChessMove>();
        ChessMove checkingMove;
        ChessPosition endingPosition;
        ChessPosition middlePosition;

        // Moving 2 at start row
        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            // White Pawn
            if (position.getRow() == 2) {
                endingPosition = new ChessPosition(position.getRow() + 2, position.getColumn());
                middlePosition = new ChessPosition(position.getRow() + 1, position.getColumn());
                checkingMove = new ChessMove(position, endingPosition, null);
                if ( isEmpty(board, endingPosition) && isEmpty(board, middlePosition) ){
                    pawnMoves.add(checkingMove);
                }
            }
        } else {
            // Black Pawn
            if (position.getRow() == 7) {
                endingPosition = new ChessPosition(position.getRow() - 2, position.getColumn());
                middlePosition = new ChessPosition(position.getRow() - 1, position.getColumn());
                checkingMove = new ChessMove(position, endingPosition, null);
                if ( isEmpty(board, endingPosition) && isEmpty(board, middlePosition) ){
                    pawnMoves.add(checkingMove);
                }
            }
        }
        return pawnMoves;
    }


    private boolean doesColorMatch(ChessBoard board, ChessPosition targetPosition, ChessGame.TeamColor teamColor) {
        ChessPiece targetPiece = board.getPiece(targetPosition);

        if (this.teamColor == targetPiece.teamColor) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isEmpty(ChessBoard board, ChessPosition targetPosition) {
        ChessPiece targetPiece = board.getPiece(targetPosition);
        if (targetPiece == null) {
            return true;
        } else {
            return false;
        }
    }
}
