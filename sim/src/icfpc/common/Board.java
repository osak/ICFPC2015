package icfpc.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author masata
 */
@JsonSerialize(using = Board.Serializer.class)
@JsonDeserialize(using = Board.Deserializer.class)
public class Board {
    private final int width;
    private final int height;
    private Set<Cell> filled;
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
                final Cell inner = angle.rotate(Cell.vector(unit.pivot.toCell(), oc.toCell()));
                final Cell c = pivot.plusVector(inner);
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
        final Set<Cell> newFilled = new HashSet<>();
        int clearedRowCount = 0;
        for (int y = height - 1; y >= 0; y--) {
            boolean rowFilled = true;
            for (int x = 0; x < width; x++) {
                if (filled.contains(new OriginalCell(x, y).toCell())) {
                    newFilled.add(new OriginalCell(x, y + clearedRowCount).toCell());
                } else {
                    rowFilled = false;
                }
            }
            if (rowFilled) {
                for (int x = 0; x < width; x++) {
                    newFilled.remove(new OriginalCell(x, y + clearedRowCount).toCell());
                }
                clearedRowCount += 1;
            }
        }
        if (clearedRowCount > 0) {
            System.err.println("[DEBUG]" + clearedRowCount + " rows are cleared!");
        }
        filled = newFilled;
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
            for (final Cell c : getUnitCells()) {
                final OriginalCell oc = c.toOriginalCell();
                f[oc.x][oc.y] = '*';
            }
        }

        final StringBuilder sb = new StringBuilder();
        if (currentUnitPivot != null) {
            final OriginalCell ocPivot = currentUnitPivot.toOriginalCell();
            if (ocPivot.x >= 0 && ocPivot.x < width && ocPivot.y >= 0 && ocPivot.y < height) {
                f[ocPivot.x][ocPivot.y] = f[ocPivot.x][ocPivot.y] == '.' ? '~' : '@';
            } else {
                sb.append("=======================\n");
                sb.append(String.format("pivot: (%d, %d)\n", ocPivot.x, ocPivot.y));
            }
        }

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
            gen.writeNumberField("width", value.width);
            gen.writeNumberField("height", value.height);
            if (value.currentUnitPivot != null) {
                gen.writeFieldName("pivot");
                gen.writeStartObject();
                gen.writeNumberField("x", value.currentUnitPivot.toOriginalCell().x);
                gen.writeNumberField("y", value.currentUnitPivot.toOriginalCell().y);
                gen.writeEndObject();
            }
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Board> {
        @Override
        public Board deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode root = p.getCodec().readTree(p);
            final int width = root.get("width").asInt();
            final int height = root.get("height").asInt();
            final List<Cell> filled = new ArrayList<>();
            for (final Iterator<JsonNode> it = root.get("fullCells").elements(); it.hasNext(); ) {
                final JsonNode node = it.next();
                final Cell cell = new Cell(node.get("x").asInt(), node.get("y").asInt());
                filled.add(cell);
            }
            final List<Cell> unitCells = new ArrayList<>();
            for (final Iterator<JsonNode> it = root.get("unitCells").elements(); it.hasNext(); ) {
                final JsonNode node = it.next();
                final Cell cell = new Cell(node.get("x").asInt(), node.get("y").asInt());
                unitCells.add(cell);
            }
            final Cell pivot = new Cell(root.get("pivot").get("x").asInt(), root.get("pivot").get("y").asInt());
            final Board ret = new Board(width, height, filled);
            final Unit unit = new Unit(FluentIterable.from(unitCells).transform(new Function<Cell, OriginalCell>() {
                @Nullable
                @Override
                public OriginalCell apply(Cell input) {
                    return input.toOriginalCell();
                }
            }).toImmutableList(), pivot.toOriginalCell());
            ret.currentUnit = unit;
            ret.currentUnitPivot = pivot;
            ret.currentAngle = Angle.CLOCK_0;
            return ret;
        }
    }
}