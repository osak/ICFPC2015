package icfpc.cli;

import com.google.common.base.Preconditions;
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

/**
 * @author masata
 */
public class CommandLineOption extends Options {
    private static final Logger LOGGER = Logger.getLogger(CommandLineOption.class);

    private final boolean debugMode;
    private final URL problemFile;
    private final URL answerFile;
    private final Mode mode;

    private CommandLineOption(
            final Mode mode,
            final boolean debugMode,
            final URL problemFile,
            final URL answerFile
    ) {
        this.debugMode = debugMode;
        this.problemFile = problemFile;
        this.answerFile = answerFile;
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }
    public boolean isDebugMode() {
        return debugMode;
    }
    public URL getProblemFile() {
        return problemFile;
    }
    public URL getAnswerFile() {
        return answerFile;
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

        public CommandLineOption build() {
            Preconditions.checkNotNull(problemFile);
            if (mode != Mode.INTERACTIVE) {
                Preconditions.checkNotNull(answerFile);
            }
            return new CommandLineOption(mode, debugMode, problemFile, answerFile);
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
        }
    }
}
