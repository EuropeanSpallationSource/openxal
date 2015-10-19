package edu.stanford.lcls.modelmanager.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides mapping between names of plots and plot data location in table.
 * 
 * @author Blaz Kranjc
 */
class PlotFunctions {
	// TODO Change this to LinkedHashMap
	static final private List<String> PLOT_FUNCTION = 
			new ArrayList<String>(Arrays.asList(new String[] {
				"ALPHA X & Y", "BETA X & Y", "PSI X & Y", "ETA X & Y",
				"ETAP X & Y", "R11 & R33", "R12 & R34", "R21 & R43", 
				"R22 & R44", "ENERGY & P", "Bmag X & Y"}));

	static final private List<String> PLOT_FUNCTIONS_IDS_1 = 
			new ArrayList<String>(Arrays.asList(new String[] {
				"ALPHA_X", "BETA_X", "PSI_X", "ETA_X",
				"ETAP_X", "R11", "R12", "R21", 
				"R22", "E", "Bmag_X"}));

	static final private List<String> PLOT_FUNCTIONS_IDS_2 = 
			new ArrayList<String>(Arrays.asList(new String[] {
				"ALPHA_Y", "BETA_Y", "PSI_Y", "ETA_Y",
				"ETAP_Y", "R33", "R34", "R43", 
				"R44", "P", "Bmag_Y"}));

	public static String[] getPlotFunctions() {
		return PLOT_FUNCTION.toArray(new String[PLOT_FUNCTION.size()]);
	}

	static public String getPlotFunctionID(String selected, int plotNumber){
		int index = PLOT_FUNCTION.indexOf(selected);
		if (plotNumber == 1) {
			return PLOT_FUNCTIONS_IDS_1.get(index);
		}
		else {
			return PLOT_FUNCTIONS_IDS_2.get(index);
		}
	}
}
