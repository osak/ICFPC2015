package icfpc.common;

/**
 * @author masata
 */
public class Cell {
    public static Cell fromOriginal(final OriginalCell ori) {
        int x = ori.x * 2 + (ori.y % 2 == 0 ? 0 : 1);
        int y = ori.y;
        return new Cell(x, y);
    }

    public static Cell vector(final Cell start, final Cell end) {
        return new Cell(end.x - start.x, end.y - start.y);
    }

    public int x;
    public int y;

    public Cell(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Cell plusVector(final Cell v) {
        return new Cell(this.x + v.x, this.y +v.y);
    }

    public OriginalCell toOriginalCell() {
        return OriginalCell.fromCell(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Cell) {
            Cell c = (Cell)obj;
            return x == c.x && y == c.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return x ^ y;
    }
}
