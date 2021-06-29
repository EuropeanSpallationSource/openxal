/*
 * PutNotifier.java
 *
 * Created on September 18, 2002, 12:32 PM
 */
package xal.plugin.jca;

import xal.ca.PutListener;
import xal.ca.Channel;

/**
 *
 * @author tap
 */
class PutNotifier implements gov.aps.jca.event.PutListener {

    protected final PutListener listener;
    protected final Channel channel;

    /**
     * Creates a new instance of PutNotifier
     */
    public PutNotifier(final Channel channel, final PutListener listener) {
        this.channel = channel;
        this.listener = listener;
    }

    /**
     * jca.event.PutListener Interface Implementation
     */
    public void putCompleted(final gov.aps.jca.event.PutEvent putEvent) {
        if (listener != null) {
            listener.putCompleted(channel);
        }
    }
}
