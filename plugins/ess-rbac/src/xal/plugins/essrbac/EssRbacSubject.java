package xal.extension.application.essrbac;

import java.util.Map;

import se.esss.ics.rbac.access.SecurityFacade;
import se.esss.ics.rbac.access.SecurityFacadeException;
import se.esss.ics.rbac.access.Token;
import xal.extension.application.rbac.AccessDeniedException;
import xal.extension.application.rbac.AutoLogoutCallback;
import xal.extension.application.rbac.ExclusiveAccess;
import xal.extension.application.rbac.RBACException;
import xal.extension.application.rbac.RBACSubject;


/**
 * Implementation of {@link RBACSubject}.
 * Basically just a wrapper for {@link SecurityFacade} for getting permissions, and log out.
 * 
 * @version 0.1 27 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class EssRbacSubject implements RBACSubject {
    private final Token token;

    public EssRbacSubject(Token token) {
        this.token = token;
    }

    public void logout() throws RBACException {
        try {
            SecurityFacade.getDefaultInstance().logout();
        } catch (SecurityFacadeException e) {
            throw new RBACException("Error logging out");
        }

    }

    public boolean hasPermission(String resource, String permission) throws AccessDeniedException, RBACException {
        try {
            return SecurityFacade.getDefaultInstance().hasPermission(resource, permission);
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            throw new AccessDeniedException("User not logged in.");
        } catch (SecurityFacadeException e) {
            throw new RBACException("Error getting permission.");
        }
    }

    public Map<String, Boolean> hasPermissions(String resource, String... permissions) throws AccessDeniedException,
            RBACException {
        try {
            return SecurityFacade.getDefaultInstance().hasPermissions(resource, permissions);
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            throw new AccessDeniedException("User not logged in.");
        } catch (SecurityFacadeException e) {
            throw new RBACException("Error getting permissions.");
        }
    }

    public ExclusiveAccess requestExclusiveAccess(String resource, String permission, int durationInMinutes)
            throws AccessDeniedException, RBACException {
            try {
                    return new EssExclusiveAccess(SecurityFacade.getDefaultInstance().requestExclusiveAccess(resource, permission, durationInMinutes));
            } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
                throw new AccessDeniedException("User not logged in.");
            } catch (IllegalArgumentException | SecurityFacadeException e) {
                throw new RBACException("Error getting exclusive acceess.");
            }
    }

    public void setAutoLogoutTimeout(int timeoutInMinutes, AutoLogoutCallback callback) {
        SecurityFacade.getDefaultInstance().setAutoLogoutTimeout(timeoutInMinutes);

    }

    public void updateLastAction() {
        // TODO update
    }
    
    public Token getToken() {
        return token;
    }

}
