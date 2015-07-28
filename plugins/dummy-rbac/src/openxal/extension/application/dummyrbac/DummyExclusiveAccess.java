package openxal.extension.application.dummyrbac;

import xal.extension.application.rbac.AccessDeniedException;
import xal.extension.application.rbac.ExclusiveAccess;
import xal.extension.application.rbac.RBACException;

public class DummyExclusiveAccess extends ExclusiveAccess{

    protected DummyExclusiveAccess(String resource, String permission, long expirationDate) {
        super(resource, permission, expirationDate);
    }

    @Override
    public void releaseExclusiveAccess() throws AccessDeniedException, RBACException {
        
    }

}
