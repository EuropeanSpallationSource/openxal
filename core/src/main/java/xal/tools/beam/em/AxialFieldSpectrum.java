/**
 * FieldSpectrum.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 23, 2015
 */
package xal.tools.beam.em;

import xal.tools.math.Complex;
import xal.tools.math.fnc.IRealFunction;

/**
 * <p>
 * Class that representing the spatial spectral properties of a time-harmonic,
 * axial electric field. The most important properties from a beam physics
 * standpoint are the transit time factors. These are the components of the
 * Fourier transform of the axial field, and their resulting Hilbert transforms.
 * The Hilbert transform of a transit time factor turns out to be the transit
 * time factor of axial field times the signum function. The spectral pre- and
 * post-envelopes are formed from a transit time factor and its Hilbert
 * transform. These pre- and post-envelopes are the primary entities for
 * computing the pre- and post-gap energy gain and phase jump, respectively.
 * </p>
 * <p>
 * <h3>Partial Field Model</h3>
 * This class attempts to maintain backward compatibility between the RF
 * acceleration model produced by Los Alamos and CERN. There the
 * <em>T<sub>q</sub></em> and <em>S</em> transit time factors are zero. Although
 * usually labeled <em>S</em>, the "sine" transit time factor is actually its
 * quadrature conjugate <em>S<sub>q</sub></em>. That model also requires an
 * "offset" &Delta;<em>z</em> which is the distance between the coordinate
 * origin and the field center (the point of symmetry). The assumptions are that
 * the field is symmetric about the axial location &Delta;<em>z</em>, if not
 * this information is lost.
 * </p>
 * <p>
 * In the partial field model the provided spectra, <em>T</em>,
 * <em>S<sub>q</sub></em>, and their derivatives, are assumed to be functions of
 * normalized particle velocity &beta; &trie;
 * <em>v</em>/<em>c</em>, where <em>v</em> is particle velocity and <em>c</em>
 * is the speed of light. Thus, the functions <em>T</em>(&beta;),
 * <em>dT</em>(&beta;)/<em>dk</em>, <em>S<sub>q</sub></em>(&beta;), and
 * <em>dS<sub>q</sub></em>(&beta;)/<em>dk</em> are provided to the partial field
 * model constructor.
 * </p>
 * <p>
 * Note the important difference that in the full-field model the arguments of
 * the spectra quantities are the particle wave number <em>k</em>. That is, the
 * constructor for the full-field model takes spectral functions
 * <em>T</em>(<em>k</em>), <em>dT</em>(<em>k</em>)/<em>dk</em>,
 * <em>S<sub>q</sub></em>(<em>k</em>), and
 * <em>dS<sub>q</sub></em>(<em>k</em>)/<em>dk</em>, along with all the other
 * spectral quantities.
 * </p>
 * <p>
 * <h3>Full Field Model</h3>
 * The current full-field model includes all four transit time factors and their
 * derivatives. Thus, all the field information is kept, no offsets are
 * necessary, and there is information enough to compute the post gap energy
 * gain and phase jump. (In the above model the post gap quantities are assumed
 * to be equal to the pre-gap quantities.) When function objects are provided
 * for all four transit time factors this class assumes that the new model is
 * being used. When only <em>T</em> and <em>S</em> are provided the class
 * assumes that the old model is being used. Further assumptions are
 * <br/>
 * <br/>
 * &middot; The sine transit time factor <em>S</em> is actually the conjugate
 * <em>S<sub>q</sub></em>
 * (see below)
 * <br/>
 * &middot; The offset &Delta;<em>z</em> must also be provided
 * <br/>
 * <br/>
 * The class then makes the appropriate conversions from the quantities
 * (<em>T,T',S,S'</em>,&Delta;<em>z</em>) to
 * (<em>T,T',T<sub>q</sub>,T'<sub>q</sub>,S,S',S<sub>q</sub>,S'<sub>q</sub></em>).
 * Of course the later set is incomplete.
 * </p>
 * <p>
 * <h4>Axial Electric Fields</h4>
 * Let the longitudinal electric field along the beam axis <em>z</em> be denoted
 * <em>E<sub>z</sub></em>(<em>z</em>). Let the total voltage gain long the field
 * be denoted <em>V</em><sub>0</sub>, that is,
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <em>V</em><sub>0</sub> &trie;
 * &int;<em>E<sub>z</sub></em>(<em>z</em>)
 * <em>dz</em> .
 * <br/>
 * <br/>
 * Note then that the total available energy gain &Delta;<em>W</em><sub>0</sub>
 * for a particle falling through the field <em>E<sub>z</sub></em>(<em>z</em>)
 * is
 * <em>qV</em><sub>0</sub>. The value <em>V</em><sub>0</sub>
 * is used to create the normalized electric field
 * <em>e<sub>z</sub></em>(<em>z</em>) given by
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <em>e</em><sub><em>z</em></sub>(<em>z</em>) &trie;
 * (1/<em>V</em><sub>0</sub>)<em>E<sub>z</sub></em>(<em>z</em>) .
 * <br/>
 * <br/>
 * All spectral quantities in this class are with respect to this normalized
 * field.
 * </p>
 * <p>
 * Next, let sgn(<em>z</em>) denote the signum function, that is,
 * <br/>
 * <pre>
 *     sgn(<em>z</em>) &trie; -1    <em>z</em>  &lt; 0
 *              +1    <em>z</em>  &gt; 0
 * </pre> Finally, define the <em>quadrature field</em>
 * <em>E<sub>q</sub></em>(<em>z</em>) as
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <em>E<sub>q</sub></em>(<em>z</em>) &trie; sgn(<em>z</em>)
 * <em>E<sub>z</sub></em>(<em>z</em>) .
 * <br/>
 * <br/>
 * Of course the quantities here will be with respect to the normalized
 * quadrature field
 * <em>e</sub>q</sub></em>(<em>z</em>) defined as
 * <br/>
 * <pre>
 *     <em>e</em><sub><em>z</em></sub>(<em>z</em>) &trie; (1/<em>V</em><sub>0</sub>)<em>E<sub>q</sub></em>(<em>z</em>) ,
 *           = sgn(<em>z</em>)<em>e<sub>z</sub></em>(</em>z</em>) .
 * </pre>
 * </p>
 * <p>
 * <h4>Transit Time Factors</h4>
 * There are four (4) transit time factors. These transit time factors are
 * described as follows:
 * <br/>
 * <br/>
 * &middot; <em>T</em>(<em>k</em>) - The Fourier cosine transform of axial field
 * <em>E<sub>z</sub></em>(<em>z</em>)
 * <br/>
 * &nbsp; &nbsp; <em>T</em>(<em>k</em>) &trie;
 * (1/<em>V</em><sub>0</sub>)&int;<em>E<sub>z</sub></em>(<em>z</em>) cos
 * <em>kz</em>
 * <em>dz</em>
 * <br/>
 * <br/>
 * &middot; <em>S</em>(<em>k</em>) - The Fourier sine transform of axial field
 * <em>E<sub>z</sub></em>(<em>z</em>)
 * <br/>
 * &nbsp; &nbsp; <em>S</em>(<em>k</em>) &trie;
 * (1/<em>V</em><sub>0</sub>)&int;<em>E<sub>z</sub></em>(<em>z</em>) sin
 * <em>kz</em>
 * <em>dz</em>
 * <br/>
 * <br/>
 * &middot; <em>T<sub>q</sub></em>(<em>k</em>) - The Fourier cosine transform of
 * the axial field sgn(<em>z</em>)<em>E<sub>z</sub></em>(<em>z</em>)
 * <br/>
 * &nbsp; &nbsp; <em>T<sub>q</sub></em>(<em>k</em>) &trie;
 * (1/<em>V</em><sub>0</sub>)&int;sgn(<em>z</em>)<em>E<sub>z</sub></em>(<em>z</em>)
 * cos
 * <em>kz</em> <em>dz</em>
 * <br/>
 * <br/>
 * &middot; <em>S<sub>q</sub></em>(<em>k</em>) - The Fourier sine transform of
 * axial field sgn(<em>z</em>)<em>E<sub>z</sub></em>(<em>z</em>)
 * <br/>
 * &nbsp; &nbsp; <em>S<sub>q</em></em>(<em>k</em>) &trie;
 * (1/<em>V</em><sub>0</sub>)&int;
 * sgn(<em>z</em>)<em>E<sub>z</sub></em>(<em>z</em>) sin
 * <em>kz</em> <em>dz</em>
 * <br/>
 * <br/>
 * where <em>k</em> is the synchronous particle wave number. Sometimes the
 * arguments to the transit time factors is the synchronous particle velocity
 * &beta;. This includes that for the derivative functions as well (see below).
 * One needs to check the method documentation for the argument type.
 * </p>
 * <p>
 * The derivatives of the transit time factors are also available. These are the
 * derivatives with respect to wave number <em>k</em> and will be denoted
 * <em>T'</em>(<em>k</em>), <em>S'</em>(<em>k</em>),
 * <em>T'<sub>q</sub></em>(<em>k</em>), and <em>S'<sub>q</sub></em>(<em>k</em>).
 * </p>
 * <p>
 * <h4>Hilbert Transform</h4>
 * The transit time factors are related to each other via the Hilbert transform
 * &Hscr;. Specifically,
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <em>T<sub>q</sub></em>(<em>k</em>) =
 * -&Hscr;[<em>S</em>(<em>k</em>)] ,
 * <br/>
 * &nbsp; &nbsp; <em>S<sub>q</sub></em>(<em>k</em>) =
 * +&Hscr;[<em>T</em>(<em>k</em>)] .
 * <br/>
 * <br/>
 * This is a transitive relation which follows from the anti-selfadjointness of
 * the Hilbert transform, thus,
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <em>T</em>(<em>k</em>) =
 * -&Hscr;[<em>S<sub>q</sub></em>(<em>k</em>)] ,
 * <br/>
 * &nbsp; &nbsp; <em>S</em>(<em>k</em>) =
 * +&Hscr;[<em>T<sub>q</sub></em>(<em>k</em>)] .
 * <br/>
 * <br/>
 * The Hilbert transform also relates the field spectra, as shown below.
 * </p>
 * <p>
 * <h4>Field Spectra</h4>
 * Denote by &Escr;<sub><em>z</em></sub>(<em>k</em>) and
 * &Escr;<sub><em>q</em></sub>(<em>k</em>) the Fourier transforms of the axial
 * field
 * <em>e<sub>z</em>(<em>z</em>) and its conjugate
 * <em>e<sub>q</em></em>(<em>z</em>), respectively. That is, the field spectra
 * are
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Escr;<sub><em>z</em></sub>(k) &trie;
 * &Fscr;[<em>e<sub>z</sub></em>](<em>k</em>) ,
 * <br/>
 * &nbsp; &nbsp; &Escr;<sub><em>q</em></sub>(k) &trie;
 * &Fscr;[<em>e<sub>q</sub></em>](<em>k</em>) ,
 * <br/>
 * <br/>
 * where &Fscr;[&middot;] is the Fourier transform operator. The Fourier
 * transforms of the fields have the decomposition
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Escr;<sub><em>z</em></sub>(<em>k</em>) = T</em>(<em>k</em>) -
 * <em>i</em><em>S</em>(<em>k</em>) ,
 * <br/>
 * &nbsp; &nbsp; &Escr;<em><sub>q</sub></em>(<em>k</em>) =
 * T<sub>q</sub></em>(<em>k</em>) - <em>i</em><em>S<sub>q</sub></em>(<em>k</em>)
 * ,
 * <br/>
 * <br/>
 * where <em>i</em> is the imaginary unit. The spectra are then related by the
 * Hilbert transforms
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Hscr;[&Escr;<sub><em>z</em></sub>(<em>k</em>)] =
 * i&Escr;<em><sub>q</sub></em>(<em>k</em>) ,
 * <br/>
 * &nbsp; &nbsp; &Hscr;[&Escr;<sub><em>q</em></sub>(<em>k</em>)] =
 * i&Escr;<em><sub>z</sub></em>(<em>k</em>) ,
 * <br/>
 * <br/>
 * Thus, we see &Escr;<sub><em>z</em></sub>(<em>k</em>) and
 * &Escr;<sub><em>q</em></sub>(<em>k</em>) are conjugates of each other.
 * </p>
 * <p>
 * <h4>Pre- and Post-Envelope Spectra</h4>
 * The pre- and post-envelope spectra can be formed from the field spectra.
 * First, denote by &Escr;<sup>-</sup>(<em>k</em>) and
 * &Escr;<sup>+</sup>(<em>k</em>) the pre- and post-envelope spectra,
 * respectively. They are defined
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Escr;<sup>-</sup>(k) &trie; (1/2)[
 * &Escr;<sub><em>z</em></sub>(<em>k</em>) +
 * i&Hscr;[&Escr;<sub><em>z</em></sub>(<em>k</em>] ] = (1/2)[
 * &Escr;<sub><em>z</em></sub>(<em>k</em>) -
 * &Escr;<sub><em>q</em></sub>(<em>k</em>) ],
 * <br/>
 * &nbsp; &nbsp; &Escr;<sup>+</sup>(k) &trie; (1/2)[
 * &Escr;<sub><em>z</em></sub>(<em>k</em>) +
 * i&Hscr;[&Escr;<sub><em>z</em></sub>(<em>k</em>] ] = (1/2)[
 * &Escr;<sub><em>z</em></sub>(<em>k</em>) +
 * &Escr;<sub><em>q</em></sub>(<em>k</em>) ] ,
 * <br/>
 * <br/>
 * Let &phi; be the synchronous particle phase at the gap center. Then the
 * quantities
 * <em>e<sup>-i&phi;</sup></em>&Escr;<sup>-</sup>(<em>k</em>) and
 * <em>e<sup>-i&phi;</sup></em>&Escr;<sup>+</sup>(<em>k</em>) contain the pre-
 * and post-gap energy gain &Delta;<em>W</em><sup>-</sup>,
 * &Delta;<em>W</em><sup>+</sup>
 * and phase jump &Delta;&phi;<sup>-</sup>, &Delta;&phi;<sup>+</sup>,
 * respectively. For example, the real part of
 * <em>e<sup>-i&phi;</sup></em>&Escr;<sup>-</sup>(<em>k</em>) tracks the pre-gap
 * energy gain while the imaginary part tracks the phase jump. We have
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Delta;<em>W</em><sup>-</sup>(&phi;,<em>k</em>) =
 * <em>qV</em><sub>0</sub> Re
 * &Escr;<sup>-</sup>(<em>k</em>)<em>e<sup>-i&phi;</sup></em> ,
 * <br/>
 * &nbsp; &nbsp; &Delta;&phi;<sup>-</sup>(&phi;,<em>k</em>) =
 * <em>d</em>/<em>dk</em>
 * Im
 * <em>K<sub>i</sub></em>&Escr;<sup>-</sup>(<em>k</em>)<em>e<sup>-i&phi;</sup></em>
 * ,
 * <br/>
 * <br/>
 * where <em>q</em> is the unit charge and <em>K<sub>i</sub></em> is the
 * quantity
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <em>K<sub>i</sub></em> &trie;
 * <em>k</em><sub>0</sub>(<em>qV</em><sub>0</sub>/<em>mc</em><sup>2</sup>)(1/&beta;<sub><em>i</em></sub><sup>3</sup>&gamma;<sub><em>i</em></sub><sup>3</sup>)
 * .
 * <br/>
 * <br/>
 * The subscript <em>i</em> indicates initial, pre-gap values. The post-gap
 * quantities have analogous expressions
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Delta;<em>W</em><sup>+</sup>(<em>k</em>,&phi;) =
 * (<em>q</em>/2) Re
 * <em>e<sup>-i&phi;</sup></em>&Escr;<sup>-</sup>(<em>k</em>) ,
 * <br/>
 * &nbsp; &nbsp; &Delta;&phi;<sup>+</sup>(<em>k</em>,&phi;) =
 * (<em>K<sub>f</sub></em>/2) Im <em>d</em>/<em>dk</em>
 * <em>e<sup>-i&phi;</sup></em>&Escr;<sup>-</sup>(<em>k</em>) ,
 * <br/>
 * <br/>
 * The subscript <em>f</em> indicates final, post-gap quantities. Methods to
 * compute these values are provided.
 * </p>
 * <p>
 *
 * @author Christopher K. Allen
 * @since Sep 23, 2015
 * @version Sep 23, 2015
 */
public class AxialFieldSpectrum {

    /*
     * Global Constants
     */
    /**
     * the value of 2&pi;
     */
    private static final double DBL_2PI = 2.0 * Math.PI;

    /**
     * Speed of light in a vacuum (meters/second)
     */
    private static final double DBL_LGHT_SPD = 299792458.0;

    /*
     * Local Attributes
     */
    //
    //  Partial Field Model with field offset and frequency to convert from beta 
    //
    /**
     * polynomial fit for the cosine transit time factor T versus beta
     */
    private final IRealFunction fncTz0;

    /**
     * polynomial fit of the cosine transit time factor derivative T' versus
     * beta
     */
    private final IRealFunction fncDTz0;

    /**
     * polynomial fit of the conjugate sine transit time factor Sq versus beta
     */
    private final IRealFunction fncSq0;

    /**
     * polynomial fit of the conjugate sine transit time factor derivative Sq'
     * versus beta
     */
    private final IRealFunction fncDSq0;

    /**
     * the offset between the field center and the geometric center
     */
    private final double dblFldOSet;

    /**
     * the time harmonic frequency of the field
     */
    private final double dblFrq;

    //
    // Full Field Acceleration Model
    //
    /**
     * polynomial fit for the cosine transit time factor T versus beta
     */
    private final IRealFunction fncTz;

    /**
     * polynomial fit of the cosine transit time factor derivative T' versus
     * beta
     */
    private final IRealFunction fncDTz;

    /**
     * polynomial fit of the conjugate sine transit time factor Sq versus beta
     */
    private final IRealFunction fncSq;

    /**
     * polynomial fit of the conjugate sine transit time factor derivative Sq'
     * versus beta
     */
    private final IRealFunction fncDSq;

    /**
     * polynomial fit for the conjugate cosine transit time factor Tq versus
     * beta
     */
    private final IRealFunction fncTq;

    /**
     * polynomial fit of the conjugate cosine transit time factor derivative Tq'
     * versus beta
     */
    private final IRealFunction fncDTq;

    /**
     * polynomial fit of the sine transit time factor S versus beta
     */
    private final IRealFunction fncSz;

    /**
     * polynomial fit of the sine transit time factor derivative S' versus beta
     */
    private final IRealFunction fncDSz;

    //
    // State Variables
    //
    /**
     * flag indicating that old acceleration model is in use
     */
    private final boolean bolPrtlFldMdl;

    /*
     * Initialization
     */
    /**
     * <p>
     * Constructor for creating a partial field model object using a central
     * field offset.
     * </p>
     * <p>
     * In this case the given field spectra are assumed to be functions of
     * normalized particle velocity &beta;. Thus, the RF frequency is needed to
     * convert from wave number <em>k</em> to normalized velocity &beta; in
     * order to evaluate the functions.
     * </p>
     *
     * @param dblFreq frequency of the time-harmonic electric field (Hz)
     * @param dblOffset offset between axial origin and central field position
     * (meters)
     * @param fncTz0 central cosine transform (transit time factor)
     * @param fncDTz0 derivative of the central cosine transform w.r.t.
     * <em>k</em>
     * @param fncSq0 conjugate central sine transform (transit time factor)
     * @param fncDSq0 derivative of the conjugate central sine transform w.r.t.
     * <em>k</em>
     *
     * @since Sep 23, 2015 by Christopher K. Allen
     */
    public AxialFieldSpectrum(
            double dblFreq,
            double dblOffset,
            IRealFunction fncTz0, IRealFunction fncDTz0,
            IRealFunction fncSq0, IRealFunction fncDSq0
    ) {
        // Get common parameters
        this.dblFrq = dblFreq;
        this.dblFldOSet = dblOffset;

        // Set parameters for the partial field acceleration model
        //  then set old model flag
        this.fncTz0 = fncTz0;
        this.fncSq0 = fncSq0;
        this.fncDTz0 = fncDTz0;
        this.fncDSq0 = fncDSq0;

        // Set the partial field model flag
        this.bolPrtlFldMdl = true;

        // Null out all the full field model parameters
        this.fncTz = null;
        this.fncDTz = null;
        this.fncSz = null;
        this.fncDSz = null;
        this.fncTq = null;
        this.fncDTq = null;
        this.fncSq = null;
        this.fncDSq = null;
    }

    /**
     * <p>
     * Constructor for creating the full field model using the sine and cosine
     * transform and their conjugates. The pre- and post-envelope spectra are
     * build from these objects and used to compute acceleration parameters.
     * </p>
     * <p>
     * Note that frequency <em>f</em> is not needed here since the given
     * arguments are assumed to be functions of <em>k</em>. RF frequency is only
     * needed to convert from wave number <em>k</em> to normalized velocity
     * &beta; in the case of spectral functions that are functions of &beta;
     * (e.g.,
     * <em>T</em>(&beta;), <em>S</em>(&beta;), etc.).
     * </p>
     *
     * @param fncTz field cosine transform (transit time factor)
     * @param fncDTz derivative of the cosine transform w.r.t. <em>k</em>
     * @param fncSq field conjugate sine transform (transit time factor)
     * @param fncDSq derivative of the sine transform w.r.t. <em>k</em>
     * @param fncTq field conjugate cosine transform (transit time factor)
     * @param fncDTq derivative of the conjugate cosine transform w.r.t.
     * <em>k</em>
     * @param fncSz field sine transform (transit time factor)
     * @param fncDSz derivative of the field sine transform w.r.t. <em>k</em>
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public AxialFieldSpectrum(
            IRealFunction fncTz, IRealFunction fncDTz,
            IRealFunction fncSz, IRealFunction fncDSz,
            IRealFunction fncTq, IRealFunction fncDTq,
            IRealFunction fncSq, IRealFunction fncDSq
    ) {
        super();

        // Set all the parameters for the full field model
        //  The primary spectra
        this.fncTz = fncTz;
        this.fncDTz = fncDTz;
        this.fncSz = fncSz;
        this.fncDSz = fncDSz;

        //  The quadrature components
        this.fncTq = fncTq;
        this.fncDTq = fncDTq;
        this.fncSq = fncSq;
        this.fncDSq = fncDSq;

        // Clear the partial field model flag and offset parameter
        this.bolPrtlFldMdl = false;

        // Null out all the partial field model parameters
        this.dblFldOSet = 0.0;
        // frequency is only used to convert from k to beta
        this.dblFrq = 0.0;

        this.fncTz0 = null;
        this.fncDTz0 = null;
        this.fncSq0 = null;
        this.fncDSq0 = null;
    }

    /*
     * Attributes
     */
    /**
     * Returns <code>true</code> when this object has been initialized to use
     * the partial field model.
     *
     * @return
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public boolean isPartialFieldModel() {
        return this.bolPrtlFldMdl;
    }

    /**
     * Returns the time-harmonic frequency of this electric field.
     *
     * @return the RF frequency of the electric field (Hz)
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public double getFrequency() {
        return this.dblFrq;
    }

    /**
     * <p>
     * Returns the offset between the axis origin and the center of the field,
     * however that is defined. That is, it could be defined as the point of
     * maximum field, or the center of mass, etc.
     * </p>
     * <p>
     * If the full field model is being used then this parameter is not defined
     * and a <code>null</code> value will be returned.
     * </p>
     *
     * @return offset between field center and axis origin in partial field
     * model (<strong>meters</strong>), or <code>null</code> if the full field
     * model is being used
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public Double getFieldOffset() {

        if (this.bolPrtlFldMdl) {
            return this.dblFldOSet;
        }

        return null;
    }

    /*
     * Complex Spectra
     */
    /**
     * <p>
     * Computes and returns the complex spectra of this spatial field at the
     * given wave number <em>k</em>. The value returned has the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sub><em>z</em></sub>(<em>k</em>) =
     * <em>T</em><sub><em>z</em></sub>(</em>k</em>) - <em>i</em>
     * <em>S</em><sub><em>z</em></sub>(<em>k</em>)
     * <br/>
     * <br/>
     * where &Escr;<sub><em>z</em></sub> is the returned spectral value,
     * <em>T</em><sub><em>z</em></sub> is the cosine transit-time factor, and
     * <em>S</em><sub><em>z</em></sub> is the sine transit-time factor.
     * </p>
     * <p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total
     * potential drop <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(<em>z</em>)<em>dz</em> so that the resulting
     * total potential of the field is 1.
     * </p>
     *
     * @param k particle wave number (in radians/meter)
     *
     * @return the Fourier spectrum &Escr;(<em>k</em>) of the field at the given
     * wave number <em>k</em>
     *
     * @since Sep 30, 2015, Christopher K. Allen
     */
    public Complex fldSpectrum(double k) {
        double dblReal = +this.tz(k);
        double dblImag = -this.sz(k);

        return new Complex(dblReal, dblImag);
    }

    /**
     * <p>
     * Computes and returns the derivative, with respect to the wave number
     * <em>k</em>, of the field spectrum at the given wave number <em>k</em>.
     * The value returned has the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sub><em>z</em></sub>'(<em>k</em>) =
     * <em>T</em>'<sub><em>z</em></sub>(</em>k</em>) - <em>i</em>
     * <em>S</em><sub><em>z</em></sub>'(<em>k</em>)
     * <br/>
     * <br/>
     * where &Escr;<sub><em>z</em></sub>' is the returned spectral derivative,
     * <em>T</em><sub><em>z</em></sub>' is the cosine transit-time factor
     * derivative, and <em>S</em><sub><em>z</em></sub>' is the sine transit-time
     * factor derivative of the field.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total
     * potential drop <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(<em>z</em>)<em>dz</em> so that the resulting
     * total potential of the field is 1.
     * </p>
     *
     * @param k particle wave number (in radians/meter)
     *
     * @return Fourier spectrum derivative &Escr;'(<em>k</em>), w.r.t.
     * <em>k</em>, of the field at the given wave number <em>k</em>
     * (meters/radian)
     *
     * @since Sep 30, 2015, Christopher K. Allen
     */
    public Complex dkFldSpectrum(double k) {
        double dblReal = +this.dkTz(k);
        double dblImag = -this.dkSz(k);

        return new Complex(dblReal, dblImag);
    }

    /**
     * <p>
     * Computes and returns the complex spectral of the conjugate spatial field
     * at the given wave number <em>k</em>. The value returned has the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sub><em>q</em></sub>(<em>k</em>) =
     * <em>T<sub>q</sub></em>(</em>k</em>) - <em>i</em>
     * <em>S<sub>q</sub></em>(<em>k</em>)
     * <br/>
     * <br/>
     * where &Escr;<em><sub>q</sub></em> is the returned spectral value,
     * <em>T<sub>q</sub></em> is the cosine transit-time factor of the
     * quadrature field, and <em>S<sub>q</sub></em> is the sine transit-time
     * factor of the quadrature field.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total
     * potential drop <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(<em>z</em>)<em>dz</em> so that the resulting
     * total potential of the field is 1.
     * </p>
     *
     * @param k particle wave number (in radians/meter)
     *
     * @return the Fourier spectrum &Escr;<sub><em>q</em></sub>(<em>k</em>) of
     * the quadrature field at the given wave number <em>k</em> (unitless)
     *
     * @since Sep 30, 2015, Christopher K. Allen
     */
    public Complex cnjSpectrum(double k) {
        double dblReal = +this.tq(k);
        double dblImag = -this.sq(k);

        return new Complex(dblReal, dblImag);
    }

    /**
     * <p>
     * Computes and returns the derivative, with respect to the wave number
     * <em>k</em>, of the quadrature field spectrum at the given wave number
     * <em>k</em>. The value returned has the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sub><em>q</em></sub>'(<em>k</em>) =
     * <em>T<sub>q</sub></em>'(</em>k</em>) - <em>i</em>
     * <em>S<sub>q</sub></em>'(<em>k</em>)
     * <br/>
     * <br/>
     * where &Escr;<em><sub>q</sub></em>' is the returned spectral derivative,
     * <em>T<sub>q</sub></em>' is the cosine transit-time factor derivative of
     * the quadrature field, and <em>S<sub>q</sub></em> is the sine transit-time
     * factor derivative of the quadrature field.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total
     * potential drop <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(<em>z</em>)<em>dz</em> so that the resulting
     * total potential of the field is 1.
     * </p>
     *
     * @param k particle wave number (in radians/meter)
     *
     * @return Fourier spectrum derivative
     * &Escr;<sub><em>q</em></sub>'(<em>k</em>) w.r.t. <em>k</em> of the
     * quadrature field at the given wave number <em>k</em>
     * (meters/radian)
     *
     * @since Sep 30, 2015, Christopher K. Allen
     */
    public Complex dkCnjSpectrum(double k) {
        double dblReal = +this.dkTq(k);
        double dblImag = -this.dkSq(k);

        return new Complex(dblReal, dblImag);
    }


    /*
     * Spectral Operations
     */
    /**
     * <p>
     * Compute and return the spectral pre-envelope
     * &Escr;<sup>-</sup>(<em>k</em>). The spectral pre- and post-envelopes turn
     * out to be equal to odd and even combinations, respectively, of the field
     * spectra. Specifically, the pre- and post-envelopes
     * &Escr;<sup>-</sup>(<em>k</em>) and &Escr;<sup>+</sup>(<em>k</em>) are
     * given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sup>-</sup>(<em>k</em>) = (1/2)[
     * &Escr;<sub>z</sub>(<em>k</em>) - &Escr;<sub>q</sub>(<em>k</em>) ] ,
     * <br/>
     * &nbsp; &nbsp; &Escr;<sup>+</sup>(<em>k</em>) = (1/2)[
     * &Escr;<sub>z</sub>(<em>k</em>) + &Escr;<sub>q</sub>(<em>k</em>) ] ,
     * <br/>
     * <br/>
     * where &Escr;<sub>z</sub>(<em>k</em>) is the Fourier spectrum of the axial
     * field and &Escr;<sub>q</sub>(<em>k</em>) is the spectrum of the
     * quadrature field (the conjugate Fourier transform).
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total
     * potential drop <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(<em>z</em>)<em>dz</em> so that the resulting
     * total potential of the field is 1.
     * </p>
     *
     * @param k the particle wave number (radians/meter)
     *
     * @return the pre-envelope &Escr;<sup>-</sup>(<em>k</em>) at the given wave
     * number (unitless)
     *
     * @since Sep 30, 2015, Christopher K. Allen
     */
    public Complex preEnvSpectrum(double k) {
        Complex cpxFldSpc = this.fldSpectrum(k);
        Complex cpxCnjSpc = this.cnjSpectrum(k);
        Complex cpxPreEnv = cpxFldSpc.minus(cpxCnjSpc);

        return cpxPreEnv.divide(2.0);
    }

    /**
     * <p>
     * Compute and return the derivative of the spectral pre-envelope
     * &Escr;<sup>-</sup>(<em>k</em>), that is
     * <em>d</em>&Escr;<sup>-</sup>(<em>k</em>)/<em>dk</em>. For a description
     * of the pre-envelope see <code>{@link #preEnvSpectrum(double)}</code> or
     * the class documentation.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total
     * potential drop <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(<em>z</em>)<em>dz</em> so that the resulting
     * total potential of the field is 1.
     * </p>
     *
     * @param k the particle wave number (radians/meter)
     *
     * @return derivative of the pre-envelope spectrum
     * <em>d</em>&Escr;<sup>-</sup>(<em>k</em>)/<em>dk</em>
     * at the given wave number (in meters/radian)
     *
     * @since Oct 1, 2015, Christopher K. Allen
     *
     * @see #preEnvSpectrum(double)
     */
    public Complex dkPreEnvSpectrum(double k) {
        Complex cpxDFldSpc = this.dkFldSpectrum(k);
        Complex cpxDCnjSpc = this.dkCnjSpectrum(k);
        Complex cpxDPreEnv = cpxDFldSpc.minus(cpxDCnjSpc);

        return cpxDPreEnv.divide(2.0);
    }

    /**
     * <p>
     * Compute and return the spectral post-envelope
     * &Escr;<sup>+</sup>(<em>k</em>). The spectral pre- and post-envelopes turn
     * out to be equal to odd and even combinations, respectively, of the field
     * spectra. Specifically, the pre- and post-envelopes
     * &Escr;<sup>-</sup>(<em>k</em>) and &Escr;<sup>+</sup>(<em>k</em>) are
     * given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sup>-</sup>(<em>k</em>) = (1/2)[
     * &Escr;<sub>z</sub>(<em>k</em>) - &Escr;<sub>q</sub>(<em>k</em>) ] ,
     * <br/>
     * &nbsp; &nbsp; &Escr;<sup>+</sup>(<em>k</em>) = (1/2)[
     * &Escr;<sub>z</sub>(<em>k</em>) + &Escr;<sub>q</sub>(<em>k</em>) ] ,
     * <br/>
     * <br/>
     * where &Escr;<sub>z</sub>(<em>k</em>) is the Fourier spectrum of the axial
     * field and &Escr;<sub>q</sub>(<em>k</em>) is the spectrum of the
     * quadrature field (the conjugate Fourier transform).
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total
     * potential drop <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(<em>z</em>)<em>dz</em> so that the resulting
     * total potential of the field is 1.
     * </p>
     *
     * @param k the particle wave number (radians/meter)
     *
     * @return the post-envelope &Escr;<sup>+</sup>(<em>k</em>) at the given
     * wave number (unitless)
     *
     * @since Sep 30, 2015, Christopher K. Allen
     */
    public Complex postEnvSpectrum(double k) {
        Complex cpxFldSpc = this.fldSpectrum(k);
        Complex cpxCnjSpc = this.cnjSpectrum(k);
        Complex cpxPstEnv = cpxFldSpc.plus(cpxCnjSpc);

        return cpxPstEnv.divide(2.0);
    }

    /**
     * <p>
     * Compute and return the derivative of the spectral post-envelope
     * &Escr;<sup>+</sup>(<em>k</em>), that is
     * <em>d</em>&Escr;<sup>+</sup>(<em>k</em>)/<em>dk</em>. For a description
     * of the post-envelope see <code>{@link #postEnvSpectrum(double)}</code> or
     * the class documentation.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total
     * potential drop <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(<em>z</em>)<em>dz</em> so that the resulting
     * total potential of the field is 1.
     * </p>
     *
     * @param k the particle wave number (radians/meter)
     *
     * @return derivative of the post-envelope spectrum
     * <em>d</em>&Escr;<sup>-</sup>(<em>k</em>)/<em>dk</em>
     * at the given wave number (in meters/radian)
     *
     * @since Oct 1, 2015, Christopher K. Allen
     *
     * @see #postEnvSpectrum(double)
     */
    public Complex dkPostEnvSpectrum(double k) {
        Complex cpxDFldSpc = this.dkFldSpectrum(k);
        Complex cpxDCnjSpc = this.dkCnjSpectrum(k);
        Complex cpxDPstEnv = cpxDFldSpc.plus(cpxDCnjSpc);

        return cpxDPstEnv.divide(2.0);
    }

    /*
     * Spectral Components
     */
    /**
     * Returns the cosine transit time factor <em>T</em><sub><em>z</em></sub>,
     * proportional to the Fourier cosine transform, for the given wave number.
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the value of <em>T</em>(<em>k</em>) (unitless)
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public double tz(double k) {
        if (this.bolPrtlFldMdl) {
            return this.tzfromTz0(k);
        } else {
            return this.fncTz.evaluateAt(k);
        }
    }

    /**
     * Returns the derivative of the cosine transit time factor
     * <em>T</em><sub><em>z</em></sub> w.r.t. the wave number <em>k</em>.
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the value of
     * <em>dT</em><sub><em>z</em></sub>(<em>k</em>)/<em>dk</em>
     * (meters/rad)
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public double dkTz(double k) {
        if (this.bolPrtlFldMdl) {
            return this.dkTzfromDkTz0(k);
        } else {
            return this.fncDTz.evaluateAt(k);
        }
    }

    /**
     * Returns the sine transit-time factor <em>S</em><sub><em>z</em></sub>,
     * proportional to the Fourier sine transform of the axial field, for the
     * given wave number.
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the value of <em>S</em><sub><em>z</em></sub>(<em>k</em>)
     * (unitless)
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public double sz(double k) {
        if (this.bolPrtlFldMdl) {
            return this.szfromTz0(k);
        } else {
            return this.fncSz.evaluateAt(k);
        }
    }

    /**
     * Returns the derivative of the sine transit-time factor
     * <em>dS</em><sub><em>z</em></sub>/<em>dk</em>
     * with respect to the particle wave number <em>k</em>
     * .
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the value of
     * <em>dS</em><sub><em>z</em></sub>(<em>k</em>)/<em>dk</em>
     * (meters/radian)
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public double dkSz(double k) {
        if (this.bolPrtlFldMdl) {
            return this.dkSzfromDkTz0(k);
        } else {
            return this.fncDSz.evaluateAt(k);
        }
    }

    /**
     * Returns the conjugate cosine transit-time factor <em>T<sub>q</sub></em>,
     * proportional to the Fourier cosine transform of the field
     * sgn(<em>z</em>)<em>E<sub>z</sub>(<em>z</em>).
     *
     * @param k the particle wave number w.r.t. the RF frequency (radians/meter)
     *
     * @return the conjugate cosine transit-time factor
     * <em>T<sub>q</sub></em>(<em>k</em>) (unitless)
     *
     * @since Sep 29, 2015 by Christopher K. Allen
     */
    public double tq(double k) {
        if (this.bolPrtlFldMdl) {
            return this.tqFromSq0(k);
        } else {
            return this.fncTq.evaluateAt(k);
        }
    }

    /**
     * Returns the derivative <em>dT</em>(<em>k</em>)/<em>dk</em> of the
     * conjugate cosine transit-time factor w.r.t. the particle wave number
     * <em>k</em>.
     *
     * @param k the particle wave number w.r.t. the RF frequency (radians/meter)
     *
     * @return the value of <em>dT<sub>q</sub></em>(<em>k</em>)/<em>dk</em>
     * (meters/rad)
     *
     * @since Sep 29, 2015, Christopher K. Allen
     */
    public double dkTq(double k) {
        if (this.bolPrtlFldMdl) {
            return this.dkTqFromDkSq0(k);
        } else {
            return this.fncDTq.evaluateAt(k);
        }
    }

    /**
     * Returns the conjugate sine transit-time factor <em>S<sub>q</sub></em>,
     * proportional to the Fourier sine transform of the field
     * sgn(<em>z</em>)<em>E<sub>z</sub></em>(<em>z</em>).
     *
     * @param k the particle wave number w.r.t. the RF frequency (radians/meter)
     *
     * @return the conjugate cosine transit-time factor
     * <em>S<sub>q</sub></em>(<em>k</em>) (unitless)
     *
     * @since Sep 29, 2015 by Christopher K. Allen
     */
    public double sq(double k) {
        if (this.bolPrtlFldMdl) {
            return this.sqFromSq0(k);
        } else {
            return this.fncSq.evaluateAt(k);
        }
    }

    /**
     * Returns the derivative <em>dS</em>(<em>k</em>)/<em>dk</em> of the
     * conjugate sine transit-time factor w.r.t. the particle wave number
     * <em>k</em>.
     *
     * @param k the particle wave number w.r.t. the RF frequency (radians/meter)
     *
     * @return the value of <em>dS<sub>q</sub></em>(<em>k</em>)/<em>dk</em>
     * (meters/rad)
     *
     * @since Sep 29, 2015, Christopher K. Allen
     */
    public double dkSq(double k) {
        if (this.bolPrtlFldMdl) {
            return this.dkSqFromDkSq0(k);
        } else {
            return this.fncDSq.evaluateAt(k);
        }
    }


    /*
     * Support - Partial Field Operations
     */
    /**
     * <p>
     * Compute and return the standard transit time factor
     * <em>T</em>(<em>k</em>) which includes any gap "offsets", from the given
     * particle velocity. The value is computed from the symmetric transit time
     * factor
     * <em>T</em><sub>0</sub> which is evaluated with the coordinate origin at
     * the point of field symmetry.
     * </p>
     * <p>
     * The returned value <em>T</em><sub><em>z</em></sub>(<em>k</em>) has the
     * expression
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T</em></em><sub><em>z</em></sub>(<em>k</em>) =
     * <em>T</em></em><sub><em>z</em>,</sub><sub>0</sub>(&beta;) cos
     * <em>k</em>&Delta;<em>z</em> ,
     * <br/>
     * <br/>
     * where <em>T</em></em><sub><em>z</em></sub><sub>0</sub> is the cosine
     * transit time factor taken with origin at point of field symmetry, &beta;
     * is the normalized particle velocity,
     * <em>k</em> &trie; 2&pi;/&beta;&lambda; is the wave number, and
     * &Delta;<em>z</em> is the offset of the point of field symmetry from the
     * origin.
     * </p>
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the transit time factor (Fourier cosine transform) evaluated at
     * <em>k</em> (unitless)
     *
     * @since Feb 13, 2015 by Christopher K. Allen
     */
    private double tzfromTz0(double k) {
        double dz = -this.getFieldOffset();
        double cos = Math.cos(k * dz);

        double beta = this.computeVelocity(k);
        double t0 = this.fncTz0.evaluateAt(beta);
        return t0 * cos;
    }

    /**
     * <p>
     * Compute and return the derivative of the standard transit time factor
     * <em>T'</em></em><sub><em>z</em></sub>(<em>k</em>) with respect to
     * <em>k</em>
     * including any gap "offsets." The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T</em></em><sub><em>z</em></sub>'(<em>k</em>) =
     * (-&beta;/<em>k</em>)<em>T'</em><sub><em>z</em></sub><sub>0</sub>(&beta;)
     * cos
     * <em>k</em>&Delta;<em>z</em>
     * - &Delta;<em>z</em> <em>T</em><sub>0</sub>(&beta;) sin
     * <em>k</em>&Delta;<em>z</em>,
     * <br/>
     * <br/>
     * whenever <em>T'</em><sub><em>z</em></sub><sub>0</sub>(&beta;) is taken
     * w.r.t. velocity &beta;, or
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T</em></em><sub><em>z</em></sub>'(<em>k</em>) =
     * <em>T'</em><sub><em>z</em></sub><sub>0</sub>(&beta;) cos
     * <em>k</em>&Delta;<em>z</em>
     * - &Delta;<em>z</em> <em>T</em><sub>0</sub>(&beta;) sin
     * <em>k</em>&Delta;<em>z</em>,
     * <br/>
     * <br/>
     * whenever <em>T'</em><sub><em>z</em></sub><sub>0</sub>(&beta;) is taken
     * w.r.t. wave number <em>k</em>. where
     * <em>T</em>'</em><sub><em>z</em></sub><sub>0</sub>
     * is the derivative of the symmetric cosine transit time factor,
     * <em>T</em><sub>0</sub>(&beta;) is the symmetric cosine transit time
     * factor, &beta; is the normalized particle velocity,
     * <em>k</em> &trie; 2&pi;/&beta;&lambda; is the wave number, and
     * &Delta;<em>z</em> is the offset of the point of field symmetry from the
     * origin.
     * </p>
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the derivative of cosine transit time factor w.r.t. wave number
     * <em>k</em> (meters/radian)
     *
     * @since Feb 16, 2015 by Christopher K. Allen
     * @version July 29, 2015: Modified to assume <code>fitTTFPrime</code> =
     * <em>dT</em><sub>0</sub>(&beta;)/<em>dk</em>
     * @version Nov 9, 2015: Modified to assume <code>fitTTFPrime</code> =
     * <strong>-</strong><em>dT</em><sub>0</sub>(&beta;)/<em>dk</em>
     */
    private double dkTzfromDkTz0(double k) {
        double dz = -this.getFieldOffset();
        double cos = Math.cos(k * dz);
        double sin = Math.sin(k * dz);

        double beta = this.computeVelocity(k);

        double dTz0 = this.fncDTz0.evaluateAt(beta);
        double tz0 = this.fncTz0.evaluateAt(beta);

        // To compare with XAL implementation
        return 0.01 * dTz0 * cos - tz0 * dz * sin;
    }

    /**
     * <p>
     * Compute and return the standard transit time factor
     * <em>S</em></em><sub><em>z</em></sub>(<em>k</em>) which includes any gap
     * "offsets", from the given particle velocity. The value is computed from
     * the symmetric transit time factor
     * <em>S</em></em><sub><em>z</em></sub><sub>0</sub> which is evaluated with
     * the coordinate origin at the point of field symmetry.
     * </p>
     * <p>
     * The returned value <em>S</em></em><sub><em>z</em></sub>(<em>k</em>) has
     * the expression
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S</em></em><sub><em>z</em></sub>(<em>k</em>) =
     * <em>T</em></em><sub><em>z</em></sub><sub>0</sub>(&beta;) sin
     * <em>k</em>&Delta;<em>z</em> ,
     * <br/>
     * <br/>
     * where &beta; is the normalized particle velocity,
     * <em>T</em></em><sub><em>z</em></sub><sub>0</sub> is the cosine transit
     * time factor taken with origin at point of field symmetry, <em>k</em>
     * &trie; 2&pi;/&beta;&lambda; is the wave number, and &Delta;<em>z</em> is
     * the offset of the point of field symmetry from the origin. Note that the
     * transit time factor <em>S</em><sub>0</sub>
     * is zero since it is taken about the point of field symmetric.
     * </p>
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the transit time factor (Fourier sine transform) evaluated at
     * <em>k</em>
     *
     * @since Feb 16, 2015 by Christopher K. Allen
     */
    private double szfromTz0(double k) {
        double dz = -this.getFieldOffset();
        double sin = Math.sin(k * dz);

        double beta = this.computeVelocity(k);
        double tz0 = this.fncTz0.evaluateAt(beta);
        return tz0 * sin;
    }

    /**
     * <p>
     * Compute and return the derivative of the standard transit time factor
     * <em>S'</em></em><sub><em>z</em></sub>(<em>k</em>) with respect to
     * <em>k</em>
     * including any gap "offsets." The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S</em>'</em><sub><em>z</em></sub>(<em>k</em>) =
     * <em>T</em>'</em><sub><em>z</em></sub><sub>0</sub>(&beta;(<em>k</em>)) sin
     * <em>k</em>&Delta;<em>z</em>
     * + &Delta;<em>z</em>
     * <em>T</em></em><sub><em>z</em></sub><sub>0</sub>(&beta;(<em>k</em>)) cos
     * <em>k</em>&Delta;<em>z</em>,
     * <br/>
     * <br/>
     * where <em>T</em>'</em><sub><em>z</em></sub><sub>0</sub> is the derivative
     * of the symmetric cosine transit time factor,
     * <em>T</em></em><sub><em>z</em>0</sub>(&beta;) is the cosine transit time
     * factor,
     * <em>k</em> &trie; 2&pi;/&beta;&lambda; is the wave number, and
     * &Delta;<em>z</em> is the offset of the point of field symmetry from the
     * origin.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot; I am unsure whether or not the approximation for
     * <em>dS</em>(&beta;)/<em>dk</em> or
     * <em>dS</em>(&beta;)/<em>d</em>&beta; is stored. If it is the later then
     * the returned value should be
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S</em>'</em><sub><em>z</em></sub>(<em>k</em>) =
     * (-&beta;/<em>k</em>)<em>T</em>'</em><sub><em>z</em></sub><sub>0</sub>(&beta;)
     * sin <em>k</em>&Delta;<em>z</em>
     * + &Delta;<em>z</em>
     * <em>T</em></em><sub><em>z</em></sub><sub>0</sub>(&beta;) cos
     * <em>k</em>&Delta;<em>z</em>,
     * <br/>
     * <br/>
     * </p>
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the derivative of sine transit time factor <em>S</em>(<em>k</em>)
     * w.r.t. wave number <em>k</em> (meters/radian)
     *
     * @since Feb 16, 2015 by Christopher K. Allen
     * @version July 29, 2015 modified to assume <code>fitSTFPrime</code> =
     * <em>dS</em><sub>0</sub>(&beta;)/<em>dk</em>
     */
    private double dkSzfromDkTz0(double k) {
        double dz = -this.getFieldOffset();
        double sin = Math.sin(k * dz);
        double cos = Math.cos(k * dz);

        double beta = this.computeVelocity(k);
        double dTz0 = this.fncDTz0.evaluateAt(beta);
        double tz0 = this.fncTz0.evaluateAt(beta);

        // compare with XAL implementation
        return 0.01 * dTz0 * sin + tz0 * dz * cos;
    }

    /**
     * <p>
     * Compute and return the conjugate transit time factor
     * <em>T<sub>q</sub></em>(<em>k</em>) which includes any gap "offsets", from
     * the given particle velocity. The value is computed from the conjugate
     * symmetric transit time factor <em>S</em><sub><em>q</em>,0</sub> which is
     * evaluated with the coordinate origin at the point of field symmetry.
     * </p>
     * <p>
     * The returned value <em>T<sub>q</sub></em>(<em>k</em>) has the expression
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T<sub>q</sub></em>(<em>k</em>) =
     * -<em>S</em><sub><em>q</em>,0</sub>(&beta;)
     * sin(<em>k</em>&Delta;<em>z</em>) ,
     * <br/>
     * <br/>
     * where <em>S</em><sub><em>q</em>,0</sub> is the conjugate sine transit
     * time factor taken with origin at point of field symmetry, <em>k</em>
     * &trie; 2&pi;/&beta;&lambda; is the wave number, and &Delta;<em>z</em> is
     * the offset of the point of field symmetry from the origin.
     * </p>
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the transit time factor (conjugate Fourier cosine transform)
     * evaluated at <em>k</em> (meters/radian)
     *
     * @since Sept 23, 2015 by Christopher K. Allen
     */
    private double tqFromSq0(double k) {
        double dz = -this.getFieldOffset();
        double sin = Math.sin(k * dz);

        double beta = this.computeVelocity(k);
        double sq0 = this.fncSq0.evaluateAt(beta);
        return -sq0 * sin;
    }

    /**
     * <p>
     * Compute and return the derivative of the conjugate transit time factor
     * <em>T<sub>q</sub>'</em>(<em>k</em>) with respect to <em>k</em> or &beta;
     * including any gap "offsets." We are unsure because the provided data for
     * <em>S</em><sub><em>q</em>,0</sub>(&beta;) never specified the derivative
     * parameter. So were are guessing for now.
     * </p>
     * <p>
     * The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T<sub>q</sub></em>'(<em>k</em>) =
     * -<em>S'</em><sub><em>q</em>,0</sub>(&beta;) sin
     * <em>k</em>&Delta;<em>z</em>
     * - &Delta;<em>z</em> <em>S</em><sub><em>q</em>,0</sub>(&beta;) cos
     * <em>k</em>&Delta;<em>z</em>,
     * <br/>
     * <br/>
     * where <em>S'</em><sub><em>q</em>,0</sub> is the derivative of the
     * conjugate sine transit time factor for the symmetric field,
     * <em>S</em><sub><em>q</em>,0</sub>(&beta;) is the conjugate sine transit
     * time factor,
     * <em>k</em> &trie; 2&pi;/&beta;&lambda; is the wave number, and
     * &Delta;<em>z</em> is the offset of the point of field symmetry from the
     * origin.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot; I am unsure of whether the polynomial stored is for
     * <em>dS<sub>q</sub></em>(&beta;)/<em>d</em>&beta; or for
     * d<em>S<sub>q</sub></em>(&beta;)/<em>dk</em>. If it is the former we need
     * <em>d</em>&beta;/<em>dk</em> = -&beta;/<em>k</em> and the value of
     * <em>T'<sub>q</sub></em> becomes
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>T<sub>q</sub></em>'(&beta;) =
     * -(-&beta;/<em>k</em>)<em>S'</em><sub><em>q</em>,0</sub>(&beta;) sin
     * <em>k</em>&Delta;<em>z</em>
     * - &Delta;<em>z</em> <em>S</em><sub><em>q</em>,0</sub>(&beta;) cos
     * <em>k</em>&Delta;<em>z</em> ,
     * <br/>
     * <br/>
     * </p>
     *
     * @param k particle wave number with respect to the RF frequency
     * (radians/meter)
     *
     * @return the derivative of conjugate sine transit time factor w.r.t.
     * velocity &beta; or wave number <em>k</em> (unitless or meters/radian)
     *
     * @since Feb 16, 2015 by Christopher K. Allen
     * @version July 29, 2015: Modified to assume <code>fitTTFPrime</code> =
     * <em>dT</em><sub>0</sub>(&beta;)/<em>dk</em>
     * <br/>
     * Sept 23, 2015: Modified to assume <code>fitSTF</code> actually contains
     * <em>S<sub>q</em></sub>(&beta;)
     */
    private double dkTqFromDkSq0(double k) {
        double dz = -this.getFieldOffset();
        double cos = Math.cos(k * dz);
        double sin = Math.sin(k * dz);

        double beta = this.computeVelocity(k);
        double dSq0 = this.fncDSq0.evaluateAt(beta);
        double sq0 = this.fncSq0.evaluateAt(beta);

        // To emulate XAL implementation
        return -0.01 * dSq0 * sin - sq0 * dz * cos;
    }

    /**
     * <p>
     * Compute and return the conjugate transit time factor
     * <em>S<sub>q</sub></em>(<em>k</em>) which includes any gap "offsets", from
     * the given particle velocity. The value is computed from the transit time
     * factor <em>S</em><sub><em>q</em>,0</sub> for the symmetric field .
     * </p>
     * <p>
     * The returned value <em>S<sub>q</sub></em>(<em>k</em>) has the expression
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S<sub>q</sub></em>(<em>k</em>) =
     * <em>S</em><sub><em>q</em>,0</sub>(&beta;) cos <em>k</em>&Delta;<em>z</em>
     * ,
     * <br/>
     * <br/>
     * where <em>S</em><sub><em>q</em>,0</sub> is the conjugate sine transit
     * time factor taken with origin at point of field symmetry, <em>k</em>
     * &trie; 2&pi;/&beta;&lambda; is the wave number, and &Delta;<em>z</em> is
     * the offset of the point of field symmetry from the origin. Note that the
     * transit time factor <em>T</em><sub><em>q</em>,0</sub>
     * is zero since it is taken about the point of field symmetric.
     * </p>
     *
     * @param k particle wave number w.r.t. the RF frequency (radians/meter)
     *
     * @return conjugate transit time factor <em>S</em><sub><em>q</em></sub>
     * evaluated at <em>k</em> (meters/radian)
     *
     * @since Sep 23, 2015 by Christopher K. Allen
     */
    private double sqFromSq0(double k) {
        double dz = -this.getFieldOffset();
        double cos = Math.cos(k * dz);

        double beta = this.computeVelocity(k);
        double sq0 = this.fncSq0.evaluateAt(beta);
        return sq0 * cos;
    }

    /**
     * <p>
     * Compute and return the derivative of the conjugate transit time factor
     * <em>S<sub>q</sub></em>(<em>k</em>) with respect to <em>k</em> (or &beta;
     * ?) including any gap "offsets." That is, compute the value
     * <em>S'</em><sub><em>q</em></sub>(<em>k</em>). The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S'<sub>q</sub></em>(<em>k</em>) =
     * <em>S'</em><sub><em>q</em>,0</sub>(&beta;) cos
     * <em>k</em>&Delta;<em>z</em>
     * - &Delta;<em>z</em> <em>S</em><sub><em>q</em>,0</sub>(&beta;) sin
     * <em>k</em>&Delta;<em>z</em> ,
     * <br/>
     * <br/>
     * where <em>S'</em><sub><em>q</em>,0</sub> is the derivative of the
     * conjugate sine transit time factor,
     * <em>k</em> &trie; 2&pi;/&beta;&lambda; is the wave number, and
     * &Delta;<em>z</em> is the offset of the point of field symmetry from the
     * origin.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot; I am unsure of whether the polynomial stored is for
     * <em>dS<sub>q</sub></em>(&beta;)/<em>d</em>&beta; or for
     * d<em>S<sub>q</sub></em>(&beta;)/<em>dk</em>. If it is the former we need
     * <em>d</em>&beta;/<em>dk</em> = -&beta;/<em>k</em> and the value of
     * <em>S'<sub>q</sub></em> becomes
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>S'<sub>q</sub></em>(<em>k</em>) =
     * (-&beta;/<em>k</em>)<em>S</em>'<sub><em>q</em>,0</sub>(&beta;) cos
     * <em>k</em>&Delta;<em>z</em>
     * - &Delta;<em>z</em> <em>S</em><sub><em>q</em>,0</sub>(&beta;) sin
     * <em>k</em>&Delta;<em>z</em> ,
     * <br/>
     * <br/>
     * </p>
     *
     * @param k particle wave number w.r.t. the RF frequency (radians/meter)
     *
     * @return the derivative of sine transit time factor w.r.t. velocity &beta;
     *
     * @since Feb 16, 2015 by Christopher K. Allen
     * @version July 29, 2015 modified to assume <code>fitSTFPrime</code> =
     * <em>dS</em><sub>0</sub>(&beta;)/<em>dk</em>
     */
    private double dkSqFromDkSq0(double k) {
        double dz = -this.getFieldOffset();
        double sin = Math.sin(k * dz);
        double cos = Math.cos(k * dz);

        double beta = this.computeVelocity(k);
        double dSq0 = this.fncDSq0.evaluateAt(beta);
        double sq0 = this.fncSq0.evaluateAt(beta);

        // To compare with XAL implementation
        return 0.01 * dSq0 * cos - sq0 * dz * sin;
    }

    /**
     * Compute the normalized particle velocity &beta; for the given particle
     * wave number <em>k</em>.
     *
     * @param k wave number of the particle with respect to RF frequency
     * (radians/meter)
     *
     * @return the normalized velocity &beta; of the particle for the given wave
     * number <em>k</em>
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    private double computeVelocity(double k) {
        double lambda = DBL_LGHT_SPD / this.getFrequency();
        return DBL_2PI / (k * lambda);
    }
}
