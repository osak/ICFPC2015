package icfpc.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import icfpc.common.Board;
import icfpc.common.SimpleBoardDiff;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author masata
 */
public class SimpleSimulatorDumpWriter implements SimulatorDumpWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = Logger.getLogger(SimpleSimulatorDumpWriter.class);

    private final OutputStream outputStream;
    private boolean firstElement;
    private Map<String, Object> appendix = new HashMap<>();

    public SimpleSimulatorDumpWriter(final OutputStream outputStream) {
        this.outputStream = outputStream;
        this.firstElement = true;
    }

    @Override
    public void begin(Board board) throws IOException {
        outputStream.write(String.format(
                "{\"settings\": %s, \"initialBoard\": %s, \"diffBoards\": [\n",
                MAPPER.writeValueAsString(board.getGameSettings()),
                MAPPER.writeValueAsString(board)
        ).getBytes());
    }

    @Override
    public void write(Board board) throws IOException {
        final SimpleBoardDiff diff = board.getDiff();
        if (diff == null) {
            LOGGER.debug("no diff");
            return;
        }
        if (!firstElement) {
            outputStream.write(",".getBytes());
        }
        firstElement = false;
        outputStream.write(MAPPER.writeValueAsString(diff).getBytes());
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
            outputStream.write(String.format("\n, \"%s\": %s", entry.getKey(), MAPPER.writeValueAsString(entry.getValue())).getBytes());
        }
        outputStream.write("}".getBytes());
        outputStream.close();
    }
}
