package edu.stanford.slac.util.zplot;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * ZPlotToolTipGenerator
 *
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public final class ZPlotToolTipGenerator implements XYToolTipGenerator {

	/**
	 * @see org.jfree.chart.labels.XYToolTipGenerator#generateToolTip(org.jfree.data.xy.XYDataset, int, int)
	 */
	public String generateToolTip(XYDataset dataset, int series, int item) {
		return null;
	}

}
