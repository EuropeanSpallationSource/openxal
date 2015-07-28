package openxal.extension.application.dummyrbac;

import xal.extension.application.rbac.AccessDeniedException;
import xal.extension.application.rbac.RBACException;
import xal.extension.application.rbac.RBACLogin;
import xal.extension.application.rbac.RBACSubject;

public class DummyRbacLogin extends RBACLogin{
    
    @Override
    public String[] getRolesForUser(String username) throws RBACException {
        return null;
        
    }

    @Override
    public RBACSubject authenticate(String username, char[] password) throws AccessDeniedException, RBACException {
        return new DummyRbacSubject();
    }

    @Override
    public RBACSubject authenticate(String username, char[] password, String preferredRole)
            throws AccessDeniedException, RBACException {
        return new DummyRbacSubject();
    }

    @Override
    public RBACSubject authenticate(String username, char[] password, String preferredRole, String ip)
            throws AccessDeniedException, RBACException {
        return new DummyRbacSubject();
    }

}
