package xal.extension.application.rbac;

/**
 * Provides callback for AutoLogout functionality
 * @author <a href="mailto:ivo.list@cosylab.com">Ivo List</a>
 *
 */
public interface AutoLogoutCallback {

    /**
     * Called when the RBAC attempts to automatically logout the user due to inactivity. The method should return
     * <code>true</code> if the facade should logout the user or <code>false</code> if the user should remain logged in.
     * If there is no response to this call within <code>timeoutInSeconds</code>, the RBAC should assume the answer was
     * <code>true</code> .
     * 
     * @param subject the subject that will be logged out
     * @param timeoutInSeconds the timeout in seconds after which RBAC will assume the answer was true
     * @return true if the user can be loggedout or false otherwise
     */
    boolean autoLogoutConfirm(RBACSubject subject, int timeoutInSeconds);
}
