package edu.stanford.lcls.modelmanager.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Provides mapping between names of plots and plot data location in table.
 * 
 * @author Blaz Kranjc
 */
class PlotFunctions {
	static final private LinkedHashMap<String, String[]> PLOT_FUNCTIONS;
	static {
		LinkedHashMap<String, String[]> functions = new LinkedHashMap<String, String[]>();
		functions.put("ALPHA X & Y", new String[]{"ALPHA_X", "ALPHA_Y"});
		functions.put("BETA X & Y", new String[]{"BETA_X", "BETA_Y"});
		functions.put("PSI X & Y", new String[]{"PSI_X", "PSI_Y"});
		functions.put("ETA X & Y", new String[]{"ETA_X", "ETA_Y"});
		functions.put("ETAP X & Y", new String[]{"ETAP_X", "ETAP_Y"});
		functions.put("R11 & R33", new String[]{"R11", "R33"});
		functions.put("R12 & R34", new String[]{"R12", "R34"});
		functions.put("R21 & R43", new String[]{"R21", "R43"});
		functions.put("R22 & R44", new String[]{"R22", "R44"});
		functions.put("ENERGY & P", new String[]{"E", "P"});
		functions.put("meanX & meanY", new String[]{"R17", "R37"});
		functions.put("Bmax X & Y", new String[]{"Bmag_X", "Bmag_Y"});
		functions.put("Ek & ALPHA Z", new String[]{"Ek", "ALPHA_Z"});
		functions.put("BETA Z & PSI Z", new String[]{"BETA_Z", "PSI_Z"});
		functions.put("ETA Z & ETAP Z", new String[]{"ETA_Z", "ETAP_Z"});
		PLOT_FUNCTIONS = functions;
	}
	static final private List<String> FUNCTION_NAMES = new ArrayList<String>(PLOT_FUNCTIONS.keySet());

	/**
	 * Provides supported function names.
	 * @return Array of all function names.
	 */
	public static String[] getPlotFunctions() {
		return FUNCTION_NAMES.toArray(new String[PLOT_FUNCTIONS.size()]);
	}

	/**
	 * Finds column name to plot from selected function string.
	 * @param selected Selected plot function ID.
	 * @param plotNumber Number of plot (1 or 2).
	 * @return Column name.
	 */
	static public String getPlotFunctionID(String selected, int plotNumber){
		String [] functions = PLOT_FUNCTIONS.get(selected);
		return functions[plotNumber - 1];
	}
}
