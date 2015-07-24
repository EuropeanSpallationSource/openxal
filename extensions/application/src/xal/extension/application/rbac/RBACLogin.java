package xal.extension.application.rbac;

import java.lang.reflect.Method;


/**
 * <code>RBACLogin</code> is the entry point for authentication of user.
 *
 * @author <a href="mailto:ivo.list@cosylab.com">Ivo List</a>
 */
public abstract class RBACLogin {
	/**
     * Returns the list of all roles for the provided username. {@link RBACException} is thrown in case of an
     * error.
     * 
     * @param username the username of the user to get assigned roles for
     * 
     * @return array of role names assigned to the user
     * 
     * @throws RBACException if there was an error
     */
    public abstract String[] getRolesForUser(String username) throws RBACException;
    
    /**
     * Authenticates the user using username and password. The method returns the Token if authentication was
     * successful or throws a {@link AccessDeniedException} if authentication failed.
     * 
     * @param username that will be used for authentication.
     * @param password that will be used for authentication.
     * 
     * @return RBACSubject class representing logged in user
     * 
     * @throws RBACException if there is an error
     */
    public abstract RBACSubject authenticate(String username, char[] password) throws AccessDeniedException, RBACException;
    
    /**
     * Authenticates the user using username, password and preferred role. The method returns the Token if authentication was
     * successful or throws a {@link AccessDeniedException} if authentication failed.
     * 
     * @param username that will be used for authentication.
     * @param password that will be used for authentication.
     * @param preferredRole the user would prefer to have.
     * 
     * @return RBACSubject class representing logged in user
     * 
     * @throws RBACException if there is an error
     */
    public abstract RBACSubject authenticate(String username, char[] password, String preferredRole) throws AccessDeniedException, RBACException;   
    
    /**
     * Authenticates the user using username and password, preferred role and ip. The method returns the Token if authentication was
     * successful or throws a {@link AccessDeniedException} if authentication failed.
     * 
     * @param username that will be used for authentication.
     * @param password that will be used for authentication.
     * @param preferredRole the user would prefer to have.
     * @param ip address that will be used for authentication.
     * 
     * @return RBACSubject class representing logged in user
     * 
     * @throws RBACException if there is an error
     */
    public abstract RBACSubject authenticate(String username, char[] password, String preferredRole, String ip) throws AccessDeniedException, RBACException;
    
    
    
    /** 
	 * Instantiate a new RBACLogin by calling xal.extensions.application.rbac.RBACPlugin.getRBACLoginInstance()
	 * @return a new RBACLogin
	 */
    static protected RBACLogin newRBACLogin() {
		try {
			// effectively returns ChannelFactoryPlugin.getChannelFactoryInstance()
			final Class<?> pluginClass = Class.forName( "xal.extensions.application.rbac.RBACPlugin" );
			final Method creatorMethod = pluginClass.getMethod( "getRBACLoginInstance" );
			return (RBACLogin)creatorMethod.invoke( null );
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Failed to load the RBACPlugin: " + exception.getMessage() );
		}
    }
    
 }
