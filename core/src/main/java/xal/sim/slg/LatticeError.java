/*
 * LatticeError.java
 *
 * Created on March 17, 2003, 1:00 PM
 */
package xal.sim.slg;

/**
 * @author wdklotz
 */
public class LatticeError extends Exception {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_HEADER = "Lattice-Error";
    private final String message;

    /**
     * Creates a new instance of <code>LatticeError</code> without detail
     * message.
     */
    public LatticeError() {
        message = MESSAGE_HEADER;
    }

    /**
     * Constructs an instance of <code>LatticeError</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public LatticeError(String msg) {
        message = MESSAGE_HEADER + ": " + msg;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return toString();
    }
}
