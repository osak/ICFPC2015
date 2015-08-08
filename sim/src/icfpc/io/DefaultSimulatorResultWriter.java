package icfpc.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import icfpc.common.Board;
import icfpc.common.GameSettings;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author masata
 */
public class DefaultSimulatorResultWriter implements SimulatorResultWriter {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final OutputStream outputStream;
    private boolean firstElement;

    public DefaultSimulatorResultWriter(final OutputStream outputStream, final GameSettings gameSettings) throws IOException {
        this.outputStream = outputStream;
        this.firstElement = true;
        outputStream.write(String.format("{\"settings\": %s, \"boards\": [", mapper.writeValueAsString(gameSettings)).getBytes());
    }

    @Override
    public void write(final Board board) throws IOException {
        if (!firstElement) {
            outputStream.write(",".getBytes());
        }
        firstElement = false;
        outputStream.write(mapper.writeValueAsString(board).getBytes());
        outputStream.write("\n".getBytes());
    }

    @Override
    public void close() throws IOException {
        outputStream.write("]}".getBytes());
        outputStream.close();
    }
}
