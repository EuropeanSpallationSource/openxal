/*
 * JcaSystem.java
 *
 * Created on August 27, 2002, 2:38 PM
 */

package xal.plugin.jcaserver;

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.TimeoutException;

import java.util.prefs.Preferences;

import xal.ca.ChannelSystem;

/**
 * JcaServerChannelSystem is the same as JcaSystem
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
class JcaServerChannelSystem extends ChannelSystem {

    /** Java Channel Access Context */
    private Context JCA_CONTEXT;

    /** Native Java Channel Access Library */
    private JCALibrary JCA_LIBRARY;

    /** Constructor */
    public JcaServerChannelSystem() {
        this(null);
    }

    public JcaServerChannelSystem(final String contextName) {
        try {
            JCA_LIBRARY = JCALibrary.getInstance();

            final String contextType = (contextName != null) ? contextName : defaultJCAContextType();
            JCA_CONTEXT = JCA_LIBRARY.createContext(contextType);
        } catch (CAException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Determine the user's preferred JCA Context otherwise defaulting to JCALibrary.CHANNEL_ACCESS_JAVA
     * 
     * @return the string identifying the JCA Context to use
     */
    static private String defaultJCAContextType() {
        final String userJCAContext = fetchUserJCAContext();
        return getJCAContextType(userJCAContext);
    }

    @Override
    public void setDebugMode(boolean debugFlag) {
        // not used
    }

    /**
     * Determine the user's preferred JCA Context first checking for a Java property, then an environment variable and
     * finally a user preference
     */
    static private String fetchUserJCAContext() {
        // This try should not be required, but is added as a work around for some strange Matlab behavior (jdg,
        // 1/05/05)
        try {
            // first check if the user has set a command line property
            final String contextProperty = System.getProperty("xal.jca.Context");
            if (contextProperty != null) {
                return contextProperty;
            } else {
                // check whether the user has set an environment variable
                final String contextEnvironment = System.getenv("JCA_CONTEXT");
                if (contextEnvironment != null) {
                    return contextEnvironment;
                } else {
                    // check the user's preferences
                    final Preferences prefs = xal.tools.apputils.Preferences.nodeForPackage(JcaServerChannelSystem.class);
                    final String preferredContext = prefs.get("Context", "");
                    return preferredContext;
                }
            }
        } catch (Exception exception) {
            // check if the user has specified a JCA Context to use
            return System.getProperty("xal.jca.Context");
        }
    }

    /**
     * Get the context type given the user specified context type.
     * 
     * @userContextType the user specified context type
     * @return the user context type if not null or empty and JCALibrary.CHANNEL_ACCESS_JAVA otherwise
     */
    private static String getJCAContextType(final String userContextType) {
        return ((userContextType != null) && (userContextType.length() > 0)) ? userContextType
                : JCALibrary.CHANNEL_ACCESS_JAVA;
    }

    @Override
    public void flushIO() {
        try {
            JCA_CONTEXT.flushIO();
        } catch (CAException exception) {
            throw new RuntimeException("Exception flushing IO requests.", exception);
        }
    }

    @Override
    public boolean pendIO(double timeout) {
        try {
            JCA_CONTEXT.pendIO(timeout);
        } catch (CAException exception) {
            return false;
        } catch (TimeoutException exception) {
            return false;
        }

        return true;
    }

    @Override
    public void pendEvent(double timeout) {
        try {
            JCA_CONTEXT.pendEvent(timeout);
        } catch (CAException exception) {
            System.err.println(exception);
        }

    }

    @Override
    public void printInfo() {
        JCA_CONTEXT.printInfo();
    }
}
