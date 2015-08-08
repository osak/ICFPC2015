package icfpc.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import icfpc.common.Board;
import icfpc.common.GameSettings;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author masata
 */
public class DefaultSimulatorResultWriter implements SimulatorResultWriter {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final OutputStream outputStream;
    private boolean firstElement;
    private Map<String, Object> appendix = new HashMap<>();

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
    public void write(String fieldName, Object object) throws IOException {
        appendix.put(fieldName, object);
    }

    @Override
    public void close() throws IOException {
        outputStream.write("]".getBytes());
        for (final Map.Entry<String, Object> entry : appendix.entrySet()) {
            outputStream.write(String.format("\n, \"%s\": %s", entry.getKey(), mapper.writeValueAsString(entry.getValue())).getBytes());
        }
        outputStream.write("}".getBytes());
        outputStream.close();
    }
}
