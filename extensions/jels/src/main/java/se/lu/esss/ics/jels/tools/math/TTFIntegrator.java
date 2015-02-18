package se.lu.esss.ics.jels.tools.math;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xal.model.IElement;
import xal.tools.math.poly.UnivariateRealPolynomial;

/**
 * Taking care of calculating TTF via numerical integration from field data.
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 */
public class TTFIntegrator {
	private static Map<String, TTFIntegrator> instances = new HashMap<>();

	private int N;
	private double zmax;
	private double[] field;
	private double e0tl;

	private double frequency;
	private boolean inverted;

	/**
	 * Constructs new TTFIntegrator and loads field from the file
	 * @param path path to the file
	 * @param frequency frequency used in integration
	 */
	public TTFIntegrator(String path, double frequency) {
		try {
			this.frequency = frequency;
			loadFile(path);
			initalizeE0TL();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	protected TTFIntegrator(int N, double zmax, double[] field, double frequency, boolean inverted)
	{
		this.N = N;
		this.zmax = zmax;
		this.field = field;
		this.frequency = frequency;
		this.inverted = inverted;
		initalizeE0TL();
	}

	/************************** File manipulation ***************************/

	/**
	 * Loads field from a file.
	 * @param path path to the file
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void loadFile(String path) throws IOException, URISyntaxException {
		BufferedReader br = new BufferedReader(new FileReader(new File(new URI(path))));

		// first line
		String line = br.readLine();
		String[] data = line.split(" ");

		N = Integer.parseInt(data[0]);
		zmax = Double.parseDouble(data[1]);
		field = new double[N];

		line = br.readLine();
		@SuppressWarnings("unused")
		double norm = Double.parseDouble(line);

		int i = 0;
		while ((line = br.readLine()) != null && i<N) {
			field[i++] = Double.parseDouble(line)*1e6;
		}

		br.close();
	}

	/**
	 * Saves given field to a file
	 * @param data the field 
	 * @param path path to the file
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@SuppressWarnings("unused")
	private void saveFile(double[] data, String path) throws IOException, URISyntaxException {
		PrintWriter pw = new PrintWriter(new FileWriter(new File(new URI(path))));
		for (int i = 0; i<N; i++)
			pw.printf("%E\n", data[i]);
		pw.close();
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
	 * @param phi0 input phase
	 * @param beta0 input beta
	 * @return synchronous phase
	 */
	public double getSyncPhase(double phi0, double beta0, double Er, double k0)
	{
		return Math.atan2(evaluateEzSin(phi0, beta0, k0, Er), evaluateEzCos(phi0, beta0, k0, Er));
	}

	/**
	 * Evaluates TTF at beta, actually a cosine transform of the field (with given phase shift). 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return TTF
	 */
	public double evaluateEzCos(double phi0, double beta, double k0, double Er)
	{
		double phi = phi0;
		double dz = getLength() / field.length;
		double DE = 0.;

		double gamma = Math.sqrt(1 / (1 - beta*beta));
		double E0 = (gamma - 1) * Er; 

		for (int i = 0; i < field.length; i++)
		{			
			DE += k0*field[i]*Math.cos(phi)*dz;
			gamma = (E0+DE)/Er + 1.0;
			//beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));
			phi += 2*Math.PI*frequency*dz / (beta * IElement.LightSpeed);
		}
		return DE/(e0tl*k0);
	}

	/**
	 * Evaluates sine transform of the field (with given phase shift). 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return sine transform
	 */
	public double evaluateEzSin(double phi0, double beta, double k0, double Er)
	{
		double phi = phi0;
		double dz = getLength() / field.length;
		double DE = 0.;
		double DS = 0.;

		double gamma = Math.sqrt(1 / (1 - beta*beta));
		double E0 = (gamma - 1) * Er; 

		for (int i = 0; i < field.length; i++)
		{			
			DE += k0*field[i]*Math.cos(phi)*dz;
			DS += k0*field[i]*Math.sin(phi)*dz;
			gamma = (E0+DE)/Er + 1.0;
			//beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));
			phi += 2*Math.PI*frequency*dz / (beta * IElement.LightSpeed);
		}
		return DS/(e0tl*k0);
	}

	/**
	 * Evaluates derivative of TTF at beta (with given phase shift). 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return derivative of TTF
	 */
	public double evaluateEzzSin(double phi0, double beta, double k0, double Er)
	{
		double phi = phi0;
		double dz = getLength() / field.length;
		double DE = 0.;
		double DZS = 0.;

		double gamma = Math.sqrt(1 / (1 - beta*beta));
		double E0 = (gamma - 1) * Er; 

		for (int i = 0; i < field.length; i++)
		{			
			DE += k0*field[i]*Math.cos(phi)*dz;
			DZS += k0*field[i]*Math.sin(phi)*(i*dz)*dz;
			gamma = (E0+DE)/Er + 1.0;
			//beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));
			phi += 2*Math.PI*frequency*dz / (beta * IElement.LightSpeed);
		}
		return DZS/e0tl*2 * Math.PI * frequency / beta / beta / IElement.LightSpeed;
	}


	/************************** Splitting methods ***************************/

	public TTFIntegrator[] getSplitIntegrators()
	{
		List<Integer> pos = new ArrayList<>();
		pos.add(0);
		boolean invert = false;
		for (int i = 0; i<N-1; i++)
			if (field[i]*field[i+1] < 0.) {
				if (field[i] < 0 && pos.size() == 1) invert = true;
				pos.add(i+1);
			}
		pos.add(N);

		TTFIntegrator[] splitIntgrs = new TTFIntegrator[pos.size()-1];
		for (int i=0; i<pos.size()-1; i++)
		{
			double[] field = new double[pos.get(i+1)-pos.get(i)];
			System.arraycopy(this.field, pos.get(i), field, 0, field.length);
			if (invert) 
				for (int j = 0; j<field.length; j++) field[j] = -field[j];
			splitIntgrs[i] = new TTFIntegrator(field.length, (pos.get(i+1)-pos.get(i))*zmax/N, field, frequency, invert);
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

	/*****************  Constructing methods ************************************/

	/**
	 * Returns the TTF integrator for a given delta_phi = input phase - synchronous phase
	 * @param dphi delta phi
	 * @return TTF integrator
	 */	
	public UnivariateRealPolynomial integratorWithOffset(final double phase, final double k0, final double Er) {
		return new UnivariateRealPolynomial() {

			@Override
			public double getCoef(int iOrder) {
				return 1.0; // fake coef to trigger evaluations
			}

			@Override
			public double evaluateAt(double beta) {
				return TTFIntegrator.this.evaluateEzCos(phase, beta, k0, Er);
			}

			@Override
			public double evaluateDerivativeAt(double beta) {
				return TTFIntegrator.this.evaluateEzzSin(phase, beta, k0, Er);
			}
		};
	}

	/**
	 * Static factory method to give TTF integrator for a specific fieldmap file
	 * @param path path to the field map file
	 * @param frequency frequency at which this fieldmap is running
	 * @return ttf integrator
	 */
	public static TTFIntegrator getInstance(String path, double frequency)
	{
		if (instances.containsKey(path)) {
			return instances.get(path);
		}
		TTFIntegrator i = new TTFIntegrator(path, frequency);
		instances.put(path, i);
		return i;
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
