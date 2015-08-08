package icfpc.io;

import icfpc.common.Command;

/**
 * @author masata
 */
public interface CommandReader {
    boolean hasNext();
    Command next();
}
