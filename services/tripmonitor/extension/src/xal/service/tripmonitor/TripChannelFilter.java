//
//  TripChannelFilter.java
//  xal
//
//  Created by Thomas Pelaia on 7/31/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.service.tripmonitor;

import java.util.Formatter;
import java.util.regex.*;

import xal.tools.data.*;

/**
 * filter to get a trip channel PV for a specific node
 */
public class TripChannelFilter {

    /**
     * data label
     */
    public static final String DATA_LABEL = "TripChannelFilter";

    /**
     * PV pattern
     */
    protected final String PV_FORMAT;

    /**
     * filter used to determine trips
     */
    protected final TripFilter TRIP_FILTER;

    /**
     * converts the matching PV segment to a node-key
     */
    protected final NodeKeyConverter NODE_KEY_CONVERTER;

    /**
     * Constructor
     */
    public TripChannelFilter(final DataAdaptor adaptor, final TripFilter tripFilter) {
        PV_FORMAT = adaptor.stringValue("PVFormat");
        TRIP_FILTER = tripFilter;

        if (adaptor.hasAttribute("conversion")) {
            final String conversion = adaptor.stringValue("conversion");
            NODE_KEY_CONVERTER = NodeKeyConverter.getConverter(conversion);
        } else {
            NODE_KEY_CONVERTER = NodeKeyConverter.defaultConverter();
        }
    }

    /**
     * Get the PV pattern
     */
    public String getPVFormat() {
        return PV_FORMAT;
    }

    /**
     * get the trip filter
     */
    public TripFilter getTripFilter() {
        return TRIP_FILTER;
    }

    /**
     * get the PV for the specified node key
     */
    public String getPV(final String nodeKey) {
        final Formatter formatter = new Formatter();
        final String segment = NODE_KEY_CONVERTER.toSegment(nodeKey);
        formatter.format(PV_FORMAT, segment);
        return formatter.toString();
    }

    /**
     * string representation of this instance
     */
    public String toString() {
        return "PV Format:  " + PV_FORMAT;
    }

}
