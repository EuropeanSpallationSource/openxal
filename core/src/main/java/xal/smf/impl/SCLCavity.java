//
//  SCLCavity.java
//  xal
//
//  Created by Thomas Pelaia on 2/8/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.smf.impl;

import xal.smf.impl.qualify.ElementTypeManager;
import xal.ca.ChannelFactory;

/**
 * SCLCavity class is used to represent an SCL RF cavity.
 */
public class SCLCavity extends RfCavity {

    /**
     * identifies this class
     */
    public static final String TYPE = "SCLCavity";

    // register this class for qualification of nodes by type
    static {
        registerType();
    }

    /**
     * Register SCLCavity's type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(SCLCavity.class, TYPE);
    }

    /**
     * Primary Constructor
     */
    public SCLCavity(final String strId, final ChannelFactory channelFactory, final int intReserve) {
        super(strId, channelFactory, intReserve);
    }

    /**
     * Constructor
     */
    public SCLCavity(final String strId, final ChannelFactory channelFactory) {
        this(strId, channelFactory, 0);
    }

    /**
     * Primary Constructor
     */
    public SCLCavity(final String strID, final int arrayReserve) {
        this(strID, null, arrayReserve);
    }

    /**
     * Constructor
     */
    public SCLCavity(final String strID) {
        this(strID, 0);
    }

    /**
     * Get this node's type.
     *
     * @return the type ID of this node.
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     *
     * @return constant TTF used in the accelerator LLRF
     */
    public double getStructureTTF() {
        return bucRfCavity.getStructureTTF();
    }

    /**
     *
     * @return Q external
     */
    public double getQLoaded() {
        return bucRfCavity.getQLoaded();
    }

    /**
     * @param current beam current (mA)
     * @param energy beam kinetic energy (MeV)
     * @param resErr cavity resonance error (Hz)
     * @param plsWdth beam pulse width (micro-sec, single number, assume beam is
     * square wave)
     *
     * @return cavity field induced by beam loading when the cavity is off
     *
     * @deprecated This is one of only two methods that uses stuff from outside,
     * and that stuff is the <code>sclcavity</code> sub-package which is
     * completely hard coded for the SNS SCL. We must get rid of
     * <code>sclcavity</code>.
     *
     */
    @Deprecated()
    public double getFieldWithCavityOff(double current, double energy,
            double resErr, double plsWdth) {
        return 0.0;
    }

    /**
     * @param current beam current (mA)
     * @param energy beam kinetic energy (MeV)
     * @param resErr cavity resonance error (Hz)
     * @param plsArry beam pulse array
     * @param sampleRate BCM sample rate (micro-sec, single number)
     *
     * @return cavity field induced by beam loading when the cavity is off
     *
     * @deprecated This is one of only two methods that uses stuff from outside,
     * and that stuff is the <code>sclcavity</code> sub-package which is
     * completely hard coded for the SNS SCL. We must get rid of
     * <code>sclcavity</code>.
     */
    @Deprecated()
    public double getFieldWithCavityOff(double current, double energy,
            double resErr, double[] plsArry, double sampleRate) {

        return 0.0;
    }

    /**
     * Convert the raw channel access value to get the cavity amplitude in MV/m.
     *
     * @param rawValue the raw channel value
     * @return the cavity amplitude in MV/m
     */
    @Override
    public double toCavAmpAvgFromCA(final double rawValue) {
        return rawValue / getStructureTTF();
    }

    /**
     * Convert the cavity amplitude to channel access.
     *
     * @param value the cavity amplitude
     * @return the channel access value
     */
    @Override
    public double toCAFromCavAmpAvg(final double value) {
        return value * getStructureTTF();
    }

    /**
     * get the average TTF for the cavity as a function of beta this should use
     * parameters form the database, the hardwired fit coefficients should go in
     * the database - this is temp.
     *
     * @deprecated Read the comments about being a hard coded kluge job.
     */
    @Deprecated()
    public double getAvgTTF(double beta) {
        double ttf = 0.;
        int cavNum = (Integer.parseInt(getId().substring(10, 12)));
        if (cavNum < 12) {
            ttf = -22.05 * Math.pow(beta, 2.) + 28.539 * beta - 8.5272;
        } else {
            ttf = -15.113 * Math.pow(beta, 2.) + 25.695 * beta - 10.196;
        }
        return ttf;
    }

}
