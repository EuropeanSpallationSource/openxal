/*
 * BunchProbeState.java
 *
 * Created on April, 2003, 5:15 PM
 * 
 * Modifications:
 *  11/2006     - CKA removed references to Twiss parameters 
 *                and correlation matrix
 *              - CKA changed primary state variables to 
 *                beam current and bunch frequency
 */
package xal.model.probe.traj;

import xal.tools.data.DataAdaptor;

import xal.model.probe.BunchProbe;
import xal.tools.data.DataFormatException;

/**
 * Encapsulates a BunchProbe's state at a point in time. Contains addition state
 * variables for probes with beam-like behavior.
 *
 * @author Craig McChesney
 * @author Christopher K. Allen
 * @version $id:
 *
 */
public abstract class BunchProbeState<S extends BunchProbeState<S>> extends ProbeState<S> {


    /*
     * Global Constants
     */
    // ************ I/O Support
    /**
     * element tag for beam state data
     */
    private static final String ELEM_BEAM = "beam";

    /**
     * attribute tag for total beam current
     */
    private static final String ATTR_BEAMCURRENT = "I";

    /**
     * attribute tag for total beam charge
     */
    private static final String ATTR_BUNCHFREQ = "f";

    /*
     * Local Attributes
     */
    /**
     * bunch frequency in Hz
     */
    private double dlbBunFreq = 0.0;

    /**
     * Beam current
     */
    private double dblBmCurr = 0.0;


    /*
     * Initialization
     */
    /**
     * Default constructor. Creates an empty <code>BunchProbeState</code>.
     *
     */
    protected BunchProbeState() {
        super();

        dblBmCurr = 0.0;
        dlbBunFreq = 0.0;
    }

    /**
     * Copy constructor for BunchProbeState. Initializes the new
     * <code>BunchProbeState</code> objects with the state attributes of the
     * given <code>BunchProbeState</code>.
     *
     * @param state initializing state
     *
     * @author Christopher K. Allen
     * @author Jonathan M. Freed
     * @since Jun 26, 2014
     */
    protected BunchProbeState(final S state) {
        super(state);

        dblBmCurr = state.getBeamCurrent();
        dlbBunFreq = state.getBunchFrequency();
    }

    /**
     * Initializing constructor. Creates a new <code>BunchProbe</code> object
     * initialized to the argument's state.
     *
     * @param probe probe object with which to initialize this state
     */
    protected BunchProbeState(final BunchProbe<S> probe) {
        super(probe);

        setBunchFrequency(probe.getBunchFrequency());
        setBeamCurrent(probe.getBeamCurrent());
    }

    /*
     * Property Accessors
     */
    /**
     * Set the bunch arrival time frequency.
     *
     * @param f new bunch frequency in <strong>Hz</strong>
     */
    public void setBunchFrequency(double f) {
        this.dlbBunFreq = f;
    }

    /**
     * Set the total beam current
     *
     * @param i new beam current in <strong>Amperes</strong>
     */
    public void setBeamCurrent(double i) {
        dblBmCurr = i;
    }

    /**
     * <p>
     * Returns the bunch frequency, that is, the rate at which beam bunches pass
     * a stationary point (in laboratory coordinates). The frequency <em>f</em>
     * of the bunches determines the beam current <em>I</em>.
     * </p>
     * <p>
     * The bunch frequency <em>f</em> is related to the beam current
     * <em>I</em> and bunch charge <em>Q</em> as
     * <br>
     * <br>
     * &nbsp; &nbsp; <em>f</em> = <em>I/Q</em>
     * <br>
     * <br>
     * </p>
     *
     * @return bunch frequency in Hertz
     */
    public double getBunchFrequency() {
        return this.dlbBunFreq;
    }

    /**
     * Returns the total beam current, which is the bunch charge <em>Q</em>
     * times the bunch frequency <em>f</em>.
     *
     * @return beam current in <strong>amps</strong>
     */
    public double getBeamCurrent() {
        return dblBmCurr;
    }

    /*
     * Computed Properties
     */
    /**
     * Computes and returns the charge in each beam bunch
     *
     * @return beam charge in <strong>coulombs</strong>
     */
    public double bunchCharge() {
        if (this.getBunchFrequency() > 0.0) {
            return this.getBeamCurrent() / this.getBunchFrequency();

        } else {
            return 0.0;

        }
    }

    /**
     * <p>
     * Returns the generalized, three-dimensional beam perveance <em>K</em>.
     * This value is defined to be
     * </p>
     *
     * <em>K</em> =
     * (<em>Q</em>/4*&pi;*<em>&epsilon;</em><sub>0</sub>)(1/&gamma;<sup>3</sup>&beta;<sup>2</sup>)(|<em>q</em>|/<em>E<sub>R</sub></em>)
     *
     * <p>
     * where <em>Q</em> is the bunch charge, <em>&epsilon;</em><sub>0</sub> is
     * the permittivity of free space, <em>&gamma;</em> is the relativistic
     * factor,
     * <em>&beta;</em> is the normalized design velocity, <em>q</em> is the
     * individual particle charge and <em>E<sub>R</sub></em> is the rest energy
     * of the beam particles.
     * </p>
     *
     * <h3>NOTES:</h3>
     * <p>
     * - The value (1/4&pi;&epsilon;<sub>0</sub>) is equal to
     * 10<sup>-7</sup><em>c</em><sup>2</sup>
     * where <em>c</em> is the speed of light.
     * </p>
     *
     * @return generalized beam perveance <strong>Units:
     * radians^2/meter</strong>
     *
     * @author Christopher K. Allen
     */
    public double beamPerveance() {

        // Get some shorthand
        double c = LIGHT_SPEED;
        double gamma = this.getGamma();
        double bg2 = gamma * gamma - 1.0;

        // Compute independent terms
        double dblPermT = 1.0e-7 * c * c * this.bunchCharge();
        double dblRelaT = 1.0 / (gamma * bg2);
        double dblEnerT = Math.abs(super.getSpeciesCharge()) / super.getSpeciesRestEnergy();

        return dblPermT * dblRelaT * dblEnerT;
    }

    /**
     * <p>
     * Returns the generalized, two-dimensional beam perveance <em>K</em>. This
     * value is defined to be
     * </p>
     *
     * <em>K</em> =
     * (<em>I</em>/&pi;*<em>&epsilon;</em><sub>0</sub>)(1/&gamma;<sup>3</sup>&beta;<sup>3</sup><em>c</em>)(|<em>q</em>|/<em>E<sub>R</sub></em>)
     *
     * <p>
     * where <em>I</em> is the current, <em>&epsilon;</em><sub>0</sub> is the
     * permittivity of free space, <em>&gamma;</em> is the relativistic factor,
     * <em>&beta;</em> is the normalized design velocity, <em>q</em> is the
     * individual particle charge and <em>E<sub>R</sub></em> is the rest energy
     * of the beam particles.
     * </p>
     *
     * <h3>NOTES:</h3>
     * <p>
     * - The value (1/&pi;&epsilon;<sub>0</sub>) is equal to
     * 40<sup>-7</sup><em>c</em><sup>2</sup>
     * where <em>c</em> is the speed of light.
     * </p>
     *
     * @return generalized beam perveance <strong>Units:
     * radians^2/meter</strong>
     *
     * @author Christopher K. Allen
     */
    public double beamDCPerveance() {

        // Get some shorthand
        double c = LIGHT_SPEED;
        double gamma = this.getGamma();
        double betagamma = Math.sqrt(gamma * gamma - 1);

        // Compute independent terms
        double dblPermT = 1.0e-7 * c * c * 4;
        double dblRelaT = this.getBeamCurrent() / (Math.pow(betagamma, 3) * c);
        double dblEnerT = Math.abs(super.getSpeciesCharge()) / super.getSpeciesRestEnergy();

        return dblPermT * dblRelaT * dblEnerT;
    }

    /*
     * Debugging
     */
    /**
     * Write out state information to a string.
     *
     * @return text version of internal state data
     */
    @Override
    public String toString() {
        return super.toString()
                + " curr: " + getBeamCurrent()
                + " freq: " + getBunchFrequency();
    }

    /*
     * Support Methods
     */
    /**
     * Save the state values particular to <code>BunchProbeState</code> objects
     * to the data sink.
     *
     * @param daSink data sink represented by <code>DataAdaptor</code> interface
     */
    @Override
    protected void addPropertiesTo(DataAdaptor daSink) {
        super.addPropertiesTo(daSink);
        DataAdaptor datBunch = daSink.createChild(ELEM_BEAM);
        datBunch.setValue(ATTR_BUNCHFREQ, getBunchFrequency());
        datBunch.setValue(ATTR_BEAMCURRENT, getBeamCurrent());
    }

    /**
     * Recover the state values particular to <code>BunchProbeState</code>
     * objects from the data source.
     *
     * @param daSource data source represented by a <code>DataAdaptor</code>
     * interface
     *
     * @exception DataFormatException state information in data source is
     * malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor daSource) throws DataFormatException {
        super.readPropertiesFrom(daSource);

        DataAdaptor daBunch = daSource.childAdaptor(ELEM_BEAM);
        if (daBunch == null) {
            throw new DataFormatException("BunchProbeState#readPropertiesFrom(): no child element = " + ELEM_BEAM);
        }

        if (daBunch.hasAttribute(ATTR_BUNCHFREQ)) {
            setBunchFrequency(daBunch.doubleValue(ATTR_BUNCHFREQ));
        }
        if (daBunch.hasAttribute(ATTR_BEAMCURRENT)) {
            setBeamCurrent(daBunch.doubleValue(ATTR_BEAMCURRENT));
        }
    }

}
