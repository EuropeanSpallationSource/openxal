package xal.plugin.pvaccess;

import xal.ca.ChannelSystem;

/**
 * A pvAccess plugin implementation of ChannelSystem. As the architecture of
 * pvAccess library is different to jca's methods of this class are not used.
 *
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class PvAccessSystem extends ChannelSystem {

    /**
     * Constructor
     */
    public PvAccessSystem() {
    }

    @Deprecated
    @Override
    public void setDebugMode(boolean debugFlag) {
        // Not implemented
    }

    @Deprecated
    @Override
    public void flushIO() {
        // Nothing to do
    }

    @Deprecated
    @Override
    public boolean pendIO(double timeout) {
        // Nothing to do
        return true;
    }

    @Deprecated
    @Override
    public void pendEvent(double timeout) {
        // Nothing to do
    }

    @Deprecated
    @Override
    public void printInfo() {
        // Not implemented
    }

}
