package edu.stanford.slac.meme.support.sys;
/**
 * DataMode defines fake data mode or real data mode - the basic choices
 * under which an MEME service may run. 
 */
import java.util.Map;
import java.util.HashMap;

public class DataMode
{
	public static Integer REAL = 0, FAKE = 1;
	public static String[] datamode_names = { "REAL", "FAKE"};

        public static final Map<String, Integer> modeMap;
	static 
	{
		modeMap = new HashMap<String,Integer>(2);
		modeMap.put(datamode_names[REAL], REAL);
		modeMap.put(datamode_names[FAKE], FAKE);
	}
}
