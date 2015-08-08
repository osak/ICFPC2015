package icfpc.common;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author masata
 */
public enum Command {
    MOVE_W,
    MOVE_E,
    MOVE_SW,
    MOVE_SE,
    CLOCK,
    C_CLOCK,
    NOOP,
    INVALID;

    private static final Map<Character, Command> COMMAND_MAP = ImmutableMap.<Character, Command>builder()
            .put('p', MOVE_W)
            .put('\'', MOVE_W)
            .put('!', MOVE_W)
            .put('.', MOVE_W)
            .put('0', MOVE_W)
            .put('3', MOVE_W)

            .put('b', MOVE_E)
            .put('c', MOVE_E)
            .put('e', MOVE_E)
            .put('f', MOVE_E)
            .put('y', MOVE_E)
            .put('2', MOVE_E)

            .put('a', MOVE_SW)
            .put('g', MOVE_SW)
            .put('h', MOVE_SW)
            .put('i', MOVE_SW)
            .put('j', MOVE_SW)
            .put('4', MOVE_SW)

            .put('l', MOVE_SE)
            .put('m', MOVE_SE)
            .put('n', MOVE_SE)
            .put('o', MOVE_SE)
            .put(' ', MOVE_SE)
            .put('5', MOVE_SE)

            .put('d', CLOCK)
            .put('q', CLOCK)
            .put('r', CLOCK)
            .put('v', CLOCK)
            .put('z', CLOCK)
            .put('1', CLOCK)

            .put('k', C_CLOCK)
            .put('s', C_CLOCK)
            .put('t', C_CLOCK)
            .put('u', C_CLOCK)
            .put('w', C_CLOCK)
            .put('x', C_CLOCK)

            .put('\t', NOOP)
            .put('\n', NOOP)
            .put('\r', NOOP)

            .build();

    public static Command fromChar(char c) {
        if (COMMAND_MAP.containsKey(c)) {
            return COMMAND_MAP.get(c);
        } else {
            return INVALID;
        }
    }
}
