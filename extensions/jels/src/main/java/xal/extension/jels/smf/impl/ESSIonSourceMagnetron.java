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
import xal.ca.PutException;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.qualify.ElementTypeManager;

/**
 * The implementation of the ESS Ion Source's magnetron.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class ESSIonSourceMagnetron extends AcceleratorNode {

    public static final String s_strType = "ISM";

    // Ion Source's magnetron channel handles
    public static final String FORWD_PRW_RB_HANDLE = "ForwdPrwRB";

    private Channel forwdPrwRBC = null;

    public static final String FORWD_PRW_R_HANDLE = "ForwdPrwR";

    private Channel forwdPrwRC = null;

    public static final String FORWD_PRW_S_HANDLE = "ForwdPrwS";

    private Channel forwdPrwSC = null;

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(ESSIonSourceMagnetron.class, s_strType);
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
    public ESSIonSourceMagnetron(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public ESSIonSourceMagnetron(final String strId) {
        this(strId, null);
    }

    /*
     *  Process variable Gets 
     */
    public double getForwdPrwRB() throws ConnectionException, GetException {
        forwdPrwRBC = lazilyGetAndConnect(FORWD_PRW_RB_HANDLE, forwdPrwRBC);
        return forwdPrwRBC.getValDbl();
    }

    public double getForwdPrwR() throws ConnectionException, GetException {
        forwdPrwRC = lazilyGetAndConnect(FORWD_PRW_R_HANDLE, forwdPrwRC);
        return forwdPrwRC.getValDbl();
    }

    public void setForwdPrwS(double dblVal) throws NoSuchChannelException, ConnectionException, PutException {
        forwdPrwSC = getAndConnectChannel(FORWD_PRW_S_HANDLE);
        forwdPrwSC.putVal(dblVal);
    }
}
