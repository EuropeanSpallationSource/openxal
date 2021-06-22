package xal.extension.jels.smf.impl;

import xal.extension.jels.smf.attr.ESSRfCavityBucket;
import xal.extension.jels.tools.math.InverseRealPolynomial;
import xal.ca.ChannelFactory;
import xal.smf.attr.RfCavityBucket;
import xal.smf.impl.RfCavity;

/**
 * This RfCavity implementation is extended to: - provide special TTF/STF fits
 * for the start gap - to fit TTF/STF using TraceWin parameters using
 * InverseRealPolinomial
 *
 * @author Ivo List
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 *
 */
public class ESSRfCavity extends RfCavity {

    public ESSRfCavity(String strId, ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setRfField(new ESSRfCavityBucket());
    }

    public ESSRfCavity(String strId) {
        super(strId);
        setRfField(new ESSRfCavityBucket());
    }

    public ESSRfCavity(String strId, int intReserve) {
        super(strId, intReserve);
        setRfField(new ESSRfCavityBucket());
    }

    public ESSRfCavity(String strId, ChannelFactory channelFactory, int intReserve) {
        super(strId, channelFactory, intReserve);
        setRfField(new ESSRfCavityBucket());
    }

    @Override
    public ESSRfCavityBucket getRfField() {
        return (ESSRfCavityBucket) bucRfCavity;
    }

    /**
     * @return a fit of the transit time factor as a function of beta
     */
    @Override
    public InverseRealPolynomial getTTFFit() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getTTFCoefs());
    }

    /**
     * @return a fit of the transit time factor prime as a function of beta
     */
    @Override
    public InverseRealPolynomial getTTFPrimeFit() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getTTFPrimeCoefs());
    }

    /**
     * @return a fit of the "S" transit time factor as a function of beta
     */
    @Override
    public InverseRealPolynomial getSTFFit() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getSTFCoefs());
    }

    /**
     * @return a fit of the "S" transit time factor prime as a function of beta
     */
    @Override
    public InverseRealPolynomial getSTFPrimeFit() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getSTFPrimeCoefs());
    }

    /**
     * @return a fit of the transit time factor for end cells as a function of
     * beta
     */
    @Override
    public InverseRealPolynomial getTTFFitEnd() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getTTFEndCoefs());
    }

    /**
     * @return a fit of the transit time factor prime for end cells as a
     * function of beta
     */
    @Override
    public InverseRealPolynomial getTTFPrimeFitEnd() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getTTFPrimeEndCoefs());
    }

    /**
     * @return a fit of the "S" transit time factor for end cells as a function
     * of beta
     */
    @Override
    public InverseRealPolynomial getSTFFitEnd() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getSTFEndCoefs());
    }

    /**
     * @return a fit of the "S" transit time factor prime for end cells as a
     * function of beta
     */
    @Override
    public InverseRealPolynomial getSTFPrimeFitEnd() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getSTFPrimeEndCoefs());
    }

    /**
     * @return a fit of the transit time factor for start cells as a function of
     * beta
     */
    public InverseRealPolynomial getTTFFitStart() {
        ESSRfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getTTFStartCoefs());
    }

    /**
     * @return a fit of the transit time factor prime for start cells as a
     * function of beta
     */
    public InverseRealPolynomial getTTFPrimeFitStart() {
        ESSRfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getTTFPrimeStartCoefs());
    }

    /**
     * @return a fit of the "S" transit time factor for start cells as a
     * function of beta
     */
    public InverseRealPolynomial getSTFFitStart() {
        ESSRfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getSTFStartCoefs());
    }

    /**
     * @return a fit of the "S" transit time factor prime for start cells as a
     * function of beta
     */
    public InverseRealPolynomial getSTFPrimeFitStart() {
        ESSRfCavityBucket rfCavBuc = this.getRfField();
        return new InverseRealPolynomial(rfCavBuc.getSTFPrimeStartCoefs());
    }
}
