package edu.stanford.slac.util.zplot;

import java.util.EventObject;

/**
 * ZPlotEvent
 *
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public class ZPlotEvent extends EventObject{

	/**
	 * My Field (please, document me!)
	 */
	private static final long serialVersionUID = -7602688628460665326L;
	
	
	public ZPlotEvent(ZPlotPanel zpp){
		super(zpp);
	}
	
	@Override
	public ZPlotPanel getSource() {
		return (ZPlotPanel) super.getSource();
	}	
	

}
