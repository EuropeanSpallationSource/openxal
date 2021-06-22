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
 * handles, together with methods to get the design and live values (when
 * needed).
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class AccessibleProperty {

    private String name;

    private String[] readbackHandles;
    private String setHandle;

    private GetterDesign getterDesign = null;
    private GetterLive getterLive = null;
    private boolean getters = false;

    public AccessibleProperty(String name, String readbackHandle, String setHandle, GetterDesign getterDesign, GetterLive getterLive) {
        this.name = name;
        this.readbackHandles = new String[]{readbackHandle};
        this.setHandle = setHandle;
        this.getterDesign = getterDesign;
        this.getterLive = getterLive;
        if (getterDesign != null && getterLive != null) {
            this.getters = true;
        }
    }

    public AccessibleProperty(String name, String[] readbackHandles, String setHandle, GetterDesign getterDesign, GetterLive getterLive) {
        this.name = name;
        this.readbackHandles = readbackHandles;
        this.setHandle = setHandle;
        this.getterDesign = getterDesign;
        this.getterLive = getterLive;
        if (getterDesign != null && getterLive != null) {
            this.getters = true;
        }
    }

    /**
     *
     * @param name Property name
     * @param readbackHandle Readback handle
     * @param setHandle Set handle
     */
    public AccessibleProperty(String name, String readbackHandle, String setHandle) {
        this.name = name;
        this.readbackHandles = new String[]{readbackHandle};
        this.setHandle = setHandle;
    }

    /**
     *
     * @param name
     * @param handle
     */
    public AccessibleProperty(String name, String handle) {
        this.name = name;
        this.readbackHandles = new String[]{handle};
        this.setHandle = handle;
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
        return getterLive.get(channelValues);
    }

    public boolean hasGetters() {
        return getters;
    }

    public String toString() {
        return String.format("%s: {rb=%s, s=%s}", name, Arrays.toString(readbackHandles), setHandle);
    }
}
