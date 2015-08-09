package icfpc.io;

import icfpc.common.Board;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author masata
 */
public interface SimulatorDumpWriter extends Closeable {
    void begin(final Board board) throws IOException;
    void write(final Board board) throws IOException;
    void write(final String fieldName, final Object object) throws IOException;
    void end() throws IOException;
}
