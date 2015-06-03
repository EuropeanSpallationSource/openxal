package se.lu.esss.ics.jels.tools.math;

import xal.tools.math.poly.UnivariateRealPolynomial;

public class MeanFieldPolynomial extends UnivariateRealPolynomial {	
	private UnivariateRealPolynomial TTF, TTFPrime, STF, STFPrime;
	
	public MeanFieldPolynomial(UnivariateRealPolynomial TTF, UnivariateRealPolynomial TTFPrime, UnivariateRealPolynomial STF, UnivariateRealPolynomial STFPrime)
	{
		this.TTF = TTF;
		this.TTFPrime = TTFPrime;
		this.STF = STF;
		this.STFPrime = STFPrime;
	}

    public double evaluateAt(double dblVal) {
    	/*double T = TTF.evaluateAt(dblVal);
    	double S = STF.evaluateAt(dblVal);
    	return Math.sqrt(T*T+S*S);*/
    	return TTF.evaluateAt(dblVal);
    }

    public double evaluateDerivativeAt(double dblVal) {
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
