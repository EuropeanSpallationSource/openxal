package xal.plugins.essrbac;

import se.esss.ics.rbac.access.SecurityFacade;
import se.esss.ics.rbac.access.SecurityFacadeException;
import xal.rbac.AccessDeniedException;
import xal.rbac.ExclusiveAccess;
import xal.rbac.RBACException;

//TODO test
//TODO check if we can use it in rbacService.

/**
 * EssExclusiveAccess implements {@link se.esss.ics.rbac.access.ExclusiveAccess}.
 * Basically just a wrapper for {@link ExclusiveAccess} and {@link SecurityFacade}.
 *  
 * @version 0.1 27 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class EssExclusiveAccess extends ExclusiveAccess {

    protected EssExclusiveAccess(se.esss.ics.rbac.access.ExclusiveAccess exclusiveAccess) {
        super(exclusiveAccess.getResource(), exclusiveAccess.getPermission(), exclusiveAccess.getExpirationDate());
    }

    @Override
    public void releaseExclusiveAccess() throws AccessDeniedException, RBACException {
        try {
            SecurityFacade.getDefaultInstance().releaseExclusiveAccess(getResource(), getPermission());
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            throw new AccessDeniedException("User not loged in.");
        } catch (IllegalArgumentException|SecurityFacadeException e) {
            throw new RBACException("Error releasing exclusive access");
        }
    }

}
