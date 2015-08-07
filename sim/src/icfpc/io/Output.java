package icfpc.io;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author masata
 */
public class Output {
    public int problemId;
    public int seed;
    public String tag;
    public String solution;
    @JsonProperty(required = false)
    public int expectedScore;
}
