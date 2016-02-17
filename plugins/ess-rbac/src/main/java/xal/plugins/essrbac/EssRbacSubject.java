package xal.plugins.essrbac;

import java.util.Map;

import se.esss.ics.rbac.access.SecurityCallbackAdapter;
import se.esss.ics.rbac.access.SecurityFacade;
import se.esss.ics.rbac.access.SecurityFacadeException;
import se.esss.ics.rbac.access.Token;
import xal.rbac.AccessDeniedException;
import xal.rbac.AutoLogoutCallback;
import xal.rbac.ExclusiveAccess;
import xal.rbac.RBACException;
import xal.rbac.RBACSubject;

/**
 * Implementation of {@link RBACSubject}. Basically just a wrapper to {@link SecurityFacade} for getting permissions,
 * and log out.
 * 
 * @version 0.2 28 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class EssRbacSubject implements RBACSubject {

    private Token token;

    
    /**
     * Constructor
     * 
     * @param token of the authenticated user.
     */
    public EssRbacSubject(Token token) {
        this.token = token;
    }

    @Override
    public void logout() throws RBACException {
        try {
            SecurityFacade.getDefaultInstance().logout();
            this.token = null;
        } catch (SecurityFacadeException e) {
            throw new RBACException(e.getMessage());
        }

    }

    @Override
    public boolean hasPermission(final String resource, final String permission) throws AccessDeniedException,
            RBACException {
        try {
            return SecurityFacade.getDefaultInstance().hasPermission(resource, permission);
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            throw new AccessDeniedException(e.getMessage());
        } catch (SecurityFacadeException e) {
            throw new RBACException(e.getMessage());
        }
    }

    @Override
    public Map<String, Boolean> hasPermissions(final String resource, final String... permissions)
            throws AccessDeniedException, RBACException {
        try {
            return SecurityFacade.getDefaultInstance().hasPermissions(resource, permissions);
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            throw new AccessDeniedException(e.getMessage());
        } catch (SecurityFacadeException e) {
            throw new RBACException(e.getMessage());
        }
    }

    @Override
    public ExclusiveAccess requestExclusiveAccess(final String resource, final String permission,
            final int durationInMinutes) throws AccessDeniedException, RBACException {
        try {
            return new EssExclusiveAccess(SecurityFacade.getDefaultInstance().requestExclusiveAccess(resource,
                    permission, durationInMinutes));
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            throw new AccessDeniedException(e.getMessage());
        } catch (SecurityFacadeException e) {
            throw new RBACException(e.getMessage());
        }
    }

    @Override
    public void setAutoLogoutTimeout(final int timeoutInMinutes, final AutoLogoutCallback callback) {
        SecurityFacade.getDefaultInstance().setAutoLogoutTimeout(timeoutInMinutes);
        final EssRbacSubject subject = this;
        SecurityFacade.getDefaultInstance().setDefaultSecurityCallback(new SecurityCallbackAdapter() {//User is already logged in so we don't need get credentials method.
            @Override
            public boolean autoLogoutConfirm(Token token, int timeoutInSeconds) {
                return callback.autoLogoutConfirm(subject, timeoutInSeconds);
            }
        });

    }

    @Override
    public void updateLastAction() {
        try {
            this.token = SecurityFacade.getDefaultInstance().renewToken();
        } catch (SecurityFacadeException e) {
            e.printStackTrace();
        }
    }

    public Token getToken() {
        return token;
    }

}
