package icfpc.common;

/**
 * @author masata
 */
public enum Angle {
    CLOCK_0(2, 0, -1, 1),
    CLOCK_60(1, 1, -2, 0),
    CLOCK_120(-1, 1, -1, -1),
    CLOCK_180(-2, 0, 1, -1),
    CLOCK_240(-1, -1, 2, 0),
    CLOCK_300(1, -1, 1, 1);

    private int ax;
    private int ay;
    private int bx;
    private int by;

    Angle(final int ax, final int ay, final int bx, final int by) {
        this.ax = ax;
        this.ay = ay;
        this.bx = bx;
        this.by = by;
    }

    public Cell rotate(final Cell c) {
        int yy = c.y;
        int xx = (c.x + c.y) / 2;
        return new Cell(ax * xx + bx * yy, ay * xx + by * yy);
    }

    public Angle clock() {
        switch (this) {
            case CLOCK_0: return CLOCK_60;
            case CLOCK_60: return CLOCK_120;
            case CLOCK_120: return CLOCK_180;
            case CLOCK_180: return CLOCK_240;
            case CLOCK_240: return CLOCK_300;
            case CLOCK_300: return CLOCK_0;
        }
        throw new Error("WOW");
    }

    public Angle counterClock() {
        switch (this) {
            case CLOCK_0: return CLOCK_300;
            case CLOCK_60: return CLOCK_0;
            case CLOCK_120: return CLOCK_60;
            case CLOCK_180: return CLOCK_120;
            case CLOCK_240: return CLOCK_180;
            case CLOCK_300: return CLOCK_240;
        }
        throw new Error("WOW");
    }
}
