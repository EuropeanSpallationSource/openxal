package xal.extension.application.rbac;

import java.util.Date;

/**
 * 
 * <code>ExclusiveAccess</code> has information about requested exclusive access. It provides the
 * information, which permission was requested and the expiration date, which is when the exclusive access will be
 * automatically released. It also provide a method to release exclusive access earlier.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * @author <a href="mailto:ivo.list@cosylab.com">Ivo List</a>
 * 
 */
public abstract class ExclusiveAccess {
	protected String resource;	
    protected String permission;
    protected Date expirationDate;
    
    /**
     * Construct a new exclusive access.
     * 
     * @param resource the name of the resource that owns the permission
     * @param permission the name of the permission for which exclusive access was requested
     * @param date the expiration date as UTC
     */
    protected ExclusiveAccess(String resource, String permission, Date date) {
        this.resource = resource;
        this.permission = permission;
        this.expirationDate = date;
    }
    
    /**
     * Returns the name of the resource that owns the permission.
     * 
     * @return the resource name
     */
    public String getResource() {
        return resource;
    }

    /**
     * Returns the name of the permission, for which exclusive access was requested.
     * 
     * @return the permission name
     */
    public String getPermission() {
        return permission;
    }

    /**
     * The date when exclusive access expires. After this date, the user no longer has exclusive access.
     * 
     * @return the exclusive access expiration date
     */
    public Date getExpirationDate() {
        return expirationDate;
    }
    
    
    /**
     * Releases this exclusive access. If there was an error a {@link RBACException} is thrown.
     * 
     * @throws AccessDeniedException if the subject is no longer logged in
     * @throws RBACException if token is missing, or if there was an error while reading or connecting to web
     *             services
     */
    public abstract void releaseExclusiveAccess() throws AccessDeniedException, RBACException;
}
