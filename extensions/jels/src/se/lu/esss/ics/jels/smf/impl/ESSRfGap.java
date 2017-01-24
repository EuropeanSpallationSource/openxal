package se.lu.esss.ics.jels.smf.impl;

import xal.ca.ChannelFactory;
import xal.smf.impl.RfGap;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

/**
 * This gap implementation is extended to return correct (special) TTF/STF fits for the start gap. 
 * 
 * @author Ivo List
 *
 */
public class ESSRfGap extends RfGap {

	public ESSRfGap(String strId) {
		super(strId);
	}
	
	public ESSRfGap(String strId, ChannelFactory channelFactory) {
		super(strId, channelFactory);
	}

	/** return a polynomial fit of the transit time factor as a function of beta */
	@Override
	public RealUnivariatePolynomial getTTFFit() {
        ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if(isFirstGap()) 
        	return rfCav.getTTFFitStart();
        else if (isEndCell())
        	return rfCav.getTTFFitEnd();
        else
        	return rfCav.getTTFFit();		
	}

	/** return a polynomial fit of the TTF-prime factor as a function of beta */  
	@Override
	public RealUnivariatePolynomial getTTFPrimeFit() {
		ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if(isFirstGap()) 
        	return rfCav.getTTFPrimeFitStart();
        else if (isEndCell())
        	return rfCav.getTTFPrimeFitEnd();
        else
        	return rfCav.getTTFPrimeFit();
	}

	/** return a polynomial fit of the S factor as a function of beta */  
	@Override
	public RealUnivariatePolynomial getSFit() {
		ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if(isFirstGap()) 
        	return rfCav.getSTFFitStart();
        else if (isEndCell())
        	return rfCav.getSTFFitEnd();
        else
        	return rfCav.getSTFFit();
	}

	/** return a polynomial fit of the S-prime factor as a function of beta */  
	@Override
	public RealUnivariatePolynomial getSPrimeFit() {
		ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if(isFirstGap()) 
        	return rfCav.getSTFPrimeFitStart();
        else if (isEndCell())
        	return rfCav.getSTFPrimeFitEnd();
        else
        	return rfCav.getSTFPrimeFit();
	}

}
