package icfpc.common;

/**
 * @author masata
 */
public class OriginalCell {
    public static OriginalCell fromCell(final Cell c) {
        return new OriginalCell(c.x / 2, c.y);
    }

    public int x;
    public int y;

    OriginalCell() {
    }

    public OriginalCell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Cell toCell() {
        return Cell.fromOriginal(this);
    }
}
