package se.lu.esss.ics.jels.model.elem.jels;

import se.lu.esss.ics.jels.model.elem.els.IdealDrift;
import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.tools.math.TTFIntegrator;
import xal.model.IProbe;
import xal.model.elem.ElementSeq;
import xal.sim.scenario.LatticeElement;

/**
 * Implementation of NCells simulation of fieldmaps.
 * Uses numeric integrator for TTF function 
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class FieldMapNCells extends ElementSeq {
	private IdealDrift[] drifts;
	private IdealRfGap[] gaps;
	
	public FieldMapNCells() {
        this(null);
    }
	
	public FieldMapNCells(String strId) {
		super("RfGapWithTTFIntegrator", strId, 3);
	}

	@Override
	public void initializeFrom(LatticeElement latticeElement) {
		super.initializeFrom(latticeElement);
	    final ESSFieldMap fm = (ESSFieldMap)latticeElement.getNode();
	    TTFIntegrator intgr = TTFIntegrator.getInstance(fm.getFieldMapFile()+".edz", fm.getFrequency()*1e6);
	    
	    TTFIntegrator[] splitIntgrs = intgr.getSplitIntegrators();
	    
	    /*
	     * Old implementation of IdealRfGap is used. First gap phase is calculated when the energy at the
	     * entrace into the gap is known. Also TTF integrator is supplied with the necessary offset.
	     */
	    gaps = new IdealRfGap[splitIntgrs.length];
	    drifts = new IdealDrift[splitIntgrs.length*2];
	    
	    for (int i=0; i<splitIntgrs.length; i++) {
	    	final double l1 = splitIntgrs[i].getCenter();
	    	double l2 = splitIntgrs[i].getLength() - l1;
	    	
	    	drifts[2*i] = new IdealDrift();
	    	drifts[2*i].setId(fm.getId()+":DR"+2*i);
			drifts[2*i].setLength(l1);
						
		    gaps[i] = new IdealRfGap(fm.getId(), splitIntgrs[i].getE0TL()*fm.getXelmax(),0, fm.getFrequency()*1e6) {
		    	@Override
		    	public void calculatePhase(IProbe probe)
		    	{    		
		    		double dphi = 2*Math.PI*getFrequency()*l1/probe.getBeta()/LightSpeed;
		    		setPhase(fm.getPhase()*Math.PI/180. + dphi);	
		    	}
		    };
			
			gaps[i].setTTFFit(splitIntgrs[i].integratorWithOffset(l1,i*Math.PI));
			gaps[i].setFirstGap(i==0);
			gaps[i].setCellLength(fm.getLength());
			gaps[i].setE0(fm.getXelmax());
			gaps[i].setStructureMode(1);
			
			drifts[2*i+1] = new IdealDrift();
			drifts[2*i+1].setId(fm.getId()+":DR"+(2*i+1));
			drifts[2*i+1].setLength(l2);	
			
			addChild(drifts[2*i]);
			addChild(gaps[i]);
			addChild(drifts[2*i+1]);	
	    }
	}

}
