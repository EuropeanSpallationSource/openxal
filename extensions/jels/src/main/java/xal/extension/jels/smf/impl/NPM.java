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
import xal.smf.AccessibleProperty;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.BPM;
import xal.smf.impl.qualify.ElementTypeManager;

/**
 * The implementation of the NPM class.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class NPM extends BPM {

    /*
     *  Constants
     */
    public static final String TYPE = "NPM";

    /**
     * The container for the NPM information
     *
     */
    protected NPMBucket npmBucket;

    // NPM channel handles
    public static final String Y_P_AVG_HANDLE = "ypAvg";
    public final AccessibleProperty ypAvg = new AccessibleProperty("ypAvg", Y_P_AVG_HANDLE);
    private Channel ypAvgC = null;

    public static final String X_P_AVG_HANDLE = "xpAvg";
    public final AccessibleProperty xpAvg = new AccessibleProperty("xpAvg", X_P_AVG_HANDLE);
    private Channel xpAvgC = null;

    public static final String SIGMA_Y_AVG_HANDLE = "ySigma";
    public final AccessibleProperty ySigma = new AccessibleProperty("ySigma", SIGMA_Y_AVG_HANDLE);
    private Channel sigmayAvgC = null;

    public static final String SIGMA_X_AVG_HANDLE = "xSigma";
    public final AccessibleProperty xSigma = new AccessibleProperty("xSigma", SIGMA_X_AVG_HANDLE);
    private Channel sigmaxAvgC = null;

    public static final String ALPHA_Y_TWISS_HANDLE = "yAlphaTwiss";
    public final AccessibleProperty yAlphaTwiss = new AccessibleProperty("yAlphaTwiss", ALPHA_Y_TWISS_HANDLE);
    private Channel alphayTwissC = null;

    public static final String BETA_Y_TWISS_HANDLE = "yBetaTwiss";
    public final AccessibleProperty yBetaTwiss = new AccessibleProperty("yBetaTwiss", BETA_Y_TWISS_HANDLE);
    private Channel betayTwissC = null;

    public static final String ALPHA_X_TWISS_HANDLE = "xAlphaTwiss";
    public final AccessibleProperty xAlphaTwiss = new AccessibleProperty("xAlphaTwiss", ALPHA_X_TWISS_HANDLE);
    private Channel alphaxTwissC = null;

    public static final String BETA_X_TWISS_HANDLE = "xBetaTwiss";
    public final AccessibleProperty xBetaTwiss = new AccessibleProperty("xBetaTwiss", BETA_X_TWISS_HANDLE);
    private Channel betaxTwissC = null;

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(NPM.class, TYPE);
    }

    /**
     * Override to provide type signature
     */
    public String getType() {
        return TYPE;
    }

    /**
     * Overriding this method since NPM is not a type of BPM. The subclass was
     * made to reduce boilerplate code.
     */
    @Override
    public boolean isKindOf(String compType) {
        return TYPE.equals(compType);
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
    public double getYpAvg() throws ConnectionException, GetException {
        ypAvgC = lazilyGetAndConnect(Y_P_AVG_HANDLE, ypAvgC);
        return ypAvgC.getValDbl();
    }

    public double getXpAvg() throws ConnectionException, GetException {
        xpAvgC = lazilyGetAndConnect(X_P_AVG_HANDLE, xpAvgC);
        return xpAvgC.getValDbl();
    }

    public double getYSigmaAvg() throws ConnectionException, GetException {
        sigmayAvgC = lazilyGetAndConnect(SIGMA_Y_AVG_HANDLE, sigmayAvgC);
        return sigmayAvgC.getValDbl();
    }

    public double getXSigmaAvg() throws ConnectionException, GetException {
        sigmaxAvgC = lazilyGetAndConnect(SIGMA_X_AVG_HANDLE, sigmaxAvgC);
        return sigmaxAvgC.getValDbl();
    }

    public double getYAlphaTwiss() throws ConnectionException, GetException {
        alphayTwissC = lazilyGetAndConnect(ALPHA_Y_TWISS_HANDLE, alphayTwissC);
        return alphayTwissC.getValDbl();
    }

    public double getYBetaTwiss() throws ConnectionException, GetException {
        betayTwissC = lazilyGetAndConnect(BETA_Y_TWISS_HANDLE, betayTwissC);
        return betayTwissC.getValDbl();
    }

    public double getXAlphaTwiss() throws ConnectionException, GetException {
        alphaxTwissC = lazilyGetAndConnect(ALPHA_X_TWISS_HANDLE, alphaxTwissC);
        return alphaxTwissC.getValDbl();
    }

    public double getXBetaTwiss() throws ConnectionException, GetException {
        betaxTwissC = lazilyGetAndConnect(BETA_X_TWISS_HANDLE, betaxTwissC);
        return betaxTwissC.getValDbl();
    }
}
