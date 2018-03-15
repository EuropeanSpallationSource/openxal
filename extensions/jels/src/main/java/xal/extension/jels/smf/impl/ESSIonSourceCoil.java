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
 * ESS implementation of the Ion Source Coils.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 *
 */
public class ESSIonSourceCoil extends AcceleratorNode {

    public static final String s_strType = "ISC";

    // Coils channel handles    
    public static final String I_HANDLE = "I";
    private Channel iC = null;
    public static final String I_SET_HANDLE = "I_Set";
    private Channel iSetC = null;

    static {
        registerType();
    }

    public ESSIonSourceCoil(String strId) {
        this(strId, null);
    }

    public ESSIonSourceCoil(String strId, ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }
    
    
    public double getCurrent() throws ConnectionException, GetException {
        iC = lazilyGetAndConnect(I_HANDLE, iC);
        return iC.getValDbl();
    }
    
    public void setCurrent(double dblValue) throws ConnectionException, PutException {
        iSetC = lazilyGetAndConnect(I_SET_HANDLE, iSetC);
        iSetC.putVal(dblValue);
    }

    @Override
    public String getType() {
        return s_strType;
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(ESSIonSourceCoil.class, s_strType);
        typeManager.registerType(ESSIonSourceCoil.class, "ionSourceCoil");
    }
}
