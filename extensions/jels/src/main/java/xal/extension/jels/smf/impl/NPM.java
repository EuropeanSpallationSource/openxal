/*
 * Copyright (C) 2018 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.jels.smf.impl;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.extension.jels.smf.attr.NPMBucket;
import xal.smf.AcceleratorNode;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.qualify.ElementTypeManager;

/**
 * The implementation of the NPM class.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class NPM extends AcceleratorNode {

    /*
     *  Constants
     */
    public static final String s_strType = "NPM";

    /**
     * The container for the NPM information
     *
     */
    protected NPMBucket npmBucket;

    // NPM channel handles
    /**
     * NPMs official xAvg channel handle
     */
    public static final String X_AVG_HANDLE = "xAvg";
    private Channel xAvgC = null;
    /**
     * NPMs official yAvg channel handle
     */
    public static final String Y_AVG_HANDLE = "yAvg";
    private Channel yAvgC = null;
    /**
     * NPMs official ampAvg channel handle
     */
    public static final String AMP_AVG_HANDLE = "amplitudeAvg";
    private Channel ampAvgC = null;
    /**
     * NPMs official phaseAvg channel handle
     */
    public static final String PHASE_AVG_HANDLE = "phaseAvg";
    private Channel phaseAvgC = null;
    /**
     * NPMs official x turn-by-turn channel handle
     */
    public static final String X_TBT_HANDLE = "xTBT";
    private Channel xTBTC = null;
    /**
     * NPMs official y turn-by-turn channel handle
     */
    public static final String Y_TBT_HANDLE = "yTBT";
    private Channel yTBTC = null;
    /**
     * NPMs official amplitude turn-by-turn channel handle
     */
    public static final String AMP_TBT_HANDLE = "ampTBT";
    private Channel ampTBTC = null;
    /**
     * NPMs official phase turn-by-turn channel handle
     */
    public static final String PHASE_TBT_HANDLE = "phaseTBT";
    private Channel phaseTBTC = null;
    /**
     * NPMs official tAvgLen channel handle
     */
    public static final String T_AVG_LEN_HANDLE = "tAvgLen";
    private Channel tAvgLenC = null;

    public static final String Y_P_AVG_HANDLE = "ypAvg";
    private Channel ypAvgC = null;
    public static final String X_P_AVG_HANDLE = "xpAvg";
    private Channel xpAvgC = null;
    public static final String SIGMA_Y_AVG_HANDLE = "sigmayAvg";
    private Channel sigmayAvgC = null;
    public static final String SIGMA_X_AVG_HANDLE = "sigmaxAvg";
    private Channel sigmaxAvgC = null;
    public static final String ALPHA_Y_TWISS_HANDLE = "alphayTwiss";
    private Channel alphayTwissC = null;
    public static final String BETA_Y_TWISS_HANDLE = "betayTwiss";
    private Channel betayTwissC = null;
    public static final String ALPHA_X_TWISS_HANDLE = "alphaxTwiss";
    private Channel alphaxTwissC = null;
    public static final String BETA_X_TWISS_HANDLE = "betaxTwiss";
    private Channel betaxTwissC = null;

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(NPM.class, s_strType);
    }

    /**
     * Override to provide type signature
     */
    public String getType() {
        return s_strType;
    }

    /**
     * Constructor
     */
    public NPM(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setNPMBucket(new NPMBucket());
    }

    /**
     * Constructor
     */
    public NPM(final String strId) {
        this(strId, null);
    }

    /**
     * return the NPM Bucket
     */
    public NPMBucket getNPMBucket() {
        return npmBucket;
    }

    /**
     * Set the attribute bucket containing the npm info
     */
    public void setNPMBucket(NPMBucket buc) {
        npmBucket = buc;
        super.addBucket(buc);
    }

    /**
     *
     * Override AcceleratorNode implementation to check for a BPMBucket
     */
    public void addBucket(AttributeBucket buc) {

        if (buc.getClass().equals(NPMBucket.class)) {
            setNPMBucket((NPMBucket) buc);
        }
        super.addBucket(buc);
    }

    /*
     *  Process variable Gets 
     */
    /**
     * returns average X position over macropulse (mm) accounting for alignment
     */
    public double getXAvg() throws ConnectionException, GetException {
        xAvgC = lazilyGetAndConnect(X_AVG_HANDLE, xAvgC);
        return xAvgC.getValDbl();
    }

    /**
     * returns average Y position over macropulse (mm) accounting for alignment
     */
    public double getYAvg() throws ConnectionException, GetException {
        yAvgC = lazilyGetAndConnect(Y_AVG_HANDLE, yAvgC);
        return yAvgC.getValDbl();
    }

    /**
     * returns average npm Amplitude signal over macropulse (au)
     */
    public double getAmpAvg() throws ConnectionException, GetException {
        ampAvgC = lazilyGetAndConnect(AMP_AVG_HANDLE, ampAvgC);
        return ampAvgC.getValDbl();
    }

    /**
     * returns average npm phase signal over macropulse (au)
     */
    public double getPhaseAvg() throws ConnectionException, GetException {
        phaseAvgC = lazilyGetAndConnect(PHASE_AVG_HANDLE, phaseAvgC);
        return phaseAvgC.getValDbl();
    }

    /**
     * returns npm x turn-by-turn array
     */
    public double[] getXTBT() throws ConnectionException, GetException {
        xTBTC = lazilyGetAndConnect(X_TBT_HANDLE, xTBTC);
        return xTBTC.getArrDbl();
    }

    /**
     * returns npm y turn-by-turn array
     */
    public double[] getYTBT() throws ConnectionException, GetException {
        yTBTC = lazilyGetAndConnect(Y_TBT_HANDLE, yTBTC);
        return yTBTC.getArrDbl();
    }

    /**
     * returns npm amplitude turn-by-turn array
     */
    public double[] getAmpTBT() throws ConnectionException, GetException {
        ampTBTC = lazilyGetAndConnect(AMP_TBT_HANDLE, ampTBTC);
        return ampTBTC.getArrDbl();
    }

    /**
     * returns npm phase turn-by-turn array
     */
    public double[] getPhaseTBT() throws ConnectionException, GetException {
        phaseTBTC = lazilyGetAndConnect(PHASE_TBT_HANDLE, phaseTBTC);
        return phaseTBTC.getArrDbl();
    }

    /**
     * returns length of the averaged period (micro-sec)
     */
    public double getTAvgLen() throws ConnectionException, GetException {
        tAvgLenC = lazilyGetAndConnect(T_AVG_LEN_HANDLE, tAvgLenC);
        return tAvgLenC.getValDbl();
    }

    public double getYpAvgC() throws ConnectionException, GetException {
        ypAvgC = lazilyGetAndConnect(Y_P_AVG_HANDLE, ypAvgC);
        return ypAvgC.getValDbl();
    }

    public double getXpAvgC() throws ConnectionException, GetException {
        xpAvgC = lazilyGetAndConnect(X_P_AVG_HANDLE, xpAvgC);
        return xpAvgC.getValDbl();
    }

    public double getSigmayAvgC() throws ConnectionException, GetException {
        sigmayAvgC = lazilyGetAndConnect(SIGMA_Y_AVG_HANDLE, sigmayAvgC);
        return sigmayAvgC.getValDbl();
    }

    public double getSigmaxAvgC() throws ConnectionException, GetException {
        sigmaxAvgC = lazilyGetAndConnect(SIGMA_X_AVG_HANDLE, sigmaxAvgC);
        return sigmaxAvgC.getValDbl();
    }

    public double getAlphayTwissC() throws ConnectionException, GetException {
        alphayTwissC = lazilyGetAndConnect(ALPHA_Y_TWISS_HANDLE, alphayTwissC);
        return alphayTwissC.getValDbl();
    }

    public double getBetayTwissC() throws ConnectionException, GetException {
        betayTwissC = lazilyGetAndConnect(BETA_Y_TWISS_HANDLE, betayTwissC);
        return betayTwissC.getValDbl();
    }

    public double getAlphaxTwissC() throws ConnectionException, GetException {
        alphaxTwissC = lazilyGetAndConnect(ALPHA_X_TWISS_HANDLE, alphaxTwissC);
        return alphaxTwissC.getValDbl();
    }

    public double getBetaxTwissC() throws ConnectionException, GetException {
        betaxTwissC = lazilyGetAndConnect(BETA_X_TWISS_HANDLE, betaxTwissC);
        return betaxTwissC.getValDbl();
    }
}
