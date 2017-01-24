package xal.plugins.dummyrbac;

import xal.rbac.AccessDeniedException;
import xal.rbac.RBACException;
import xal.rbac.RBACLogin;
import xal.rbac.RBACSubject;

/**
 * Dummy RBAC Login extends {@link RBACLogin} returning {@link DummyRbacSubject} immediately when authenticate is called.
 * 
 * @version 0.1 28 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
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
