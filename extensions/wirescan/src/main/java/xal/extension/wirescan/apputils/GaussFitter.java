package xal.extension.wirescan.apputils;

import java.util.List;
import java.util.ArrayList;


import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.solver.Scorer;
import xal.extension.solver.Trial;
import xal.extension.solver.Variable;
import xal.extension.solver.Stopper;
import xal.extension.solver.SolveStopperFactory;
import xal.extension.solver.ProblemFactory;
import xal.extension.solver.Solver;
import xal.extension.solver.Problem;
import xal.extension.solver.algorithm.SimplexSearchAlgorithm;
import xal.extension.solver.hint.InitialDelta;

/*
 * The  GaussFitter class fits the waveforms from WireScanData class. 
 * It assumes the Gaussian form of the function.
 */
public class GaussFitter{
	
	private int nIterations = 1000;
	
	private int nGraphPoints = 200;
	
	private double wCoeff = 5.0;
	
	private boolean [] fitOnArr = {true,true,true,true};
	
	/** Constructor of a default wire scanner data fitter */
	public GaussFitter(){	
	}	
	
	/** Stes the variables that will be used in fitting. The input parameter 
	    is an 4-elements boolean array with true or false for the base line, 
			center position, sigma, and amplitude of the Gaussian approximation. 
	*/ 
	public void setVariablesOn(boolean [] fitOnArr){
		this.fitOnArr[0] = fitOnArr[0];
		this.fitOnArr[1] = fitOnArr[1];
		this.fitOnArr[2] = fitOnArr[2];
		this.fitOnArr[3] = fitOnArr[3];
	}
	
	/** Guess initial Gauss parameters and fit for both planes: X and Y */
	public boolean guessAndFit(WireScanData wsD){
		boolean res_x = this.guessAndFitX(wsD);
		boolean res_y = this.guessAndFitY(wsD);
		if(res_x == true && res_y == true) return true;
		return false;
	}
		
	/** Fit for both planes X and Y without initial guess */
	public boolean fitAgain(WireScanData wsD){
		boolean res_x = this.fitAgainX(wsD);
		boolean res_y = this.fitAgainY(wsD);
		if(res_x == true && res_y == true) return true;
		return false;
	}
	
	/** Guess initial Gauss parameters and fit for X plane */
	public boolean guessAndFitX(WireScanData wsD){
		wsD.getFitWFX().removeAllPoints();
		wsD.getLogFitWFX().removeAllPoints(); 		
		double [] params_arr = this.guessParams(wsD.getRawWFX());
		if(params_arr == null) return false;
		boolean res = this.gaussFit(params_arr,wsD.getRawWFX(),wsD.getFitWFX(),wsD.getLogFitWFX());
		double base = params_arr[0];
		double center = params_arr[1];			
		double sigma = params_arr[2];
		double amp = params_arr[3];
		wsD.setBaseX(base);
		wsD.setCenterX(center);		
		wsD.setSigmaX(sigma);
		wsD.setAmpX(amp);
		if(res == false) return false;
		double [] res_rms_arr = this.getCenterAndSigmaRms(center,sigma,wsD.getRawWFX());
		wsD.setCenterRmsX(res_rms_arr[0]);
		wsD.setSigmaRmsX(	res_rms_arr[1]);	
		return true;
	}
	
	/** Guess initial Gauss parameters and fit for Y plane */
	public boolean guessAndFitY(WireScanData wsD){
		wsD.getFitWFY().removeAllPoints();
		wsD.getLogFitWFY().removeAllPoints(); 		
		double [] params_arr = this.guessParams(wsD.getRawWFY());
		if(params_arr == null) return false;
		boolean res = this.gaussFit(params_arr,wsD.getRawWFY(),wsD.getFitWFY(),wsD.getLogFitWFY());
		double base = params_arr[0];
		double center = params_arr[1];			
		double sigma = params_arr[2];
		double amp = params_arr[3];
		wsD.setBaseY(base);
		wsD.setCenterY(center);		
		wsD.setSigmaY(sigma);
		wsD.setAmpY(amp);
		if(res == false) return false;
		double [] res_rms_arr = this.getCenterAndSigmaRms(center,sigma,wsD.getRawWFY());
		wsD.setCenterRmsY(res_rms_arr[0]);
		wsD.setSigmaRmsY(	res_rms_arr[1]);			
		return true;
	}	
	
	/** Fit for X plane without initial guess */
	public boolean fitAgainX(WireScanData wsD){
		wsD.getFitWFX().removeAllPoints();
		wsD.getLogFitWFX().removeAllPoints(); 
    double [] params_arr = new double[4];
		params_arr[0] = wsD.getBaseX();
		params_arr[1] = wsD.getCenterX();
		params_arr[2] = wsD.getSigmaX();
		params_arr[3] = wsD.getAmpX();	
		if(wsD.getSigmaX() == 0.) params_arr = this.guessParams(wsD.getRawWFX());
		boolean res = this.gaussFit(params_arr,wsD.getRawWFX(),wsD.getFitWFX(),wsD.getLogFitWFX());
		double base = params_arr[0];
		double center = params_arr[1];			
		double sigma = params_arr[2];
		double amp = params_arr[3];
		wsD.setBaseX(base);
		wsD.setCenterX(center);		
		wsD.setSigmaX(sigma);
		wsD.setAmpX(amp);
		if(res == false) return false;
		double [] res_rms_arr = this.getCenterAndSigmaRms(center,sigma,wsD.getRawWFX());
		wsD.setCenterRmsX(res_rms_arr[0]);
		wsD.setSigmaRmsX(	res_rms_arr[1]);		
		return true;		
	}
	
	/** Fit for Y plane without initial guess */
	public boolean fitAgainY(WireScanData wsD){
		wsD.getFitWFY().removeAllPoints();
		wsD.getLogFitWFY().removeAllPoints(); 
    double [] params_arr = new double[4];
		params_arr[0] = wsD.getBaseY();
		params_arr[1] = wsD.getCenterY();
		params_arr[2] = wsD.getSigmaY();
		params_arr[3] = wsD.getAmpY();		
		if(wsD.getSigmaY() == 0.) params_arr = this.guessParams(wsD.getRawWFY());		
		boolean res = this.gaussFit(params_arr,wsD.getRawWFY(),wsD.getFitWFY(),wsD.getLogFitWFY());
		double base = params_arr[0];
		double center = params_arr[1];			
		double sigma = params_arr[2];
		double amp = params_arr[3];
    //System.out.println("debug fit again Y fit base="+base+" center="+center+" sigma="+sigma+" amp="+amp);		
		wsD.setBaseY(base);
		wsD.setCenterY(center);		
		wsD.setSigmaY(sigma);
		wsD.setAmpY(amp);
		if(res == false) return false;
		double [] res_rms_arr = this.getCenterAndSigmaRms(center,sigma,wsD.getRawWFY());
		wsD.setCenterRmsY(res_rms_arr[0]);
		wsD.setSigmaRmsY(	res_rms_arr[1]);	
		return true;		
	}	
	
	private boolean gaussFit( double [] paramsArr, final BasicGraphData gD, BasicGraphData fitGD,BasicGraphData fitLogGD){		
		final double base = paramsArr[0];
		final double center = paramsArr[1];			
		final double sigma = paramsArr[2];
		final double amp = paramsArr[3];
		
		int indstart0 = gD.getNumbOfPoints();
		int ind_stop0 = 0;
		for(int ix = 0; ix <  gD.getNumbOfPoints() ; ix++){
			double x = gD.getX(ix);
			if( Math.abs(x-center) < wCoeff*sigma){
				if(indstart0 > ix) indstart0 = ix;
				if(ind_stop0 < ix) ind_stop0 = ix;
			}
		}
		if( (ind_stop0 - indstart0) < 3 ) return false;
		
		final int indstart = indstart0;
		final int ind_stop = ind_stop0;
		//System.out.println("debug fit xMin="+gD.getX(indstart)+" xMax="+gD.getX(ind_stop));
		final ArrayList<Variable> variables = new ArrayList<>();
		variables.add(new Variable( "base",   base,   - Double.MAX_VALUE, Double.MAX_VALUE ) );
		variables.add(new Variable( "center", center, - Double.MAX_VALUE, Double.MAX_VALUE ) );
		variables.add(new Variable( "sigma",  sigma,  - Double.MAX_VALUE, Double.MAX_VALUE ) );
		variables.add(new Variable( "amp",    amp,    - Double.MAX_VALUE, Double.MAX_VALUE ) )	;			
		
		Scorer scorer = new Scorer(){
                        @Override
			public double score( final Trial trial, final List<Variable> variables_tmp ){
				double diff = 0.;
				java.util.Map<Variable,java.lang.Number> var_map = trial.getTrialPoint().getValueMap();
				double base0 = base;
				double center0 = center;
				double sigma0 = sigma;
				double amp0 =amp;
				if(var_map.containsKey(variables.get(0))){
					base0 = trial.getTrialPoint().getValue(variables.get(0));
				}
				if(var_map.containsKey(variables.get(1))){
					center0 = trial.getTrialPoint().getValue(variables.get(1));
				}
				if(var_map.containsKey(variables.get(2))){
					sigma0 = trial.getTrialPoint().getValue(variables.get(2));
				}
				if(var_map.containsKey(variables.get(3))){
					amp0 = trial.getTrialPoint().getValue(variables.get(3));
				}
				double y_th,x,y;
				for(int ix = indstart; ix <= ind_stop; ix++){
					x = gD.getX(ix);
					y = gD.getY(ix);
					y_th = base0 + amp0*Math.exp(-(x-center0)*(x-center0)/(2*sigma0*sigma0));
					diff += (y - y_th)*(y - y_th);
				}
				//System.out.println("debug iteration fit base="+base0+" center="+center0+" sigma="+sigma0+" amp="+amp0);
				return diff;
			}
		};
		//System.out.println("debug init fit base="+base+" center="+center+" sigma="+sigma+" amp="+amp);
		
		Stopper maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(nIterations); 
		Solver solver = new Solver(new SimplexSearchAlgorithm(),maxSolutionStopper);
		ArrayList<Variable> variables_on = new ArrayList<>();
		for(int iv = 0; iv < 4; iv++){
			if(fitOnArr[iv]) variables_on.add(variables.get(iv));
		}
		Problem problem = ProblemFactory.getInverseSquareMinimizerProblem(variables_on,scorer,amp*0.0001);
		InitialDelta hint = new InitialDelta();
		hint.addInitialDelta(variables.get(0), amp*0.001);
		hint.addInitialDelta(variables.get(1), sigma*0.05);
		hint.addInitialDelta(variables.get(2), sigma*0.05);
		hint.addInitialDelta(variables.get(3), amp*0.05);
		problem.addHint(hint);	
		solver.solve(problem);
		
		double base0 = base;
		double center0 = center;
		double sigma0 = sigma;
		double amp0 = amp;				
		Trial trial = solver.getScoreBoard().getBestSolution();
		java.util.Map<Variable,java.lang.Number> var_map = trial.getTrialPoint().getValueMap();
		if(var_map.containsKey(variables.get(0))){
			base0 = trial.getTrialPoint().getValue(variables.get(0));
		}
		if(var_map.containsKey(variables.get(1))){
			center0 = trial.getTrialPoint().getValue(variables.get(1));
		}
		if(var_map.containsKey(variables.get(2))){
			sigma0 = trial.getTrialPoint().getValue(variables.get(2));
		}
		if(var_map.containsKey(variables.get(3))){
			amp0 = trial.getTrialPoint().getValue(variables.get(3));
		}		
		paramsArr[0] = base0;
		paramsArr[1] = center0;
		paramsArr[2] = sigma0;
		paramsArr[3] = amp0;	
		//System.out.println("debug end fit base="+base0+" center="+center0+" sigma="+sigma0+" amp="+amp0);
		double step = (gD.getX(ind_stop) - gD.getX(indstart))/(nGraphPoints-1);
		for(int ix = 0; ix < nGraphPoints; ix++){
			double x = gD.getX(indstart) + step*ix;
			double y = base0 + amp0*Math.exp(-(x-center0)*(x-center0)/(2*sigma0*sigma0));
			fitGD.addPoint(x,y);
			if(y > 0.){
				fitLogGD.addPoint(x,Math.log10(y));
			}
		}
		return true;
	}
	
	private double [] getCenterAndSigmaRms(double center, double sigma, BasicGraphData gD){
		double centerRms = 0.;		
		double sigmaRms = 0.;
		double weight = 0.;
		double [] res_arr = new double[2];
		res_arr[0] = centerRms;
		res_arr[1] = sigmaRms;
		for(int ix = 0; ix <  gD.getNumbOfPoints() ; ix++){
			double x = gD.getX(ix);
			double y = gD.getY(ix);
			if(Math.abs(center -x) < sigma*wCoeff && y > 0.){
				weight += y;
				centerRms += x*y;
			}
		}
		if(weight == 0.) return res_arr;
		centerRms = centerRms/weight;
		for(int ix = 0; ix <  gD.getNumbOfPoints() ; ix++){
			double x = gD.getX(ix);
			double y = gD.getY(ix);
			if(Math.abs(center -x) < sigma*wCoeff && y > 0.){
				sigmaRms += (x - centerRms)*(x - centerRms)*y;
			}
		}
		sigmaRms = Math.sqrt(sigmaRms/weight);
		res_arr[0] = centerRms;
		res_arr[1] = sigmaRms;	
		return res_arr;
	}
	
	private double [] guessParams(BasicGraphData gD){
		if(gD.getNumbOfPoints() < 4) return null;
		double xMax = - Double.MAX_VALUE;
		double yMax = - Double.MAX_VALUE;
		double yMin = + Double.MAX_VALUE;
		for(int ix = 0; ix <  gD.getNumbOfPoints() ; ix++){
					double x = gD.getX(ix);
					double y = gD.getY(ix);
					if(yMax < y){
						yMax = y;
						xMax = x;
					}
					if(yMin > y) { 
						yMin = y;
					}
		}
		//System.out.println("debug xMax="+xMax+" yMax="+yMax+" yMin="+yMin);
		double yLevel = yMin + (yMax - yMin)*0.7;
		double xLower = gD.getX(0);
		double xUpper = gD.getX(gD.getNumbOfPoints() -1);
		for(int ix = 0; ix <  (gD.getNumbOfPoints() - 1) ; ix++){
			double x0 = gD.getX(ix);
			double x1 = gD.getX(ix+1);
			double y0 = gD.getY(ix);
			double y1 = gD.getY(ix+1);
			if( (yLevel -  y0)*(yLevel -  y1) <= 0.){
				if( (yLevel -  y0) >= 0.){
					double x = x0 - y0*(x1-x0)/(y1-y0);
					if((x - xMax) < 0. && Math.abs(xLower - xMax) > Math.abs(x - xMax)){
						xLower = x;
					}
				}
				if( (yLevel -  y0) <= 0.){
					double x = x0 - y0*(x1-x0)/(y1-y0);
					if((x - xMax) > 0. && Math.abs(xUpper - xMax) > Math.abs(x - xMax)){
						xUpper = x;
					}	
				}
			}
		}		
		//System.out.println("debug xLower="+xLower+" xUpper="+xUpper);
		double base = 0.;
		double center = (xUpper + xLower)/2.0;			
		double sigma = (xUpper - xLower)/2.0;
		double amp = (yMax - yMin);
		if(sigma < 0.) return null;
		double [] res_arr = new double[4];
		res_arr[0] = base;
		res_arr[1] = center;
		res_arr[2] = sigma;
		res_arr[3] = amp;
		//System.out.println("debug base="+base+" center="+center+" sigma="+sigma+" amp="+amp);
		return res_arr;
	}
	
	
	/** Sets the number of iterations diring the fitting */ 
	public void setIterations(int nIterations){
		this.nIterations = nIterations;
	}
	
	/** Sets the number of graph points in the fitting curve */
	public void setGraphPoints(int nGraphPoints){
		this.nGraphPoints = nGraphPoints;
	}	
	
	/** Sets the width coefficient for fitting. The fit will use  
	    - wCoeff*sigma : + wCoeff*sigma region around pick for fitting 
	*/
	public void setWidthCoeff(double wCoeff){
		this.wCoeff = wCoeff;
	}	
	
	/** Returns the number of iterations during the fitting */ 
	public void getIterations(int nIterations){
		this.nIterations = nIterations;
	}
	
	/** Returns the number of graph points in the fitting curve */
	public void getGraphPoints(int nGraphPoints){
		this.nGraphPoints = nGraphPoints;
	}	
	
	/** Returns the width coefficient for fitting. The fit will use  
	    - wCoeff*sigma : + wCoeff*sigma region around pick for fitting 
	*/
	public double getWidthCoeff(){
		return wCoeff;
	}	
	
	
}
