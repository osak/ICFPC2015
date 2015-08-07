package icfpc.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author masata
 */
@JsonSerialize(using = Board.Serializer.class)
public class Board {
    private final int width;
    private final int height;
    private final Set<Cell> filled;
    private Unit currentUnit;
    private Cell currentUnitPivot;
    private Angle currentAngle;

    public Board(final int width, final int height, Collection<? extends Cell> filled) {
        this.width = width;
        this.height = height;
        this.filled = new HashSet<>(filled);
    }

    /**
     *
     * @param unit unit
     * @return false if game ended
     */
    public boolean spawn(final Unit unit) {
        final Cell pivot = unit.pivot.toCell().plusVector(spawnVector(unit));
        if (!check(unit, pivot, Angle.CLOCK_0)) {
            return false;
        }
        currentUnit = unit;
        currentUnitPivot = pivot;
        currentAngle = Angle.CLOCK_0;
        return true;
    }

    private static List<Cell> getUnitCells(final Unit unit, final Cell pivot, final Angle angle) {
        List<Cell> cells = new ArrayList<>();
        if (unit != null) {
            for (final OriginalCell oc : unit.members) {
                final Cell pivotDiff = angle.rotate(Cell.vector(unit.pivot.toCell(), pivot));
                final Cell c = oc.toCell().plusVector(pivotDiff);
                cells.add(c);
            }
        }
        return cells;
    }

    private List<Cell> getUnitCells() {
        return getUnitCells(currentUnit, currentUnitPivot, currentAngle);
    }

    private boolean check(final Unit unit, final Cell pivot, final Angle angle) {
        for (final Cell c : getUnitCells(unit, pivot, angle)) {
            if (filled.contains(c)) {
                return false;
            }
            final OriginalCell occ = c.toOriginalCell();
            if (occ.x < 0 || occ.x >= width || occ.y < 0 || occ.y >= height) {
                return false;
            }
        }
        return true;
    }

    private void lock() {
        filled.addAll(getUnitCells());
        currentUnit = null;
    }

    /**
     *
     * @param command cmd
     * @return false if locked
     */
    public boolean operate(final Command command) {
        Cell newUnitPivot = new Cell(currentUnitPivot.x, currentUnitPivot.y);
        Angle newAngle = currentAngle;
        switch (command) {
            case MOVE_E:
                newUnitPivot.x += 2;
                break;
            case MOVE_W:
                newUnitPivot.x -= 2;
                break;
            case MOVE_SE:
                newUnitPivot.x += 1;
                newUnitPivot.y += 1;
                break;
            case MOVE_SW:
                newUnitPivot.x -= 1;
                newUnitPivot.y += 1;
                break;
            case CLOCK:
                newAngle = newAngle.clock();
                break;
            case C_CLOCK:
                newAngle = newAngle.counterClock();
                break;
            default:
                throw new Error("WOW");
        }
        if (!check(currentUnit, newUnitPivot, newAngle)) {
            lock();
            return false; // LOCKED!!
        }
        currentUnitPivot = newUnitPivot;
        currentAngle = newAngle;
        return true;
    }

    public boolean unitIsEmpty() {
        return currentUnit == null;
    }

    public void debug() {
        char[][] f = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                f[x][y] = '.';
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (filled.contains(Cell.fromOriginal(new OriginalCell(x, y)))) {
                    f[x][y] = '#';
                }
            }
        }

        if (currentUnit != null) {
            for (final OriginalCell mem : currentUnit.members) {
                final Cell pivotDiff = currentAngle.rotate(Cell.vector(currentUnit.pivot.toCell(), currentUnitPivot));
                final Cell c = mem.toCell().plusVector(pivotDiff);
                final OriginalCell oc = c.toOriginalCell();
                f[oc.x][oc.y] = '*';
            }
        }

        if (currentUnitPivot != null) {
            OriginalCell pivotOc = currentUnitPivot.toOriginalCell();
            f[pivotOc.x][pivotOc.y] = f[pivotOc.x][pivotOc.y] == '.' ? '~' : '@';
        }

        final StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            if (y % 2 != 0) {
                sb.append(' ');
            }
            for (int x = 0; x < width; x++) {
                sb.append(f[x][y]);
                sb.append(' ');
            }
            sb.append('\n');
        }

        System.err.println("=======================");
        System.err.println(sb.toString());
        System.err.println("=======================");
    }

    private Cell spawnVector(final Unit unit) {
        int topmost = height;
        int leftSpace = width * 2;
        int rightSpace = width * 2;
        for (OriginalCell oc : unit.members) {
            topmost = Math.min(topmost, oc.toCell().y);
        }
        for (OriginalCell oc : unit.members) {
            // (-y, -1)に仮おき
            Cell c = oc.toCell();
            int leftSide = c.y % 2 == 0 ? 0 : 1;
            int rightSide = 2 * (width - 1) + leftSide;
            int y = c.y - topmost;
            if (y % 2 != 0 && oc.y % 2 != 0) {
                leftSpace = Math.min(leftSpace, (c.x - 1 - leftSide));
                rightSpace = Math.min(rightSpace, (rightSide - (c.x - 1)));
            } else {
                leftSpace = Math.min(leftSpace, (c.x - leftSide));
                rightSpace = Math.min(rightSpace, (rightSide - c.x));
            }
        }
        return new Cell((rightSpace - leftSpace) / 4 * 2 - (topmost % 2 != 0 ? 1 : 0), -topmost);
    }

    public static class Serializer extends JsonSerializer<Board> {
        @Override
        public void serialize(Board value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeFieldName("fullCells");
            gen.writeStartArray();
            for (final Cell cell : value.filled) {
                final OriginalCell oc = cell.toOriginalCell();
                gen.writeStartObject();
                gen.writeNumberField("x", oc.x);
                gen.writeNumberField("y", oc.y);
                gen.writeEndObject();
            }
            gen.writeEndArray();
            gen.writeFieldName("unitCells");
            gen.writeStartArray();
            for (final Cell cell : value.getUnitCells()) {
                final OriginalCell oc = cell.toOriginalCell();
                gen.writeStartObject();
                gen.writeNumberField("x", oc.x);
                gen.writeNumberField("y", oc.y);
                gen.writeEndObject();
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }
}