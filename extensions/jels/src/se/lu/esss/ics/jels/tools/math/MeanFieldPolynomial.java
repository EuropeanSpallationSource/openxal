package se.lu.esss.ics.jels.tools.math;

import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

public class MeanFieldPolynomial extends RealUnivariatePolynomial {	
	private RealUnivariatePolynomial TTF, TTFPrime, STF, STFPrime;
	
	public MeanFieldPolynomial(RealUnivariatePolynomial TTF, RealUnivariatePolynomial TTFPrime, RealUnivariatePolynomial STF, RealUnivariatePolynomial STFPrime)
	{
		this.TTF = TTF;
		this.TTFPrime = TTFPrime;
		this.STF = STF;
		this.STFPrime = STFPrime;
	}

	@Override
    public double evaluateAt(double dblVal) {
    	/*double T = TTF.evaluateAt(dblVal);
    	double S = STF.evaluateAt(dblVal);
    	return Math.sqrt(T*T+S*S);*/
    	return TTF.evaluateAt(dblVal);
    }

    @Override
    public double derivativeAt(double dblVal) {
    	/*double T = TTF.evaluateAt(dblVal);
    	double S = STF.evaluateAt(dblVal);
    	double Tp = 0.01*TTFPrime.evaluateAt(dblVal);
    	double Sp = 0.01*STFPrime.evaluateAt(dblVal);
    	return (T*Sp-Tp*S)/Math.sqrt(T*T+S*S);*/
    	//return TTF.evaluateDerivativeAt(dblVal);
    	return 0.01*TTFPrime.evaluateAt(dblVal);
	}
    
    public double getCoef(int iOrder)   {
    	return 1.0;
    }
}
