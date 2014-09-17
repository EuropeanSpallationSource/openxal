package se.lu.esss.ics.jels.tools.math;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.jtransforms.dct.DoubleDCT_1D;
import org.jtransforms.dst.DoubleDST_1D;

import xal.model.IElement;
import xal.tools.math.poly.UnivariateRealPolynomial;

public class TTFIntegrator {
	private static Map<String, TTFIntegrator> instances = new HashMap<>();
	
	private int N;
	private double zmax;
	private double[] field;
	private double[] dct;
	private double[] dst;
	
	private double maxttf;
	private double betas;
	
	private double frequency;
	
	public TTFIntegrator(String path, double frequency) {
		try {
			this.frequency = frequency;
			loadFile(path);
			DCF();
			saveFile(dct, path+".dct");
			saveFile(dst, path+".dst");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}


	private void loadFile(String path) throws IOException, URISyntaxException {
		BufferedReader br = new BufferedReader(new FileReader(new File(new URI(path))));
		
		// first line
		String line = br.readLine();
		String[] data = line.split(" ");
		
		N = Integer.parseInt(data[0]);
		zmax = Double.parseDouble(data[1]);
		field = new double[N];
		
		int i = 0;
		while ((line = br.readLine()) != null && i<N) {
			field[i++] = Double.parseDouble(line)*1e6;
		}
	}

	private void saveFile(double[] data, String path) throws IOException, URISyntaxException {
		PrintWriter pw = new PrintWriter(new FileWriter(new File(new URI(path))));
		for (int i = 0; i<N; i++)
			pw.printf("%E\n", data[i]);
		pw.close();
	}
	

	private void DCF() {
		dct = field.clone();
		dst = field.clone();
		//DoubleDCT_1D dct = new DoubleDCT_1D(N);
		//dct.forward(this.dct, false);
		forwarddcti(dct);
		
		forwarddsti(dst);
		
		/*DoubleDST_1D dst = new DoubleDST_1D(N);
		dst.forward(this.dst, false);	
		
		double max = 0.;
		for (int k=0; k<N; k++) max += this.dst[k];
		for (int k=0; k<N; k++) this.dst[k] /= max;*/
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
		betas = 2. * frequency * zmax / kmax /  IElement.LightSpeed;
	}
	
	public double getE0TL()
	{
		double v = 0.;
		for (int k=0; k<N; k++) v += Math.abs(field[k]);
		v *= zmax/N;
		
		maxttf = v;
		
		return v;
		
		//return  maxttf * zmax/N;
		
		//return maxttf * zmax/N;
	}
	
	public double getSyncPhase(double phi0, double beta0)
	{
		return Math.atan2(evaluateSinTransform(phi0, beta0), evaluateAt(phi0, beta0));
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
	}
	
	public double evaluateAt(double phi0, double beta)
	{
		
		double kd = 2. * frequency * zmax / (beta * IElement.LightSpeed);
		
		// TODO fix hardcoded frequency
		// TODO interpolate
		int k = (int)Math.round(kd);
		if (k < 0) k=0;
		if (k > N) k=N;
		double r = dctk(kd, phi0)/maxttf * zmax/N; //dct[k] ;
		System.out.printf("%E %E %E\n", beta, kd, r);
		return r;
	}

	public double evaluateSinTransform(double phi0, double beta)
	{
		
		// TODO fix hardcoded frequency
		double index = 2. * frequency * zmax/(beta * IElement.LightSpeed);
		// TODO interpolate
		int i = (int)Math.round(index);
		if (i < 0) i=0;
		if (i > N) i=N;
		return dstk(index, phi0)/maxttf * zmax/N; //dst[i];
	}
	
	public double evaluateDerivativeAt(double phi0, double beta)
	{
		int N = field.length;
		double d = 0;
		
		for (int n = 0; n<N; n++)
			d+=field[n]*Math.sin(2*Math.PI*frequency*zmax*n/N/(beta*IElement.LightSpeed) + phi0) * n;
		
		d*=(zmax/N)*(zmax/N) * 2 * Math.PI * frequency / beta / beta / IElement.LightSpeed / maxttf;
		
		//System.out.printf("der: %E %E %E", phi0, beta, d);
		return d;
	}
	
//	@Override
	public double getCoef(int iOrder)
	{
		return 1.;
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
	
	public static TTFIntegrator getInstance(String path, double frequency)
	{
		if (instances.containsKey(path)) {
			return instances.get(path);
		}
		TTFIntegrator i = new TTFIntegrator(path, frequency);
		instances.put(path, i);
		return i;
	}
	
	
}
