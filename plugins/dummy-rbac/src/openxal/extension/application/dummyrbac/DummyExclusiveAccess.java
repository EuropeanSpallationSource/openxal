package openxal.extension.application.dummyrbac;

import java.sql.Date;

import xal.extension.application.rbac.AccessDeniedException;
import xal.extension.application.rbac.ExclusiveAccess;
import xal.extension.application.rbac.RBACException;

public class DummyExclusiveAccess extends ExclusiveAccess{

    protected DummyExclusiveAccess(String resource, String permission, int expirationDate) {
        super(resource, permission, new Date(expirationDate * 60 * 1000 + System.currentTimeMillis()));
    }

    @Override
    public void releaseExclusiveAccess() throws AccessDeniedException, RBACException {
        
    }

}
