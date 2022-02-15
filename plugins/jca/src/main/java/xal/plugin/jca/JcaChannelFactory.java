/*
 * JcaChannelFactory.java
 *
 * Created on August 26, 2002, 1:25 PM
 */
package xal.plugin.jca;

import java.util.logging.Logger;
import xal.ca.*;
import xal.tools.apputils.Preferences;

/**
 * Concrete implementation of ChannelFactory that uses JCA.
 *
 * @author tap
 */
public class JcaChannelFactory extends ChannelFactory {

    /**
     * JCA channel system
     */
    private final JcaSystem jcaSystem;

    /**
     * cache of native JCA channels
     */
    private final JcaNativeChannelCache nativeChannelCache;

    /**
     * Constructor
     */
    public JcaChannelFactory() {
        // If the property "jca.use_env" is not defined in the command line, 
        // it will try to get it from Open XAL preferences. By default it will be true.
        if (System.getProperty("jca.use_env") == null) {
            java.util.prefs.Preferences defaults = Preferences.nodeForPackage(JcaChannelFactory.class);
            Boolean jca_use_env = defaults.getBoolean("jca.use_env", true);
            System.setProperty("jca.use_env", jca_use_env.toString());
        }
        if (Boolean.getBoolean("jca.use_env")) {
            Logger.getLogger(JcaChannelFactory.class.getName()).info("Using environment variables for EPICS configuration.");
        } else {
            Logger.getLogger(JcaChannelFactory.class.getName()).info("Using JCALibrary.properties for EPICS configuration.");
        }
        jcaSystem = new JcaSystem();
        nativeChannelCache = new JcaNativeChannelCache(jcaSystem);
    }

    /**
     * Initialize the channel system
     *
     * @return true if the initialization was successful and false if not
     */
    @Override
    public boolean init() {
        return jcaSystem.init();
    }

    /**
     * Create a JCA channel for the specified PV
     *
     * @param signalName The name of the PV signal
     */
    @Override
    protected Channel newChannel(final String signalName) {
        return new JcaChannel(signalName, jcaSystem.getJcaContext(), nativeChannelCache);
    }

    /**
     * JcaSystem handles static behavior of Jca channels
     *
     * @return the JCA channel system
     */
    @Override
    protected ChannelSystem channelSystem() {
        return jcaSystem;
    }

    /**
     * print information about this channel factory
     */
    @Override
    public void printInfo() {
        jcaSystem.printInfo();
    }

    @Override
    protected void dispose() {
        //
    }
}
