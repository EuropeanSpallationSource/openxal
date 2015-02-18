package se.lu.esss.ics.jels.model.elem.jels;

import se.lu.esss.ics.jels.model.elem.els.IdealDrift;
import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.tools.math.TTFIntegrator;
import xal.model.IProbe;
import xal.model.elem.ElementSeq;
import xal.sim.scenario.LatticeElement;

/**
 * Implementation of drift-gap-drift simulation of fieldmaps.
 * Uses numeric integrator for TTF function 
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class DriftGapDrift extends ElementSeq {
	private IdealDrift drift1 = new IdealDrift(), drift2 = new IdealDrift();
	private IdealRfGap gap;
	
	public DriftGapDrift() {
        this(null);
    }
	
	public DriftGapDrift(String strId) {
		super("RfGapWithTTFIntegrator", strId, 3);
	}

	@Override
	public void initializeFrom(LatticeElement latticeElement) {
		super.initializeFrom(latticeElement);
	    final ESSFieldMap fm = (ESSFieldMap)latticeElement.getNode();
	    final TTFIntegrator intgr = TTFIntegrator.getInstance(fm.getFieldMapFile()+".edz", fm.getFrequency()*1e6);
	    
    	final double pho;	
    	
    	if (fm.getFieldMapFile().endsWith("spokeFieldMap")) pho = -Math.PI;
    	else if (fm.getFieldMapFile().endsWith("medBetaFieldMap")) pho = Math.PI/2.; 
    	else pho = 0.;
    	
	    /*
	     * Old implementation of IdealRfGap is used. Middle phase is calculated when the energy at the
	     * entrance into the gap is known. Also TTF integrator is supplied with the necessary offset.
	     */
	    gap = new IdealRfGap(fm.getId(), intgr.getE0TL()*fm.getXelmax(),0, fm.getFrequency()*1e6) {
	    	@Override
	    	public void calculatePhase(IProbe probe)
	    	{
	    		double inputphase = fm.getPhase()*Math.PI/180.;
	    		double phim = 2*Math.PI*getFrequency() * fm.getLength()/2. / probe.getBeta()/LightSpeed;
	    		setPhase(inputphase + phim + pho);
	    		setTTFFit(intgr);
	    	}
	    };
		gap.setFirstGap(true);
		gap.setCellLength(fm.getLength());
		gap.setE0(fm.getXelmax());
		
		drift1.setId(fm.getId()+":DR1");
		drift1.setLength(fm.getLength()/2.);
		drift2.setId(fm.getId()+":DR2");
		drift2.setLength(fm.getLength()/2.);	

		addChild(drift1);
		addChild(gap);
		addChild(drift2);
	}

}
