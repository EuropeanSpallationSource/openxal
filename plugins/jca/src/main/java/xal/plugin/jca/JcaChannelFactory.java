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
 * @author  tap
 */
public class JcaChannelFactory extends ChannelFactory {
	/** JCA channel system */
	final private JcaSystem JCA_SYSTEM;
	
	/** cache of native JCA channels */
	final private JcaNativeChannelCache NATIVE_CHANNEL_CACHE;
	
	
	/** Constructor */
	public JcaChannelFactory() {
                  // If the property "jca.use_env" is not defined in the command line, 
                  // it will try to get it from Open XAL preferences. By default it will be true.
                  if (System.getProperty("jca.use_env") == null) {
                      java.util.prefs.Preferences defaults = Preferences.nodeForPackage(JcaChannelFactory.class);
                      Boolean jca_use_env = defaults.getBoolean("jca.use_env", true);
                      System.setProperty("jca.use_env", jca_use_env.toString());
                  }
                  if (Boolean.getBoolean("jca.use_env")){
                      Logger.getLogger(JcaChannelFactory.class.getName()).info("Using environment variables for EPICS configuration.");
                  } else {
                      Logger.getLogger(JcaChannelFactory.class.getName()).info("Using JCALibrary.properties for EPICS configuration.");
                  }
		JCA_SYSTEM = new JcaSystem();
		NATIVE_CHANNEL_CACHE = new JcaNativeChannelCache( JCA_SYSTEM );
	}
	
	
	/**
	 * Initialize the channel system
	 * @return true if the initialization was successful and false if not
	 */
	public boolean init() {
		return JCA_SYSTEM.init();
	}
	
	
    /** 
	 * Create a JCA channel for the specified PV
	 * @param signalName The name of the PV signal
	 */
    protected Channel newChannel( final String signalName ) {
        return new JcaChannel( signalName, JCA_SYSTEM.getJcaContext(), NATIVE_CHANNEL_CACHE );
    }
    
    
    /** 
	 * JcaSystem handles static behavior of Jca channels 
	 * @return the JCA channel system
	 */
    protected ChannelSystem channelSystem() {
        return JCA_SYSTEM;
    }
    
    
    /** print information about this channel factory */
    public void printInfo() {
        JCA_SYSTEM.printInfo();
    }
}
