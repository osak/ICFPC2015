package icfpc.ema.cli;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
 * @author tomoyuki
 */

public class CommandLineOption extends Options {
    private static final Logger LOGGER = Logger.getLogger(CommandLineOption.class);

    private final boolean debugMode;
    private final List<URL> problemFiles;
    private int spellLength;

    private CommandLineOption(
            final boolean debugMode,
            final List<URL> problemFiles,
            int spellLength
    ) {
        this.debugMode = debugMode;
        this.problemFiles = problemFiles;
        this.spellLength = spellLength;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public List<URL> getProblemFiles() {
        return problemFiles;
    }

    public static void printHelp() {
        new HelpFormatter().printHelp("ema", Ops.INSTANCE);
    }

    public static CommandLineOption parse(final String[] args) throws IOException, ParseException {
        final CommandLine cl = new BasicParser().parse(Ops.INSTANCE, args);
        final Builder builder = builder();
        if (cl.hasOption("d")) {
            builder.debugMode(true);
        }
        if (cl.hasOption("p")) {
            builder.problemFiles(new File(cl.getOptionValue("p")).listFiles());
        }
        if (cl.hasOption("s")) {
            builder.setN(Integer.valueOf(cl.getOptionValue("s")));
        }

        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getSpellLength() {
        return spellLength;
    }

    public static class Builder {
        private boolean debugMode = false;
        private List<URL> problemFiles = Lists.newArrayList();
        private int n = 0;

        public Builder() {}

        public Builder debugMode(final boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        public Builder problemFiles(final File[] problemFiles) {
            try {
                for (File problemFile : problemFiles) {
                    this.problemFiles.add(problemFile.toURI().toURL());
                }
            } catch (MalformedURLException e) {
                LOGGER.error(e);
            }
            return this;
        }

        public Builder setN(int n) {
            this.n = n;
            return this;
        }

        public CommandLineOption build() {
            Preconditions.checkNotNull(problemFiles);
            return new CommandLineOption(debugMode, problemFiles, n);
        }
    }

    private static class Ops extends Options {
        private static final Ops INSTANCE = new Ops();

        private Ops() {
            addOption("d", false, "Debug mode");
            addOption("p", "problem", true, "Problem files");
            addOption("s", "spellLength", true, "Length of a spell");
        }
    }
}
