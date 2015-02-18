package se.lu.esss.ics.jels.model.elem.jels;

import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.tools.math.TTFIntegrator;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ElementSeq;
import xal.model.elem.IdealDrift;
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
	private TTFIntegrator[] splitIntgrs;
	private double phi0 = 0.;
	private double phase;
	private double frequency;
	
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
	    final TTFIntegrator intgr = TTFIntegrator.getInstance(fm.getFieldMapFile()+".edz", fm.getFrequency()*1e6);
	    
	    splitIntgrs = intgr.getSplitIntegrators();
	    
	    /*
	     * Old implementation of IdealRfGap is used. First gap phase is calculated when the energy at the
	     * entrace into the gap is known. Also TTF integrator is supplied with the necessary offset.
	     */
	    gaps = new IdealRfGap[splitIntgrs.length];
	    drifts = new IdealDrift[splitIntgrs.length*2];
	    frequency = fm.getFrequency()*1e6;
	    phase = fm.getPhase()*Math.PI/180.;
	    
	    double beta;
		if (fm.getFieldMapFile().endsWith("Spoke_F2F")) { beta = 0.5; }
    	else if (fm.getFieldMapFile().endsWith("MB_F2F")) { beta = 0.68; phi0 = Math.PI; }
    	else beta = 0.87;
	    
	    for (int i=0; i<splitIntgrs.length; i++) {
	    	//final double l1 = splitIntgrs[i].getCenter();
	    	final double l1 = splitIntgrs[i].getSyncCenter(beta, phi0);	    	
	    	double l2 = splitIntgrs[i].getLength() - l1;
	    	
	    	drifts[2*i] = new IdealDrift();
	    	drifts[2*i].setId(fm.getId()+":DR"+2*i);
			drifts[2*i].setLength(l1);
						
		    gaps[i] = new IdealRfGap(fm.getId(), splitIntgrs[i].getE0TL()*fm.getXelmax(),0, fm.getFrequency()*1e6);
			
			gaps[i].setTTFFit(splitIntgrs[i].integratorWithOffset(0.));
			gaps[i].setFirstGap(i==0);
			gaps[i].setCellLength(fm.getLength());
			gaps[i].setE0(fm.getXelmax());
			gaps[i].setStructureMode(0);
			
			drifts[2*i+1] = new IdealDrift();
			drifts[2*i+1].setId(fm.getId()+":DR"+(2*i+1));
			drifts[2*i+1].setLength(l2);	
			
			addChild(drifts[2*i]);
			addChild(gaps[i]);
			addChild(drifts[2*i+1]);	
	    }
	}
	
	
	public void propagate(IProbe probe) throws ModelException {
		double phase = this.phase;
		for (int i=0; i<splitIntgrs.length; i++) {			
			double phis = splitIntgrs[i].getSyncPhase(phase, probe.getBeta()) + i*Math.PI + phi0;
			double phil = Math.IEEEremainder(phis - phase, 2*Math.PI);
			if (phil < 0) phil+=2*Math.PI;
			double l1 =  phil / (2*Math.PI*frequency) * probe.getBeta() * IElement.LightSpeed;		
			double l2 = splitIntgrs[i].getLength() - l1;
			if (i==0) {
    			gaps[i].setPhase(phis);
			}
			drifts[2*i].setLength(l1);
			drifts[2*i+1].setLength(l2);
			gaps[i].setTTFFit(splitIntgrs[i].integratorWithOffset(phase - phis));
			
			drifts[2*i].propagate(probe);
			gaps[i].propagate(probe);
			drifts[2*i+1].propagate(probe);
			
			phase = probe.getLastGapPhase() + 2*Math.PI*frequency*l2/(probe.getBeta()*IElement.LightSpeed);
        }
    }

}
