/*
 * DefaultElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package se.lu.esss.ics.jels.model.elem.jels;

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.elem.IdealDrift;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.IdealMagSteeringDipole;
import xal.model.elem.IdealRfCavityDrift;
import xal.model.elem.Marker;
import xal.sim.scenario.ElementMapping;

/**
 * The default element mapping implemented as singleton.
 * 
 * @author Ivo List
 *
 */
public class JElsElementMapping extends ElementMapping {
	protected static ElementMapping instance;	

	public JElsElementMapping() {
		initialize();
	}
	
	/**
	 *  Returns the default element mapping.
	 *  
	 * @return the default element mapping
	 */
	public static ElementMapping getInstance()
	{
		if (instance == null) instance = new JElsElementMapping();
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
		putMap("fm", FieldMapNCells.class);
		putMap("dh", IdealMagWedgeDipole2.class);
		putMap("q", IdealMagQuad.class);
		putMap("qt", IdealMagQuad.class);
		putMap("pq", IdealMagQuad.class);
		putMap("rfgap", IdealRfGap.class);
		putMap("dch", IdealMagSteeringDipole.class);
		putMap("dcv", IdealMagSteeringDipole.class);
		putMap("marker", Marker.class);
	}

	@Override
	public Class<? extends IComposite> getDefaultSequenceType() {
		return Sector.class;
	}
}
