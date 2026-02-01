package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor teamColor;
    private  PieceType pieceType;

    @Override
    public String toString() {
        return "ChessPiece{" +
                "teamColor=" + teamColor +
                ", pieceType=" + pieceType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return getTeamColor() == that.getTeamColor() && getPieceType() == that.getPieceType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTeamColor(), getPieceType());
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        setTeamColor(pieceColor);
        setPieceType(type);
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

    public void setTeamColor(ChessGame.TeamColor teamColor) {
        this.teamColor = teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    public void setPieceType(PieceType pieceType) {
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
        ArrayList<ChessMove> moves = new ArrayList<>();
        ArrayList<ChessPosition> endPositions = new ArrayList<>();


        // get endPositions
        switch (board.getPiece(myPosition).getPieceType()) {
            case QUEEN:
                endPositions.addAll(getQueenEndPositions(board, myPosition));
                break;
            case ROOK:
                endPositions.addAll(getRookEndPositions(board, myPosition));
                break;
            case BISHOP:
                endPositions.addAll(getBishopEndPositions(board, myPosition));
                break;
            case KING:
                endPositions.addAll(getKingEndPositions(board, myPosition));
                break;
            case KNIGHT:
                endPositions.addAll(getKnightEndPositions(board, myPosition));
                break;
            case PAWN:
                endPositions.addAll(getPawnEndPositions(board, myPosition));
                break;

        }

        // build moves
        moves.addAll(buildMoves(board, myPosition, endPositions));

        return moves;
    }

    private ArrayList<ChessPosition> getPawnEndPositions(ChessBoard board, ChessPosition startPos) {
        ArrayList<ChessPosition> pawnEndPositions = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPos);
        ChessGame.TeamColor color = piece.getTeamColor();


        // forward 1
        ChessPosition forward1;

        if (color == ChessGame.TeamColor.WHITE) {
            forward1 = new ChessPosition(startPos.getRow() + 1, startPos.getColumn());
        } else {
            forward1 = new ChessPosition(startPos.getRow() - 1, startPos.getColumn());
        }

        if (board.isEmptyAndInbounds(forward1)) {
            pawnEndPositions.add(forward1);
        }


        // forward 2
        ChessPosition forward2;
        int startRow;

        if (color == ChessGame.TeamColor.WHITE) {
            forward2 = new ChessPosition(startPos.getRow() + 2, startPos.getColumn());
            startRow = 2;
        } else {
            forward2 = new ChessPosition(startPos.getRow() - 2, startPos.getColumn());
            startRow = 7;
        }

        if (board.isEmptyAndInbounds(forward1) && board.isEmptyAndInbounds(forward2) && startPos.getRow() == startRow) {
            pawnEndPositions.add(forward2);
        }

        // capture
        ChessPosition diagLeft;
        ChessPosition diagRight;

        if (color == ChessGame.TeamColor.WHITE) {
            diagLeft = new ChessPosition(startPos.getRow() + 1, startPos.getColumn() -1);
            diagRight = new ChessPosition(startPos.getRow() + 1, startPos.getColumn() + 1);
        } else {
            diagLeft = new ChessPosition(startPos.getRow() - 1, startPos.getColumn() -1);
            diagRight = new ChessPosition(startPos.getRow() - 1, startPos.getColumn() + 1);
        }

        if (board.isCapturable(startPos, diagLeft)) {
            pawnEndPositions.add(diagLeft);
        }

        if (board.isCapturable(startPos, diagRight)) {
            pawnEndPositions.add(diagRight);
        }
        return pawnEndPositions;
    }

    private ArrayList<ChessPosition> getKnightEndPositions(ChessBoard board, ChessPosition startPos) {
        ArrayList<ChessPosition> knightEndPositions = new ArrayList<>();
        ArrayList<ChessPosition> knightPossibleEndPositions = new ArrayList<>();

        knightPossibleEndPositions.add(new ChessPosition(startPos.getRow() + 2, startPos.getColumn() + 1));
        knightPossibleEndPositions.add(new ChessPosition(startPos.getRow() + 2, startPos.getColumn() - 1));
        knightPossibleEndPositions.add(new ChessPosition(startPos.getRow() - 2, startPos.getColumn() + 1));
        knightPossibleEndPositions.add(new ChessPosition(startPos.getRow() - 2, startPos.getColumn() - 1));
        knightPossibleEndPositions.add(new ChessPosition(startPos.getRow() + 1, startPos.getColumn() + 2));
        knightPossibleEndPositions.add(new ChessPosition(startPos.getRow() - 1, startPos.getColumn() + 2));
        knightPossibleEndPositions.add(new ChessPosition(startPos.getRow() + 1, startPos.getColumn() - 2));
        knightPossibleEndPositions.add(new ChessPosition(startPos.getRow() - 1, startPos.getColumn() - 2));

        for (ChessPosition knightPossibleEndPosition : knightPossibleEndPositions) {
            if (board.isEmptyAndInbounds(knightPossibleEndPosition)) {
                knightEndPositions.add(knightPossibleEndPosition);
            } else if (board.isCapturable(startPos, knightPossibleEndPosition)) {
                knightEndPositions.add(knightPossibleEndPosition);
            }
        }

        return knightEndPositions;
    }

    private ArrayList<ChessPosition> getKingEndPositions(ChessBoard board, ChessPosition startPos) {
        ArrayList<ChessPosition> kingEndPositions = new ArrayList<>();
        ChessPosition targetPosition;

        directions[] directions = ChessPiece.directions.values();
        int [] mods;

        for (ChessPiece.directions direction : directions) {
            mods = getMods(direction);
            targetPosition = new ChessPosition(startPos.getRow() + mods[0], startPos.getColumn() + mods[1]);
            if (board.isEmptyAndInbounds(targetPosition)) {
                kingEndPositions.add(targetPosition);
            } else if (board.isCapturable(startPos, targetPosition)) {
                kingEndPositions.add(targetPosition);
            }
        }

        return kingEndPositions;
    }

    private ArrayList<ChessPosition> getBishopEndPositions(ChessBoard board, ChessPosition startPos) {
        ArrayList<ChessPosition> bishopEndPositions = new ArrayList<>();
        directions[] directions = new ChessPiece.directions [] {ChessPiece.directions.upLeft, ChessPiece.directions.upRight, ChessPiece.directions.downLeft, ChessPiece.directions.downRight};

        for (ChessPiece.directions direction : directions) {
            bishopEndPositions.addAll(getBarEndPositions(board, direction, startPos));
        }

        return bishopEndPositions;
    }

    private ArrayList<ChessPosition> getRookEndPositions(ChessBoard board, ChessPosition startPos) {
        ArrayList<ChessPosition> rookEndPositions = new ArrayList<>();
        directions[] directions = new ChessPiece.directions [] {ChessPiece.directions.up, ChessPiece.directions.down, ChessPiece.directions.left, ChessPiece.directions.right};

        for (ChessPiece.directions direction : directions) {
            rookEndPositions.addAll(getBarEndPositions(board, direction, startPos));
        }

        return rookEndPositions;
    }

    private ArrayList<ChessMove> buildMoves (ChessBoard board, ChessPosition startPos, ArrayList<ChessPosition> endPositions) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int endRow = 0;

        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            endRow = 8;
        } else {
            endRow = 1;
        }

        // check if is pawn and promotable
        for (ChessPosition endPosition : endPositions) {
            if (this.getPieceType() == PieceType.PAWN && endPosition.getRow() == endRow) {
                moves.add(new ChessMove(startPos, endPosition, PieceType.QUEEN));
                moves.add(new ChessMove(startPos, endPosition, PieceType.ROOK));
                moves.add(new ChessMove(startPos, endPosition, PieceType.BISHOP));
                moves.add(new ChessMove(startPos, endPosition, PieceType.KNIGHT));

            } else {
                moves.add(new ChessMove(startPos, endPosition, null));
            }
        }

        return moves;
    }

    private ArrayList<ChessPosition> getQueenEndPositions(ChessBoard board, ChessPosition startPos) {
        ArrayList<ChessPosition> queenEndPositions = new ArrayList<>();
        directions[] directions = ChessPiece.directions.values();

        for (ChessPiece.directions direction : directions) {
            queenEndPositions.addAll(getBarEndPositions(board, direction, startPos));
        }

        return queenEndPositions;
    }

    private ArrayList<ChessPosition> getBarEndPositions (ChessBoard board, directions direction, ChessPosition startPos) {
        ArrayList<ChessPosition> barEndPositions = new ArrayList<>();
        int [] mods = getMods(direction);

        int rowMod = mods[0];
        int colMod = mods[1];

        int row = 0;
        int col = 0;

        ChessPosition targetPosition;

        // start while loop

        while (true) {
            row += rowMod;
            col += colMod;

            targetPosition = new ChessPosition(startPos.getRow() + row, startPos.getColumn() + col);

            if (board.isEmptyAndInbounds(targetPosition)) {
                barEndPositions.add(targetPosition);
            } else if (board.isCapturable(startPos, targetPosition)) {
                barEndPositions.add(targetPosition);
                break;
            } else {
                break;
            }
        }



        return barEndPositions;
    }

    private int [] getMods (directions direction) {
        int [] mods = new int[2];

        int row = 0;
        int col = 0;

        switch (direction) {
            case up:
                row = 1;
                break;
            case down:
                row = -1;
                break;
            case left:
                col = -1;
                break;
            case right:
                col = 1;
                break;
            case upLeft:
                row = 1;
                col = -1;
                break;
            case upRight:
                row = 1;
                col = 1;
                break;
            case downLeft:
                row = -1;
                col = -1;
                break;
            case downRight:
                row = -1;
                col = 1;
                break;

        }

        mods[0] = row;
        mods[1] = col;

        return mods;
    }

    public enum directions {
        up,
        down,
        left,
        right,
        upLeft,
        upRight,
        downLeft,
        downRight
    }
}