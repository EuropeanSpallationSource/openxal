package se.lu.esss.ics.jels.model.elem.jels;

import se.lu.esss.ics.jels.model.elem.els.IdealDrift;
import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.tools.math.TTFIntegrator;
import xal.model.elem.ElementSeq;
import xal.sim.scenario.LatticeElement;

public class DriftGapDrift extends ElementSeq {
	private IdealDrift drift1 = new IdealDrift(), drift2 = new IdealDrift();
	private IdealRfGapInputPhase gap = new IdealRfGapInputPhase();
	
	public DriftGapDrift() {
        this(null);
    }
	
	public DriftGapDrift(String strId) {
		super("RfGapWithTTFIntegrator", strId, 3);
		addChild(drift1);
		addChild(gap);
		addChild(drift2);
	}

	@Override
	public void initializeFrom(LatticeElement latticeElement) {
		super.initializeFrom(latticeElement);
	    ESSFieldMap fm = (ESSFieldMap)latticeElement.getNode();

		gap.initialGap = true;
		gap.cellLength = fm.getLength();
		TTFIntegrator intgr = TTFIntegrator.getInstance(fm.getFieldMapFile()+".edz", fm.getFrequency()*1e6);
		gap.TTFFit = intgr;
		gap.setFrequency(fm.getFrequency()*1e6);
		gap.setInputPhase(fm.getPhase()*Math.PI/180.);
		gap.setE0(fm.getXelmax());
		gap.setETL(intgr.getE0TL()*fm.getXelmax());
		drift1.setLength(fm.getLength()/2.);
		drift2.setLength(fm.getLength()/2.);	
	}

}
