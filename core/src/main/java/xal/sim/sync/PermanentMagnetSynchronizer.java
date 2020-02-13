/*
 * Created on Oct 14, 2014
 */
package xal.sim.sync;

import java.util.Map;
import xal.model.IComponent;

import xal.model.elem.IdealPermMagQuad;

import xal.smf.proxy.PermanentMagnetPropertyAccessor;

/**
 * Synchronizes IdealPermMagQuad elements using the supplied value map.
 * For now just permanent quads, but may be generalized in the future to other permanent magnets
 *
 * @author Tom Pelaia
 */
public class PermanentMagnetSynchronizer implements Synchronizer {

	/* resync the element from the value map */
         @Override
	public void resync( final IComponent aComp, final Map<String,Double> valueMap ) throws SynchronizationException {
		if ( !( aComp instanceof IdealPermMagQuad ) ) {
			throw new IllegalArgumentException( "expected IdealPermMagQuad instance, got: " + aComp.getClass().getName() );
		}

		final IdealPermMagQuad mag = (IdealPermMagQuad)aComp;
		final Double field = valueMap.get( PermanentMagnetPropertyAccessor.PROPERTY_FIELD );
		if ( field == null )  throw new SynchronizationException("missing value for Field property");
		mag.setMagField(field);
	}

	
	/* check synchronization */
         @Override
	public void checkSynchronization( final IComponent aComp, final Map<String,Double> valueMap ) throws SynchronizationException {
		if ( !(aComp instanceof IdealPermMagQuad) ) {
			throw new IllegalArgumentException( "expected IElectromagnet instance, got: " + aComp.getClass().getName() );
		}

		final IdealPermMagQuad mag = (IdealPermMagQuad)aComp;
		final Double field = valueMap.get( PermanentMagnetPropertyAccessor.PROPERTY_FIELD );
		if ( field == null )  throw new SynchronizationException( "missing value for Field property: " );
		if ( mag.getMagField() != field )  throw new SynchronizationException( "synchronized value doesn't agree with node property" );
	}
	
}
