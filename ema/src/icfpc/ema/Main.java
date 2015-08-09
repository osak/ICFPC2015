package icfpc.ema;

import icfpc.ema.angel.PhraseTester;
import icfpc.ema.angel.TestResult;
import icfpc.ema.cli.CommandLineOption;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.List;
import java.util.Scanner;

/**
 * @author tomoyuki
 */

public class Main {
    public static void main(final String[] args) throws Exception {
        setLogger();
        final CommandLineOption opts;
        try {
            opts = CommandLineOption.parse(args);
        } catch(final Throwable t) {
            CommandLineOption.printHelp();
            return;
        }

        if (opts.isDebugMode()) {
            Logger.getRootLogger().setLevel(Level.DEBUG);
        } else {
            Logger.getRootLogger().setLevel(Level.INFO);
        }

        final Scanner scanner = new Scanner(System.in);

        String phrase = scanner.nextLine();

        final List<TestResult> results = new PhraseTester(opts.getProblemFiles()).test(phrase).getResult();
        if (results.size() == 0) {
            Logger.getRootLogger().info("A game where spelling is possible has not found.");
        } else {
            for (TestResult result : results) {
                Logger.getRootLogger().info(result.show());
            }
        }
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
