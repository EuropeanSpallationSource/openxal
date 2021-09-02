/*
 * Copyright (C) 2021 European Spallation Source ERIC.
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
package xal.smf;

import java.util.Arrays;

/**
 * Container for properties with their corresponding readback and set channel
 * handles, together with methods to get and set the design and live values.
 * <p>
 * Some properties may have getter and setter for design values, or only for
 * live values if the properties are not used by the model.
 * <p>
 * For live values, getter and setters are meant to only convert the value, so
 * if they are not provided the value will be returned the same. EPICS
 * communication is done by the {@link xal.smf.AcceleratorNode} object using the
 * methods {@link xal.smf.AcceleratorNode#getLivePropertyValue} and
 * {@link xal.smf.AcceleratorNode#setLivePropertyValue}.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class AccessibleProperty {

    private String name;

    private String[] readbackHandles;
    private String setHandle;

    private GetterDesign getterDesign = null;
    private GetterLive getterLive = null;

    private SetterDesign setterDesign = null;
    private SetterLive setterLive = null;

    // Flag that indicates if the property has design values. Otherwise it is a value only available in EPICS.
    private boolean designValues = false;

    public AccessibleProperty(String name, String readbackHandle, String setHandle, GetterDesign getterDesign, SetterDesign setterDesign) {
        this(name, new String[]{readbackHandle}, setHandle, getterDesign, setterDesign, null, null);
    }

    public AccessibleProperty(String name, String readbackHandle, String setHandle, GetterLive getterLive, SetterLive setterLive) {
        this(name, new String[]{readbackHandle}, setHandle, null, null, getterLive, setterLive);
    }

    public AccessibleProperty(String name, String readbackHandle, String setHandle, GetterDesign getterDesign, SetterDesign setterDesign, GetterLive getterLive, SetterLive setterLive) {
        this(name, new String[]{readbackHandle}, setHandle, getterDesign, setterDesign, getterLive, setterLive);
    }

    public AccessibleProperty(String name, String[] readbackHandles, String setHandle, GetterDesign getterDesign, SetterDesign setterDesign, GetterLive getterLive, SetterLive setterLive) {
        this.name = name;
        this.readbackHandles = readbackHandles;
        this.setHandle = setHandle;
        this.getterDesign = getterDesign;
        this.setterDesign = setterDesign;
        this.getterLive = getterLive;
        this.setterLive = setterLive;

        if (getterDesign != null && setterDesign != null) {
            designValues = true;
        }
    }

    public AccessibleProperty(String name, String handle, GetterDesign getterDesign, SetterDesign setterDesign) {
        this(name, handle, handle, getterDesign, setterDesign);
    }

    /**
     *
     * @param name Property name
     * @param readbackHandle Readback handle
     * @param setHandle Set handle
     */
    public AccessibleProperty(String name, String readbackHandle, String setHandle) {
        this(name, readbackHandle, setHandle, null, null, null, null);
    }

    public AccessibleProperty(String name, String handle) {
        this(name, handle, handle);
    }

    public String getName() {
        return name;
    }

    public String[] getReadbackHandles() {
        return readbackHandles;
    }

    public String getSetHandle() {
        return setHandle;
    }

    public void setSetHandle(String setHandle) {
        this.setHandle = setHandle;
    }

    public double getDesign() {
        return getterDesign.get();
    }

    public double getLive(double[] channelValues) {
        if (getterLive != null) {
            return getterLive.get(channelValues);
        } else {
            return channelValues[0];
        }
    }

    public void setDesign(double channelValue) {
        setterDesign.set(channelValue);
    }

    public boolean hasDesignValues() {
        return designValues;
    }

    public double setLive(double channelValue) {
        if (setterLive != null) {
            return setterLive.set(channelValue);
        } else {
            return channelValue;
        }
    }

    @Override
    public String toString() {
        return String.format("%s: {rb=%s, s=%s}", name, Arrays.toString(readbackHandles), setHandle);
    }
}
