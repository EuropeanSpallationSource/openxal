/*
 * BunchProbe.java
 *
 * Created on November 12, 2002, 6:17 PM
 * Modifcations:
 *      11/2006 - CKA changed the primary state variables to bunch frequency Q
 *                and beam current I
 *      11/2013 - CKA removed the "betatron phase" attribute.
 */
package xal.model.probe;

import xal.tools.annotation.AProperty.Units;
import xal.model.probe.traj.BunchProbeState;

/**
 * <p>
 * Abstract base class for all probes having beam properties. That is derived
 * classes should represent probes with collective beam dynamics.
 * </p>
 * <h3>Note:</h3>
 * <p>
 * The bunch charge <em>Q</em> is computed from the beam current <em>I</em> and
 * bunch frequency <em>f</em> as
 * <br>
 * <br>
 * &nbsp; &nbsp;  <em>Q</em> = <em>I/f</em>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 2, 2002
 */
public abstract class BunchProbe<S extends BunchProbeState<S>> extends Probe<S> {

    /*
     *  Abstract Methods
     */
 /*
     *  Initialization
     */
    /**
     * Default constructor.
     *
     * Since BunchProbe is abstract constructor should only be calls by a
     * derived class. Creates a new (empty) instance of BunchProbe.
     */
    protected BunchProbe() {
        super();
    }

    /**
     * Copy constructor - clones the argument Since BunchProbe is abstract
     * constructor should only be calls by a derived class.
     *
     * @param probe BunchProbe object to be cloned
     */
    protected BunchProbe(final BunchProbe<S> probe) {
        super(probe);

        setBunchFrequency(probe.getBunchFrequency());
        setBeamCurrent(probe.getBeamCurrent());
    }

    /**
     * Set the bunch arrival time frequency.
     *
     * @param f new bunch frequency in <strong>Hz</strong>
     */
    public void setBunchFrequency(double f) {
        this.stateCurrent.setBunchFrequency(f);
    }

    /**
     * Set the total beam current.
     *
     * @param i new beam current in <strong>Amperes</strong>
     */
    public void setBeamCurrent(double i) {
        this.stateCurrent.setBeamCurrent(i);
    }

    /*
     *  Attribute Query
     */
    /**
     * Returns the bunch frequency, that is the frequency of the bunches need to
     * create the beam current.
     *
     * The bunch frequency f is computed from the beam current I and bunch
     * charge Q as
     *
     * f = I/Q
     *
     * @return bunch frequency in Hertz
     */
    @Units("Hz")
    public double getBunchFrequency() {
        return this.stateCurrent.getBunchFrequency();
    }

    /**
     * Returns the total beam current
     *
     * @return beam current in <strong>amps</strong>
     */
    @Units("amps")
    public double getBeamCurrent() {
        return this.stateCurrent.getBeamCurrent();
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
        return this.stateCurrent.bunchCharge();
    }

    /**
     * <p>
     * Returns the generalized, three-dimensional beam perveance <em>K</em>.
     * This value is defined to be
     * </p>
     *
     * K = (Q/4*pi*e0)*(1/gamma^3*beta^2)*(|q|/ER)
     *
     * <p>
     * where <em>Q</em> is the bunch charge, <em>e0</em> is the permittivity of
     * free space, <em>gamma</em> is the relativistic factor, <em>beta</em> is
     * the normalized design velocity, <em>q</em> is the charge of the beam
     * particles and <em>ER</em> is the rest energy of the beam particles.
     * </p>
     *
     * <p>
     * NOTES: - The value (1/4*pi*e0) is equal to 1e-7*c^2 where <em>c</em> is
     * the speed of light.
     *
     * @return generalized beam perveance <strong>Units:
     * radians^2/meter</strong>
     *
     * @author Christopher K. Allen
     */
    public double beamPerveance() {
        return this.stateCurrent.beamPerveance();
    }

    /**
     * <p>
     * Returns the generalized, two-dimensional beam perveance <em>K</em>. This
     * value is defined to be
     * </p>
     *
     * K = (I/pi*e0)*(1/gamma^3*beta^3*c)*(|q|/ER)
     *
     * <p>
     * where <em>I</em> is the current, <em>e0</em> is the permittivity of free
     * space, <em>gamma</em> is the relativitic factor, <em>beta</em> is the
     * normalized design velocity, <em>q</em> is the charge of the beam
     * particles and <em>ER</em> is the rest energy of the beam partiles.
     * </p>
     *
     * <p>
     *
     * @return generalized DC beam perveance <strong>Units:
     * radians^2/meter</strong>
     *
     * @author Christopher K. Allen
     */
    public double beamDCPerveance() {
        return this.stateCurrent.beamDCPerveance();
    }

    /*
     * Probe Overrides
     */
    /**
     * Just restating <code>Probe.{@link #createProbeState()}</code>
     *
     * @see xal.model.probe.Probe#createProbeState()
     *
     * @author Christopher K. Allen
     * @since Nov 5, 2013
     */
    @Override
    public abstract S createProbeState();

    /**
     * Just restating <code>Probe.{@link #createEmptyProbeState()}</code>.
     *
     * @author Jonathan M. Freed
     * @since Jul 1, 2014
     */
    @Override
    public abstract S createEmptyProbeState();
}
