package icfpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import icfpc.cli.CommandLineOption;
import icfpc.common.Board;
import icfpc.common.Cell;
import icfpc.common.GameSettings;
import icfpc.common.OriginalCell;
import icfpc.io.Answer;
import icfpc.io.CommandReader;
import icfpc.io.DefaultSimulatorResultWriter;
import icfpc.io.MockSimulatorResultWriter;
import icfpc.io.Problem;
import icfpc.io.SimpleSimulatorResultWriter;
import icfpc.io.SimulatorResultWriter;
import icfpc.io.StdInCommandReader;
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
    private static final ObjectMapper mapper = new ObjectMapper();

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

        final Problem problem = mapper.readValue(opts.getProblemFile(), Problem.class);
        final List<Cell> cellFilled = FluentIterable.from(problem.filled).transform(new Function<OriginalCell, Cell>() {
            @Nullable
            @Override
            public Cell apply(OriginalCell input) {
                return input.toCell();
            }
        }).toImmutableList();
        final GameSettings gameSettings = new GameSettings(problem.width, problem.height, problem.units, problem.sourceLength);

        final SimulatorResultWriter simulatorResultWriter;
        switch (opts.getMode()) {
            case NORMAL:
                simulatorResultWriter = new DefaultSimulatorResultWriter(System.out, gameSettings);
                break;
            case SIMPLE:
                simulatorResultWriter = new SimpleSimulatorResultWriter(System.out, new Board(gameSettings, new Randomizer(problem.sourceSeeds.get(0)), cellFilled));
                break;
            default:
                simulatorResultWriter = new MockSimulatorResultWriter();
        }

        final int n = problem.sourceSeeds.size();
        int sumScore = 0;
        int sumSpawn = 0;
        double sumElapsedTime = 0;
        int alive = 0;
        for (int i = 0; i < n; i++) {
            final CommandReader commandReader;
            if (opts.getMode() != CommandLineOption.Mode.INTERACTIVE) {
                final TypeFactory typeFactory = TypeFactory.defaultInstance();
                final List<Answer> answers = mapper.readValue(opts.getAnswerFile(), typeFactory.constructCollectionType(List.class, Answer.class));
                commandReader = answers.get(i).getCommandReader();
                simulatorResultWriter.write("expectedScore", answers.get(i).expectedScore);
                sumElapsedTime += answers.get(i).elapsedTime;
            } else {
                commandReader = new StdInCommandReader();
            }

            final Randomizer randomizer = new Randomizer(problem.sourceSeeds.get(i));
            final Board board = new Board(gameSettings, randomizer, cellFilled);
            if (i == 0) {
                simulatorResultWriter.write(board);
            }
            while (commandReader.hasNext()) {
                final char cmd = commandReader.next();
                if (board.hasEnded()) {
                    board.violateRule("余分な命令");
                    if (i == 0) {
                        simulatorResultWriter.write(board);
                    }
                    break;
                }
                board.operate(cmd);
                if (opts.getMode() == CommandLineOption.Mode.INTERACTIVE) {
                    board.debug();
                }
                if (i == 0) {
                    simulatorResultWriter.write(board);
                }

                if (board.hasEnded()) {
                    break;
                }
            }
            sumScore += board.getScore();
            sumSpawn += board.getSpawnedUnitCount();
            if (i == 0) {
                simulatorResultWriter.write("SampleScore", board.getScore());
            }
            if (board.getSpawnedUnitCount() == problem.sourceLength) {
                alive++;
            }
            simulatorResultWriter.write("memo", board.memo);
        }
        simulatorResultWriter.write("averageScore", sumScore / n);
        simulatorResultWriter.write("averageElapsedTime", sumElapsedTime / n);
        simulatorResultWriter.write("aliveCount", alive);
        simulatorResultWriter.write("aliveRate", (double)sumSpawn / (n * problem.sourceLength));
        simulatorResultWriter.write("testCaseCount", n);
        simulatorResultWriter.close();
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
