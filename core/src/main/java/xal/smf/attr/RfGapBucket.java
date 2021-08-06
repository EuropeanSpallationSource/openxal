package xal.smf.attr;

/**
 * <p>
 * A set of RF gap attributes.
 * </p>
 * <p>
 * This bucket contains information about a specific gap within a resonate
 * cavity such as a DTL or CCL, which is driven by a common RF source.
 * </p>
 * <p>
 * <
 * pre>
 * Elements of this bucket are: length - The length is the length of the gap (m)
 * phaseFactor - the ratio of the RF phase in the gap over the phase in the
 * first gap ampFactor - the ratio of the RF amplitude in the gap over the
 * amplitude in the first gap TTF - The transit time factor of this gap
 * </pre>
 * </p>
 *
 * @author J. Galambos
 * @author Christopher K. Allen
 *
 * @since The Beginning
 * @version May 29, 2015
 */
public class RfGapBucket extends AttributeBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Global Constants
     */
    public static final String TYPE = "rfgap";

    static final String[] ARR_NAMES = {"length",
        "phaseFactor",
        "ampFactor",
        "TTF",
        "endCell",
        "gapOffset",
        "ttfCoeffs",
        "ttfpCoeffs",
        "stfCoeffs",
        "stfpCoeffs"
    };

    /*
     *  Local Attributes
     */
    private Attribute attLength;
    private Attribute attPhaseFactor;
    private Attribute attAmpFactor;
    private Attribute attTTF;

    /**
     * flag for whether this is and end cell (i.e. uses the end cell TTFs)"
     */
    private Attribute attEndCell;

    /**
     * the distance between the Electric and geometric center (E_ctr - G_ctr)
     * (m)
     */
    private Attribute attGapOffset;

    /**
     * (Polynomial) coefficients for an expansion of the T(b) transit time
     * factor about the design value
     */
    private Attribute attTCoeffs;

    /**
     * (Polynomial) coefficients for an expansion of the T(b) derivative (w.r.t.
     * k) about the design value
     */
    private Attribute attTpCoeffs;

    /**
     * (Polynomial) coefficients for an expansion of the S(b) transit time
     * factor about the design value
     */
    private Attribute attSCoeffs;

    /**
     * (Polynomial) coefficients for an expansion of the S(b) derivative (w.r.t.
     * k) about the design value
     */
    private Attribute attSpCoeffs;

    /*
     *  User Interface
     */
 /*
     * Initialization
     */
    /**
     * Constructor for RfGapBucket.
     *
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public RfGapBucket() {
        super();

        // 
        //  Instantiate the original attributes for RF gap
        attLength = new Attribute(0.0);
        attPhaseFactor = new Attribute(0.0);
        attAmpFactor = new Attribute(1.0);
        attTTF = new Attribute(0.0);
        attEndCell = new Attribute(0);
        attGapOffset = new Attribute(0.);

        // Instantiate the fits for the transit time factors
        //  and derivatives
        attTCoeffs = new Attribute(new double[]{});
        attTpCoeffs = new Attribute(new double[]{});
        attSCoeffs = new Attribute(new double[]{});
        attSpCoeffs = new Attribute(new double[]{});

        // Register the attributes with the attribute manager in the
        //  base class.
        super.registerAttribute(ARR_NAMES[0], attLength, "The length is the length of the gap (m).");
        super.registerAttribute(ARR_NAMES[1], attPhaseFactor, "This factor is added to the cavity phase to calculate the RF phase in the gap (rad).");
        super.registerAttribute(ARR_NAMES[2], attAmpFactor, "TThe ratio of the RF amplitude  in the gap over the amplitude in the first gap.");
        super.registerAttribute(ARR_NAMES[3], attTTF, "TTF - The transit time factor of this gap.");
        super.registerAttribute(ARR_NAMES[4], attEndCell, "Flag for whether this is an end cell (i.e. uses the end cell TTFs).");
        super.registerAttribute(ARR_NAMES[5], attGapOffset, "The distance between the Electric and geometric center (E_ctr - G_ctr) (m).");

        // Register the fits for the transit time factors with the
        //  base class attribute manager.
        super.registerAttribute(ARR_NAMES[6], attTCoeffs, "(Polynomial) coefficients for an expansion of the T(b) transit time factor about the design value.");
        super.registerAttribute(ARR_NAMES[7], attTpCoeffs, "(Polynomial) coefficients for an expansion of the T(b) derivative (w.r.t. k) about the design value.");
        super.registerAttribute(ARR_NAMES[8], attSCoeffs, "(Polynomial) coefficients for an expansion of the S(b) transit time factor about the design value.");
        super.registerAttribute(ARR_NAMES[9], attSpCoeffs, "(Polynomial) coefficients for an expansion of the S(b) derivative (w.r.t. k) about the design value.");
    }

    public void setLength(double val) {
        attLength.set(val);
    }

    public void setAmpFactor(double val) {
        attAmpFactor.set(val);
    }

    public void setPhaseFactor(double val) {
        attPhaseFactor.set(val);
    }

    public void setTTF(double val) {
        attTTF.set(val);
    }

    public void setEndCell(int intVal) {
        attEndCell.set(intVal);
    }

    public void setGapOffset(double dblVal) {
        attGapOffset.set(dblVal);
    }

    /**
     * Sets the coefficients of the polynomial expansion for transit time factor
     * <em>T</em>(&beta;). (The Fourier cosine transform of
     * <em>E<sub>z</sub></em>(<em>z</em>).) The coefficients should be in
     * increasing order of monomial degree. Specifically,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T</em>(&beta;) &approx; <em>a</em><sub>0</sub> +
     * <em>a</em><sub>1</sub>&beta; + <em>a</em><sub>2</sub>&beta;<sup>2</sup> +
     * ...
     *
     *
     * @param arrCoeffs {<em>a</em><sub>0</sub>, <em>a</em><sub>1</sub>,
     * <em>a</em><sub>2</sub>, ...}
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public void setTCoefficients(double[] arrCoeffs) {
        this.attTCoeffs.set(arrCoeffs);
    }

    /**
     * Sets the array of coefficients forming the polynomial expansion for the
     * transit time factor derivative <em>T</em>'(&beta;) with respect to wave
     * number <em>k</em>. (Note that the argument is &beta;.) The coefficients
     * are arranged in increasing order so that
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T</em>'(&beta;) &approx; <em>b</em><sub>0</sub> +
     * <em>b</em><sub>1</sub>&beta; + <em>b</em><sub>2</sub>&beta;<sup>2</sup> +
     * ...
     *
     * @param arrCoeffs
     * {<em>b</em><sub>0</sub>,<em>b</em><sub>1</sub>,<em>b</em><sub>2</sub>,...}
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public void setTpCoefficients(double[] arrCoeffs) {
        this.attTpCoeffs.set(arrCoeffs);
    }

    /**
     * Sets the coefficients of the polynomial expansion for transit time factor
     * <em>S</em>(&beta;). (The Fourier sine transform of
     * <em>E<sub>z</sub></em>(<em>z</em>).) The coefficients should be in
     * increasing order of monomial degree. Specifically,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S</em>(&beta;) &approx; <em>a</em><sub>0</sub> +
     * <em>a</em><sub>1</sub>&beta; + <em>a</em><sub>2</sub>&beta;<sup>2</sup> +
     * ...
     *
     *
     * @param arrCoeffs {<em>a</em><sub>0</sub>, <em>a</em><sub>1</sub>,
     * <em>a</em><sub>2</sub>, ...}
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public void setSCoefficients(double[] arrCoeffs) {
        this.attSCoeffs.set(arrCoeffs);
    }

    /**
     * Sets the array of coefficients forming the polynomial expansion for the
     * transit time factor derivative <em>S</em>'(&beta;) with respect to wave
     * number <em>k</em>. (Note that the argument is &beta;.) The coefficients
     * are arranged in increasing order so that
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S</em>'(&beta;) &approx; <em>b</em><sub>0</sub> +
     * <em>b</em><sub>1</sub>&beta; + <em>b</em><sub>2</sub>&beta;<sup>2</sup> +
     * ...
     *
     * @param arrCoeffs
     * {<em>b</em><sub>0</sub>,<em>b</em><sub>1</sub>,<em>b</em><sub>2</sub>,...}
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public void setSpCoefficients(double[] arrCoeffs) {
        this.attSpCoeffs.set(arrCoeffs);
    }


    /*
     * Attribute Query
     */
    /**
     * Override virtual to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    public double getLength() {
        return attLength.getDouble();
    }

    public double getAmpFactor() {
        return attAmpFactor.getDouble();
    }

    public double getPhaseFactor() {
        return attPhaseFactor.getDouble();
    }

    public double getTTF() {
        return attTTF.getDouble();
    }

    public int getEndCell() {
        return attEndCell.getInteger();
    }

    public double getGapOffset() {
        return attGapOffset.getDouble();
    }

    /**
     * Returns the array of coefficients forming the polynomial expansion for
     * the transit time factor <em>T</em>(&beta;) about the design &beta;. (This
     * is the Fourier cosine transform.) The coefficients are arranged in
     * increasing order so that
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T</em>(&beta;) &approx; <em>a</em><sub>0</sub> +
     * <em>a</em><sub>1</sub>&beta; + <em>a</em><sub>2</sub>&beta;<sup>2</sup> +
     * ...
     *
     * @return
     * {<em>a</em><sub>0</sub>,<em>a</em><sub>1</sub>,<em>a</em><sub>2</sub>,...}
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public double[] getTCoefficients() {
        return this.attTCoeffs.getArrDbl();
    }

    /**
     * Returns the array of coefficients forming the polynomial expansion for
     * the transit time factor derivative <em>T</em>'(&beta;) with respect to
     * wave number <em>k</em>. (Note that the argument is &beta;.) The
     * coefficients are arranged in increasing order so that
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T</em>'(&beta;) &approx; <em>b</em><sub>0</sub> +
     * <em>b</em><sub>1</sub>&beta; + <em>b</em><sub>2</sub>&beta;<sup>2</sup> +
     * ...
     *
     * @return
     * {<em>b</em><sub>0</sub>,<em>b</em><sub>1</sub>,<em>b</em><sub>2</sub>,...}
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public double[] getTpCoefficients() {
        return this.attTpCoeffs.getArrDbl();
    }

    /**
     * Returns the array of coefficients forming the polynomial expansion for
     * the transit time factor <em>S</em>(&beta;) about the design &beta;. (This
     * is the Fourier sine transform.) The coefficients are arranged in
     * increasing order so that
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S</em>(&beta;) &approx; <em>a</em><sub>0</sub> +
     * <em>a</em><sub>1</sub>&beta; + <em>a</em><sub>2</sub>&beta;<sup>2</sup> +
     * ...
     *
     * @return
     * {<em>a</em><sub>0</sub>,<em>a</em><sub>1</sub>,<em>a</em><sub>2</sub>,...}
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public double[] getSCoefficients() {
        return this.attSCoeffs.getArrDbl();
    }

    /**
     * Returns the array of coefficients forming the polynomial expansion for
     * the transit time factor derivative <em>S</em>'(&beta;) with respect to
     * wave number <em>k</em>. (Note that the argument is &beta;.) The
     * coefficients are arranged in increasing order so that
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S</em>'(&beta;) &approx; <em>b</em><sub>0</sub> +
     * <em>b</em><sub>1</sub>&beta; + <em>b</em><sub>2</sub>&beta;<sup>2</sup> +
     * ...
     *
     * @return
     * {<em>b</em><sub>0</sub>,<em>b</em><sub>1</sub>,<em>b</em><sub>2</sub>,...}
     *
     * @since May 29, 2015 by Christopher K. Allen
     */
    public double[] getSpCoefficients() {
        return this.attSpCoeffs.getArrDbl();
    }
}
