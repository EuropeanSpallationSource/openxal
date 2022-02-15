package xal.model.xml;

import xal.XalException;

/**
 * Encapsulates description of error encountered parsing a <code>Lattice</code>.
 *
 * @author Craig McChesney
 * @version $id:
 *
 */
public class ParsingException extends XalException {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates exception with detail message.
     *
     * @param msg description of error
     */
    public ParsingException(String msg) {
        super(msg);
    }

    /**
     * Create a new <code>ParsingException</code> object which is spawned (in
     * principle) by the given exception object.
     *
     * @param excSrc originating cause for the exception
     *
     */
    public ParsingException(Throwable excSrc) {
        super(excSrc);
    }

}
