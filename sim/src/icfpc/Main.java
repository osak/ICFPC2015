package icfpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import icfpc.cli.CommandLineOption;
import icfpc.common.Board;
import icfpc.common.Cell;
import icfpc.common.GameSettings;
import icfpc.common.OriginalCell;
import icfpc.io.CommandReader;
import icfpc.io.SimulatorDumpWriter;
import icfpc.io.StdInCommandReader;
import icfpc.io.model.Answer;
import icfpc.io.model.Problem;
import icfpc.random.Randomizer;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author masata
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(final String[] args) throws Exception {
        setLogger();
        final CommandLineOption opts;
        try {
            opts = CommandLineOption.parse(args);
        } catch (final Throwable t) {
            CommandLineOption.printHelp();
            return;
        }

        if (opts.isDebugMode()) {
            Logger.getRootLogger().setLevel(Level.DEBUG);
        } else {
            Logger.getRootLogger().setLevel(Level.INFO);
        }

        final Problem problem = opts.getProblem();
        final List<Cell> cellFilled = FluentIterable.from(problem.filled).transform(new Function<OriginalCell, Cell>() {
            @Nullable
            @Override
            public Cell apply(OriginalCell input) {
                return input.toCell();
            }
        }).toImmutableList();

        final int n = problem.sourceSeeds.size();
        int sumScore = 0;
        int sumSpawn = 0;
        double sumElapsedTime = 0;
        int alive = 0;
        for (int i = 0; i < n; i++) {
            final int seed = problem.sourceSeeds.get(i);
            final SimulatorDumpWriter simulatorDumpWriter = opts.getDumpWriter();
            final GameSettings gameSettings = new GameSettings(problem.width, problem.height, problem.units, problem.sourceLength, seed);
            final CommandReader commandReader;
            if (opts.getMode() == CommandLineOption.Mode.INTERACTIVE) {
                commandReader = new StdInCommandReader();
            } else {
                final Answer answer = opts.getAnswerBySeed(seed);
                if (answer == null) {
                    LOGGER.warn(String.format("No answer found for seed %d", seed));
                    continue;
                }
                commandReader = answer.getCommandReader();
                simulatorDumpWriter.write("expectedScore", answer.expectedScore);
                sumElapsedTime += answer.elapsedTime;
            }

            final Randomizer randomizer = new Randomizer(seed);
            final Board board = new Board(gameSettings, randomizer, cellFilled);
            simulatorDumpWriter.begin(board);
            if (i == 0) {
                simulatorDumpWriter.write(board);
            }
            while (commandReader.hasNext()) {
                final char cmd = commandReader.next();
                if (board.hasEnded()) {
                    board.violateRule("余分な命令");
                    if (i == 0) {
                        simulatorDumpWriter.write(board);
                    }
                    break;
                }
                board.operate(cmd);
                if (opts.getMode() == CommandLineOption.Mode.INTERACTIVE) {
                    board.debug();
                }
                if (i == 0) {
                    simulatorDumpWriter.write(board);
                }

                if (board.hasEnded()) {
                    break;
                }
            }
            sumScore += board.getScore();
            sumSpawn += board.getSpawnedUnitCount();
            if (i == 0) {
                simulatorDumpWriter.write("SampleScore", board.getScore());
            }
            if (board.getSpawnedUnitCount() == problem.sourceLength) {
                alive++;
            }
            if (board.memo != null) {
                simulatorDumpWriter.write("memo", board.memo);
            }
            simulatorDumpWriter.close();
        }
        LOGGER.info("averageScore: " + sumScore / n);
        LOGGER.info("averageElapsedTime: " + sumElapsedTime / n);
        LOGGER.info("aliveCount: " + alive);
        LOGGER.info("aliveRate: " + (double) sumSpawn / (n * problem.sourceLength));
        LOGGER.info("testCaseCount: " + n);
    }

    private static void setLogger() {
        final PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%d %5p %c{1} - %m%n");
        final ConsoleAppender appender = new ConsoleAppender();
        appender.setLayout(layout);
        appender.setTarget("System.err");
        appender.setName("stderr");
        appender.activateOptions();
        Logger.getRootLogger().addAppender(appender);
    }
}
