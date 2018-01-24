package xal.plugin.pvaccess;

import xal.ca.*;

/**
 * PvAccess Channel Factory.
 * @see {xal.ca.ChannelFactory}
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class PvAccessChannelFactory extends ChannelFactory {
    
    private static final PvAccessSystem PVACCESS_SYSTEM = new PvAccessSystem();
    
	public PvAccessChannelFactory() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
    @Override
    public boolean init() {
        // Nothing to do
        return true;
    }

	/**
	 * Creates a new channel with provided name.
	 * {@inheritDoc}
	 * @param signalName name of the channel.
	 * @return A constructed channel.
	 */
    @Override
    protected Channel newChannel(String signalName) {
        return new PvAccessChannel(signalName);
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    protected ChannelSystem channelSystem() {
        return PVACCESS_SYSTEM;
    }

    /**
	 * {@inheritDoc}
	 */
	@Deprecated
    @Override
    public void printInfo() {
        PVACCESS_SYSTEM.printInfo();
    }
	
}
