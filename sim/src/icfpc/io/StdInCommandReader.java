package icfpc.io;

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
    public char next() {
        try {
            final String line = scanner.nextLine();
            return line.charAt(0);
        } catch (Throwable t) {
            return next();
        }
    }
}
