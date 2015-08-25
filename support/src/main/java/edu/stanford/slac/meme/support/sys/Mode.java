package edu.stanford.slac.meme.support.sys;

import java.util.HashMap;
import java.util.Map;

public class Mode {
    public static Integer LOCAL = 0, DEV = 1, SCI = 2, PROD = 3;
    public static String[] modes = { "LOCAL", "DEV", "SCI", "PROD" };

    public static final Map<String, Integer> modeMap;
    static {
        modeMap = new HashMap<String, Integer>(4);
        modeMap.put(modes[LOCAL], LOCAL);
        modeMap.put(modes[DEV], DEV);
        modeMap.put(modes[SCI], SCI);
        modeMap.put(modes[PROD], PROD);
    }
}
