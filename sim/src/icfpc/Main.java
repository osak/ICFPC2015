package icfpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import icfpc.common.Board;
import icfpc.common.Cell;
import icfpc.common.Command;
import icfpc.common.OriginalCell;
import icfpc.io.Input;
import icfpc.io.Output;
import icfpc.random.Randomizer;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

/**
 * @author masata
 */
public class Main {
    public static void main(final String[] args) throws Exception {
        final ClassLoader classLoader = Main.class.getClassLoader();
        final ObjectMapper mapper = new ObjectMapper();
        final TypeFactory typeFactory = TypeFactory.defaultInstance();
        final URL inputFile = classLoader.getResource("problems/problem_0.json");
        final URL outputFile = classLoader.getResource("example_output/problem_0.json/output_0.json");
        final Input input = mapper.readValue(inputFile, Input.class);
        final List<Output> outputs = mapper.readValue(outputFile, typeFactory.constructCollectionType(List.class, Output.class));
        boolean interactive = false;
        if (args.length > 0 && args[0].equals("-i")) {
            interactive = true;
        }

        for (final Output output : outputs) {
            final Board board = new Board(input.width, input.height, FluentIterable.from(input.filled).transform(new Function<OriginalCell, Cell>() {
                @Nullable
                @Override
                public Cell apply(OriginalCell input) {
                    return input.toCell();
                }
            }).toImmutableList());
            board.debug();
            final Randomizer randomizer = new Randomizer(input.sourceSeeds.get(0));
            {
                int unitId = randomizer.next(input.units.size());
                boolean gameEnded = !board.spawn(input.units.get(unitId));
                if (gameEnded) {
                    System.err.println("[DEBUG]GAME ENDED");
                    break;
                } else {
                    System.err.println("[DEBUG]SPAWN UNIT ID: " + unitId);
                    board.debug();
                }
            }
            if (!interactive) {
                System.out.print("[");
            }
            for (int i = 0; i < output.solution.length(); i++) {
                final Command cmd;
                if (interactive) {
                    Scanner scanner = new Scanner(System.in);
                    cmd = Command.fromChar(scanner.nextLine().charAt(0));
                } else {
                    cmd = Command.fromChar(output.solution.charAt(i));
                }
                boolean locked = !board.operate(cmd);
                System.err.println("[DEBUG]command: " + cmd);
                board.debug();
                if (!interactive) {
                    if (i > 0) {
                        System.out.print(",");
                    }
                    System.out.print(mapper.writeValueAsString(board));
                }
                board.debug();
                if (locked) {
                    int unitId = randomizer.next(input.units.size());
                    boolean gameEnded = !board.spawn(input.units.get(unitId));
                    if (gameEnded) {
                        System.err.println("[DEBUG]GAME ENDED");
                        break;
                    } else {
                        System.err.println("[DEBUG]SPAWN UNIT ID: " + unitId);
                        board.debug();
                    }
                }
            }
            break;
        }
        if (!interactive) {
            System.out.println("]");
            System.out.flush();
        }
    }
}
