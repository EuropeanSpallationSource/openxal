/*
 * Copyright (C) 2020 European Spallation Source ERIC
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
package xal.plugin.epics7;

import org.epics.pvdata.pv.PVStructure;
import xal.ca.ChannelStatusRecord;

/**
 * ChannelStatusRecord implementation for Epics7.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelStatusRecord extends Epics7ChannelRecord implements ChannelStatusRecord {

    protected int status;
    protected int severity;

    public static final String ALARM_FIELD_NAME = "alarm";

    public static final String STATUS_FIELD_NAME = "status";
    public static final String SEVERITY_FIELD_NAME = "severity";

    /**
     * Creates new Epics7ChannelStatusRecord
     *
     * @param pvStructure
     * @param channelName
     */
    public Epics7ChannelStatusRecord(PVStructure pvStructure, String channelName) {
        super(pvStructure, channelName);

        status = pvStructure.getStructureField(ALARM_FIELD_NAME).getIntField(STATUS_FIELD_NAME).get();
        severity = pvStructure.getStructureField(ALARM_FIELD_NAME).getIntField(SEVERITY_FIELD_NAME).get();
    }

    /**
     * Get the internal status code for this data.
     *
     * @return the status code for this data.
     */
    @Override
    public int status() {
        return status;
    }

    /**
     * Get the internal severity code for this data.
     *
     * @return the severity code for this data.
     */
    @Override
    public int severity() {
        return severity;
    }

    /**
     * Override the inherited method to return a description of this object.
     *
     * @return A description of this object.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(super.toString());
        buffer.append(", status: ").append(status);
        buffer.append(", severity: ").append(severity);

        return buffer.toString();
    }
}
