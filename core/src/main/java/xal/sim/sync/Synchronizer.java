/*
 * Created on Oct 27, 2003
 */
package xal.sim.sync;


import java.util.Map;
import xal.model.IComponent;


/**
 * Specifies abstract interface for element synchronizers, used by the
 * SynchronizationManager to synchronize lattice elements to a variety of data
 * sources.
 * 
 * @author Craig McChesney
 */
public interface Synchronizer {
	
	void resync( final IComponent aComp, final Map<String,Double> valueMap )
			throws SynchronizationException;
			
	void checkSynchronization( final IComponent aComp, final Map<String,Double> valueMap )
			throws SynchronizationException;

}
