package xal.rbac;

/**
 * 
 * <code>AccessDeniedException</code> is an exception which is thrown when the user does not have permission to executed
 * the action, because he is no longer logged in.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * 
 */
public class AccessDeniedException extends Exception {

	private static final long serialVersionUID = -304684912356623808L;

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialised, and may subsequently
     * be initialised by a call to {@link #initCause(Throwable)}.
     * 
     * @param message the detailed message of the exception
     */
	public AccessDeniedException(String message) {
		super(message);
	}

}
