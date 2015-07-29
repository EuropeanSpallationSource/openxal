package xal.plugins.essrbac;

import java.util.Map;

import se.esss.ics.rbac.access.SecurityFacade;
import se.esss.ics.rbac.access.SecurityFacadeException;
import xal.rbac.AccessDeniedException;
import xal.rbac.AutoLogoutCallback;
import xal.rbac.ExclusiveAccess;
import xal.rbac.RBACException;
import xal.rbac.RBACSubject;


/**
 * Implementation of {@link RBACSubject}.
 * Basically just a wrapper to {@link SecurityFacade} for getting permissions, and log out.
 * 
 * @version 0.2 28 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class EssRbacSubject implements RBACSubject {
    

 
    @Override
    public void logout() throws RBACException {
        try {
            SecurityFacade.getDefaultInstance().logout();
        } catch (SecurityFacadeException e) {
            e.printStackTrace();
            throw new RBACException("Error logging out");
        }

    }

    @Override
    public boolean hasPermission(final String resource,final String permission) throws AccessDeniedException, RBACException {
        try {
            return SecurityFacade.getDefaultInstance().hasPermission(resource, permission);
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            e.printStackTrace();
            throw new AccessDeniedException("User logged out.");
        } catch (SecurityFacadeException e) {
            e.printStackTrace();
            throw new RBACException("Error getting permission.");
        }
    }

    @Override
    public Map<String, Boolean> hasPermissions(final String resource,final String... permissions) throws AccessDeniedException,
            RBACException {
        try {
            return SecurityFacade.getDefaultInstance().hasPermissions(resource, permissions);
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            e.printStackTrace();
            throw new AccessDeniedException("User logged out.");
        } catch (SecurityFacadeException e) {
            e.printStackTrace();
            throw new RBACException("Error getting permissions.");
        }
    }

    @Override
    public ExclusiveAccess requestExclusiveAccess(final String resource,final String permission,final int durationInMinutes)
            throws AccessDeniedException, RBACException {
            try {
                   return new EssExclusiveAccess(SecurityFacade.getDefaultInstance().requestExclusiveAccess(resource, permission, durationInMinutes));
            } catch (se.esss.ics.rbac.access.AccessDeniedException e) {//TODO check SecurityFacade.getDefaultInstance().requestExclusiveAccess throws. (Documentation might not be correct)
                e.printStackTrace();
                throw new AccessDeniedException("User logged out.");
            } catch (IllegalArgumentException | SecurityFacadeException e) {
                e.printStackTrace();
                throw new RBACException("Error getting exclusive acceess.");
            }
    }

    @Override
    public void setAutoLogoutTimeout(final int timeoutInMinutes,final AutoLogoutCallback callback) {
        SecurityFacade.getDefaultInstance().setAutoLogoutTimeout(timeoutInMinutes);
    }

    @Override
    public void updateLastAction() {
        try {
            SecurityFacade.getDefaultInstance().renewToken();
        } catch (SecurityFacadeException e) {
            e.printStackTrace();
        }
    }

}
