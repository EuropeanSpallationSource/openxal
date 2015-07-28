package xal.plugins.dummyrbac;

import java.sql.Date;

import xal.rbac.AccessDeniedException;
import xal.rbac.ExclusiveAccess;
import xal.rbac.RBACException;

public class DummyExclusiveAccess extends ExclusiveAccess{

    protected DummyExclusiveAccess(String resource, String permission, int expirationDate) {
        super(resource, permission, new Date(expirationDate * 60 * 1000 + System.currentTimeMillis()));
    }

    @Override
    public void releaseExclusiveAccess() throws AccessDeniedException, RBACException {
        
    }

}
