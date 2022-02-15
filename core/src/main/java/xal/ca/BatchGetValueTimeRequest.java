//
// BatchGetValueTimeRequest.java
// xal
//
// Created by Tom Pelaia on 4/9/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//
package xal.ca;

import java.util.*;

/**
 * batch of CA Get requests including value, status, severity and timestamp with
 * convenient batch operations
 */
public class BatchGetValueTimeRequest extends AbstractBatchGetRequest<ChannelTimeRecord> {

    /**
     * request handler
     */
    protected final RequestHandler requestHandler;

    /**
     * Primary Constructor
     *
     * @param channels for which to request the value, status and timestamp
     */
    public BatchGetValueTimeRequest(final Collection<Channel> channels) {
        super(channels);

        requestHandler = new RequestHandler();
    }

    /**
     * Constructor
     */
    public BatchGetValueTimeRequest() {
        this(Collections.<Channel>emptySet());
    }

    /**
     * request to get the data for the channel
     */
    @Override
    protected void requestChannelData(final Channel channel) throws ConnectionException, GetException {
        channel.getValueTimeCallback(requestHandler, false);
    }

    /**
     * handle get request events
     */
    protected class RequestHandler implements IEventSinkValTime {

        @Override
        public void eventValue(final ChannelTimeRecord channelRecord, final Channel channel) {
            processRecordEvent(channel, channelRecord);
        }
    }
}
