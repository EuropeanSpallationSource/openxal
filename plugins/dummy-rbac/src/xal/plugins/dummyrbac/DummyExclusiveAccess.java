package xal.plugins.dummyrbac;

import java.sql.Date;

import xal.rbac.AccessDeniedException;
import xal.rbac.ExclusiveAccess;
import xal.rbac.RBACException;

/**
 * Dummy exclusive access extends {@link ExclusiveAccess}.
 * 
 * @version 0.1 28 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class DummyExclusiveAccess extends ExclusiveAccess{

   
    protected DummyExclusiveAccess(String resource, String permission, int expirationDate) {
        super(resource, permission, new Date(expirationDate * 60 * 1000 + System.currentTimeMillis()));
    }

    @Override
    public void releaseExclusiveAccess() throws AccessDeniedException, RBACException {
        return;
    }

}
