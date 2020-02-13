/*
 * Created on Mar 17, 2004
 */
package xal.sim.sync;

import java.util.Map;
import xal.model.IComponent;

import xal.model.elem.sync.IRfCavity;
import xal.smf.proxy.RfCavityPropertyAccessor;

/**
 * TODO CKA Add PROPERTY_FREQUENCY property synchronizer??
 * 
 * @author Craig McChesney
 */
public class RfCavitySynchronizer implements Synchronizer {

	/*
	 * @see xal.model.sync.Synchronizer#resync(xal.model.IElement, java.util.Map)
	 */
         @Override
	public void resync( final IComponent aComp, final Map<String,Double> valueMap ) throws SynchronizationException {
		if ( !(aComp instanceof IRfCavity) )  throw new IllegalArgumentException( "expected IRfCavity instance, got: " + aComp.getClass().getName() );
		final IRfCavity rfCav = (IRfCavity) aComp;
		final Double amp = valueMap.get( RfCavityPropertyAccessor.PROPERTY_AMPLITUDE );
		if ( amp == null )  throw new SynchronizationException("missing value for RF Amplitude property");
		rfCav.setCavAmp(amp);

		final Double phase = valueMap.get( RfCavityPropertyAccessor.PROPERTY_PHASE );
		if ( phase == null )  throw new SynchronizationException("missing value for RF Phase property");
		rfCav.setCavPhase(phase);
	}

	
	/*
	 * @see xal.model.sync.Synchronizer#checkSynchronization(xal.model.IElement, java.util.Map)
	 */
         @Override
	public void checkSynchronization( final IComponent aComp, final Map<String,Double> valueMap ) throws SynchronizationException {
		if ( !(aComp instanceof IRfCavity) ) throw new IllegalArgumentException( "expected IRfCavity instance, got: " + aComp.getClass().getName() );
		final IRfCavity rfCav = (IRfCavity) aComp;
		final Double amp = valueMap.get( RfCavityPropertyAccessor.PROPERTY_AMPLITUDE );
		if ( amp == null ) throw new SynchronizationException("missing value for RF Amplitude property");
		if ( rfCav.getCavAmp() != amp )throw new SynchronizationException("synchronized value doesn't agree with node property");

		final Double phase = valueMap.get( RfCavityPropertyAccessor.PROPERTY_PHASE );
		if ( phase == null ) throw new SynchronizationException("missing value for RF Phase property");
		if ( rfCav.getCavPhase() != phase ) throw new SynchronizationException("synchronized value doesn't agree with node property");
	}

}
