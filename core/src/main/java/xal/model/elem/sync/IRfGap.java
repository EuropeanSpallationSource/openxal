/*
 * IRfGap.java
 *
 * Created on November 4, 2002, 3:44 PM
 */
package xal.model.elem.sync;

import xal.model.IProbe;

/**
 * This interface defines the common properties of all RF Gap structures.
 *
 * @author Christopher K. Allen
 * @since Nov 4, 2002
 */
public interface IRfGap /*extends gov.sns.xal.model.IElement*/ {

    /**
     * Set the ETL product of the RF gap where E is the longitudinal electric
     * field of the gap, T is the transit time factor of the gap, L is the
     * length of the gap.
     *
     * The maximum energy gain from the gap is given by qETL where q is the
     * charge (in Coulombs) of the species particle.
     *
     * @param dblETL ETL product of gap (in <bold>volts</bold>).
     */
    public void setETL(double dblETL);

    /**
     * Set the on accelerating field. This method should be called by the RF
     * cavity containing this gap and should use the amplitude factor.
     *
     * @param e - the on axis field (V/m)
     */
    public void setE0(double e);

    /**
     * Set the phase delay of the RF in gap with respect to the synchronous
     * particle. The actual energy gain from the gap is given by qETLcos(dblPhi)
     * where dbkPhi is the phase delay. This method should be called by the RF
     * cavity containing this gap and should use the phase factor.
     *
     * @param dblPhase phase delay of the RF w.r.t. synchronous particle (in
     * <bold>radians</bold>).
     */
    public void setPhase(double dblPhase);

    /**
     * Set the operating frequency of the RF gap.
     *
     * @param dblFreq frequency of RF gap (in <bold>Hertz</bold>)
     */
    public void setFrequency(double dblFreq);

    /**
     * Return the ETL product of the gap, where E is the longitudinal electric
     * field, T is the transit time factor, and L is the gap length.
     *
     * @return the ETL product of the gap (in <bold>volts</bold>).
     */
    public double getETL();

    /**
     * Return the RF phase delay of the gap with respect to the synchronous
     * particle.
     *
     * @return phase delay w.r.t. synchronous particle (in
     * <bold>radians</bold>).
     */
    public double getPhase();

    /**
     * Get the operating frequency of the RF gap.
     *
     * @return frequency of RF gap (in <bold>Hertz</bold>)
     */
    public double getFrequency();

    /**
     * Get the on accelerating field (V/m)
     */
    public double getE0();

    /**
     * Compute the synchronous phase and the energy gain for a cavity gap.
     *
     * @param probe
     */
    public void computeSynchronousPhaseAndEnergyGain(IProbe probe);

    /**
     * Return the synchronous phase of a cavity gap, which must be previously
     * calculated using computeSynchronousPhase.
     *
     * @return synchronous phase [rad]
     */
    public double getSynchronousPhase();

    /**
     * Return the energy gain of a cavity gap previously calculated.
     *
     * @return
     */
    public double getEnergyGain();
}
