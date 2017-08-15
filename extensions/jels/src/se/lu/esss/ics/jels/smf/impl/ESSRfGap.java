package se.lu.esss.ics.jels.smf.impl;

import se.lu.esss.ics.jels.tools.math.InverseRealPolynomial;
import xal.ca.ChannelFactory;
import xal.smf.impl.RfGap;

/**
 * This gap implementation is extended to return correct (special) TTF/STF fits
 * for the start gap.
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

    /**
     * return a polynomial fit of the transit time factor as a function of beta
     */
    @Override
    public InverseRealPolynomial getTTFFit() {
        double[] arrCoeffs = this.m_bucRfGap.getTCoefficients();

        // Defaults to the RF cavity transit time factor if none is
        //  defined for this gap.
        if (arrCoeffs == null || arrCoeffs.length == 0) {

            ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
            if (isFirstGap()) {
                return rfCav.getTTFFitStart();
            } else if (isEndCell()) {
                return rfCav.getTTFFitEnd();
            } else {
                return rfCav.getTTFFit();
            }
        }

        // A set of coefficients is defined for this fit.
        //  Create the fitting function and return it.
        InverseRealPolynomial polyFit = new InverseRealPolynomial(arrCoeffs);

        return polyFit;
    }

    /**
     * return a polynomial fit of the TTF-prime factor as a function of beta
     */
    @Override
    public InverseRealPolynomial getTTFPrimeFit() {
        ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if (isFirstGap()) {
            return rfCav.getTTFPrimeFitStart();
        } else if (isEndCell()) {
            return rfCav.getTTFPrimeFitEnd();
        } else {
            return rfCav.getTTFPrimeFit();
        }
    }

    /**
     * return a polynomial fit of the S factor as a function of beta
     */
    @Override
    public InverseRealPolynomial getSFit() {
        double[] arrCoeffs = this.m_bucRfGap.getSCoefficients();

        // Defaults to the RF cavity transit time factor if none is
        //  defined for this gap.
        if (arrCoeffs == null || arrCoeffs.length == 0) {

            ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
            if (isFirstGap()) {
                return rfCav.getSTFFitStart();
            } else if (isEndCell()) {
                return rfCav.getSTFFitEnd();
            } else {
                return rfCav.getSTFFit();
            }
        }

        // A set of coefficients is defined for this fit.
        //  Create the fitting function and return it.
        InverseRealPolynomial polyFit = new InverseRealPolynomial(arrCoeffs);

        return polyFit;
    }

    /**
     * return a polynomial fit of the S-prime factor as a function of beta
     */
    @Override
    public InverseRealPolynomial getSPrimeFit() {
        ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if (isFirstGap()) {
            return rfCav.getSTFPrimeFitStart();
        } else if (isEndCell()) {
            return rfCav.getSTFPrimeFitEnd();
        } else {
            return rfCav.getSTFPrimeFit();
        }
    }

}
