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

        // jump 2 forward when empty in front
        ChessMove twoForward = getTwoForwardPawnMove(board, position);
        if (twoForward != null) {
            pawnMoves.add(twoForward);
        }

        // diagonal capture
        ArrayList<ChessMove> diagCaptureMoves = diagCapture(board, position);
        pawnMoves.addAll(diagCaptureMoves);

            // check for promotion
        // forward 1
            // check for promotion

        return pawnMoves;
    }

    private ArrayList<ChessMove> diagCapture(ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> diagMoves = new ArrayList<ChessMove>();

        // going to rewrite this to return target positions depending on if it's black or white and then apply the logic checks
        ArrayList<ChessPosition> positions = getDiagPositions(position);

        for (ChessPosition diagPosition : positions) {
            if ( !isOutOfBounds(diagPosition) && !isEmpty(board, diagPosition) && !doesColorMatch(board, diagPosition, this.teamColor) ) {
                if (isEligibleForPromo(diagPosition)) {
                    // create 4 moves with different promotion options
                    ChessMove diagMoveQueenPromo = new ChessMove(position, diagPosition, PieceType.QUEEN);
                    diagMoves.add(diagMoveQueenPromo);

                    ChessMove diagMoveBishopPromo = new ChessMove(position, diagPosition, PieceType.BISHOP);
                    diagMoves.add(diagMoveBishopPromo);

                    ChessMove diagMoveKnightPromo = new ChessMove(position, diagPosition, PieceType.KNIGHT);
                    diagMoves.add(diagMoveKnightPromo);

                    ChessMove diagMoveRookPromo = new ChessMove(position, diagPosition, PieceType.ROOK);
                    diagMoves.add(diagMoveRookPromo);

                } else {
                    ChessMove diagMove = new ChessMove(position, diagPosition, null);
                    diagMoves.add(diagMove);
                }
            }
        }

        return diagMoves;

    }

    private ChessMove getTwoForwardPawnMove(ChessBoard board, ChessPosition position) {
        ChessMove targetMove;
        ChessPosition endingPosition;
        ChessPosition middlePosition;

        // Moving 2 at start row
        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            // White Pawn
            if (position.getRow() == 2) {
                endingPosition = new ChessPosition(position.getRow() + 2, position.getColumn());
                middlePosition = new ChessPosition(position.getRow() + 1, position.getColumn());
                targetMove = new ChessMove(position, endingPosition, null);
                if ( isEmpty(board, endingPosition) && isEmpty(board, middlePosition) ){
                    return targetMove;
                }
            }
        } else if (this.getTeamColor() == ChessGame.TeamColor.BLACK){
            // Black Pawn
            if (position.getRow() == 7) {
                endingPosition = new ChessPosition(position.getRow() - 2, position.getColumn());
                middlePosition = new ChessPosition(position.getRow() - 1, position.getColumn());
                targetMove = new ChessMove(position, endingPosition, null);
                if ( isEmpty(board, endingPosition) && isEmpty(board, middlePosition) ){
                    return targetMove;
                }
            }
        } else {
            return null;
        }
        return null;
    }

    private ArrayList<ChessPosition> getDiagPositions (ChessPosition position) {
        ArrayList<ChessPosition> diagPositions = new ArrayList<ChessPosition>();
        ChessPosition diagLeft;
        ChessPosition diagRight;
        if (this.teamColor == ChessGame.TeamColor.WHITE) {
            diagLeft = new ChessPosition(position.getRow() + 1, position.getColumn() -1);
            diagPositions.add(diagLeft);

            diagRight = new ChessPosition(position.getRow() + 1, position.getColumn() + 1);
            diagPositions.add(diagRight);
            return diagPositions;
        } else {
            diagLeft = new ChessPosition(position.getRow() - 1, position.getColumn() + 1);
            diagPositions.add(diagLeft);

            diagRight = new ChessPosition(position.getRow() - 1, position.getColumn() - 1);
            diagPositions.add(diagRight);
            return diagPositions;
        }
    }

    private ArrayList<ChessMove> getPromoMoves (ChessMove move){
        ChessPiece.PieceType[] promoPieces = {PieceType.QUEEN, PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP};
        ArrayList<ChessMove> promoMoves = new ArrayList<>();
        for (PieceType pieceType : promoPieces) {
            promoMoves.add(new ChessMove(move.startPosition, move.endPosition, pieceType));
        }
        return promoMoves;
    }

    private boolean isEligibleForPromo(ChessPosition position) {
        if (this.teamColor == ChessGame.TeamColor.WHITE && position.getRow() == 8) {
            return true;
        } else if (this.teamColor == ChessGame.TeamColor.BLACK && position.getRow() == 1) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isOutOfBounds(ChessPosition position) {
        if (position.getRow() > 8 || position.getRow() < 1 || position.getColumn() > 8 || position.getColumn() < 1) {
           return true;
        }
        return false;
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
