package xal.plugins.essrbac;

import java.util.HashMap;
import java.util.Map;

import xal.rbac.AccessDeniedException;
import xal.rbac.AutoLogoutCallback;
import xal.rbac.ExclusiveAccess;
import xal.rbac.RBACException;
import xal.rbac.RBACSubject;
import xal.rbac.RBACUserInfo;

/**
 * Dummy Rbac Subject implements {@link RBACSubject} returning true for all
 * permissions.
 *
 * @version 0.1 28 Jul 2015
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class DummyRbacSubject implements RBACSubject {

    private static final String DUMMY_USERNAME = "dummy_user";

    DummyRbacSubject() {
    }

    @Override
    public void logout() throws RBACException {
        // Do nothing
    }

    @Override
    public boolean hasPermission(String resource, String permission) throws RBACException {
        return true;
    }

    @Override
    public Map<String, Boolean> hasPermissions(String resource, String... permissions) throws AccessDeniedException,
            RBACException {
        Map<String, Boolean> map = new HashMap<>();
        for (String permission : permissions) {
            map.put(permission, true);
        }
        return map;
    }

    @Override
    public ExclusiveAccess requestExclusiveAccess(String resource, String permission, int durationInMinutes)
            throws AccessDeniedException, RBACException {
        return new DummyExclusiveAccess(resource, permission, durationInMinutes);
    }

    @Override
    public void setAutoLogoutTimeout(int timeoutInMinutes, AutoLogoutCallback callback) {
        // Do nothing
    }

    @Override
    public void updateLastAction() {
        // Do nothing
    }

    @Override
    public RBACUserInfo getUserInfo() {
        return new RBACUserInfo(DUMMY_USERNAME, null, null);
    }

}
