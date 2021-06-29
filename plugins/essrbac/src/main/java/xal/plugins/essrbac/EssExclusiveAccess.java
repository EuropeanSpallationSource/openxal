package xal.plugins.essrbac;

import java.util.logging.Level;
import java.util.logging.Logger;
import se.esss.ics.rbac.access.SecurityFacade;
import se.esss.ics.rbac.access.SecurityFacadeException;
import xal.rbac.AccessDeniedException;
import xal.rbac.ExclusiveAccess;
import xal.rbac.RBACException;

/**
 * EssExclusiveAccess implements
 * {@link se.esss.ics.rbac.access.ExclusiveAccess}. Basically just a wrapper for
 * {@link ExclusiveAccess} and {@link SecurityFacade}.
 *
 * @version 0.1 27 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class EssExclusiveAccess extends ExclusiveAccess {

    private static final Logger LOGGER = Logger.getLogger(EssExclusiveAccess.class.getName());

    public EssExclusiveAccess(final se.esss.ics.rbac.access.ExclusiveAccess exclusiveAccess) {
        super(exclusiveAccess.getResource(), exclusiveAccess.getPermission(), exclusiveAccess.getExpirationDate());
    }

    @Override
    public void releaseExclusiveAccess() throws AccessDeniedException, RBACException {
        try {
            SecurityFacade.getDefaultInstance().releaseExclusiveAccess(getResource(), getPermission());
        } catch (se.esss.ics.rbac.access.AccessDeniedException e) {
            LOGGER.log(Level.SEVERE, "User not loged in.", e);
            throw new AccessDeniedException("User not loged in.");
        } catch (SecurityFacadeException e) {
            LOGGER.log(Level.SEVERE, "Error releasing exclusive access", e);
            throw new RBACException("Error releasing exclusive access");
        }
    }

}
