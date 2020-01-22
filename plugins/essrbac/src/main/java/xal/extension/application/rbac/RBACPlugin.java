package xal.extension.application.rbac;

import xal.XalException;
import xal.plugins.essrbac.EssRbacLogin;
import xal.plugins.essrbac.DummyRbacLogin;
import xal.rbac.RBACLogin;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;

/**
 * This is a plugin for rbac authentication and authorization. It provides a method for retrieving its RbacLogin, thus
 * making it a runtime dependency. It also includes the RBAC Single Sign On (SSO) server to enable the users to use 
 * a single login for all OpenXal apps.
 *
 * Version 0.2 by Yngve Levinsen. Allows using dummy login based on system preferences.
 *
 * @version 0.2 12 Jan 2017
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class RBACPlugin {

    private static final String USE_RBAC_KEY = "useRbac";

    // Return default settings for RBAC plugin
    static protected java.util.prefs.Preferences getDefaults() {
        return xal.tools.apputils.Preferences.nodeForPackage(RBACPlugin.class);
    }

    /**
     * If the system has configured RBAC to be active, 
     * then return true, otherwise return false.
     *
     * Returning false should imply that we will use dummy RBAC.
     * If nothing has been configured in system, RBAC is used by default.
     *
     * @return use proper RBAC or not
     */
    public static boolean useRBACLogin() {
        java.util.prefs.Preferences defaults = getDefaults();
        return defaults.getBoolean(USE_RBAC_KEY, false);
    }
    
    
    public static void enableRBACLogin() {
        java.util.prefs.Preferences defaults = getDefaults();
        defaults.putBoolean(USE_RBAC_KEY, true);
        try {
            defaults.sync();
        } catch (BackingStoreException ex) {
            Logger.getLogger(RBACPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void disableRBACLogin() {
        java.util.prefs.Preferences defaults = getDefaults();
        defaults.putBoolean(USE_RBAC_KEY, false);
        try {
            defaults.sync();
        } catch (BackingStoreException ex) {
            Logger.getLogger(RBACPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Gets us an instance of {@link EssRbacLogin} and starts the SSO server.
     * The login is still provided even if the SSO server cannot be started.
     *
     * A dummy RBAC is returned in case RBAC is disabled on the system.
     * 
     * @return instance of {@link EssRbacLogin}
     */
    public static RBACLogin getRBACLoginInstance() {
        if (useRBACLogin()) {
            Logger.getLogger("global").log( Level.CONFIG, "Using proper RBAC login..." );
            try {
                SingleSignOnServerManager.startSSO();
            } catch (XalException e) {
                Logger.getLogger("global").log( Level.WARNING, "SSO server couldn't be started: " + e.getMessage());
            }
            return new EssRbacLogin();
        } else {
            Logger.getLogger("global").log( Level.CONFIG, "Using dummy RBAC..." );
            return new DummyRbacLogin();
        }
    }

}
