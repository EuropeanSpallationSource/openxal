package se.lu.esss.ics.jels.model.elem.jels;

import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import se.lu.esss.ics.jels.smf.impl.FieldProfile;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThickElement;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.BaseMatrix;
import xal.tools.math.GenericMatrix;

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
		FieldProfile fp = fm.getFieldProfile();
		field = fp.getField();
		phi0 = fm.getPhase()/180.*Math.PI;
		frequency = fm.getFrequency() * 1e6;
		setLength(fp.getLength());
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
		phase = new double[field.length];
		
		double phi = phi0;
		double dz = getLength() / field.length;
		double DE = 0.;
		
		for (int i = 0; i < field.length; i++)
		{
			phase[i] = phi;
			//System.out.printf("%E %E\n", getPosition()-getLength()/2.+dz*i, phi);
			
			DE += k0 * field[i]*Math.cos(phi)*dz;
			double gamma = (E0+DE)/Er + 1.0;
			beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));
			phi += 2*Math.PI*frequency*dz / (beta * LightSpeed);
		}
	}
	
	
	@Override
	public double energyGain(IProbe probe, double dblLen) {
		double p0 = probe.getPosition() - (getPosition() - getLength()/2.);
		int i0 = (int)Math.round(p0/getLength()*field.length);
		int in = (int)Math.round((p0+dblLen)/getLength()*field.length);
		
		double DE = 0;
		double dz = getLength() / field.length;
		
		for (int i = i0; i < Math.min(in,field.length-1); i++)
			DE += k0 * field[i]*Math.cos(phase[i])*dz;
		
		return  DE;
	}
	
	/**
	 * Method calculates transfer matrix for the fieldmap on the current range (i.e from probe.getPosition, and for dblLength).
	 * It does so by numerically integrating equations of motion and calculating matrix exponent of them, to get the transfer matrix.
	 */
	@Override
	public PhaseMap transferMap(IProbe probe, double dblLen)
			throws ModelException {
		double p0 = probe.getPosition() - (getPosition() - getLength()/2.);
		int i0 = (int)Math.round(p0/getLength()*field.length);
		int in = (int)Math.round((p0+dblLen)/getLength()*field.length);
		if (in >= field.length) in = field.length;
		
		double dz = getLength() / field.length;
		double E0 = probe.getKineticEnergy();
		double Er = probe.getSpeciesRestEnergy();
		
		GenericMatrix Ttr = new GenericMatrix(2,2);
		Ttr.assignIdentity();

		GenericMatrix Tz = new GenericMatrix(2, 2);
		Tz.assignIdentity();
		
		double gamma;
		
		for (int i = i0; i < in; i++)
		{
			double Edz = field[i] * dz;
			double phi = phase[i];
			double dE = (i == 0 ? field[i+1] : (i == field.length - 1 ? field[i-1] : field[i+1]-field[i-1]))/2.;
			//double dE = (-field(i+2)+8*field(i+1)-8*field(i-1)+field(i-2))/12.; // higher precision derivative
			
			gamma = E0/Er + 1.0;
			double beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));
			double DE = k0 * Edz * Math.cos(phi);
			double dgamma = DE/Er;
			
			double Ezdz = k0 * Edz * Math.cos(phi);
			double pEz_pzdz = k0 * dE * Math.cos(phi);
			
			double pEx_pxdz = - 0.5 * k0 * dE * Math.cos(phi);
			double pBx_pydz = 2*Math.PI*frequency / (2. * LightSpeed * LightSpeed) * k0 * Edz * Math.sin(phi);
			//double pBy_px = -pBx_py;
			
			double k = 1. / (gamma*Math.pow(beta,2)*Er);
			
			double gammae = (E0 + DE)/Er + 1.0;
			double betae = Math.sqrt(1.0 - 1.0/(gammae*gammae));
			
			//double Ax[][] = new double[][] {{0,dz},{k*(pEx_px-beta*LightSpeed*pBy_px)*dz, -k*Ez*dz}};
			double Ay[][] = new double[][] {{0,dz},{k*(pEx_pxdz+beta*LightSpeed*pBx_pydz), -k*Ezdz}};
			//double Az[][] = new double[][] {{0,dz},{k*pEz_pzdz / (gamma * gamma), -k*Ezdz}};
			double Az[][] = new double[][] {{0,dz},{k*pEz_pzdz / (gamma*gamma), Math.log((beta*gamma)/(betae*gammae))}};
			
			// Following line fixes the determinant of longitudinal transfer matrix
			//Az[0][0] = ((beta*gamma)/(betae*gammae) + Az[0][1]*Az[1][0]) / Az[1][1];
			
			GenericMatrix Atr = new GenericMatrix(matrix22Exp(Ay)); 
			Ttr = Atr.times(Ttr);			
			Tz = new GenericMatrix(matrix22Exp(Az)).times(Tz);
			
			E0 += DE;
		}
		
		PhaseMatrix T = PhaseMatrix.identity();
		T.setSubMatrix(0, 1, 0, 1, Ttr.getArrayCopy());
		T.setSubMatrix(2, 3, 2, 3, Ttr.getArrayCopy());
		T.setSubMatrix(4, 5, 4, 5, Tz.getArrayCopy());
		
		//Following is a handy printout of transfer matrices useful for comparison with TW transfer matrices
		/*PhaseMap tw = new PhaseMap(T);
		ROpenXal2TW(probe.getGamma(), gamma, tw);
		
		System.out.printf("%E ", probe.getPosition());
		for (int j=0; j<6; j++)
			for (int k=0; k<6; k++)
				System.out.printf("%E ", tw.getFirstOrder().getElem(j, k));
		System.out.println();*/
		
		return new PhaseMap(T);
	}

	public double[][] matrix22Exp(double[][] A)
	{
		double a = A[0][0], b = A[0][1], c = A[1][0], d = A[1][1];
		double D = Math.pow((a-d),2)+4*b*c;
		
		double cosh, sinhD;
		if (D > 0.) {
			D = Math.sqrt(D);
			cosh = Math.cosh(D/2.);
			sinhD = Math.sinh(D/2.)/D;
		} else if (D < 0.) {
			D = Math.sqrt(-D);
			cosh = Math.cos(D/2.);
			sinhD = Math.sin(D/2.)/D;
		} else {
			cosh = 1;
			sinhD = 0.5;
		}
		
		double exp = Math.exp((a+d)/2.);
		
		
		double m11 = exp * (cosh + (a-d)*sinhD);
		double m12 = 2 * b * exp * sinhD;
		double m21 = 2 * c * exp * sinhD;
		double m22 = exp * (cosh + (d-a)*sinhD);
		
		A[0][0] = m11;
		A[0][1] = m12;
		A[1][0] = m21;
		A[1][1] = m22;
		
		return A;
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
	
	/**
	 * Since it is currently hard to track phase on the probe, this way we initialize the phase
	 * and deinitialize it when the probe pases.
	 * 
	 */
	@Override
	public void propagate(IProbe probe) throws ModelException {
		initPhase(probe.getBeta(), probe.getKineticEnergy(), probe.getSpeciesRestEnergy());
		super.propagate(probe);
		phase = null;
    }
	
}
