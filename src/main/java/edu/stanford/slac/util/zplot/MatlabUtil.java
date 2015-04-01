package edu.stanford.slac.util.zplot;

import edu.stanford.slac.util.zplot.ZPlot.RendererType;


public class MatlabUtil {

	public static RendererType getRendererType(int i) {
		switch (i) {
		case 0:
			return RendererType.FEATHER;
		case 1:
			return RendererType.LINE;
		}
		return null;
	}
	
	public static void skipSubplot(ZPlot zPlot, int index){
		zPlot.getSkippedSubplotIndices().add(index);
	}
}
