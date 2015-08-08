package icfpc.random;

/**
 * @author masata
 */
public class Randomizer {
    private static final int MLT = 1103515245;
    private static final int INC = 12345;
    private int val;

    public Randomizer(final int seed) {
        val = seed;
    }

    public int next() {
        int ret = (val >> 16) & 0x7fff;
        val = val * MLT + INC;
        return ret;
    }

    public int next(int n) {
        return next() % n;
    }

    public int getSeed() {
        return val;
    }
}
