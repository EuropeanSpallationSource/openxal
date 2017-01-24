package xal.plugins.dummyrbac;

import java.util.HashMap;
import java.util.Map;

import xal.rbac.AccessDeniedException;
import xal.rbac.AutoLogoutCallback;
import xal.rbac.ExclusiveAccess;
import xal.rbac.RBACException;
import xal.rbac.RBACSubject;
import xal.rbac.RBACUserInfo;

/**
 * Dummy Rbac Subject implements {@link RBACSubject} returning true for all permissions.
 * 
 * @version 0.1 28 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class DummyRbacSubject implements RBACSubject {
	
	private final static String DUMMY_USERNAME = "dummy_user";
	
    DummyRbacSubject() {
    }

    @Override
    public void logout() throws RBACException {
        return;

    }

    @Override
    public boolean hasPermission(String resource, String permission) throws RBACException {
        return true;
    }

    @Override
    public Map<String, Boolean> hasPermissions(String resource, String... permissions) throws AccessDeniedException,
            RBACException {
        Map<String,Boolean> map = new HashMap<String,Boolean>();
        for (String permission : permissions){
            map.put(permission, true);
        }
        return map;
    }

    @Override
    public ExclusiveAccess requestExclusiveAccess(String resource, String permission, int durationInMinutes)
            throws AccessDeniedException, RBACException {
        return new DummyExclusiveAccess(resource,permission,durationInMinutes);
    }

    @Override
    public void setAutoLogoutTimeout(int timeoutInMinutes, AutoLogoutCallback callback) {
        return;
    }

    @Override
    public void updateLastAction() {
        return;
    }
    
    @Override
    public RBACUserInfo getUserInfo() {
		return new RBACUserInfo(DUMMY_USERNAME, null, null);
    }

}
