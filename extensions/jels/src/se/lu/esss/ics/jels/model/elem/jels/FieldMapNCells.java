package se.lu.esss.ics.jels.model.elem.jels;

import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.smf.impl.FieldProfile;
import se.lu.esss.ics.jels.tools.math.TTFIntegrator;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ElementSeq;
import xal.model.elem.IdealDrift;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.RfCavity;
import xal.smf.impl.qualify.QualifierFactory;

/**
 * Implementation of NCells simulation of fieldmaps.
 * Uses numeric integrator for TTF function 
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class FieldMapNCells extends ElementSeq {
	private double frequency;
	
	private IdealRfGap[] gaps;
	private TTFIntegrator[] splitIntgrs;
	
	private double phi0;
	private double phipos;
	private boolean inverted;
	
	private Double startPosition;
	private Double sliceStartPosition;
	private double sliceLength;
	
	private FieldMapNCells firstSliceElement;
	
	public FieldMapNCells() {
        this(null);
    }
	
	public FieldMapNCells(String strId) {
		super("RfGapWithTTFIntegrator", strId, 3);
	}

	@Override
	public void initializeFrom(LatticeElement latticeElement) {
		super.initializeFrom(latticeElement);		
		sliceLength = latticeElement.getLength();
		
		if (latticeElement.isFirstSlice()) {
		    final ESSFieldMap fm  = (ESSFieldMap)latticeElement.getHardwareNode();
		    FieldProfile fp = fm.getFieldProfile();	    		    
		    
		    double k0;
		    boolean firstInRFCavity;
		    
		    if (fm.getParent() instanceof RfCavity) {
		    	RfCavity cavity = (RfCavity)fm.getParent();
				phi0 = cavity.getDfltCavPhase()/180.*Math.PI;
				frequency = cavity.getCavFreq() * 1e6;
				phipos = fm.getPhasePosition();	
				k0 = cavity.getDfltCavAmp()*1e6 * fm.getXelmax() / (fp.getE0L(frequency)/fp.getLength());
				
				inverted = fp.isFirstInverted();
				firstInRFCavity = true;
				for (ESSFieldMap fm2 : cavity.getNodesOfClassWithQualifier(ESSFieldMap.class, QualifierFactory.getStatusQualifier(true))) {
					if (fm2.getPosition() < fm.getPosition()) {
						firstInRFCavity = false;
						break;
					}
				}		
		    } else {
		    	frequency = fm.getFrequency()*1e6;
		    	phi0 = fm.getPhase()*Math.PI/180.;		    	
		    	k0 = fm.getXelmax();
		    	firstInRFCavity = true;
		    	inverted = false;
		    }
		    
		    /*
		     * Old implementation of IdealRfGap is used. First gap phase is calculated when the energy at the
		     * entrance into the gap is known. Also TTF integrator is supplied with the necessary offset.
		     */
		    
		    splitIntgrs = TTFIntegrator.getSplitIntegrators(fp, frequency);
		    gaps = new IdealRfGap[splitIntgrs.length];		    
		    
		    for (int i=0; i<splitIntgrs.length; i++) {
		    	gaps[i] = new IdealRfGap(fm.getId(), splitIntgrs[i].getE0TL()*k0, 0, frequency);
				
				gaps[i].setFirstGap(i==0 && firstInRFCavity);
				gaps[i].setCellLength(fm.getLength());
				gaps[i].setE0(splitIntgrs[i].getE0TL()*k0/fm.getLength());				
				gaps[i].setStructureMode(1);
		    }
		    
		} 
		try {
			firstSliceElement = (FieldMapNCells)latticeElement.getFirstSlice().createModelingElement();
		} catch (ModelException e) {
		}
	}
	
	
	public void propagate(IProbe probe) throws ModelException {
		if (sliceStartPosition == null) sliceStartPosition = probe.getPosition();
		firstSliceElement.propagate(probe, sliceStartPosition, sliceStartPosition + sliceLength);				
	}
	
	public void propagate(IProbe probe, double sliceStartPosition, double sliceEndPosition) throws ModelException {
		if (startPosition == null) startPosition = probe.getPosition();
		
		double cellPos = startPosition;
		double pos = probe.getPosition();
		
		//if (sliceStartPosition > pos || pos > sliceEndPosition) 
		//	throw new ModelException("Bad slicing");
		
		//System.out.println("#"+"propagate: " + sliceStartPosition + " " + sliceEndPosition);
		
		for (int i=0; i<splitIntgrs.length; i++) {
			if (cellPos + splitIntgrs[i].getLength() < pos) {
				cellPos += splitIntgrs[i].getLength();
				continue;
			}  // invariant: cellPos + splitIntgrs[i].getLength() >= pos && cellPos < pos
						
			// initialize the cell
			double phim = splitIntgrs[i].getSyncPhase(probe.getBeta()); // phis = phim + phi
			if (phim < 0) phim += 2*Math.PI;
			double l1 =  phim * probe.getBeta() * IElement.LightSpeed / (2*Math.PI*frequency);		
			double l2 = splitIntgrs[i].getLength() - l1;
			if (i==0) {
				double phim0 = phipos / (probe.getBeta() * IElement.LightSpeed / (2*Math.PI*frequency));			    
			    double phiInput = Math.IEEEremainder(phi0 - phim0 - (inverted ? Math.PI : 0.), 2*Math.PI);			    
    			gaps[i].setPhase(phiInput + phim + (splitIntgrs[i].getInverted() ? Math.PI : 0));
			}
			gaps[i].setTTFFit(splitIntgrs[i]);
		
			// propagate
			// before gap
			if (pos - cellPos <= l1) {
				double plen = Math.min(l1 - (pos - cellPos), sliceEndPosition - pos);
				boolean doGap = (plen == l1 - (pos - cellPos));
				new IdealDrift("", plen).propagate(probe);
				pos += plen;
				//System.out.println("#"+i+":drift1 "+l1+" for "+plen);
		
				// the gap
				if (doGap) {
					gaps[i].propagate(probe);
					//System.out.println("#"+i+":gap");
				}				
				
				if (pos >= sliceEndPosition) break;				
			}
			
		
			
			// after gap
			double plen2 = Math.min(l2 - (pos - cellPos - l1), sliceEndPosition - pos);			
			new IdealDrift("", plen2).propagate(probe);
			//System.out.println("#"+i+":drift2 "+l2+" for "+plen2);
			pos += plen2;
			if (pos >= sliceEndPosition) break;
			cellPos += splitIntgrs[i].getLength();
	    }
    }

}
