//-*-jde-*-
package edu.stanford.slac.meme.support.err;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.Status;

/**
 * MEMERPCRequestException can be used to implement the MEME error handling and logging standard rules. Specifically
 * AidaRPCRequestException offers constructors that put together the message text given, plus the message text of a
 * given "cause" Throwable also given, inroder to construct a single message that says the first was caused by the
 * second. This "chained excpetion" text is a required feature of MEME services.
 *
 * @author Greg White, greg@slac.stanford.edu, SLAC
 * @version 23/Oct/2013, original version
 */
public class MEMERequestException extends RPCRequestException {
    public MEMERequestException(String message) {
        super(Status.StatusType.ERROR, message);
    }

    public MEMERequestException(Status status, String message) {
        super(status.getType(), message);
    }

    public MEMERequestException(String message, Throwable cause) {
        super(Status.StatusType.ERROR, message + "; " + cause.getMessage(), cause);
    }

    public MEMERequestException(Status status, Throwable cause) {
        super(status.getType(), status.getMessage() + "; " + cause.getMessage(), cause);
    }
}
