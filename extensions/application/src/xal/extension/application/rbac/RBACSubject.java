package xal.extension.application.rbac;

import java.util.Map;


/**
 * <code>RBACSubject</code> is the main entry point authorization of all actions. It represent a logged in user and 
 * is returned by {@link RBACLogin} after the user is logged in.
 * 
 *  It also provides a mechanism for autologout of the user. 
 *
 * @author <a href="mailto:ivo.list@cosylab.com">Ivo List</a>
 */
public interface RBACSubject {
    /**
     * Logs the user out. If there was an error during the logout a {@link RBACException} is thrown.
     * 
     * @throws RBACException if there was an error while logout.
     */
    void logout() throws RBACException;

    
    /**
     * Checks if the logged in user is granted the permission provided as parameter. If the access is granted, 
     * method returns <code>true</code>, if not it returns <code>false</code>. If there was an
     * error a {@link RBACException} is thrown.
     * 
     * @param resource the name of the resource
     * @param permission the name of the permission
     * 
     * @return <code>true</code> if the user has the specified permission
     * 
     * @throws RBACException if there was an error
     * @throws AccessDeniedException 
     */
    boolean hasPermission(String resource, String permission) throws RBACException, AccessDeniedException;

    /**
     * Checks if the logged in user is granted the permissions provided as parameters.
     * Method returns a map of permission-grant pairs. For every permission, which was granted, value true is returned;
     * for every permission, which was denied, value false is returned. {@link RBACException} is thrown in
     * case of an error.
     * 
     * @param resource the name of the resource
     * @param permissions the names of the permission
     * 
     * @return map of permission name - permission grant pairs.
     * 
     * @throws AccessDeniedException if the user was logged out
     * @throws RBACException if token is missing, or if there was an error while reading or connecting to web
     *             services.
     */
    Map<String, Boolean> hasPermissions(String resource, String... permissions) throws AccessDeniedException,
            RBACException;

    /**
     * Requests exclusive access to the specified permission for the currently logged in user, on the specified
     * resource. If the access is granted, method returns ExclusivAccess object. If the access was not granted 
     * {@link AccessDeniedException} is thrown. If there was an error {@link RBACException} is thrown.
     * 
     * @param resource name of the resource
     * @param permission name of the permission
     * @param durationInMinutes the duration of exclusive access in minutes, if less than 1 minute, default value will
     *            be used (defined by the service)
     * 
     * @return ExclusiveAccess containing information about the requested permission and the expiration date of the
     *         exclusive access, if the request was successful.
     * 
     * @throws AccessDeniedException if the access was not granted
     * @throws RBACException if there was an error
     */
    ExclusiveAccess requestExclusiveAccess(String resource, String permission, int durationInMinutes)
            throws AccessDeniedException, RBACException;

    /**
     * Sets the auto logout timeout in minutes. If there was no user activity for the specified duration the facade will
     * notify the user and request confirmation through {@link AutoLogoutCallback}. Based on
     * the response or if there is no response for a specific duration, the user will be logged out.
     * 
     * @param timeoutInMinutes the timeout in minutes after which the user will be logged out if inactive
     * @param callback handler called before subject is logged out 
     */
    void setAutoLogoutTimeout(int timeoutInMinutes, AutoLogoutCallback callback);

    /**
     * Update the last action time to now to prevent auto logout.
     */
    void updateLastAction();
}
