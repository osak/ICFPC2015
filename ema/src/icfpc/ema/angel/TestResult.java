package icfpc.ema.angel;

import com.google.common.collect.Lists;
import com.google.gag.annotation.remark.ThisWouldBeOneLineIn;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author tomoyuki
 */

public class TestResult {
    public URL problemFile = null;
    public Integer seed = null;

    public TestResult(final URL problemFile, final int seed) {
        this.problemFile = problemFile;
        this.seed = seed;
    }

    public String show() throws URISyntaxException, MalformedURLException {
        String[] ss = problemFile.getFile().split("/");
        return "problemFile: " + ss[ss.length - 1] + ", seed: " + seed.toString();
    }
}
