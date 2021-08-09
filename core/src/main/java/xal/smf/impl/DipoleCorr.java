package xal.smf.impl;

import xal.ca.ChannelFactory;
import xal.smf.attr.DipoleCorrBucket;

/**
 * Base class for dipole correctors.
 *
 * @author Blaz Kranjc
 */
public abstract class DipoleCorr extends Dipole {

    /**
     * standard type for nodes of this class
     */
    /**
     * Primary Constructor
     */
    public DipoleCorr(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setMagBucket(new DipoleCorrBucket());
    }

    /**
     * Determine whether this magnet is a corrector.
     *
     * @return true since all derived classes will be correctors
     */
    @Override
    public boolean isCorrector() {
        return true;
    }
}
