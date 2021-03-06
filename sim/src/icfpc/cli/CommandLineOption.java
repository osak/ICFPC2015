package icfpc.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;
import icfpc.io.DefaultSimulatorDumpWriter;
import icfpc.io.MockSimulatorDumpWriter;
import icfpc.io.SimpleSimulatorDumpWriter;
import icfpc.io.SimulatorDumpWriter;
import icfpc.io.model.Answer;
import icfpc.io.model.Problem;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author masata
 */
public class CommandLineOption extends Options {
    private static final Logger LOGGER = Logger.getLogger(CommandLineOption.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    private final boolean debugMode;
    private final Problem problem;
    private final List<Answer> answers;
    private final SimulatorDumpWriter dumpWriter;
    private final Mode mode;

    private CommandLineOption(
            final Mode mode,
            final boolean debugMode,
            final Problem problem,
            final List<Answer> answers,
            final SimulatorDumpWriter dumpWriter
    ) {
        this.debugMode = debugMode;
        this.problem = problem;
        this.answers = answers;
        this.mode = mode;
        this.dumpWriter = dumpWriter;
    }

    public Mode getMode() {
        return mode;
    }
    public boolean isDebugMode() {
        return debugMode;
    }
    public Problem getProblem() {
        return problem;
    }
    public List<Answer> getAnswers() {
        return answers;
    }
    public Answer getAnswerBySeed(final int seed) {
        for (final Answer answer : answers) {
            if (answer.seed == seed) {
                return answer;
            }
        }
        return null;
    }
    public SimulatorDumpWriter getDumpWriter() {
        return dumpWriter;
    }

    public static void printHelp() {
        new HelpFormatter().printHelp("sim", Ops.INSTANCE);
    }

    public static CommandLineOption parse(final String[] args) throws ParseException, IOException {
        final CommandLine cl = new BasicParser().parse(Ops.INSTANCE, args);
        final Builder builder = builder();
        if (cl.hasOption("n")) {
            builder.normalMode();
        } else if (cl.hasOption("i")) {
            builder.interactiveMode();
        } else if (cl.hasOption("s")) {
            builder.simpleMode();
        }
        if (cl.hasOption("d")) {
            builder.debugMode(true);
        }
        if (cl.hasOption("p")) {
            builder.problemFile(cl.getOptionValue("p"));
        }
        if (cl.hasOption("a")) {
            builder.answerFile(cl.getOptionValue("a"));
        }
        if (cl.hasOption("A")) {
            builder.allMode(true);
        }
        return builder.build();
    }

    public enum Mode {
        NORMAL,
        SIMPLE,
        INTERACTIVE,
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Mode mode = Mode.NORMAL;
        private boolean debugMode = false;
        private boolean allMode = false;
        private URL problemFile;
        private URL answerFile;

        public Builder() {}

        public Builder normalMode() {
            this.mode = Mode.NORMAL;
            return this;
        }
        public Builder simpleMode() {
            this.mode = Mode.SIMPLE;
            return this;
        }
        public Builder interactiveMode() {
            this.mode = Mode.INTERACTIVE;
            return this;
        }
        public Builder debugMode(final boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }
        public Builder allMode(final boolean allMode) {
            this.allMode = allMode;
            return this;
        }
        public Builder problemFile(final String problemFilePath) {
            try {
                this.problemFile = new File(problemFilePath).toURI().toURL();
            } catch (final MalformedURLException e) {
                LOGGER.error(e);
            }
            return this;
        }
        public Builder answerFile(final String answerFile) {
            try {
                this.answerFile = new File(answerFile).toURI().toURL();
            } catch (final MalformedURLException e) {
                LOGGER.error(e);
            }
            return this;
        }

        public CommandLineOption build() throws IOException {
            Preconditions.checkNotNull(problemFile);
            if (mode != Mode.INTERACTIVE) {
                Preconditions.checkNotNull(answerFile);
                final List<Answer> answers = MAPPER.readValue(answerFile, TYPE_FACTORY.constructCollectionType(List.class, Answer.class));
                return new CommandLineOption(
                        mode,
                        debugMode,
                        MAPPER.readValue(problemFile, Problem.class),
                        answers,
                        makeDumpWriter(mode, allMode));
            } else {
                return new CommandLineOption(
                        mode,
                        debugMode,
                        MAPPER.readValue(problemFile, Problem.class),
                        null,
                        makeDumpWriter(mode, allMode));
            }
        }

        private static SimulatorDumpWriter makeDumpWriter(final Mode mode, final boolean allMode) {
            switch (mode) {
                case NORMAL:
                    return new DefaultSimulatorDumpWriter(System.out, allMode);
                case SIMPLE:
                    return new SimpleSimulatorDumpWriter(System.out, allMode);
                default:
                    return new MockSimulatorDumpWriter();
            }
        }
    }

    private static class Ops extends Options {
        private static final Ops INSTANCE = new Ops();

        private Ops() {
            addOption("n", "normal", false, "Normal mode (default)");
            addOption("i", "interactive", false, "Interactive mode");
            addOption("s", "simple", false, "Simple mode");
            addOption("d", false, "log4j.level=DEBUG");
            addOption("p", "problem", true, "Problem file");
            addOption("a", "answer", true, "AI output file");
            addOption("A", "all", false, "Output all results");
        }
    }
}
