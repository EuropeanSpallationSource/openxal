package xal.extension.application.rbac;

import xal.plugins.dummyrbac.DummyRbacLogin;
import xal.rbac.RBACLogin;

/**
 * Dummy Rbac plugin called from {@link RBACLogin#newRBACLogin()}
 * 
 * @version 0.1 28 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class RBACPlugin {

    public static RBACLogin getRBACLoginInstance() {
        return new DummyRbacLogin();
    }

}
