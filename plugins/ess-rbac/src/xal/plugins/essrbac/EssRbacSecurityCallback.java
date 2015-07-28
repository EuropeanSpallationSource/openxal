package xal.plugins.essrbac;

import se.esss.ics.rbac.access.SecurityCallbackAdapter;
import se.esss.ics.rbac.access.Token;
import se.esss.ics.rbac.access.localservice.LocalAuthServiceDetails;


/**
 *EssRbacSecurityCallback extends  
 * @version 0.1 27 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class EssRbacSecurityCallback extends SecurityCallbackAdapter{
    
    public LocalAuthServiceDetails getLocalAuthServiceDetails() {
        return new LocalAuthServiceDetails();
    }
    
    public boolean autoLogoutConfirm(Token token, int timeoutInSeconds) {
        return false;
        //CHECK
    }


}
