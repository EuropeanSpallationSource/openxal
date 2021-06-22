package xal.sim.mpx;

import java.util.EventListener;

/**
 * @author wdklotz
 *  
 * created May 22, 2003
 * 
 */

/**The Interface for objects that listen to events from the on-line model proxy.
 * @author wdklotz
 */
public interface ModelProxyListener extends EventListener {

/**Named constant to indicate the cause 'accelerator changed'.*/
	public static final int ACCEL_CHANGED= 1;
/**Named constant to indicate the cause 'accelerator sequence changed'.*/
	public static final int SEQUENCE_CHANGED= 2;
/**Named constant to indicate the cause 'probe changed'.*/
	public static final int PROBE_CHANGED= 3;
/**Named constant to indicate the cause 'model results changed'.*/
	public static final int RESULTS_CHANGED= 4;
/**Named constant to indicate the cause 'not enough input to run the model'.*/
	public static final int MISSING_INPUT= 5;

/**Called by the model proxy to notify the listener in cause of <code>ACCEL_CHANGED</code>.*/
	public void accelMasterChanged(ModelProxy source);

/**Called by the model proxy to notify the listener in cause of <code>SEQUENCE_CHANGED</code>.*/
	public void accelSequenceChanged(ModelProxy source);

/**Called by the model proxy to notify the listener in cause of <code>PROBE_CHANGED</code>.*/
	public void probeMasterChanged(ModelProxy source);

/**Called by the model proxy to notify the listener in cause of <code>RESULTS_CHANGED</code>.*/
	public void modelResultsChanged(ModelProxy source);

/**Called by the model proxy to notify the listener in cause of <code>MISSING_INPUT</code>.*/
	public void missingInputToRunModel(ModelProxy source);

} ///////////////////
