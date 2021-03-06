package icfpc.io.model;

import icfpc.common.OriginalCell;
import icfpc.common.Unit;

import java.util.ArrayList;

/**
 * @author masata
 */
public class Problem {
    public int id;
    public ArrayList<Unit> units;
    public int width;
    public int height;
    public ArrayList<OriginalCell> filled;
    public int sourceLength;
    public ArrayList<Integer> sourceSeeds;
}
