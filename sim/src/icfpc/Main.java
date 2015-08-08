package icfpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import icfpc.cli.CommandLineOption;
import icfpc.common.Board;
import icfpc.common.Cell;
import icfpc.common.Command;
import icfpc.common.OriginalCell;
import icfpc.io.Answer;
import icfpc.io.CommandReader;
import icfpc.io.DefaultSimulatorResultWriter;
import icfpc.io.MockSimulatorResultWriter;
import icfpc.io.Problem;
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

        final SimulatorResultWriter simulatorResultWriter;
        if (opts.isNormalMode()) {
            simulatorResultWriter = new DefaultSimulatorResultWriter(System.out);
        } else {
            simulatorResultWriter = new MockSimulatorResultWriter();
        }

        final CommandReader commandReader;
        if (opts.isNormalMode()) {
            final TypeFactory typeFactory = TypeFactory.defaultInstance();
            final List<Answer> answers = mapper.readValue(opts.getAnswerFile(), typeFactory.constructCollectionType(List.class, Answer.class));
            commandReader = answers.get(0).getCommandReader();
        } else {
            commandReader = new StdInCommandReader();
        }

        final Randomizer randomizer = new Randomizer(problem.sourceSeeds.get(0));
        final Board board = new Board(problem.width, problem.height, problem.units, randomizer, FluentIterable.from(problem.filled).transform(new Function<OriginalCell, Cell>() {
            @Nullable
            @Override
            public Cell apply(OriginalCell input) {
                return input.toCell();
            }
        }).toImmutableList());
        simulatorResultWriter.write(board);
        while (commandReader.hasNext()) {
            final Command cmd = commandReader.next();
            board.operate(cmd);
            simulatorResultWriter.write(board);
            if (board.hasEnded()) {
                break;
            }
        }
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
