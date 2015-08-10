package icfpc.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import icfpc.random.Randomizer;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private static final Logger LOGGER = Logger.getLogger(Board.class);
    private final GameSettings gameSettings;

    private Set<Spell> castedSpells = new HashSet<>();
    private List<Character> allCommands = new ArrayList<>();
    private Set<Cell> filled;
    private Unit currentUnit;
    private Cell currentUnitPivot;
    private Angle currentAngle;
    private Randomizer randomizer;
    private boolean gameInProgress = true;
    private int prevClearedRows;
    private int moveScore;
    private int powerScore;
    private int spawnedUnitCount = 0;
    private boolean isNewUnit;
    private Set<Set<Cell>> moveHistory;
    public String memo;
    private SimpleBoardDiff diff = null;
    private String castedNow = null;
    // TODO そもそもボード以外の状態を切り分ける

    public Board(final GameSettings gameSettings, final Randomizer randomizer, final Collection<? extends Cell> filled) {
        this.gameSettings = gameSettings;
        this.randomizer = randomizer;
        this.filled = new HashSet<>(filled);
        this.prevClearedRows = 0;
        this.moveScore = 0;
        this.powerScore = 0;
        this.moveHistory = Sets.newHashSet();
        spawn();
        historyCheck();
        this.isNewUnit = false;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public SimpleBoardDiff getDiff() {
        return diff;
    }

    public boolean hasEnded() {
        return !gameInProgress;
    }

    public int getSpawnedUnitCount() {
        return spawnedUnitCount;
    }

    public void violateRule(final String memo) {
        this.memo = memo;
        gameInProgress = false;
        moveScore = 0;
        powerScore = 0;
        LOGGER.debug("ゲームは異常終了しました。");
        debug();
    }

    private void spawn(final Unit unit) {
        final Cell pivot = unit.pivot.toCell().plusVector(spawnVector(unit));
        if (spawnedUnitCount >= gameSettings.maxSources) {
            gameInProgress = false;
            LOGGER.debug("ゲームは終了しました。");
            debug();
            return;
        }
        if (!check(unit, pivot, Angle.CLOCK_0)) {
            gameInProgress = false;
            LOGGER.debug("ゲームは終了しました。");
            debug();
            return;
        }
        currentUnit = unit;
        currentUnitPivot = pivot;
        currentAngle = Angle.CLOCK_0;
        spawnedUnitCount += 1;
    }

    private void spawn() {
        if (gameInProgress) {
            int unitId = randomizer.next(gameSettings.units.size());
            spawn(gameSettings.units.get(unitId));
        } else {
            LOGGER.warn("Game has ended");
        }
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
            if (occ.x < 0 || occ.x >= gameSettings.width || occ.y < 0 || occ.y >= gameSettings.height) {
                return false;
            }
        }
        return true;
    }

    private void lock() {
        filled.addAll(getUnitCells());
        final Set<Cell> newFilled = new HashSet<>();
        int clearedRowCount = 0;
        for (int y = gameSettings.height - 1; y >= 0; y--) {
            boolean rowFilled = true;
            for (int x = 0; x < gameSettings.width; x++) {
                if (filled.contains(new OriginalCell(x, y).toCell())) {
                    newFilled.add(new OriginalCell(x, y + clearedRowCount).toCell());
                } else {
                    rowFilled = false;
                }
            }
            if (rowFilled) {
                for (int x = 0; x < gameSettings.width; x++) {
                    newFilled.remove(new OriginalCell(x, y + clearedRowCount).toCell());
                }
                clearedRowCount += 1;
            }
        }
        if (clearedRowCount > 0) {
            //System.err.println("[DEBUG]" + clearedRowCount + " rows are cleared!");
        }

        accMoveScore(currentUnit.members.size(), clearedRowCount);
        prevClearedRows = clearedRowCount;
        filled = newFilled;
        currentUnit = null;
        spawn();
    }

    private void accMoveScore(final int size, int ls) {
        int points = size + 100 * (1 + ls) * ls / 2;
        int line_bonus = prevClearedRows > 1 ? (int) Math.floor((prevClearedRows - 1) * points / 10) : 0;
        moveScore += points + line_bonus;
    }

    private void accPowerScore() {
        for (Spell spell : Spell.values()) {
            if (allCommands.size() >= spell.phrase.length()) {
                boolean casted = true;
                for (int i = 0; i < spell.phrase.length(); i++) {
                    final int iAllCommands = allCommands.size() - spell.phrase.length() + i;
                    if (allCommands.get(iAllCommands) != spell.phrase.charAt(i)) {
                        casted = false;
                        break;
                    }
                }
                if (casted) {
                    if (!castedSpells.contains(spell)) {
                        castedSpells.add(spell);
                        castedNow = spell.phrase;
                        powerScore += 300;
                    }
                    LOGGER.info("CASTED: " + spell.phrase);
                    powerScore += 2 * spell.phrase.length();
                }
            }
        }
        // TODO Phraseの詳細
        // invalidでもいい？
        // ゲームが終了してもいい？
    }

    public int getScore() {
        return moveScore + powerScore;
    }

    /**
     *
     * @param command cmd
     * @return false if locked
     */
    public boolean operate(final char command) {
        castedNow = null;
        allCommands.add(command);
        return operate(Command.fromChar(command));
    }

    private boolean operate(final Command command) {
        LOGGER.info(command.toString());
        if (!gameInProgress) {
            LOGGER.warn("Game has ended.");
            diff = null;
            return false;
        }

        if (isNewUnit) {
            moveHistory = Sets.newHashSet();
            isNewUnit = false;
        }

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
            case NOOP:
                diff = null;
                break;
            case INVALID:
                violateRule("invalid move");
                diff = null;
                return false;
            default:
                throw new Error("WOW");
        }

        if (command != Command.NOOP) {
            if (!check(currentUnit, newUnitPivot, newAngle)) {
                final Set<Cell> prevFilled = ImmutableSet.copyOf(filled);
                lock();
                diff = new SimpleBoardDiff(Sets.difference(filled, prevFilled), Sets.difference(prevFilled, filled), getUnitCells(), currentUnitPivot, getScore());
                isNewUnit = true;
                accPowerScore();
                return false; // LOCKED!!
            } else {
                diff = new SimpleBoardDiff(Collections.<Cell>emptyList(), Collections.<Cell>emptyList(), getUnitCells(currentUnit, newUnitPivot, newAngle), newUnitPivot, getScore());
                currentUnitPivot = newUnitPivot;
                currentAngle = newAngle;

                if (!historyCheck()) {
                    violateRule("same history");
                    return false;
                }
            }
        }
        accPowerScore();
        return true;
    }

    private boolean historyCheck() {
        final Set<Cell> cells = Sets.newHashSet(getUnitCells());
        cells.add(currentUnitPivot.plusVector(new Cell(-100000, -100000)));
        if (moveHistory.contains(cells)) {
            return false;
        }
        moveHistory.add(cells);
        return true;
    }

    public void debug() {
        char[][] f = new char[gameSettings.width][gameSettings.height];
        for (int x = 0; x < gameSettings.width; x++) {
            for (int y = 0; y < gameSettings.height; y++) {
                f[x][y] = '.';
            }
        }

        for (int x = 0; x < gameSettings.width; x++) {
            for (int y = 0; y < gameSettings.height; y++) {
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

        final StringBuilder sb = new StringBuilder("\n");
        if (currentUnitPivot != null) {
            final OriginalCell ocPivot = currentUnitPivot.toOriginalCell();
            if (ocPivot.x >= 0 && ocPivot.x < gameSettings.width && ocPivot.y >= 0 && ocPivot.y < gameSettings.height) {
                f[ocPivot.x][ocPivot.y] = f[ocPivot.x][ocPivot.y] == '.' ? '~' : '@';
            } else {
                LOGGER.debug(String.format("pivot: (%d, %d)\n", ocPivot.x, ocPivot.y));
            }
        }

        for (int y = 0; y < gameSettings.height; y++) {
            if (y % 2 != 0) {
                sb.append(' ');
            }
            for (int x = 0; x < gameSettings.width; x++) {
                sb.append(f[x][y]);
                sb.append(' ');
            }
            sb.append('\n');
        }
        LOGGER.debug(sb.toString());
    }

    private Cell spawnVector(final Unit unit) {
        int topmost = gameSettings.height;
        int leftSpace = gameSettings.width * 2;
        int rightSpace = gameSettings.width * 2;
        for (OriginalCell oc : unit.members) {
            topmost = Math.min(topmost, oc.toCell().y);
        }
        for (OriginalCell oc : unit.members) {
            // (-y, -1)に仮おき
            Cell c = oc.toCell();
            int leftSide = c.y % 2 == 0 ? 0 : 1;
            int rightSide = 2 * (gameSettings.width - 1) + leftSide;
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

    public String getCastedNow() {
        return castedNow;
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
            if (value.currentUnitPivot != null) {
                gen.writeFieldName("pivot");
                gen.writeStartObject();
                gen.writeNumberField("x", value.currentUnitPivot.toOriginalCell().x);
                gen.writeNumberField("y", value.currentUnitPivot.toOriginalCell().y);
                gen.writeEndObject();
            }
            gen.writeNumberField("randomSeed", value.randomizer.getSeed());
            gen.writeNumberField("score", value.getScore());
            gen.writeNumberField("moveScore", value.moveScore);
            gen.writeNumberField("powerScore", value.powerScore);
            gen.writeNumberField("clearedRows", value.prevClearedRows);
            gen.writeObjectField("castedSpells", value.castedSpells);
            //gen.writeNumberField("width", value.gameSettings.width);
            //gen.writeNumberField("height", value.gameSettings.height);
            //gen.writeNumberField("maxSources", value.gameSettings.maxSources);
            //gen.writeObjectField("units", value.gameSettings.units);
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Board> {
        private static final TypeFactory FACTORY = TypeFactory.defaultInstance();

        @Override
        public Board deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode root = p.getCodec().readTree(p);
            final List<Cell> filled = new ArrayList<>();
            for (final Iterator<JsonNode> it = root.get("fullCells").elements(); it.hasNext(); ) {
                final JsonNode node = it.next();
                final Cell cell = new OriginalCell(node.get("x").asInt(), node.get("y").asInt()).toCell();
                filled.add(cell);
            }
            final List<Cell> unitCells = new ArrayList<>();
            for (final Iterator<JsonNode> it = root.get("unitCells").elements(); it.hasNext(); ) {
                final JsonNode node = it.next();
                final Cell cell = new OriginalCell(node.get("x").asInt(), node.get("y").asInt()).toCell();
                unitCells.add(cell);
            }
            final Cell pivot = new OriginalCell(root.get("pivot").get("x").asInt(), root.get("pivot").get("y").asInt()).toCell();
            final Randomizer randomizer = new Randomizer(root.get("randomSeed").asInt());
            //final int width = root.get("width").asInt();
            //final int height = root.get("height").asInt();
            //final int maxSources = root.get("maxSources").asInt();
            //final List<Unit> units = root.get("units").traverse(p.getCodec()).readValueAs(new TypeReference<List<Unit>>() {});
            //final Board ret = new Board(new GameSettings(width, height, units, maxSources), randomizer, filled);
            final Board ret = new Board(new GameSettings(10, 10, ImmutableList.<Unit>of(), 100, 0), randomizer, filled);
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
            ret.moveScore = root.get("moveScore").asInt();
            ret.powerScore = root.get("powerScore").asInt();
            ret.prevClearedRows = root.get("clearedRows").asInt();
            ret.castedSpells = root.get("castedSpells").traverse(p.getCodec()).readValueAs(new TypeReference<Set<Spell>>() {});

            return ret;
        }
    }
}