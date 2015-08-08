package icfpc.io;

import icfpc.common.Board;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author masata
 */
public interface SimulatorResultWriter extends Closeable {
    void write(final Board board) throws IOException;
    void write(final String fieldName, final Object object) throws IOException;
}
