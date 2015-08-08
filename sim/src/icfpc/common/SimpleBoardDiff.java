package icfpc.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author masata
 */
public class SimpleBoardDiff {
    @JsonProperty("a")
    public final ArrayList<Cell> addFullCells;
    @JsonProperty("d")
    public final ArrayList<Cell> delFullCells;
    @JsonProperty("u")
    public final ArrayList<Cell> unitCells;
    @JsonProperty("p")
    public final Cell pivot;
    @JsonProperty("s")
    public final int score;

    public SimpleBoardDiff(
            final Collection<? extends Cell> addFullCells,
            final Collection<? extends Cell> delFullCells,
            final Collection<? extends Cell> unitCells,
            final Cell pivot,
            final int score) {
        this.addFullCells = new ArrayList<>(addFullCells);
        this.delFullCells = new ArrayList<>(delFullCells);
        this.unitCells = new ArrayList<>(unitCells);
        this.pivot = pivot;
        this.score = score;
    }
}
