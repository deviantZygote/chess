package chess;

import javax.swing.text.Position;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return getTeamTurn() == chessGame.getTeamTurn() && Objects.equals(getBoard(), chessGame.getBoard());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTeamTurn(), getBoard());
    }

    public ChessGame() {
        this.setTeamTurn(TeamColor.WHITE);
        this.setBoard(new ChessBoard());
        board.resetBoard();
    }


    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ArrayList<ChessMove> validMoves = new ArrayList<>();
        ArrayList<ChessMove> pieceMoves = new ArrayList<>();

        ChessPiece piece;

        // getPiece
        piece = this.board.getPiece(startPosition);
        pieceMoves.addAll(piece.pieceMoves(this.getBoard(), startPosition));

        for (ChessMove pieceMove : pieceMoves ) {
            if (validateMove(pieceMove, piece)) {
                validMoves.add(pieceMove);
            }
        }

        return validMoves;
    }

    private boolean validateMove (ChessMove pieceMove, ChessPiece piece) {
        ChessPiece.PieceType pieceType = piece.getPieceType();
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        ChessPiece targetPiece = this.board.getPiece(pieceMove.getEndPosition());
        TeamColor targetPieceColor = null;

        if (targetPiece != null) {
            targetPieceColor = targetPiece.getTeamColor();
        }

        if (putsPlayerInCheck(pieceMove)) {
            return false;
        } else if (targetPieceColor != null && pieceColor == targetPieceColor) {
            return false;
        }  else if ( pieceColor != getTeamTurn()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean putsPlayerInCheck (ChessMove pieceMove) {
        // store piece at end position
        PositionAndPiece targetPositionAndPiece = new PositionAndPiece(pieceMove.getEndPosition(), this.getBoard().getPiece(pieceMove.getEndPosition()));
        PositionAndPiece startPositionAndPiece = new PositionAndPiece(pieceMove.getStartPosition(), this.getBoard().getPiece(pieceMove.getStartPosition()));
        // make the move
        this.getBoard().addPiece(targetPositionAndPiece.pos(), startPositionAndPiece.piece());
        this.getBoard().addPiece(startPositionAndPiece.pos(), null);

        // see if king's in check

        if (isInCheck(startPositionAndPiece.piece().getTeamColor())) {
            this.getBoard().addPiece(targetPositionAndPiece.pos(), targetPositionAndPiece.piece());
            this.getBoard().addPiece(startPositionAndPiece.pos(), startPositionAndPiece.piece());
            return true;
        }

        // undo move
        this.getBoard().addPiece(targetPositionAndPiece.pos(), targetPositionAndPiece.piece());
        this.getBoard().addPiece(startPositionAndPiece.pos(), startPositionAndPiece.piece());

        return false;
    }



    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        PositionAndPiece startPieceAndPosition = new PositionAndPiece(move.getStartPosition(), this.getBoard().getPiece(move.getStartPosition()));
        if (startPieceAndPosition.piece() == null) {
            throw new InvalidMoveException("No piece on start position" + move);
        }


        if (validateMove(move, startPieceAndPosition.piece())) {
            // make the move
            this.getBoard().addPiece(move.getEndPosition(), startPieceAndPosition.piece());
        } else {
            throw new InvalidMoveException("Illegal move" + move);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ArrayList<PositionAndPiece> enemyTeamPiecesAndPositions = new ArrayList<>();
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        PositionAndPiece friendlyKing;
        boolean canCaptureFriendlyKing = false;

        enemyTeamPiecesAndPositions.addAll(getEnemyTeamPiecesAndPositions(teamColor));
        friendlyKing = getFriendlyKing(teamColor);

        canCaptureFriendlyKing = canCaptureFriendlyKing(enemyTeamPiecesAndPositions, friendlyKing);

        return canCaptureFriendlyKing;
    }

    private boolean canCaptureFriendlyKing(ArrayList<PositionAndPiece> enemyTeamPiecesAndPositions, PositionAndPiece friendlyKing) {
        ArrayList<ChessMove> enemyMoves = new ArrayList<>();
        for (PositionAndPiece enemyTeamPieceAndPosition : enemyTeamPiecesAndPositions) {
            enemyMoves.addAll(enemyTeamPieceAndPosition.piece().pieceMoves(this.getBoard(), enemyTeamPieceAndPosition.pos()));
            if (endPositionOnKing(enemyMoves, friendlyKing)) {
                return true;
            }
        }

        return false;
    }

    private boolean endPositionOnKing(ArrayList<ChessMove> enemyMoves, PositionAndPiece friendlyKing) {
        for (ChessMove enemyMove : enemyMoves) {
            if (enemyMove.getEndPosition().equals(friendlyKing.pos())) {
                return true;
            }
        }

        return false;
    }

    private PositionAndPiece getFriendlyKing (ChessGame.TeamColor teamColor) {
        PositionAndPiece friendlyKingPositionAndPiece = null;
        ChessPosition targetPosition;
        ChessPiece piece;
        ChessPiece.PieceType pieceType = null;
        TeamColor pieceColor = null;

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                targetPosition = new ChessPosition(i, j);
                if (this.board.getPiece(targetPosition) != null) {
                    piece = this.board.getPiece(targetPosition);
                    pieceType = piece.getPieceType();
                    pieceColor = piece.getTeamColor();
                }

                if (pieceType == ChessPiece.PieceType.KING && pieceColor == teamColor) {
                    friendlyKingPositionAndPiece = new PositionAndPiece(targetPosition, this.board.getPiece(targetPosition));
                    return friendlyKingPositionAndPiece;
                }
            }
        }

        return friendlyKingPositionAndPiece;
    }

    private ArrayList<PositionAndPiece> getEnemyTeamPiecesAndPositions(TeamColor pieceColor) {
        ArrayList<PositionAndPiece> enemyChessPiecesAndPositions = new ArrayList<>();
        ArrayList<PositionAndPiece> boardChessPiecesAndPositions = new ArrayList<>();

        boardChessPiecesAndPositions.addAll(getBoardPiecesAndPositions());

        for (PositionAndPiece positionAndPiece : boardChessPiecesAndPositions) {
            if (positionAndPiece.piece().getTeamColor() != pieceColor) {
                enemyChessPiecesAndPositions.add(positionAndPiece);
            }
        }

        return enemyChessPiecesAndPositions;
    }

    private ArrayList<PositionAndPiece> getFriendlyTeamPiecesAndPositions(TeamColor pieceColor) {
        ArrayList<PositionAndPiece> friendlyChessPiecesAndPositions = new ArrayList<>();
        ArrayList<PositionAndPiece> boardChessPiecesAndPositions = new ArrayList<>();

        boardChessPiecesAndPositions.addAll(getBoardPiecesAndPositions());

        for (PositionAndPiece positionAndPiece : boardChessPiecesAndPositions) {
            if (positionAndPiece.piece().getTeamColor() == pieceColor) {
                friendlyChessPiecesAndPositions.add(positionAndPiece);
            }
        }

        return friendlyChessPiecesAndPositions;
    }


    private ArrayList<PositionAndPiece> getBoardPiecesAndPositions() {
        ArrayList<PositionAndPiece> boardChessPiecesAndPositions = new ArrayList<>();
        ChessBoard board = this.getBoard();
        ChessPosition targetPosition;

        // get enemy team pieces from the board.
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                if (this.board.getPiece(new ChessPosition(i, j)) != null) {
                    targetPosition = new ChessPosition(i, j);
                    boardChessPiecesAndPositions.add(new PositionAndPiece(targetPosition, this.board.getPiece(targetPosition)));
                }
            }
        }
        return boardChessPiecesAndPositions;
    }

    public record PositionAndPiece(ChessPosition pos, ChessPiece piece) {}

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ArrayList<PositionAndPiece> friendlyPositionsAndPieces = new ArrayList<>();
        friendlyPositionsAndPieces.addAll(getFriendlyTeamPiecesAndPositions(teamColor));


        if (isInCheck(teamColor) && !legalMovesExist(friendlyPositionsAndPieces)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ArrayList<PositionAndPiece> friendlyPositionsAndPieces = new ArrayList<>();

        friendlyPositionsAndPieces.addAll(getFriendlyTeamPiecesAndPositions(teamColor));

        if (!legalMovesExist(friendlyPositionsAndPieces) && !isInCheck(teamColor) ) {
            return true;
        } else {
            return false;
        }
    }

    private boolean legalMovesExist(ArrayList<PositionAndPiece> friendlyPositionsAndPieces) {
        ArrayList<ChessMove> validMoves = new ArrayList<>();

        for (PositionAndPiece friendlyPositionAndPiece : friendlyPositionsAndPieces) {
            validMoves.addAll(this.validMoves(friendlyPositionAndPiece.pos()));
        }

        if (validMoves.isEmpty()) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
