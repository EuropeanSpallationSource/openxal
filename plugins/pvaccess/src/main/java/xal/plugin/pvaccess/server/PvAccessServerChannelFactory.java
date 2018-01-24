package xal.plugin.pvaccess.server;

import xal.ca.ChannelFactory;
import xal.ca.ChannelSystem;
import xal.plugin.pvaccess.PvAccessSystem;

/**
 * A factory class that is used to create server side channels.
 *
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class PvAccessServerChannelFactory extends ChannelFactory {

    private static final ChannelSystem CHANNEL_SYSTEM = new PvAccessSystem();

    /**
     * Constructor
     */
    public PvAccessServerChannelFactory() {
        /* Empty constructor */
    }

    /**
     * Create a channel with a signal name.
     *
     * @param signalName Name of the channel
     * @return Channel object
     */
    protected xal.ca.Channel newChannel(final String signalName) {
        return new PvAccessServerChannel(signalName);
    }

    /**
     * @return Channel system associated with this factory
     */
    protected ChannelSystem channelSystem() {
        return CHANNEL_SYSTEM;
    }

    public void printInfo() {
        // Nothing to do
    }

    @Override
    public boolean init() {
        // Nothing to do
        return true;
    }

}
