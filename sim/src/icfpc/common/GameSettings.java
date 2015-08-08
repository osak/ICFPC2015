package icfpc.common;

import java.util.List;

/**
 * @author masata
 */
public class GameSettings {
    public int width;
    public int height;
    public List<Unit> units;
    public int maxSources;
    public int initialSeed;

    GameSettings() {}

    public GameSettings(
            final int width,
            final int height,
            List<Unit> units,
            int maxSources,
            int initialSeed) {
        this.width = width;
        this.height = height;
        this.units = units;
        this.maxSources = maxSources;
        this.initialSeed = initialSeed;
    }
}
