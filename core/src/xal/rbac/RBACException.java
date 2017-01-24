package xal.rbac;

/**
 * This exception is thrown if there is an error while using {@link RBACLogin} or {@link RBACSubject}.
 * 
 * @author <a href="mailto:ivo.list@cosylab.com">Ivo List</a>
 */
public class RBACException extends Exception {

	private static final long serialVersionUID = -7943743107941109377L;

	/**
     * Constructs a new exception with the specified detail message. The cause is not initialised, and may subsequently
     * be initialized by a call to {@link #initCause(Throwable)}.
     * 
     * @param message the detailed message of the exception
     */
	public RBACException(String message) {
		super(message);
	}
}
