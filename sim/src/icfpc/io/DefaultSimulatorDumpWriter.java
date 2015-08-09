package icfpc.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import icfpc.common.Board;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author masata
 */
public class DefaultSimulatorDumpWriter implements SimulatorDumpWriter {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final OutputStream outputStream;
    private boolean firstBoard = true;
    private boolean firstTest = true;
    private final boolean allMode;
    private Map<String, Object> appendix = new HashMap<>();

    public DefaultSimulatorDumpWriter(final OutputStream outputStream, final boolean allMode) {
        this.outputStream = outputStream;
        this.allMode = allMode;
    }

    @Override
    public void begin(Board board) throws IOException {
        if (allMode) {
            if (firstTest) {
                outputStream.write("[".getBytes());
                firstTest = false;
            } else {
                outputStream.write(",".getBytes());
            }
            firstBoard = true;
            appendix.clear();
        }
        if (allMode || firstTest) {
            outputStream.write(String.format("{\"settings\": %s, \"boards\": [\n", mapper.writeValueAsString(board.getGameSettings())).getBytes());
        }
    }

    @Override
    public void write(final Board board) throws IOException {
        if (allMode || firstTest) {
            if (!firstBoard) {
                outputStream.write(",".getBytes());
            }
            firstBoard = false;
            outputStream.write(mapper.writeValueAsString(board).getBytes());
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
                outputStream.write(String.format("\n, \"%s\": %s", entry.getKey(), mapper.writeValueAsString(entry.getValue())).getBytes());
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
