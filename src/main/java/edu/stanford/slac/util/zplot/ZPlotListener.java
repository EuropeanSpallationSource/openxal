package edu.stanford.slac.util.zplot;

/**
 * ZPlotListener
 *
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public interface ZPlotListener {
	
	/**
	 * My Method (please, document me!)
	 */
	public void tooltipShown(ZPlotEvent event);

	public void zoomCompleted(ZPlotEvent event);

}
