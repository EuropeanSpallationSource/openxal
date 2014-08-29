/*
 * IdealMagQuad.java
 *
 * Created on October 7, 2002, 10:36 PM
 *
 * Modified:
 *      02/13/03 CKA    - refactored to new model architecture
 *      03/21/03 CKA    - added JavaBean
 */

package se.lu.esss.ics.jels.model.elem.jels;



import java.net.URI;
import java.net.URISyntaxException;

import se.lu.esss.ics.jels.model.elem.jels.TransferMapLoader.TransferMaps;
import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import xal.model.IProbe;
import xal.model.elem.ThickElement;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;

/**
 * A Fake element model implementation. It loads transfer matrices from a file.
 * Limitation is that it works only at single energy.
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class FakeElement extends ThickElement {
    /** string type identifier for all FakeFieldMap objects */
    public static final String s_strType = "FakeFieldMap";
    
    protected double xelmax;
    protected double phase;
    protected double frequency;
    
    protected TransferMaps transferMaps;
    
    
    /*
     * Initialization
     */
         
         
    /** 
     *  Creates a new instance of FieldMap
     *
     *  @param  strId     identifier for this FakeFieldMap object
     *  @param  dblLen    length of the fieldmap
     */
    public FakeElement(String strId, double dblLen) {
        super(s_strType, strId, dblLen);

    };

    /** 
     *  JavaBean constructor - creates a new uninitialized instance of FakeFieldMap
     *
     *  <b>BE CAREFUL</b>
     */
    public FakeElement() {
        super(s_strType);
    };


    /*
     *  ThickElement Protocol
     */

    /**
     * Returns the time taken for the probe to drift through part of the
     * element.
     * 
     *  @param  probe   propagating probe
     *  @param  dblLen  length of subsection to propagate through <b>meters</b>
     *  
     *  @return         the elapsed time through section<bold>Units: seconds</bold> 
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen)  {
        return 0.0;
    }
    
    /**
     *  Return the energy gain imparted to a particular probe.
     *  @param  probe   dummy argument
     *  @param  dblLen  dummy argument
     *  @return         returns a zero value
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return transferMaps.energyGain(probe, dblLen);
    }

	
    /**
     * 
     *  @param  probe   supplies the charge, rest and kinetic energy parameters
     *  @param  length  compute transfer matrix for section of this length
     *  @return         transfer map
     */
    @Override
    public PhaseMap transferMap( final IProbe probe, final double L) {  
    	return new PhaseMap(transferMaps.transferMap(probe, L));
    }    
    
	/**
	 * Conversion method to be provided by the user
	 * 
	 * @param latticeElement the SMF node to convert
	 */
    @Override
	public void initializeFrom(LatticeElement latticeElement) { 
    	super.initializeFrom(latticeElement);
    	ESSFieldMap fm = (ESSFieldMap)latticeElement.getNode();
		this.phase = fm.getPhase();
		this.xelmax = fm.getXelmax();
		this.frequency = fm.getFrequency();
	    double start = getPosition()-getLength()/2.;
	    
	    URI tmFile;
		try {
			tmFile = new URI(fm.getFieldMapFile()).resolve("ess.tm");
			this.transferMaps = TransferMapLoader.getInstance(tmFile).prepare(start, getLength());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	
   	}
};
