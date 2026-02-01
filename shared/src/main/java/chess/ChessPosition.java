package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private int row;
    private int col;

    @Override
    public String toString() {
        return "ChessPosition{" +
                "row=" + row +
                ", col=" + col +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition position = (ChessPosition) o;
        return getRow() == position.getRow() && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRow(), col);
    }

    public ChessPosition(int row, int col) {
        setRow(row);
        setCol(col);
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {

        return this.col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
