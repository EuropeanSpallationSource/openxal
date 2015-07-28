package xal.extension.application.rbac;

import xal.extension.application.dummyrbac.DummyRbacLogin;
import xal.rbac.RBACLogin;


public class RBACPlugin{

    public static RBACLogin getRBACLoginInstance(){
        return new DummyRbacLogin();
    }

    
}
