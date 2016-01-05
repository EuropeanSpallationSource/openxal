package xal.extension.application.rbac;

import xal.XalException;
import xal.plugins.essrbac.EssRbacLogin;
import xal.rbac.RBACLogin;

/**
 * This is a plugin for rbac authentication and authorization. It provides a method for retrieving its RbacLogin, thus
 * making it a runtime dependency. It also includes the RBAC Single Sign On (SSO) server to enable the users to use 
 * a single login for all OpenXal apps.
 * 
 * @version 0.1 27 Jul 2015
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class RBACPlugin {

    /**
     * Gets us an instance of {@link EssRbacLogin} and starts the SSO server.
     * The login is still provided even if the SSO server cannot be started.
     * 
     * @return instance of {@link EssRbacLogin}
     */
    public static RBACLogin getRBACLoginInstance() {
        try {
            SingleSignOnServerManager.startSSO();
        } catch (XalException e) {
            System.err.println("SSO server couldn't be started: " + e.getMessage());
        }
        return new EssRbacLogin();
    }

}
