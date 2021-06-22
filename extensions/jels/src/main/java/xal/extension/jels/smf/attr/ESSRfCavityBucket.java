package xal.extension.jels.smf.attr;

import xal.smf.attr.Attribute;
import xal.smf.attr.RfCavityBucket;

/**
 * An extended set of RF cavity attributes. Added are TTF/STF coefficients for
 * the start gap.
 *
 * @author Ivo List
 */
public class ESSRfCavityBucket extends RfCavityBucket {

    private static final long serialVersionUID = 1;

    /*
     *  Constants
     */
    public static final String c_strType = "rfcavity";

    static final String[] ARR_NAMES = {"TTF_startCoefs",
        "TTFPrime_startCoefs",
        "STF_startCoefs",
        "STFPrime_startCoefs",};

    /*
     *  Local Attributes
     */
    /**
     * quadratic fit coefficients for the transit time factor as a function of
     * beta for the start cells (constant, linear, quad)
     */
    private Attribute attTTFStartCoefs;
    /**
     * quadratic fit coefficients for the transit time factor prime as a
     * function of beta for the start cells (constant, linear, quad)
     */
    private Attribute attTTFPrimeStartCoefs;
    /**
     * quadratic fit coefficients for the "S transit time factor" as a function
     * of beta for the start cells (constant, linear, quad)
     */
    private Attribute attSTFStartCoefs;
    /**
     * quadratic fit coefficients for the "S transit time factor" prime as a
     * function of beta for the start cells (constant, linear, quad)
     */
    private Attribute attSTFPrimeStartCoefs;

    /*
     *  User Interface
     */
    /**
     * Override virtual to provide type signature
     */
    @Override
    public String getType() {
        return c_strType;
    }

    public ESSRfCavityBucket() {
        super();

        attTTFStartCoefs = new Attribute(new double[]{});
        attTTFPrimeStartCoefs = new Attribute(new double[]{});
        attSTFStartCoefs = new Attribute(new double[]{});
        attSTFPrimeStartCoefs = new Attribute(new double[]{});

        super.registerAttribute(ARR_NAMES[0], attTTFStartCoefs, "Quadratic fit coefficients for the transit time factor as a function of beta for the start cells (constant, linear, quad).");
        super.registerAttribute(ARR_NAMES[1], attTTFPrimeStartCoefs, "Quadratic fit coefficients for the transit time factor prime as a function of beta for the start cells (constant, linear, quad).");
        super.registerAttribute(ARR_NAMES[2], attSTFStartCoefs, "Quadratic fit coefficients for the \"S transit time factor\" as a function of beta for the start cells (constant, linear, quad).");
        super.registerAttribute(ARR_NAMES[3], attSTFPrimeStartCoefs, "Quadratic fit coefficients for the \"S transit time factor\" prime as a function of beta for the start cells (constant, linear, quad).");
    }

    public double[] getTTFStartCoefs() {
        return attTTFStartCoefs.getArrDbl();
    }

    public double[] getTTFPrimeStartCoefs() {
        return attTTFPrimeStartCoefs.getArrDbl();
    }

    public double[] getSTFStartCoefs() {
        return attSTFStartCoefs.getArrDbl();
    }

    public double[] getSTFPrimeStartCoefs() {
        return attSTFPrimeStartCoefs.getArrDbl();
    }

    public void setTTFStartCoefs(double[] arrVal) {
        attTTFStartCoefs.set(arrVal);
    }

    public void setTTFPrimeStartCoefs(double[] arrVal) {
        attTTFPrimeStartCoefs.set(arrVal);
    }

    public void setSTFStartCoefs(double[] arrVal) {
        attSTFStartCoefs.set(arrVal);
    }

    public void setSTFPrimeStartCoefs(double[] arrVal) {
        attSTFPrimeStartCoefs.set(arrVal);
    }
}
