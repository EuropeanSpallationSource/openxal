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
 * The implementation of the Repeller Electrode class.
 * 
 * @author Natalia Milas <natalia.milas@esss.se>
 */
public class Doppler extends AcceleratorNode {
    /*
     *  Constants
     */
    public static final String s_strType = "DPL";
    
    public static final String FRACTION_H_R_HANDLE = "fractionH+";
    private Channel fractionHRC = null;
    public static final String FRACTION_H2_R_HANDLE = "fractionH2+";
    private Channel fractionH2RC = null; 
    public static final String FRACTION_H3_R_HANDLE = "fractionH3+";
    private Channel fractionH3RC = null; 

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(Doppler.class, s_strType);
    }

    /**
     * Override to provide type signature
     * @return String type
     */
    @Override
    public String getType() {
        return s_strType;
    }

    /**
     * Constructor
     */
    public Doppler(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public Doppler(final String strId) {
        this(strId, null);
    }

    /*
     *  Process variable Gets 
     */
    public double getFraction_H() throws ConnectionException, GetException {
        fractionHRC = lazilyGetAndConnect(FRACTION_H_R_HANDLE, fractionHRC);
        return fractionHRC.getValDbl();
    }
    
    public double getFraction_H2() throws ConnectionException, GetException {
        fractionH2RC = lazilyGetAndConnect(FRACTION_H2_R_HANDLE, fractionH2RC);
        return fractionH2RC.getValDbl();
    }
    
    public double getFraction_H3() throws ConnectionException, GetException {
        fractionH3RC = lazilyGetAndConnect(FRACTION_H_R_HANDLE, fractionH3RC);
        return fractionH3RC.getValDbl();
    }
  
   
}
