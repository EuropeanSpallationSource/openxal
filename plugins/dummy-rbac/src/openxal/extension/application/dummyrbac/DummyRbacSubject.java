package openxal.extension.application.dummyrbac;

import java.util.HashMap;
import java.util.Map;

import xal.extension.application.rbac.AccessDeniedException;
import xal.extension.application.rbac.AutoLogoutCallback;
import xal.extension.application.rbac.ExclusiveAccess;
import xal.extension.application.rbac.RBACException;
import xal.extension.application.rbac.RBACSubject;

public class DummyRbacSubject implements RBACSubject {

    public DummyRbacSubject() {
    }

    public void logout() throws RBACException {
        return;

    }

    public boolean hasPermission(String resource, String permission) throws RBACException {
        return true;
    }

    public Map<String, Boolean> hasPermissions(String resource, String... permissions) throws AccessDeniedException,
            RBACException {
        Map<String,Boolean> map = new HashMap<String,Boolean>();
        for (String permission : permissions){
            map.put(permission, true);
        }
        return map;
    }

    public ExclusiveAccess requestExclusiveAccess(String resource, String permission, int durationInMinutes)
            throws AccessDeniedException, RBACException {
        return new DummyExclusiveAccess(resource,permission,durationInMinutes);
    }

    public void setAutoLogoutTimeout(int timeoutInMinutes, AutoLogoutCallback callback) {
        return;
    }

    public void updateLastAction() {
        return;

    }

}
