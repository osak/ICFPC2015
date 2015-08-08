package icfpc.io;

import icfpc.common.Command;

import java.util.Scanner;

/**
 * @author masata
 */
public class StdInCommandReader implements CommandReader {
    private Scanner scanner = new Scanner(System.in);

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Command next() {
        try {
            final String line = scanner.nextLine();
            return Command.fromChar(line.charAt(0));
        } catch (Throwable t) {
            return next();
        }
    }
}
