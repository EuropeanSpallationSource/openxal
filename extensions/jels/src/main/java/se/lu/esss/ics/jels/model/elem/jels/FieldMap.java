package se.lu.esss.ics.jels.model.elem.jels;

import org.jblas.DoubleMatrix;

import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.tools.math.TTFIntegrator;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThickElement;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * This is direct fieldmap implementation, matching TraceWin implementation.
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class FieldMap extends ThickElement  {
	private double field[];
	private double phi0;
	private double frequency;
	private double k0;
	private double phase[];
	private double E[];
	
	public FieldMap() {
        this(null);
    }
	
	public FieldMap(String strId) {
		super("FieldMap", strId, 3);
	}

	@Override
	public void initializeFrom(LatticeElement latticeElement) {
		super.initializeFrom(latticeElement);
	    final ESSFieldMap fm = (ESSFieldMap)latticeElement.getNode();
	    final TTFIntegrator intgr = TTFIntegrator.getInstance(fm.getFieldMapFile()+".edz", fm.getFrequency()*1e6);
	    field = intgr.getField();
	    phi0 = fm.getPhase()/180.*Math.PI;
	    frequency = fm.getFrequency() * 1e6;
	    setLength(intgr.getLength());
	    k0 = fm.getXelmax();
	}
	
	/**
	 * This method precalculates the energy and phase on the whole current element, so that we don't have
	 * any problems when a probe visits of smaller parts of the element.
	 *  
	 * @param beta energy at the start of the element
	 * @param E0 energy at the start of the element
	 * @param Er particles rest energy
	 */
	private void initPhase(double beta, double E0, double Er)
	{
		if (phase != null) return;
		
		phase = new double[field.length];
		E = new double[field.length];
		
		double phi = phi0;
		double dz = getLength() / field.length;
		double DE = 0.;
		
		for (int i = 0; i < field.length; i++)
		{
			phase[i] = phi;
			E[i] = E0+DE;
			
			DE += k0 * field[i]*Math.cos(phi)*dz;
			double gamma = (E0+DE)/Er + 1.0;
			beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));
			phi += 2*Math.PI*frequency*dz / (beta * LightSpeed);
		}
	}
	
	
	@Override
	public double energyGain(IProbe probe, double dblLen) {
		initPhase(probe.getBeta(), probe.getKineticEnergy(), probe.getSpeciesRestEnergy());
		
		double p0 = probe.getPosition() - (getPosition() - getLength()/2.);
		int i0 = (int)Math.round(p0/getLength()*field.length);
		int in = (int)Math.round((p0+dblLen)/getLength()*field.length);
		
		return E[Math.min(in,field.length-1)] - E[i0];
	}

	private double field(int i)
	{
		return i<0?0.: (i>=field.length ? 0  : field[i]);
	}
	
	
	/**
	 * Method calculates transfer matrix for the fieldmap on the current range (i.e from probe.getPosition, and for dblLength).
	 * It does so by numerically integrating equations of motion and calculating matrix exponent of them, to get the transfer matrix.
	 */
	@Override
	public PhaseMap transferMap(IProbe probe, double dblLen)
			throws ModelException {
		initPhase(probe.getBeta(), probe.getKineticEnergy(), probe.getSpeciesRestEnergy());
		
		double p0 = probe.getPosition() - (getPosition() - getLength()/2.);
		int i0 = (int)Math.round(p0/getLength()*field.length);
		int in = (int)Math.round((p0+dblLen)/getLength()*field.length);
		
		double dz = getLength() / field.length;
	//	double beta = probe.getBeta();
		//double E0 = probe.getKineticEnergy();
	//	double DE = 0.;
		double Er = probe.getSpeciesRestEnergy();
		
		PhaseMatrix t = PhaseMatrix.identity();
		double gamma = 0;
		
		for (int i = i0; i < in && i < field.length; i++)
		{
			gamma = (E[i])/Er + 1.0;
			double beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));
			double DE = k0 * field[i]*Math.cos(phase[i])*dz;
			double dgamma = DE/Er;
			
			//double Ez0 = k0 * field[i];
			double Ez = k0 * field[i] * Math.cos(phase[i]);
			//double pEz_pz = k0 * (field(i+1)-field(i-1))/(2.*dz) * Math.cos(phase[i])
			//double pEz_pz = k0 * field[i] * Math.sin(phase[i]) * (phase[Math.min(phase.length-1,i+1)]-phase[i])/ dz;
			
			// partial derivative of E_z by z: seems to be a bug in TW here, since derivative is done over time
			double pEz_pz = k0 * field[i] * Math.sin(phase[i]) * 2*Math.PI*frequency / (beta * LightSpeed);
					
			double pEx_px = - 0.5 * k0 * (field(i+1)-field(i-1))/(2.*dz) * Math.cos(phase[i]);
			double pBx_py = 2*Math.PI*frequency / (2. * LightSpeed * LightSpeed) * k0 * field[i] * Math.sin(phase[i]);
			double pBy_px = -pBx_py;
			
			double k = 1. / (gamma*Math.pow(beta,2)*Er);
			DoubleMatrix Ax = new DoubleMatrix(new double[][] {{0,1},{k*(pEx_px-beta*LightSpeed*pBy_px), -k*Ez}}).mul(dz);
			DoubleMatrix Ay = new DoubleMatrix(new double[][] {{0,1},{k*(pEx_px+beta*LightSpeed*pBx_py), -k*Ez}}).mul(dz);
			DoubleMatrix Az = new DoubleMatrix(new double[][] {{0,1},{k*pEz_pz / (gamma * gamma), -k*(2-beta*beta)*Ez  - dgamma/gamma }}).mul(dz);
//			DoubleMatrix Az = new DoubleMatrix(new double[][] {{0,1./(gamma*(gamma+dgamma))},{k*pEz_pz, -k*(2-beta*beta)*Ez }}).mul(dz);
//			DoubleMatrix Az = new DoubleMatrix(new double[][] {{0,1/(gamma*gamma)},{k*pEz_pz, -k*(2-beta*beta)*Ez}}).mul(dz);
			
			PhaseMatrix pm = new PhaseMatrix();
			pm.setSubMatrix(0, 1, 0, 1, org.jblas.MatrixFunctions.expm(Ax).toArray2());
			pm.setSubMatrix(2, 3, 2, 3, org.jblas.MatrixFunctions.expm(Ay).toArray2());
			pm.setSubMatrix(4, 5, 4, 5, org.jblas.MatrixFunctions.expm(Az).toArray2());
			
			double gammae = (E[Math.min(i+1,E.length-1)])/Er + 1.0;
			double betae = Math.sqrt(1.0 - 1.0/(gammae*gammae));
		
			// Following line fixes the determinant of longitudinal transfer matrix
			// This is another bug in TW.
			pm.setElem(4, 4, ((beta*gamma)/(betae*gammae) + pm.getElem(4,5)*pm.getElem(5,4)) / pm.getElem(5, 5));
			
			t = pm.times(t);	
		}
		
		//Following is a handy printout of transfer matrices useful for comparison with TW transfer matrices
		
		/*PhaseMap tw = new PhaseMap(t);
		ROpenXal2TW(probe.getGamma(), gamma, tw);
		
		System.out.printf("%E ", probe.getPosition());
		for (int j=0; j<6; j++)
			for (int k=0; k<6; k++)
				System.out.printf("%E ", tw.getFirstOrder().getElem(j, k));
		System.out.println();*/

		return new PhaseMap(t);
	}

	public static void ROpenXal2TW(double gamma_start, double gamma_end, PhaseMap pm) {		
		PhaseMatrix r = pm.getFirstOrder();
		
		for (int i=0; i<6; i++) {
			r.setElem(i, 4, r.getElem(i,4)*gamma_start);
			r.setElem(i, 5, r.getElem(i,5)/gamma_start);
			r.setElem(4, i, r.getElem(4,i)/gamma_end);
			r.setElem(5, i, r.getElem(5,i)*gamma_end);			
		}			
		pm.setLinearPart(r);
	}

	
	@Override
	public double elapsedTime(IProbe probe, double dblLen) {
		return 0;
	}
			
}
