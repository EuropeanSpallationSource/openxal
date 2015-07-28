package openxal.extension.application.dummyrbac;

import xal.extension.application.rbac.AutoLogoutCallback;
import xal.extension.application.rbac.RBACSubject;

public class DummyAutoLogoutCallback implements AutoLogoutCallback{

    public boolean autoLogoutConfirm(RBACSubject subject, int timeoutInSeconds) {
        return false;
    }

}
