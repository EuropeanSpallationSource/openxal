package xal.ca;

/*
 * BatchConnectionRuntimeException.java
 *
 * Created on Aug 5, 2021, 11:03 AM
 */
/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class BatchConnectionRuntimeException extends RuntimeException {

    /**
     * Creates new <code>BatchConnectionRuntimeException</code> without detail
     * message.
     */
    public BatchConnectionRuntimeException() {
    }

    /**
     * Constructs an <code>BatchConnectionRuntimeException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BatchConnectionRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Constructs an <code>BatchConnectionRuntimeException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BatchConnectionRuntimeException(String msg, Exception exception) {
        super(msg, exception);
    }
}
