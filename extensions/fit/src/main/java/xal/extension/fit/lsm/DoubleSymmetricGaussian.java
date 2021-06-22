/*
 *  DoubleSymmetricGaussian.java
 *
 *  Created on January 3, 2008, 10:59 AM
 */
package xal.extension.fit.lsm;

import java.util.Date;

/**
 *  This class is for data fitting with two Gaussian functions with the same center. 
 *  The function form used in this class is 
 *  y = pedestal+amp*exp(-(x-center0)^2/(sigma^2/2.)) + amp*exp(-(x-center1)^2/(sigma^2/2.)).
 *  Users should keep in mind that guess does not work very well 
 *  when two peaks are not separated clearly. You should use nonlinear methods first,
 *  and then try the linear approach to get errors for parameters. 
 *
 *@author    shishlo
 */
public class DoubleSymmetricGaussian {

	private double sigma = 0.5;
	private double amp = 1.;
	private double center0 = 0.;
	private double center1 = 0.;
	private double pedestal = 0.;

	private double sigmaErr = 0.;
	private double ampErr = 0.;
	private double center0Err = 0.;
	private double center1Err = 0.;
	private double pedestalErr = 0.;

	private boolean sigmaIncl = true;
	private boolean ampIncl = true;
	private boolean center0Incl = true;
	private boolean center1Incl = true;
	private boolean pedestalIncl = true;

	private ModelFunction1D mf = null;

	private SolverLM solver = new SolverLM();

	private DataStore ds = new DataStore();

	private double[] a = new double[5];
	private double[] aErr = new double[5];

	private double[] xTmp = new double[1];

	/**
	 *  The "sigma0" parameter
	 */
	public static final String SIGMA = "sigma";
	/**
	 *  The "amplitude0" parameter
	 */
	public static final String AMP = "amplitude";
	/**
	 *  The "center" parameter
	 */
	public static final String CENTER = "center";	
	/**
	 *  The "center0" parameter
	 */
	public static final String CENTER0 = "center0";
	/**
	 *  The "center1" parameter
	 */
	public static final String CENTER1 = "center1";
	/**
	 *  The "pedestal" parameter
	 */
	public static final String PEDESTAL = "pedestal";


	/**
	 *  Creates a new instance of Gaussian
	 */
	public DoubleSymmetricGaussian() {
		init();
	}


	/**
	 *  Description of the Method
	 */
	private void init() {

		mf =
			new ModelFunction1D() {

                                @Override
				public double getValue(double x, double[] a) {
					if (a.length != 5) {
						return 0.;
					}

					double res = a[3] + a[1] * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0]));
					res = res + a[1] * Math.exp(-(x - a[4]) * (x - a[4]) / (2.0 * a[0] * a[0]));
					return res;
				}


                                @Override
				public double getDerivative(double x, double[] a, int indexArr) {
					double res = 0.;
					if (a.length != 5) {
						return 0.;
					}
					switch (indexArr) {
						case 0:
							res = a[1] * (x - a[2]) * (x - a[2]) * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0])) / (a[0] * a[0] * a[0]);
							res = res + a[1] * (x - a[4]) * (x - a[4]) * Math.exp(-(x - a[4]) * (x - a[4]) / (2.0 * a[0] * a[0])) / (a[0] * a[0] * a[0]);
							break;
						case 1:
							res = Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0])) + Math.exp(-(x - a[4]) * (x - a[4]) / (2.0 * a[0] * a[0]));
							break;
						case 2:
							res = a[1] * (x - a[2]) * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0])) / (a[0] * a[0]);
							break;
						case 3:
							res = 1.0;
							break;
						case 4:
							res = a[1] * (x - a[4]) * Math.exp(-(x - a[4]) * (x - a[4]) / (2.0 * a[0] * a[0])) / (a[0] * a[0]);;
							break;
					}

					return res;
				}

			};
	}


	/**
	 *  Sets parameters array from all parameters
	 */
	private void updateParams() {
		a[0] = sigma;
		a[1] = amp;
		a[2] = center0;
		a[3] = pedestal;
		a[4] = center1;	
	}


	/**
	 *  Returns the parameter value
	 *
	 *@param  key  The parameter name
	 *@return      The parameter value
	 */
	public double getParameter(String key) {
		if (key.equals(SIGMA)) {
			return sigma;
		} else if (key.equals(AMP)) {
			return amp;
		} else if (key.equals(CENTER0)) {
			return center0;
		} else if (key.equals(CENTER1)) {
			return center1;
		} else if (key.equals(CENTER)) {
			return (center0+center1)/2.0;
		} else if (key.equals(PEDESTAL)) {
			return pedestal;
		}
		return 0.;
	}


	/**
	 *  Returns the parameter value error
	 *
	 *@param  key  The parameter name
	 *@return      The parameter value error
	 */
	public double getParameterError(String key) {
		if (key.equals(SIGMA)) {
			return sigmaErr;
		} else if (key.equals(AMP)) {
			return ampErr;
		} else if (key.equals(CENTER)) {
			return (center0Err+center1Err)/2.0;
		} else if (key.equals(CENTER0)) {
			return center0Err;
		} else if (key.equals(CENTER1)) {
			return center1Err;
		} else if (key.equals(PEDESTAL)) {
			return pedestalErr;
		}
		return 0.;
	}


	/**
	 *  Includes or excludes the parameter into fitting
	 *
	 *@param  key   The parameter name
	 *@param  incl  The boolean variable about including variable into the fitting
	 */
	public void fitParameter(String key, boolean incl) {
		if (key.equals(SIGMA)) {
			sigmaIncl = incl;
		} else if (key.equals(AMP)) {
			ampIncl = incl;
		} else if (key.equals(CENTER)) {
			center0Incl = incl;
			center1Incl = incl;
		} else if (key.equals(CENTER0)) {
			center0Incl = incl;
		} else if (key.equals(CENTER1)) {
			center1Incl = incl;
		} else if (key.equals(PEDESTAL)) {
			pedestalIncl = incl;
		}
	}


	/**
	 *  Returns the boolean variable about including variable into the fitting
	 *
	 *@param  key  The parameter name
	 */
	public boolean fitParameter(String key) {
		if (key.equals(SIGMA)) {
			return sigmaIncl;
		} else if (key.equals(AMP)) {
			return ampIncl;
		} else if (key.equals(CENTER0)) {
			return center0Incl;
		} else if (key.equals(CENTER1)) {
			return center1Incl;
		} else if (key.equals(PEDESTAL)) {
			return pedestalIncl;
		}
		return false;
	}



	/**
	 *  Sets the parameter value
	 *
	 *@param  key  The parameter name
	 *@param  val  The new parameter value
	 */
	public void setParameter(String key, double val) {
		if (key.equals(SIGMA)) {
			sigma = val;
		} else if (key.equals(AMP)) {
			amp = val;
		} else if (key.equals(CENTER0)) {
			center0 = val;
		} else if (key.equals(CENTER1)) {
			center1 = val;
		} else if (key.equals(PEDESTAL)) {
			pedestal = val;
		}
		updateParams();
	}


	/**
	 *  Sets the data attribute of the Gaussian object
	 *
	 *@param  yArr      Y data array
	 *@param  yErrArr  Y values error array
	 *@param  xArr      The new data value
	 */
	public void setData(double[] xArr,
			double[] yArr,
			double[] yErrArr) {

		ds.clear();

		if (xArr.length != yArr.length) {
			return;
		}

		double[] x = new double[1];

		for (int i = 0; i < xArr.length; i++) {
			x[0] = xArr[i];
			if (yErrArr != null) {
				ds.addRecord(yArr[i], yErrArr[i], x);
			} else {
				ds.addRecord(yArr[i], x);
			}
		}
	}


	/**
	 *  Sets the data attribute of the Gaussian object
	 *
	 *@param  yArr  Y data array
	 *@param  xArr  The new data value
	 */
	public void setData(double[] xArr,
			double[] yArr) {
		setData(xArr, yArr, null);
	}


	/**
	 *  Removes all internal data
	 */
	public void clear() {
		ds.clear();
	}


	/**
	 *  Adds a data point to the internal data
	 *
	 *@param  x  The x value
	 *@param  y  The y value
	 */
	public void addData(double x, double y) {
		xTmp[0] = x;
		ds.addRecord(y, xTmp);
	}


	/**
	 *  Adds a data point to the internal data
	 *
	 *@param  x      The x value
	 *@param  y      The y value
	 *@param  yErr  The error of the y value
	 */
	public void addData(double x, double y, double yErr) {
		xTmp[0] = x;
		ds.addRecord(y, yErr, xTmp);
	}



	/**
	 *  perform the data fit
	 *
	 *@param  iteration  The number of iterations
	 *@return            Success or not
	 */
	public boolean fit(int iteration) {
		for (int i = 0; i < iteration; i++) {
			if (!fit()) {
				return false;
			}
		}
		return true;
	}


	/**
	 *  perform one step of the data fit
	 *
	 *@return    Success or not
	 */
	public boolean fit() {

		boolean[] mask = new boolean[6];
		mask[0] = sigmaIncl;
		mask[1] = ampIncl;
		mask[2] = center0Incl;
		mask[3] = pedestalIncl;
		mask[4] = center1Incl;

		updateParams();

		aErr[0] = 0.;
		aErr[1] = 0.;
		aErr[2] = 0.;
		aErr[3] = 0.;
		aErr[4] = 0.;

		solver = new SolverLM();
		boolean res = solver.solve(ds, mf, a, aErr, mask);

		if (res) {
			sigma = a[0];
			amp = a[1];
			center0 = a[2];
			center1 = a[4];
			pedestal = a[3];

			sigmaErr = aErr[0];
			ampErr = aErr[1];
			center0Err = aErr[2];
			center1Err = aErr[4];
			pedestalErr = aErr[3];
		}

		return res;
	}


	/**
	 *  Perform the several iterations of the data fit with guessing the initial
	 *  values of parameters
	 *
	 *@param  iteration  The number of iterations
	 *@return            Success or not
	 */
	public boolean guessAndFit(int iteration) {

		if (!guessAndFit()) {
			return false;
		}

		for (int i = 1; i < iteration; i++) {
			if (!fit()) {
				return false;
			}
		}
		return true;
	}


	/**
	 *  Finds the parameters of Gaussian with initial values defined from raw data
	 *
	 *@return    The true is the initial parameters have been defined successfully
	 */
	public boolean guessAndFit() {
		int n = ds.size();
		double yMin = Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double y = 0.;
		for (int i = 0; i < n; i++) {
			y = ds.getY(i);
			if (y > yMax) {
				yMax = y;
			}
			if (y < yMin) {
				yMin = y;
			}
		}
		if (yMin > yMax) {
			return false;
		}
		double yLevel = 0.607 * (yMax - yMin) + yMin;
		int nCross = 0;
		double xMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		int iXMin = -1;
		int iXMax = -1;		
		for (int i = 1; i < n; i++) {
			if ((yLevel - ds.getY(i - 1)) * (yLevel - ds.getY(i)) <= 0.) {
				nCross++;
				if (xMin > ds.getArrX(i)[0]) {
					xMin = ds.getArrX(i)[0];
					iXMin = i;
				}
				if (xMax < ds.getArrX(i)[0]) {
					xMax = ds.getArrX(i)[0];
					iXMax = i;
				}
			}
		}
		if (xMax <= xMin || iXMin < 0 || iXMax < 0) {
			return false;
		}
		
		if( (iXMax - iXMin) < 3){
			sigma = Math.abs(xMin - xMax) / 2.0;
			center0 = (xMin + xMax) / 2.0 - sigma*0.1;
			center1 = (xMin + xMax) / 2.0 + sigma*0.1;
			pedestal = Math.min(Math.abs(yMin), Math.abs(yMax));
			amp = (yMax - yMin);
		} else {
			//System.out.println("Debug  i_xMin = " + i_xMin + " i_xMax=" + i_xMax);
			int iCent = (iXMin + iXMax)/2;
			int iMin = -1;
			for (int i = iXMin; i < iCent; i++) {
				if(ds.getY(i+1) > ds.getY(i)){
					iMin = i+1;
				}
			}
			if(iMin <0){
				return false;
			}
			center0 = ds.getArrX(iMin)[0];
			double sig0 = ds.getArrX(iMin)[0] - ds.getArrX(iXMin)[0];
			
			int iMax = -1;
			for (int i = iXMax; i > iCent; i--) {
				if(ds.getY(i-1) > ds.getY(i)){
					iMax = i-1;
				}
			}
			if(iMax < 0){
				return false;
			}
			center1 = ds.getArrX(iMax)[0];
			double sig1 = ds.getArrX(iMax)[0] - ds.getArrX(iXMax)[0];
			
			//System.out.println("Debug  i_min = " + i_min + " i_max=" + i_max);
			
			sigma = (Math.abs(sig0)+Math.abs(sig1))/2.0;
			
			pedestal = Math.min(Math.abs(yMin), Math.abs(yMax));
			amp = (yMax - yMin);			
		}
		
		boolean sigmaInclIni = sigmaIncl;
		boolean ampInclIni = ampIncl;
		boolean center0InclIni = center0Incl;
		boolean center1InclIni = center1Incl;
		boolean pedestalInclIni = pedestalIncl;
		
		sigmaIncl = false;
		ampIncl = true;
		center0Incl = false;
		center1Incl = false;
		pedestalIncl = false;
		
		boolean res = fit(4);
		if(res == false) {
			return res;
		}
		
		/**
		System.out.println("Debug  s = " + getParameter(DoubleSymmetricGaussian.SIGMA) + " +- " + getParameterError(DoubleSymmetricGaussian.SIGMA));
		System.out.println("Debug  a = " + getParameter(DoubleSymmetricGaussian.AMP) + " +- " + getParameterError(DoubleSymmetricGaussian.AMP));
		System.out.println("Debug  c0  = " + getParameter(DoubleSymmetricGaussian.CENTER0) + " +- " + getParameterError(DoubleSymmetricGaussian.CENTER0));
		System.out.println("Debug  c1  = " + getParameter(DoubleSymmetricGaussian.CENTER1) + " +- " + getParameterError(DoubleSymmetricGaussian.CENTER1));
		System.out.println("Debug  p  = " + getParameter(DoubleSymmetricGaussian.PEDESTAL) + " +- " + getParameterError(DoubleSymmetricGaussian.PEDESTAL));
		*/
		
		sigmaIncl =  sigmaInclIni;
		ampIncl = ampInclIni;
		center0Incl =  center0InclIni;
		center1Incl =  center1InclIni;
		pedestalIncl =  pedestalInclIni;		
		
		res = fit(1);
		if(res == false) {
			return res;
		}		
		
		return res;
	}


	/**
	 *  Returns the value of Gaussian function
	 *
	 *@param  x  The x-value
	 *@return    The Gauss function value
	 */
	public double getValue(double x) {
		return mf.getValue(x, a);
	}


	/**
	 *  MAIN for debugging
	 *
	 *@param  args  The array of strings as parameters
	 */
	public static void main(String args[]) {

		double p = 0.2;
		double a = 1.5;
		double c0 = 0.1;
		double c1 = 0.9;
		double s = 0.3;


		int n = 100;
		double xMin = c0 - 3 * s;
		double xMax = c1 + 3 * s;
		double step = (xMax - xMin) / (n - 1);

		double[] xArr = new double[n];
		double[] yArr = new double[n];

		double x = 0.;
		double errLevel = 0.0;

		for (int i = 0; i < n; i++) {
			x = xMin + step * i;
			xArr[i] = x;
			yArr[i] = p + a * Math.exp(-(x-c0) * (x-c0) / (2.0*s*s)) + a * Math.exp(-(x-c1) * (x-c1) / (2.0*s*s));
			yArr[i] = yArr[i] * (1.0 + errLevel * 2.0 * (Math.random() - 0.5));
		}

		DoubleSymmetricGaussian gs = new DoubleSymmetricGaussian();

		gs.setData(xArr, yArr);

		gs.setParameter(DoubleSymmetricGaussian.SIGMA, s * 1.1);
		gs.setParameter(DoubleSymmetricGaussian.AMP, a * 1.1);
		gs.setParameter(DoubleSymmetricGaussian.CENTER0, c0 * 0.9);
		gs.setParameter(DoubleSymmetricGaussian.CENTER1, c1 * 1.1);
		gs.setParameter(DoubleSymmetricGaussian.PEDESTAL, p * 1.0);

		gs.fitParameter(DoubleSymmetricGaussian.SIGMA, true);
		gs.fitParameter(DoubleSymmetricGaussian.AMP, true);
		gs.fitParameter(DoubleSymmetricGaussian.CENTER0, true);
		gs.fitParameter(DoubleSymmetricGaussian.CENTER1, true);
		gs.fitParameter(DoubleSymmetricGaussian.PEDESTAL, true);

		System.out.println("================START================");
		System.out.println("data error level [%]= " + errLevel * 100);
		System.out.println("Main ini: s  = " + s);
		System.out.println("Main ini: a  = " + a);
		System.out.println("Main ini: c0 = " + c0);
		System.out.println("Main ini: c1 = " + c1);
		System.out.println("Main ini: p  = " + p);

		int nIter = 8;

		boolean res = false;

		//guess does not work very well 
		//when two peaks are not separated clearly
		res = gs.guessAndFit();

		for (int j = 0; j < nIter; j++) {
			System.out.println("Main: iteration =" + j + "  res = " + res);
			System.out.println("Main: s = " + gs.getParameter(DoubleSymmetricGaussian.SIGMA) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.SIGMA));
			System.out.println("Main: a = " + gs.getParameter(DoubleSymmetricGaussian.AMP) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.AMP));
			System.out.println("Main: c0  = " + gs.getParameter(DoubleSymmetricGaussian.CENTER0) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.CENTER0));
			System.out.println("Main: c1  = " + gs.getParameter(DoubleSymmetricGaussian.CENTER1) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.CENTER1));
			System.out.println("Main: p  = " + gs.getParameter(DoubleSymmetricGaussian.PEDESTAL) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.PEDESTAL));
			res = gs.fit();
		}

		for (int i = 0; i < n; i++) {
			x = xMin + step * i;
			System.out.println("i=" + i + " x=" + x + " yIni=" + yArr[i] + " model=" + gs.getValue(x));
		}

		nIter = 100;
		java.util.Date start = new java.util.Date();
		for (int j = 0; j < nIter; j++) {
			res = gs.fit();
		}
		Date stop = new Date();
		double time = (stop.getTime() - start.getTime()) / 1000.;
		time /= nIter;
		System.out.println("time for one step [sec] =" + time);

	}

}

