package openxal.extension.application.rbac;

import openxal.extension.application.dummyrbac.DummyRbacLogin;
import xal.extension.application.rbac.RBACLogin;


public class RBACPlugin{

    public RBACLogin getRBACLoginInstance(){
        return new DummyRbacLogin();
    }

    
}
