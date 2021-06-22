package xal.sim.mpx;
/**
 *@author wdklotz
 * 
 * created May 22, 2003
 */

/**A convenience adaptor that implements default methods for the ModelProxyListener
 * interface. This class is meant to be used in nested implementations of the
 * ModelProxyListener interface:<p>
 * <pre><code>
 * ModelProxy mproxy = new ModelProxy();
 * mproxy.addModelProxyListener(new ModelProxyListenerAdaptor() {
*
*  //override the default implementation
*	public void modelResultsChanged(ModelProxy mp) {
*	:
*	custom code
*	:
*	}
 *  });
 * </code></pre>
 *
 *@author wdklotz
 * 
 */
public abstract class ModelProxyListenerAdaptor implements ModelProxyListener {

	/* (non-Javadoc)
	 * @see xal.model.mpx.MPXProxyListener#accelMasterChanged(MPXProxy)
	 */
        @Override
	public void accelMasterChanged(ModelProxy source) {
	}

	/* (non-Javadoc)
	 * @see xal.model.mpx.ModelProxyListener#accelSequenceChanged(ModelProxy)
	 */
        @Override
	public void accelSequenceChanged(ModelProxy source) {
	}

	/* (non-Javadoc)
	 * @see xal.model.mpx.ModelProxyListener#probeMasterChanged(ModelProxy)
	 */
        @Override
	public void probeMasterChanged(ModelProxy source) {
	}

	/* (non-Javadoc)
	 * @see xal.model.mpx.ModelProxyListener#modelResultsChanged(ModelProxy)
	 */
        @Override
	public void modelResultsChanged(ModelProxy source) {
	}

	/* (non-Javadoc)
	 * @see xal.model.mpx.ModelProxyListener#missingInputToRun(ModelProxy)
	 */
        @Override
	public void missingInputToRunModel(ModelProxy source) {
	}

}
