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

import java.math.BigDecimal;
import org.epics.pvdata.pv.PVStructure;
import xal.ca.ChannelTimeRecord;
import xal.ca.Timestamp;

/**
 * ChannelTimeRecord implementation for Epics7.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelTimeRecord extends Epics7ChannelStatusRecord implements ChannelTimeRecord {

    protected Timestamp timestamp;

    private static final String TIMESTAMP_FIELD_NAME = "timeStamp";

    public static final String SECONDS_FIELD_NAME = "secondsPastEpoch";
    public static final String NANOSECONDS_FIELD_NAME = "nanoseconds";

    /**
     * Creates new Epics7ChannelStatusRecord
     *
     * @param pvStructure
     * @param channelName
     */
    public Epics7ChannelTimeRecord(PVStructure pvStructure, String channelName) {
        super(pvStructure, channelName);

        long seconds = pvStructure.getStructureField(TIMESTAMP_FIELD_NAME).getLongField(SECONDS_FIELD_NAME).get();
        int nanoSeconds = pvStructure.getStructureField(TIMESTAMP_FIELD_NAME).getIntField(NANOSECONDS_FIELD_NAME).get();

        timestamp = new Timestamp(new BigDecimal(nanoSeconds).multiply(new BigDecimal("1e-9")).add(new BigDecimal(seconds)));
    }

    /**
     * Get the timestamp.
     *
     * @return the timestamp
     */
    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Get the time stamp in seconds since the Java epoch epoch. Some precision
     * is lost as we move away from the epoch since the double precision number
     * cannot hold the full native precision.
     *
     * @return The time stamp in seconds as a double.
     */
    @Override
    public double timeStampInSeconds() {
        return timestamp.getSeconds();
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
        buffer.append(", time: ").append(timestamp.toString());

        return buffer.toString();
    }
}
