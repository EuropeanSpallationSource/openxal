//
//  BatchGetRequest.java
//  xal
//
//  Created by Tom Pelaia on 2/22/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//
package xal.ca;

import java.util.*;

/**
 * batch of CA Get requests including value only with convenient batch
 * operations
 */
public class BatchGetValueRequest extends AbstractBatchGetRequest<ChannelRecord> {

    /**
     * request handler
     */
    protected final RequestHandler requestHandler;

    /**
     * Primary Constructor
     *
     * @param channels for which to request the value
     */
    public BatchGetValueRequest(final Collection<Channel> channels) {
        super(channels);

        requestHandler = new RequestHandler();
    }

    /**
     * Constructor
     */
    public BatchGetValueRequest() {
        this(Collections.<Channel>emptySet());
    }

    /**
     * request to get the data for the channel
     */
    @Override
    protected void requestChannelData(final Channel channel) throws ConnectionException, GetException {
        channel.getValueCallback(requestHandler, false);
    }

    /**
     * handle get request events
     */
    protected class RequestHandler implements IEventSinkValue {

        @Override
        public void eventValue(final ChannelRecord channelRecord, final Channel channel) {
            processRecordEvent(channel, channelRecord);
        }
    }
}
