package se.lu.esss.ics.jels.tools.math;

import java.util.ArrayList;
import java.util.List;

import se.lu.esss.ics.jels.smf.impl.FieldProfile;
import xal.model.IElement;
import xal.tools.math.poly.UnivariateRealPolynomial;

/**
 * Taking care of calculating TTF via numerical integration from field data.
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 */
public class TTFIntegrator extends UnivariateRealPolynomial {
	private int N;
	private double zmax;
	private double[] field;
	private double e0tl;

	private double frequency;
	private boolean inverted;
	//Caching -- not static as we do not want a polynomial with different coefficients to use it.
	private double ezzSin;
	private double ezzSinBeta = 0;
	private double ezCos0;
	private double cos0Beta = 0;

	/**
	 * Constructs new TTFIntegrator
	 */
	public TTFIntegrator(double zmax, double[] field, double frequency, boolean inverted)
	{
		this.N = field.length;
		this.zmax = zmax;
		this.field = field;
		this.frequency = frequency;
		this.inverted = inverted;
		initalizeE0TL();
	}



	/************************** Numerical methods ***************************/

	public double getE0TL()
	{
		return e0tl;
	}


	/**
	 * Calculates e0tl as \int |E(z)|dz.
	 */
	public void initalizeE0TL()
	{
		double v = 0.;
		for (int k=0; k<N; k++) v += Math.abs(field[k]);
		e0tl = v * zmax/N;;
	}

	/**
	 * Calculates synchronous phase given input phase and input beta.
	 * atan( \int E(z)sin(..)dz / \int E(z)cos(..)dz )
	 * @param beta0 input beta
	 * @return synchronous phase
	 */
	public double getSyncPhase(double beta0)
	{
		return Math.atan2(evaluateEzSin(beta0), evaluateEzCos(beta0));
	}

	/**
	 * Evaluates TTF at beta, actually a cosine transform of the field. 
	 * @param beta energy
	 * @return TTF
	 */
	public double evaluateEzCos(double beta)
	{
		double phi = 0;
		double dz = getLength() / field.length;
		double DE = 0.;

		for (int i = 0; i < field.length; i++)
		{			
			DE += field[i]*Math.cos(phi)*dz;
			phi += 2*Math.PI*frequency*dz / (beta * IElement.LightSpeed);
		}
		return DE/e0tl;
	}

	/**
	 * Evaluates sine transform of the field. 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return sine transform
	 */
	public double evaluateEzSin(double beta)
	{
		double phi = 0;
		double dz = getLength() / field.length;
		double DS = 0.;

		for (int i = 0; i < field.length; i++)
		{			
			DS += field[i]*Math.sin(phi)*dz;
			phi += 2*Math.PI*frequency*dz / (beta * IElement.LightSpeed);
		}
		return DS/e0tl;
	}
	
	/**
	 * Evaluates cosine transform of the field with sync phase offset. 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return sine transform
	 */
	public double evaluateEzCos0(double beta)
	{
	    if(beta == cos0Beta){
            return ezCos0;
        }
	    
		double dz = getLength() / field.length;
		double dphi = 2*Math.PI*frequency*dz / (beta * IElement.LightSpeed);
		double DC = 0., DS = 0.;

		for (int i = 0; i < field.length; i++)
		{	
			DC += field[i]*Math.cos(i*dphi);
			DS += field[i]*Math.sin(i*dphi);
		}
		cos0Beta = beta;
		return ezCos0 = Math.sqrt(DC*DC+DS*DS)*dz/e0tl;
	}

	/**
	 * Evaluates derivative of TTF at beta (with sync phase shift). 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return derivative of TTF
	 */
	public double evaluateEzzSin(double beta)
	{
	    
	    if(beta == ezzSinBeta){
            return ezzSin;
        }
		double dz = getLength() / field.length;
		double DZS = 0., DZC = 0.;
		double DC = 0., DS = 0.;
		double dphi = 2*Math.PI*frequency*dz / (beta * IElement.LightSpeed);
		
		for (int i = 0; i < field.length; i++)
		{
			double phi = i*dphi;
			double a = field[i]*Math.cos(phi);
			double b = field[i]*Math.sin(phi);
	        DC += a;
	        DS += b;
			DZS += b*i;
			DZC += a*i;
		}
		double DZ = (DZS*DC-DZC*DS)*dz*dz/Math.sqrt(DC*DC+DS*DS);
        ezzSinBeta = beta;
        return ezzSin = DZ/e0tl*2 * Math.PI * frequency / beta / beta / IElement.LightSpeed;
	}


	/************************** Splitting methods ***************************/

	public static TTFIntegrator[] getSplitIntegrators(FieldProfile fp, double frequency)
	{
		double[] field = fp.getField();
		
		List<Integer> pos = new ArrayList<>();
		pos.add(0);
		boolean invert = false;
		for (int i = 0; i<field.length-1; i++)
			if (field[i]*field[i+1] < 0.) {
				if (field[i] < 0 && pos.size() == 1) invert = true;
				pos.add(i+1);
			}
		pos.add(field.length);

		TTFIntegrator[] splitIntgrs = new TTFIntegrator[pos.size()-1];
		for (int i=0; i<pos.size()-1; i++)
		{
			double[] subfield = new double[pos.get(i+1)-pos.get(i)];
			System.arraycopy(field, pos.get(i), subfield, 0, subfield.length);
			if (invert) 
				for (int j = 0; j<subfield.length; j++) subfield[j] = -subfield[j];
			splitIntgrs[i] = new TTFIntegrator((pos.get(i+1)-pos.get(i))*fp.getLength()/field.length, subfield, frequency, invert);
			invert = !invert;
		}

		return splitIntgrs;
	}

	public boolean getInverted()
	{
		return inverted;
	}

	public double getLength()
	{
		return zmax;
	}	

	/*****************  TTF function methods ************************************/

	@Override
	public double getCoef(int iOrder) {
		return 1.0; // fake coef to trigger evaluations
	}

	@Override
	public double evaluateAt(double beta) {
		return evaluateEzCos0(beta);
	}

	@Override
	public double evaluateDerivativeAt(double beta) {
		return TTFIntegrator.this.evaluateEzzSin(beta);
	}
	
	@Override
	public String toString() {
		return null;
	}

	/**
	 * Returns the field on which current integrator is working.
	 *  
	 *  @return the field
	 */
	public double[] getField() {
		return field;
	}

}
