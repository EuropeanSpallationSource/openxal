/*
 * DefaultElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package se.lu.esss.ics.jels.model.elem.els;

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.elem.IdealRfCavityDrift;
import xal.model.elem.Marker;
import xal.sim.scenario.ElementMapping;

/**
 * The default element mapping implemented as singleton.
 * 
 * @author Ivo List
 *
 */
public class ElsElementMapping extends ElementMapping {
	protected static ElementMapping instance;	
	private static boolean warning = false;

	protected ElsElementMapping() {
		initialize();
	}
	
	/**
	 *  Returns the default element mapping.
	 *  
	 * @return the default element mapping
	 */
	public static ElementMapping getInstance()
	{
		if (instance == null) instance = new ElsElementMapping();
		return instance;
	}
	
	
	@Override
	public Class<? extends IComponent> getDefaultElementType() {
		return Marker.class;
	}

	@Override
	public IComponent createDefaultDrift(String name, double len) {
		return new IdealDrift(name, len);
	}
	
	@Override
	public IComponent createRfCavityDrift(String name, double len, double freq, double mode) throws ModelException {
		return new IdealRfCavityDrift(name, len, freq, mode);		
	}
	
	protected void initialize() {
		putMap("dh", IdealMagWedgeDipole2.class);
		putMap("q", IdealMagQuad.class);
		putMap("qt", IdealMagQuad.class);
		putMap("pq", IdealMagQuad.class);
		putMap("rfgap", IdealRfGap.class);
		putMap("marker", Marker.class);
	}
	
	static void printWarning()
	{
		if (!warning) {
			System.err.println("WARNING: USING ELS ELEMENTS!!! Please check to be sure this is correct.");
			warning = true;
		}
	}

	@Override
	public Class<? extends IComposite> getDefaultSequenceType() {
		return Sector.class;
	}
}
