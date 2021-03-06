package icfpc.io.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import icfpc.io.CommandReader;

/**
 * @author masata
 */
public class Answer {
    public int problemId;
    public int seed;
    public String tag;
    public String solution;
    public int expectedScore;
    public double elapsedTime;

    @JsonIgnore
    public CommandReader getCommandReader() {
        return new AnswerCommandReader();
    }

    public class AnswerCommandReader implements CommandReader {
        private int ix = 0;

        @Override
        public boolean hasNext() {
            return ix < solution.length();
        }

        @Override
        public char next() {
            final char ret = solution.charAt(ix);
            ix++;
            return ret;
        }
    }
}
