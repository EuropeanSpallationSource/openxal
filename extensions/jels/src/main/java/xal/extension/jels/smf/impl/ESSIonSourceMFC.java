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
import xal.smf.impl.qualify.ElementTypeManager;

/**
 * The implementation of the ESS Ion Source's mass flow controller.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class ESSIonSourceMFC extends AcceleratorNode {

    public static final String s_strType = "ISMFC";

    // Ion Source's mass flow controller channel handles
    public static final String H_2_FLOW_RB_HANDLE = "h2FlowRB";
    public static final String H_2_FLOW_R_HANDLE = "h2FlowR";
    public static final String H_2_FLOW_S_HANDLE = "h2FlowS";
    private Channel h2FlowRBC = null;
    private Channel h2FlowRC = null;
    private Channel h2FlowSC = null;

    // High-voltage power supply channel handles
    public static final String VOLTAGE_SET_HANDLE = "VolSet";
    public static final String VOLTAGE_READ_HANDLE = "VolRead";
    public static final String VOLTAGE_RB_HANDLE = "VolRB";
    public static final String CURRENT_SET_HANDLE = "CurrSet";
    public static final String CURRENT_READ_HANDLE = "CurrRead";
    public static final String CURRENT_RB_HANDLE = "CurrRB";
    Channel voltageSetChannel = null;
    Channel voltageReadChannel = null;
    Channel voltageRBChannel = null;
    Channel currentSetChannel = null;
    Channel currentReadChannel = null;
    Channel currentRBChannel = null;
    /**
     * the ID of this magnet's main power supply
     */
    protected String mainSupplyId;

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(ESSIonSourceMFC.class, s_strType);
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
    public ESSIonSourceMFC(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public ESSIonSourceMFC(final String strId) {
        this(strId, null);
    }

    /*
     *  Process variable Gets 
     */
    public double getH2FlowRB() throws ConnectionException, GetException {
        h2FlowRBC = lazilyGetAndConnect(H_2_FLOW_RB_HANDLE, h2FlowRBC);
        return h2FlowRBC.getValDbl();
    }

    public double getH2FlowR() throws ConnectionException, GetException {
        h2FlowRC = lazilyGetAndConnect(H_2_FLOW_R_HANDLE, h2FlowRC);
        return h2FlowRC.getValDbl();
    }

    public void setH2FlowS(double dblVal) throws ConnectionException, PutException {
        h2FlowSC = lazilyGetAndConnect(H_2_FLOW_S_HANDLE, h2FlowSC);
        h2FlowSC.putVal(dblVal);
    }

    public void setVoltage(double dblVal) throws ConnectionException, PutException {
        voltageSetChannel = lazilyGetAndConnect(VOLTAGE_SET_HANDLE, voltageSetChannel);

        voltageSetChannel.putVal(dblVal);
    }

    public double getVoltage() throws ConnectionException, GetException {
        voltageReadChannel = lazilyGetAndConnect(VOLTAGE_READ_HANDLE, voltageReadChannel);

        return voltageReadChannel.getValDbl();
    }

    public double getVoltageRB() throws ConnectionException, GetException {
        voltageRBChannel = lazilyGetAndConnect(VOLTAGE_RB_HANDLE, voltageRBChannel);

        return voltageRBChannel.getValDbl();
    }

    public void setCurrent(double dblVal) throws ConnectionException, PutException {
        currentSetChannel = lazilyGetAndConnect(CURRENT_SET_HANDLE, currentSetChannel);

        currentSetChannel.putVal(dblVal);
    }

    public double getCurrent() throws ConnectionException, GetException {
        currentReadChannel = lazilyGetAndConnect(CURRENT_READ_HANDLE, currentReadChannel);

        return currentReadChannel.getValDbl();
    }

    public double getCurrentRB() throws ConnectionException, GetException {
        currentRBChannel = lazilyGetAndConnect(CURRENT_RB_HANDLE, currentRBChannel);

        return currentRBChannel.getValDbl();
    }
}
