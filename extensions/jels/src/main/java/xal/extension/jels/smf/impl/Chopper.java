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
 * The implementation of the Chopper class.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class Chopper extends AcceleratorNode {

    /*
     *  Constants
     */
    public static final String s_strType = "CHP";

    public static final String LENGTH_SET_HANDLE = "lengthS";
    private Channel lengthSC = null;

    public static final String STATUS_RB_HANDLE = "statusRB";
    private Channel statusRC = null;

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(Chopper.class, s_strType);
    }

    /**
     * Override to provide type signature
     */
    @Override
    public String getType() {
        return s_strType;
    }

    /**
     * Constructor
     */
    public Chopper(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public Chopper(final String strId) {
        this(strId, null);
    }

    /*
     *  Process variable Gets
     */
    public int getStatusON_OFF() throws ConnectionException, GetException {
        statusRC = lazilyGetAndConnect(STATUS_RB_HANDLE, statusRC);
        return statusRC.getValEnum();
    }

    public void setPulseLength(double dblVal) throws ConnectionException, PutException {
        lengthSC = lazilyGetAndConnect(LENGTH_SET_HANDLE, lengthSC);
        lengthSC.putVal(dblVal);
    }
}
