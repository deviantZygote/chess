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
        ArrayList<ChessPosition> possibleEndPositions = new ArrayList<>();

        possibleEndPositions = getPossibleEndPositions(board, myPosition);

        legalMoves = buildMoves(myPosition, possibleEndPositions);

        return legalMoves;
    }

    private ArrayList<ChessPosition> getPossibleEndPositions (ChessBoard board, ChessPosition startingPosition) {
       ArrayList<ChessPosition> possibleEndPositions = new ArrayList<>();

        switch (this.pieceType) {
            case KING -> possibleEndPositions = getKingPossibleEndPositions(board, startingPosition);
            case QUEEN -> possibleEndPositions = getQueenPossiblePositions(board, startingPosition);
            case BISHOP -> possibleEndPositions = getBishopPossiblePositions(board, startingPosition);
            case KNIGHT -> possibleEndPositions = getKnightPossiblePositions(board, startingPosition);
            case ROOK -> possibleEndPositions = getRookPossiblePositions(board, startingPosition);
            case PAWN -> possibleEndPositions = getPawnPossiblePositions(board, startingPosition);
        }

        return possibleEndPositions;
    }

    private ArrayList<ChessPosition> getPawnPossiblePositions (ChessBoard board, ChessPosition startingPosition) {
        ArrayList<ChessPosition> possiblePositions = new ArrayList<>();

        // diag captures
        possiblePositions.addAll(getDiagCapturePositions(board, startingPosition));

        // forward 1
        possiblePositions.addAll(getOneForwardPosition(board, startingPosition));

        // forward 2
        possiblePositions.addAll(getTwoForwardPosition(board, startingPosition));

        return possiblePositions;
    }

    private ArrayList<ChessMove> buildMoves(ChessPosition startingPosition, ArrayList<ChessPosition> targetPositions) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        PieceType [] promoTypes = {PieceType.QUEEN, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};

        for (ChessPosition position : targetPositions) {
            if (this.pieceType == PieceType.PAWN && isEligibleForPromo(position)) {
                for (PieceType promoType : promoTypes) {
                    moves.add(new ChessMove(startingPosition, position, promoType));
                }
            } else {
                moves.add(new ChessMove(startingPosition, position, null));
            }
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

    private ArrayList<ChessPosition> getKingPossibleEndPositions (ChessBoard board, ChessPosition position) {
        ArrayList<ChessPosition> possiblePositions = new ArrayList<ChessPosition>();
        ArrayList<ChessPosition> legalPositions = new ArrayList<>();

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

        for (ChessPosition legalPosition : possiblePositions) {
            if (!board.isOutOfBounds(legalPosition) && isEmpty(board, legalPosition)) {
                legalPositions.add(legalPosition);
            } else if (!board.isOutOfBounds(legalPosition) && !doesColorMatch(board, legalPosition, this.getTeamColor())) {
                legalPositions.add(legalPosition);
            }
        }

        return legalPositions;
    }

    private ArrayList<ChessPosition> getDiagCapturePositions(ChessBoard board, ChessPosition position) {

        ArrayList<ChessPosition> positions = new ArrayList<>();

        ChessPosition diagLeft;
        ChessPosition diagRight;


        if (this.teamColor == ChessGame.TeamColor.WHITE) {
            diagLeft = new ChessPosition(position.getRow() + 1, position.getColumn() -1);
            positions.add(diagLeft);

            diagRight = new ChessPosition(position.getRow() + 1, position.getColumn() + 1);
            positions.add(diagRight);
        } else {
            diagLeft = new ChessPosition(position.getRow() - 1, position.getColumn() + 1);
            positions.add(diagLeft);

            diagRight = new ChessPosition(position.getRow() - 1, position.getColumn() - 1);
            positions.add(diagRight);
        }

        ArrayList<ChessPosition> legalPositions = new ArrayList<>();

        for (ChessPosition endPosition : positions) {
            if ( !board.isOutOfBounds(endPosition) && !isEmpty(board, endPosition) && !doesColorMatch(board, endPosition, this.teamColor) ) {
                legalPositions.add(endPosition);
            }
        }

        return legalPositions;

    }

    private ArrayList<ChessPosition> getOneForwardPosition(ChessBoard board, ChessPosition position) {
        ArrayList<ChessPosition> targetPositions = new ArrayList<ChessPosition>();
        ChessPosition targetPosition;

        if (this.teamColor == ChessGame.TeamColor.WHITE) {
            targetPosition = new ChessPosition(position.getRow() + 1, position.getColumn());
        } else {
            targetPosition = new ChessPosition(position.getRow() - 1, position.getColumn());
        }

        if ( isEmpty(board, targetPosition) ) {
            targetPositions.add(targetPosition);
        }
        return targetPositions;
    }

    private ArrayList<ChessPosition> getTwoForwardPosition(ChessBoard board, ChessPosition position) {
        ArrayList<ChessPosition> endingPositions = new ArrayList<>();
        ChessPosition endingPosition;
        ChessPosition middlePosition;

        // Moving 2 at start row
        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            // White Pawn
            if (position.getRow() == 2) {
                endingPosition = new ChessPosition(position.getRow() + 2, position.getColumn());
                middlePosition = new ChessPosition(position.getRow() + 1, position.getColumn());
                if ( isEmpty(board, endingPosition) && isEmpty(board, middlePosition) ){
                     endingPositions.add(endingPosition);
                }
            }
        } else if (this.getTeamColor() == ChessGame.TeamColor.BLACK){
            // Black Pawn
            if (position.getRow() == 7) {
                endingPosition = new ChessPosition(position.getRow() - 2, position.getColumn());
                middlePosition = new ChessPosition(position.getRow() - 1, position.getColumn());
                if ( isEmpty(board, endingPosition) && isEmpty(board, middlePosition) ){
                    endingPositions.add(endingPosition);
                }
            }
        }
        return endingPositions;
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
