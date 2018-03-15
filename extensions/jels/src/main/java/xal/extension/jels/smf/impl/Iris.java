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
 * The implementation of the Iris class.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class Iris extends AcceleratorNode {

    /*
     *  Constants
     */
    public static final String s_strType = "IRIS";

    public static final String APERTURE_SET_HANDLE = "apertureS";
    private Channel apertureSC = null;
    public static final String APERTURE_RB_HANDLE = "apertureRB";
    private Channel apertureRBC = null;
    public static final String OFFSET_X_SET_HANDLE = "xOffsetS";
    private Channel offsetXSC = null;
    public static final String OFFSET_X_RB_HANDLE = "xOffsetRB";
    private Channel offsetXRBC = null;
    public static final String OFFSET_Y_SET_HANDLE = "yOffsetS";
    private Channel offsetYSC = null;
    public static final String OFFSET_Y_RB_HANDLE = "yOffsetRB";
    private Channel offsetYRBC = null;

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(Iris.class, s_strType);
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
    public Iris(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public Iris(final String strId) {
        this(strId, null);
    }

    /*
     *  Process variable Gets 
     */
    public double getAperture() throws ConnectionException, GetException {
        apertureRBC = lazilyGetAndConnect(APERTURE_RB_HANDLE, apertureRBC);
        return apertureRBC.getValDbl();
    }

    public double getOffsetX() throws ConnectionException, GetException {
        offsetXRBC = lazilyGetAndConnect(OFFSET_X_RB_HANDLE, offsetXRBC);
        return offsetXRBC.getValDbl();
    }

    public double getOffsetY() throws ConnectionException, GetException {
        offsetYRBC = lazilyGetAndConnect(OFFSET_Y_RB_HANDLE, offsetYRBC);
        return offsetYRBC.getValDbl();
    }

    /*
     *  Process variable Puts 
     */
    public void setAperture(double dblVal) throws ConnectionException, PutException {
        apertureSC = lazilyGetAndConnect(APERTURE_SET_HANDLE, apertureSC);
        apertureSC.putVal(dblVal);
    }

    public void setOffsetX(double dblVal) throws ConnectionException, PutException {
        offsetXSC = lazilyGetAndConnect(OFFSET_X_SET_HANDLE, offsetXSC);
        offsetXSC.putVal(dblVal);
    }

    public void setOffsetY(double dblVal) throws ConnectionException, PutException {
        offsetYSC = lazilyGetAndConnect(OFFSET_Y_SET_HANDLE, offsetYSC);
        offsetYSC.putVal(dblVal);
    }
}
