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
 * The implementation of the EMU class.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class EMU extends AcceleratorNode {

    /*
     *  Constants
     */
    public static final String s_strType = "EMU";

    // EMU channel handles              
    public static final String EMITT_X_HANDLE = "emittX";
    private Channel emittXC = null;
    public static final String EMITT_Y_HANDLE = "emittY";
    private Channel emittYC = null;
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
        ElementTypeManager.defaultManager().registerTypes(EMU.class, s_strType);
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
    public EMU(final String strId) {
        this(strId, null);
    }

    /**
     * Primary Constructor
     */
    public EMU(final String strId, final ChannelFactory channeFactory) {
        super(strId, channeFactory);
    }

    /*
     *  Process variable Gets 
     */
    public double getEmittanceX() throws ConnectionException, GetException {
        emittXC = lazilyGetAndConnect(EMITT_X_HANDLE, emittXC);
        return emittXC.getValDbl();
    }

    public double getEmittanceY() throws ConnectionException, GetException {
        emittYC = lazilyGetAndConnect(EMITT_Y_HANDLE, emittYC);
        return emittYC.getValDbl();
    }

    public double getAlphayTwiss() throws ConnectionException, GetException {
        alphayTwissC = lazilyGetAndConnect(ALPHA_Y_TWISS_HANDLE, alphayTwissC);
        return alphayTwissC.getValDbl();
    }

    public double getBetayTwiss() throws ConnectionException, GetException {
        betayTwissC = lazilyGetAndConnect(BETA_Y_TWISS_HANDLE, betayTwissC);
        return betayTwissC.getValDbl();
    }

    public double getAlphaxTwiss() throws ConnectionException, GetException {
        alphaxTwissC = lazilyGetAndConnect(ALPHA_X_TWISS_HANDLE, alphaxTwissC);
        return alphaxTwissC.getValDbl();
    }

    public double getBetaxTwiss() throws ConnectionException, GetException {
        betaxTwissC = lazilyGetAndConnect(BETA_X_TWISS_HANDLE, betaxTwissC);
        return betaxTwissC.getValDbl();
    }
}
