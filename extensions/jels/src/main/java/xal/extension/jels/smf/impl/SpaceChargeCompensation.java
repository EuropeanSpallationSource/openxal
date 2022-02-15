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
import xal.ca.GetException;
import xal.ca.PutException;
import xal.smf.AcceleratorNode;
import xal.smf.AccessibleProperty;
import xal.smf.impl.qualify.ElementTypeManager;

/**
 * The implementation of the Gas flow element for Space Charge Compensation.
 *
 * @author Natalia Milas <natalia.milas@esss.se>
 */
public class SpaceChargeCompensation extends AcceleratorNode {

    /*
     *  Constants
     */
    public static final String TYPE = "SCC";

    public static final String N2FLOW_RB_HANDLE = "n2flowRB";

    private Channel n2flowRC = null;
    public static final String N2FLOW_SET_HANDLE = "n2flowS";
    private Channel n2flowSC = null;

    public final AccessibleProperty n2flow = new AccessibleProperty("n2flow", N2FLOW_RB_HANDLE, N2FLOW_SET_HANDLE);

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(SpaceChargeCompensation.class, TYPE);
    }

    /**
     * Override to provide type signature
     *
     * @return String type
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Constructor
     */
    public SpaceChargeCompensation(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public SpaceChargeCompensation(final String strId) {
        this(strId, null);
    }

    /*
     *  Process variable Gets 
     */
    public double getN2Flow() throws GetException {
        n2flowRC = lazilyGetAndConnect(N2FLOW_RB_HANDLE, n2flowRC);
        return n2flowRC.getValDbl();
    }

    /*
     *  Process variable Puts 
     */
    public void setN2Flow(double dblVal) throws PutException {
        n2flowSC = lazilyGetAndConnect(N2FLOW_SET_HANDLE, n2flowSC);
        n2flowSC.putVal(dblVal);
    }
}
