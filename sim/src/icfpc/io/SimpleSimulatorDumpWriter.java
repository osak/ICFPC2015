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
    private boolean firstBoard = true;
    private boolean firstTest = true;
    private Map<String, Object> appendix = new HashMap<>();
    private final boolean allMode;

    public SimpleSimulatorDumpWriter(final OutputStream outputStream, final boolean allMode) {
        this.outputStream = outputStream;
        this.allMode = allMode;
    }

    @Override
    public void begin(Board board) throws IOException {
        if (allMode) {
            if (firstTest) {
                outputStream.write("[".getBytes());
            } else {
                outputStream.write(",".getBytes());
            }
            firstBoard = true;
            appendix.clear();
        }
        if (allMode || firstTest) {
            outputStream.write(String.format(
                    "{\"settings\": %s, \"initialBoard\": %s, \"diffBoards\": [\n",

                    MAPPER.writeValueAsString(board.getGameSettings()),
                    MAPPER.writeValueAsString(board)
            ).getBytes());
        }
    }

    @Override
    public void write(Board board) throws IOException {
        if (allMode || firstTest) {
            final SimpleBoardDiff diff = board.getDiff();
            if (diff == null) {
                LOGGER.debug("no diff");
                return;
            }
            if (!firstBoard) {
                outputStream.write(",".getBytes());
            }
            firstBoard = false;
            outputStream.write(MAPPER.writeValueAsString(diff).getBytes());
            outputStream.write("\n".getBytes());
        }
    }

    @Override
    public void write(String fieldName, Object object) throws IOException {
        appendix.put(fieldName, object);
    }

    @Override
    public void end() throws IOException {
        if (allMode || firstTest) {
            if (firstTest) {
                firstTest = false;
            }
            outputStream.write("]".getBytes());
            for (final Map.Entry<String, Object> entry : appendix.entrySet()) {
                outputStream.write(String.format("\n, \"%s\": %s", entry.getKey(), MAPPER.writeValueAsString(entry.getValue())).getBytes());
            }
            outputStream.write("}".getBytes());
        }
    }

    @Override
    public void close() throws IOException {
        if (allMode) {
            outputStream.write("]".getBytes());
        }
        outputStream.close();
    }
}
