/*
 * TimingDataManager.java
 *
 * Created on Thu Feb 26 11:40:08 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.smf.data;

import xal.ca.ChannelFactory;
import xal.tools.data.*;
import xal.tools.xml.*;
import xal.smf.*;

/**
 * TimingDataManager parses the XML timing source to update the TimingCenter
 * with the timing information. Typically there is one TimingDataManager per
 * XMLDataManager.
 *
 * @author tap
 */
class TimingDataManager {

    /**
     * factory from which to generate the channels
     */
    private final ChannelFactory channelFactory;

    protected String urlSpec;
    protected TimingCenter timingCenter;
    protected String timingSchema = "/xal/schemas/xdxf.xsd";

    /**
     * TimingDataManager constructor
     *
     * @param urlSpec The URL spec of the timing data source
     * @param channelFactory factory from which to generate channels
     */
    public TimingDataManager(final String urlSpec, final ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
        this.urlSpec = urlSpec;
        timingCenter = null;
    }

    /**
     * TimingDataManager constructor
     *
     * @param channelFactory factory from which to generate channels
     */
    public TimingDataManager(final ChannelFactory channelFactory) {
        this(null, channelFactory);
    }

    /**
     * TimingDataManager constructor
     *
     * @param urlSpec The URL spec of the timing data source
     */
    public TimingDataManager(final String urlSpec) {
        this(urlSpec, ChannelFactory.defaultFactory());
    }

    /**
     * TimingDataManager constructor
     */
    public TimingDataManager() {
        this(null, ChannelFactory.defaultFactory());
    }

    /**
     * Set the timing source's URL
     *
     * @param urlSpec The new URL spec of the timing data source
     */
    public void setURLSpec(String urlSpec, String schemaUrl) {
        this.urlSpec = urlSpec;
        timingSchema = schemaUrl;
    }

    /**
     * Get the timing center generated from this manager's timing data source
     *
     * @return the timing center generated from this manager's timing data
     * source
     */
    public TimingCenter getTimingCenter() {
        return (timingCenter != null) ? timingCenter : parseTimingCenter();
    }

    /**
     * Parse the timing data source to get the timing center
     *
     * @return The timing center parsed from this manager's timing data source
     */
    protected TimingCenter parseTimingCenter() {
        timingCenter = new TimingCenter(channelFactory);
        updateTimingCenter(timingCenter);
        return timingCenter;
    }

    /**
     * Update the specified timing center with data from the timing data source
     *
     * @param timingCenter The timing center to update
     */
    protected void updateTimingCenter(final TimingCenter timingCenter) {
        if (urlSpec != null) {
            XmlDataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl(urlSpec, false, timingSchema);
            DataAdaptor timingAdaptor = documentAdaptor.childAdaptor(TimingCenter.DATA_LABEL);
            timingCenter.update(timingAdaptor);
        }
    }

    /**
     * Write the timing center to the specified URL
     *
     * @param urlSpec The URL where we will write the timing XML
     * @param dtdURISpec The URI spec of the DTD used for validating the XML
     */
    public void writeTimingCenterToURL(final String urlSpec, final String dtdURISpec) {
        XmlDataAdaptor adaptor = XmlDataAdaptor.newDocumentAdaptor(timingCenter, dtdURISpec);
        adaptor.writeToUrlSpec(urlSpec);
    }

    /**
     * Write the timing center to the URL of this TimingDataManager
     *
     * @param dtdURISpec The URI spec of the DTD used for validating the XML
     */
    public void writeTimingCenter(final String dtdURISpec) {
        writeTimingCenterToURL(urlSpec, dtdURISpec);
    }
}
