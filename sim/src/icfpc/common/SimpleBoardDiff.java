package icfpc.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author masata
 */
public class SimpleBoardDiff {
    @JsonProperty("a")
    public final ArrayList<OriginalCell> addFullCells;
    @JsonProperty("d")
    public final ArrayList<OriginalCell> delFullCells;
    @JsonProperty("u")
    public final ArrayList<OriginalCell> unitCells;
    @JsonProperty("p")
    public final OriginalCell pivot;
    @JsonProperty("s")
    public final int score;

    public SimpleBoardDiff(
            final Collection<? extends Cell> addFullCells,
            final Collection<? extends Cell> delFullCells,
            final Collection<? extends Cell> unitCells,
            final Cell pivot,
            final int score) {
        this.addFullCells = toOriginalCellList(addFullCells);
        this.delFullCells = toOriginalCellList(delFullCells);
        this.unitCells = toOriginalCellList(unitCells);
        this.pivot = pivot.toOriginalCell();
        this.score = score;
    }

    private static ArrayList<OriginalCell> toOriginalCellList(final Collection<? extends Cell> cells) {
        final ArrayList<OriginalCell> ret = new ArrayList<>();
        for (final  Cell cell : cells) {
            ret.add(cell.toOriginalCell());
        }
        return ret;
    }
}
