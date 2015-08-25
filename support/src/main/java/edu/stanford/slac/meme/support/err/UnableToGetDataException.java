//-*-jde-*/
package edu.stanford.slac.meme.support.err;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.Status;

/**
 * UnableToGetDataException indicates that an MEME service was not able successfully satisfy a request for data.
 *
 * Specifically, use of this Exception helps an MEME service satisfy the MEME rule that an unsuccessful request for data
 * MUST finally result in the request method throwing an RPCRequestExcption whose text indicates that that specific
 * service request method invocation was "Unable to get data". An alternative to use of this general
 * UnableToGetDataException would be to use its parent AidaRPCRequestException with an unable to get data message
 * specific to the kind of data being acquired, like "Unable to get archive data".
 *
 * @author Greg White, greg@slac.stanford.edu, SLAC
 * @version 23/Oct/2013, original version
 */
public class UnableToGetDataException extends RPCRequestException {

    private static final long serialVersionUID = 2175761868356771465L;
    private static final String _Msg = "Unable to get data";
    private static final String _msgWithKind = "Unable to get %s data";

    public UnableToGetDataException() {
        super(Status.StatusType.ERROR, _Msg);
    }

    /**
     * Suitable for MEME internally generated error.
     */
    public UnableToGetDataException(String message) {
        super(Status.StatusType.ERROR, message);
    }

    /**
     * Suitable for MEME internally generated error.
     */
    public UnableToGetDataException(Status status, String message) {
        super(status.getType(), message);
    }

    /**
     * Suitable for request method
     */
    public UnableToGetDataException(Throwable cause) {
        super(Status.StatusType.ERROR, _Msg + "; "
                + (cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName()), cause);
    }

    /**
     * Suitable for request method
     */
    public UnableToGetDataException(Status status, Throwable cause) {
        super(status.getType(), _Msg + status.getMessage() + "; "
                + (cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName()), cause);
    }

    /**
     * Suitable for request method
     */
    public UnableToGetDataException(String message, Throwable cause) {
        super(Status.StatusType.ERROR, message + "; "
                + (cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName()), cause);
    }

    /**
     * Suitable for request method
     */
    public UnableToGetDataException(Status status, String message, Throwable cause) {
        super(status.getType(), message + " - " + status.getMessage() + "; "
                + (cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName()), cause);
    }

}
