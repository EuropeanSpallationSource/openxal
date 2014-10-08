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
	//private double[] dct;
	//private double[] dst;
	private double e0tl;
	private double frequency;
	private int gapCount;
	
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
		//	DCF();
		//	saveFile(dct, path+".dct");
		//	saveFile(dst, path+".dst");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	protected TTFIntegrator(int N, double zmax, double[] field, double frequency, int gapCount)
	{
		this.N = N;
		this.zmax = zmax;
		this.field = field;
		this.frequency = frequency;
		this.gapCount = gapCount;
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
	public double getSyncPhase(double phi0, double beta0)
	{
		return Math.atan2(evaluateSinTransform(phi0, beta0), evaluateAt(phi0, beta0));
	}
	
	/**
	 * Evaluates TTF at beta, actually a cosine transform of the field (with given phase shift). 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return TTF
	 */
	public double evaluateAt(double phi0, double beta)
	{
		double kd = 2. * frequency * zmax / (beta * IElement.LightSpeed);
		double r = dctk(kd, phi0)/e0tl * zmax/N; //dct[k] ;
		//System.out.printf("%E %E %E\n", beta, kd, r);
		return r;
	}

	/**
	 * Evaluates sine transform of the field (with given phase shift). 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return sine transform
	 */
	public double evaluateSinTransform(double phi0, double beta)
	{
		double index = 2. * frequency * zmax/(beta * IElement.LightSpeed);
		return dstk(index, phi0)/e0tl * zmax/N; //dst[i];
	}
	
	/**
	 * Evaluates derivative of TTF at beta (with given phase shift). 
	 * @param phi0 phase shift in TTF transform
	 * @param beta energy
	 * @return derivative of TTF
	 */
	public double evaluateDerivativeAt(double phi0, double beta)
	{
		int N = field.length;
		double d = 0;
		
		for (int n = 0; n<N; n++)
			d+=field[n]*Math.sin(2*Math.PI*frequency*zmax*n/N/(beta*IElement.LightSpeed) + phi0) * n;
		
		d*=(zmax/N)*(zmax/N) * 2 * Math.PI * frequency / beta / beta / IElement.LightSpeed / e0tl;
		
		//System.out.printf("der: %E %E %E", phi0, beta, d);
		return d;
	}
		
	private double dctk(double k, double phi)
	{
		double Xk = 0;
		for (int n = 0; n<N; n++)
			Xk+=field[n]*Math.cos(Math.PI/N*n*k+phi);
		return Xk;
	}
	
	private double dstk(double k, double phi)
	{
		double Xk = 0;
		for (int n = 0; n<N; n++)
			Xk+=field[n]*Math.sin(Math.PI/N*n*k+phi);
		return Xk;
	}
	

	/*
	private void DCF() {
		dct = field.clone();
		dst = field.clone();
		//DoubleDCT_1D dct = new DoubleDCT_1D(N);
		//dct.forward(this.dct, false);
		forwarddcti(dct);
		forwarddsti(dst);
		
		/*DoubleDST_1D dst = new DoubleDST_1D(N);
		dst.forward(this.dst, false);	
	}

	private void forwarddcti(double[] X)
	{
		double N = X.length;
		double[] x = X.clone();
		for (int k=0; k<N; k++) {
			X[k] = 0;
			for (int n = 0; n<N; n++)
				X[k]+=x[n]*Math.cos(Math.PI/N*n*k);
		}
		
		int kmax = 0;
		maxttf = 0.;
		for (int k=0; k<N; k++) if (X[k] > maxttf) { kmax = k; maxttf = X[k]; };
		for (int k=0; k<N; k++) X[k] /= maxttf;
	//	betas = 2. * frequency * zmax / kmax /  IElement.LightSpeed;
	}
	
	private void forwarddsti(double[] X)
	{
		double N = X.length;
		double[] x = X.clone();
		for (int k=0; k<N; k++) {
			X[k] = 0;
			for (int n = 0; n<N; n++)
				X[k]+=x[n]*Math.sin(Math.PI/N*n*k);
		}
		for (int k=0; k<N; k++) X[k] /= maxttf;
	}*/
	
	/************************** Splitting methods ***************************/
	
	public TTFIntegrator[] getSplitIntegrators()
	{
		List<Integer> pos = new ArrayList<>();
		pos.add(0);
		for (int i = 0; i<N-1; i++)
			if (field[i]*field[i+1] < 0.)
				pos.add(i+1);
		pos.add(N);
		
		TTFIntegrator[] splitIntgrs = new TTFIntegrator[pos.size()-1];
		for (int i=0; i<pos.size()-1; i++)
		{
			double[] field = new double[pos.get(i+1)-pos.get(i)];
			System.arraycopy(this.field, pos.get(i), field, 0, field.length);
			splitIntgrs[i] = new TTFIntegrator(field.length, (pos.get(i+1)-pos.get(i))*zmax/N, field, frequency, i);
		}
		
		return splitIntgrs;
	}
	
	public double getSyncCenter(double beta, double phi0)
	{
		double phis = Math.IEEEremainder(getSyncPhase(0., beta) + gapCount*Math.PI + phi0, 2*Math.PI) ;
		if (phis < 0) phis+=2*Math.PI;
		double l =  (phis) / (2*Math.PI*frequency) * beta * IElement.LightSpeed;		
		return l;
	}
	
	public double getCenter()
	{	
		int c = 0;
		for (int i=0; i<N; i++) 
			if (Math.abs(field[i]) > Math.abs(field[c]))
				c = i;
		
		double csum = 0.; double cN = 0; 
		for (int i=0; i<N; i++) 
			if (Math.abs(field[i]) > Math.abs(field[c])*(0.4))
			{
				csum += zmax*i/N;
				cN++;
			}
		
		return csum/cN;
		//return getLength()/2.;
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
	public UnivariateRealPolynomial integratorWithInputPhase(final double dphi) {
		return new UnivariateRealPolynomial() {

			@Override
			public double getCoef(int iOrder) {
				return 1.0; // fake coef to trigger evaluations
			}

			@Override
			public double evaluateAt(double beta) {
				return TTFIntegrator.this.evaluateAt(dphi, beta);
			}

			@Override
			public double evaluateDerivativeAt(double beta) {
				return TTFIntegrator.this.evaluateDerivativeAt(dphi, beta);
			}
		};
	}
	
	public UnivariateRealPolynomial integratorWithOffset(final double off, final double phase) {
		return new UnivariateRealPolynomial() {

			@Override
			public double getCoef(int iOrder) {
				return 1.0; // fake coef to trigger evaluations
			}

			@Override
			public double evaluateAt(double beta) {
				return TTFIntegrator.this.evaluateAt(-2*Math.PI*frequency*off/beta/IElement.LightSpeed-phase, beta);
			}

			@Override
			public double evaluateDerivativeAt(double beta) {
				return TTFIntegrator.this.evaluateDerivativeAt(-2*Math.PI*frequency*off/beta/IElement.LightSpeed-phase, beta);
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

}
