package icfpc.common;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author masata
 */
public class Unit {
    public ArrayList<OriginalCell> members;
    public OriginalCell pivot;

    Unit() {}

    public Unit(final Collection<? extends OriginalCell> members, final OriginalCell pivot) {
        this.members = new ArrayList<>(members);
        this.pivot = pivot;
    }
}
