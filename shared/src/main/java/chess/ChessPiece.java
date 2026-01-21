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
                legalMoves = rookMoves(board, myPosition);
                break;
            case PieceType.KNIGHT:
                // method to knight
                legalMoves = knightMoves(board, myPosition);
                break;
            case PieceType.BISHOP:
                // method to bishop
                legalMoves = bishopMoves(board, myPosition);
                break;
            case PieceType.QUEEN:
                // method to queen
                legalMoves = queenMoves(board, myPosition);
                break;
            case PieceType.KING:
                // method to king
                legalMoves = kingMoves(board, myPosition);
                break;
        }
        return legalMoves;
    }

    private ArrayList<ChessMove> knightMoves (ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> knightMoves = new ArrayList<ChessMove>();
        ArrayList<ChessPosition> possiblePositions = new ArrayList<>();

        possiblePositions = getKnightPossiblePositions(board, myPosition);
        knightMoves.addAll(getMoves(myPosition, possiblePositions, null));

        return knightMoves;
    }

    private ArrayList<ChessMove> getMoves(ChessPosition startingPosition, ArrayList<ChessPosition> targetPositions, ChessPiece.PieceType promoPiece) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        for (ChessPosition position : targetPositions) {
            moves.add(new ChessMove(startingPosition, position, promoPiece));
        }

        return moves;
    }

    private ArrayList<ChessPosition> getKnightPossiblePositions(ChessBoard board, ChessPosition startingPosition) {
        ArrayList<ChessPosition> possiblePositions = new ArrayList<>();
        ArrayList<ChessPosition> validatedPositions = new ArrayList<>();
        ChessPosition possiblePosition;

        direction [] upDown = {direction.UP, direction.DOWN};
        direction [] leftRight = {direction.LEFT, direction.RIGHT};

        for (direction upDown2Position : upDown) {
            for (direction leftRight1Position : leftRight) {
                if (upDown2Position == direction.UP && leftRight1Position == direction.LEFT) {
                    possiblePositions.add(possiblePosition = new ChessPosition(startingPosition.getRow() + 2, startingPosition.getColumn() - 1));
                } else if (upDown2Position == direction.UP && leftRight1Position == direction.RIGHT) {
                    possiblePositions.add(possiblePosition = new ChessPosition(startingPosition.getRow() + 2, startingPosition.getColumn() + 1));
                } else if (upDown2Position == direction.DOWN && leftRight1Position == direction.LEFT) {
                    possiblePositions.add(possiblePosition = new ChessPosition(startingPosition.getRow() - 2, startingPosition.getColumn() - 1));
                } else if (upDown2Position == direction.DOWN && leftRight1Position == direction.RIGHT) {
                    possiblePositions.add(possiblePosition = new ChessPosition(startingPosition.getRow() - 2, startingPosition.getColumn() + 1));
                }
            }
        }

        for (direction leftRight2 : leftRight) {
            for (direction upDown1 : upDown) {
                if (leftRight2 == direction.LEFT && upDown1 == direction.UP) {
                    possiblePositions.add(possiblePosition = new ChessPosition(startingPosition.getRow() + 1, startingPosition.getColumn() - 2));
                } else if (leftRight2 == direction.LEFT && upDown1 == direction.DOWN){
                    possiblePositions.add(possiblePosition = new ChessPosition(startingPosition.getRow() - 1, startingPosition.getColumn() - 2));
                } else if (leftRight2 == direction.RIGHT && upDown1 == direction.UP) {
                    possiblePositions.add(possiblePosition = new ChessPosition(startingPosition.getRow() + 1, startingPosition.getColumn() + 2));
                } else if (leftRight2 == direction.RIGHT && upDown1 == direction.DOWN) {
                    possiblePositions.add(possiblePosition = new ChessPosition(startingPosition.getRow() - 1, startingPosition.getColumn() + 2));
                }
            }
        }

        for (ChessPosition position : possiblePositions) {
            if (validatePosition(board, position)) {
                validatedPositions.add(position);
            }
        }

        return validatedPositions;
    }

    private boolean validatePosition(ChessBoard board, ChessPosition targetPosition) {
        if (isEmpty(board, targetPosition) && !board.isOutOfBounds(targetPosition)) {
            return true;
        }

        if (!isEmpty(board, targetPosition) && !board.isOutOfBounds(targetPosition) && !doesColorMatch(board, targetPosition, this.getTeamColor())) {
            return true;
        }

        return false;
    }

    private ArrayList<ChessMove> queenMoves (ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> queenMoves = new ArrayList<ChessMove>();
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();

        possiblePositions = getQueenPossiblePositions(board, position);
        queenMoves.addAll(getPossibleMoves(possiblePositions, position));

        return queenMoves;
    }

    private ArrayList<ChessPosition> getQueenPossiblePositions(ChessBoard board, ChessPosition position) {
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();

        possiblePositions.addAll(getBarMove(board, position, direction.UPLEFT));
        possiblePositions.addAll(getBarMove(board, position, direction.UP));
        possiblePositions.addAll(getBarMove(board, position, direction.UPRIGHT));
        possiblePositions.addAll(getBarMove(board, position, direction.RIGHT));
        possiblePositions.addAll(getBarMove(board, position, direction.DOWNRIGHT));
        possiblePositions.addAll(getBarMove(board, position, direction.DOWN));
        possiblePositions.addAll(getBarMove(board, position, direction.DOWNLEFT));
        possiblePositions.addAll(getBarMove(board, position, direction.LEFT));

        return possiblePositions;

    }

    private ArrayList<ChessMove> bishopMoves (ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> bishopMoves = new ArrayList<ChessMove>();
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();

        possiblePositions = getBishopPossiblePositions(board, position);
        bishopMoves.addAll(getPossibleMoves(possiblePositions, position));

        return bishopMoves;
    }

    private ArrayList<ChessMove> getPossibleMoves(ArrayList<ChessPosition> legalPositions, ChessPosition startingPosition) {
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();

        for(ChessPosition legalPosition : legalPositions) {
            legalMoves.add(new ChessMove(startingPosition, legalPosition, null));
        }

        return legalMoves;
    }

    private ArrayList<ChessMove> rookMoves (ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();
        possiblePositions = getRookPossiblePositions(board, position);
        legalMoves.addAll(getRookPossibleMoves(possiblePositions, position));

        return legalMoves;
    }

    private ArrayList<ChessMove> getRookPossibleMoves (ArrayList<ChessPosition> legalPositions, ChessPosition startingPostion) {
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();

        for (ChessPosition legalPosition : legalPositions) {
            legalMoves.add(new ChessMove(startingPostion, legalPosition, null));
        }

        return legalMoves;
    }

    private enum direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        UPLEFT,
        UPRIGHT,
        DOWNLEFT,
        DOWNRIGHT
    }

    private ArrayList<ChessPosition> getBishopPossiblePositions(ChessBoard board, ChessPosition position) {
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();

        // upleft
        possiblePositions.addAll(getBarMove(board, position, direction.UPLEFT));

        // upright
        possiblePositions.addAll(getBarMove(board, position, direction.UPRIGHT));

        // downleft
        possiblePositions.addAll(getBarMove(board, position, direction.DOWNLEFT));

        // downright
        possiblePositions.addAll(getBarMove(board, position, direction.DOWNRIGHT));


        return possiblePositions;
    }

    private ArrayList<ChessPosition> getRookPossiblePositions (ChessBoard board, ChessPosition position) {
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();

        // bar up
        possiblePositions.addAll(getBarMove(board, position, direction.UP));

        // bar down
        possiblePositions.addAll(getBarMove(board, position, direction.DOWN));

        // bar left
        possiblePositions.addAll(getBarMove(board, position, direction.LEFT));

        // bar right
        possiblePositions.addAll(getBarMove(board, position, direction.RIGHT));


        return possiblePositions;
    }

    private ArrayList<ChessPosition> getBarMove(ChessBoard board, ChessPosition position, ChessPiece.direction direction) {
        ArrayList<ChessPosition> legalEndPositions = new ArrayList<ChessPosition>();
        ChessPosition targetPosition;
        int row = position.getRow();
        int col = position.getColumn();
        int rowMod = 0;
        int colMod = 0;

        switch (direction) {
            case UP:
                rowMod = 1;
                colMod = 0;
                break;
            case DOWN:
                rowMod = -1;
                colMod = 0;
                break;
            case LEFT:
                rowMod = 0;
                colMod = -1;
                break;
            case RIGHT:
                rowMod = 0;
                colMod = 1;
                break;
            case UPLEFT:
                rowMod = 1;
                colMod = -1;
                break;
            case UPRIGHT:
                rowMod = 1;
                colMod = 1;
                break;
            case DOWNLEFT:
                rowMod = -1;
                colMod = -1;
                break;
            case DOWNRIGHT:
                rowMod = -1;
                colMod = 1;

        }

        while (true) {
            row += rowMod;
            col += colMod;
            targetPosition = new ChessPosition(row, col);


            // is the target position out of bounds
            if (board.isOutOfBounds(targetPosition)) {
                break;
            } else if (isEmpty(board, targetPosition)) {
                legalEndPositions.add(targetPosition);
            } else if (!isEmpty(board, targetPosition) && board.getPiece(targetPosition).getTeamColor() == this.getTeamColor() ) {
                break;
            } else {
                // if there is an enemy piece
                legalEndPositions.add(targetPosition);
                break;
            }
        }
        return legalEndPositions;
    }

    private ArrayList<ChessMove> kingMoves (ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();
        possiblePositions = getKingPossibleEndPositions(board, position);
        ChessMove possibleMove;
        for (ChessPosition possibleEndPosition : possiblePositions) {
            if (!board.isOutOfBounds(possibleEndPosition) && isEmpty(board, possibleEndPosition)) {
                possibleMove = new ChessMove(position, possibleEndPosition, null);
                legalMoves.add(possibleMove);
            } else if (!isEmpty(board, possibleEndPosition) && !doesColorMatch(board, possibleEndPosition, this.teamColor) && !board.isOutOfBounds(possibleEndPosition)) {
                possibleMove = new ChessMove(position, possibleEndPosition, null);
                legalMoves.add(possibleMove);
            }
        }
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

        // forward 1
        ArrayList<ChessMove> oneForwardMoves = oneForward(board, position);
        pawnMoves.addAll(oneForwardMoves);

        return pawnMoves;
    }

    private ArrayList<ChessPosition> getKingPossibleEndPositions (ChessBoard board, ChessPosition position) {
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();

        // up left
        possiblePositions.add(new ChessPosition(position.getRow() + 1, position.getColumn() -1));

        // up
        possiblePositions.add(new ChessPosition(position.getRow() + 1, position.getColumn()));

        // up right
        possiblePositions.add(new ChessPosition(position.getRow() + 1, position.getColumn() + 1));


        // right
        possiblePositions.add(new ChessPosition(position.getRow(), position.getColumn() + 1));


        // down right
        possiblePositions.add(new ChessPosition(position.getRow() -1, position.getColumn() + 1));

        // down
        possiblePositions.add(new ChessPosition(position.getRow() -1, position.getColumn()));

        // down left
        possiblePositions.add(new ChessPosition(position.getRow() -1 , position.getColumn() - 1));

        // left
        possiblePositions.add(new ChessPosition(position.getRow(), position.getColumn() - 1));

        return possiblePositions;
    }

    private ArrayList<ChessMove> diagCapture(ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> diagMoves = new ArrayList<ChessMove>();

        // going to rewrite this to return target positions depending on if it's black or white and then apply the logic checks
        ArrayList<ChessPosition> positions = getDiagPositions(position);

        for (ChessPosition diagPosition : positions) {
            if ( !board.isOutOfBounds(diagPosition) && !isEmpty(board, diagPosition) && !doesColorMatch(board, diagPosition, this.teamColor) ) {
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

    private ArrayList<ChessMove> oneForward(ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> targetMoves = new ArrayList<ChessMove>();
        ChessMove targetMove;
        ChessPosition targetPosition = getOneForwardPosition(position);

        if ( isEmpty(board, targetPosition) ) {
            if (!isEligibleForPromo(targetPosition)) {
                targetMove = new ChessMove(position, targetPosition, null);
                targetMoves.add(targetMove);
                return targetMoves;
            } else {
                ChessMove diagMoveQueenPromo = new ChessMove(position, targetPosition, PieceType.QUEEN);
                targetMoves.add(diagMoveQueenPromo);

                ChessMove diagMoveBishopPromo = new ChessMove(position, targetPosition, PieceType.BISHOP);
                targetMoves.add(diagMoveBishopPromo);

                ChessMove diagMoveKnightPromo = new ChessMove(position, targetPosition, PieceType.KNIGHT);
                targetMoves.add(diagMoveKnightPromo);

                ChessMove diagMoveRookPromo = new ChessMove(position, targetPosition, PieceType.ROOK);
                targetMoves.add(diagMoveRookPromo);

                return targetMoves;
            }
        }
        return targetMoves;
    }

    private ChessPosition getOneForwardPosition(ChessPosition position) {
        ChessPosition forwardPosition;
        if (this.teamColor == ChessGame.TeamColor.WHITE) {
            return forwardPosition = new ChessPosition(position.getRow() + 1, position.getColumn());
        } else {
            return forwardPosition = new ChessPosition(position.getRow() - 1, position.getColumn());

        }
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
