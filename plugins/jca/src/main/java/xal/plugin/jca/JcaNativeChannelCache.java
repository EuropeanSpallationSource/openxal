//
//  JcaNativeChannelCache.java
//  xal
//
//  Created by Thomas Pelaia on 9/19/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.plugin.jca;

import java.util.*;

import gov.aps.jca.Channel;

/**
 * Cache JCA native channels for reuse among several XAL channels. JCA won't
 * allow us to create more than one channel for the same PV signal.
 */
class JcaNativeChannelCache {

    /**
     * JCA System
     */
    protected final JcaSystem jcaSystem;

    /**
     * map of native channel's keyed by PV signal name
     */
    protected final Map<String, Channel> channelMap;

    /**
     * Constructor
     */
    public JcaNativeChannelCache(final JcaSystem jcaSystem) {
        this.jcaSystem = jcaSystem;
        channelMap = new HashMap<>();
    }

    /**
     * Get an existing channel if available or else, create a new channel for
     * specified signal name.
     *
     * @param signalName the PV signal name.
     * @return the native JCA channel corresponding to the specified PV signal
     * @throws gov.aps.jca.CAException if the channel fails to be created
     */
    public Channel getChannel(final String signalName) throws gov.aps.jca.CAException {
        Channel channel;

        synchronized (channelMap) {
            channel = channelMap.get(signalName);

            if (channel == null) {
                channel = jcaSystem.getJcaContext().createChannel(signalName);
                channelMap.put(signalName, channel);
            }
        }

        return channel;
    }
}
