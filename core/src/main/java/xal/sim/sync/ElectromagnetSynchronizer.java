/*
 * Created on Oct 28, 2003
 */
package xal.sim.sync;

import java.util.Map;
import xal.model.IComponent;
import xal.model.elem.sync.IElectromagnet;
import xal.smf.proxy.ElectromagnetPropertyAccessor;

/**
 * Synchronizes IElectromagnet elements using the supplied value map.
 *
 * @author Craig McChesney
 */
public class ElectromagnetSynchronizer implements Synchronizer {

	/*
	 * @see xal.model.sync.Synchronizer#resync(xal.model.IElement, java.util.Map)
	 */
        @Override
	public void resync( final IComponent aComp, final Map<String,Double> valueMap ) throws SynchronizationException {
		if ( !(aComp instanceof IElectromagnet) )  throw new IllegalArgumentException( "expected IElectromagnet instance, got: " + aComp.getClass().getName() );
		final IElectromagnet mag = (IElectromagnet) aComp;
		final Double field = valueMap.get( ElectromagnetPropertyAccessor.PROPERTY_FIELD );
		if ( field == null )  throw new SynchronizationException("missing value for Field property");
		mag.setMagField(field);
	}

	
	/*
	 * @see xal.model.sync.Synchronizer#checkSynchronization(xal.model.IElement, java.util.Map)
	 */
        @Override
	public void checkSynchronization( final IComponent aComp, Map<String,Double> valueMap ) throws SynchronizationException {
		if ( !(aComp instanceof IElectromagnet) ) {
			throw new IllegalArgumentException( "expected IElectromagnet instance, got: " + aComp.getClass().getName() );
		}

		final IElectromagnet mag = (IElectromagnet) aComp;
		final Double field = valueMap.get( ElectromagnetPropertyAccessor.PROPERTY_FIELD );
		if ( field == null )  throw new SynchronizationException( "missing value for Field property: " );
		if ( mag.getMagField() != field )  throw new SynchronizationException( "synchronized value doesn't agree with node property" );
	}
	
}
