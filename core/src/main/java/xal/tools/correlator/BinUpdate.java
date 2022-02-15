/*
 * BinUpdate.java
 *
 * Created on June 27, 2002, 10:11 AM
 */
package xal.tools.correlator;

/**
 *
 * @author tap
 * @version
 */
interface BinUpdate<T> {

    public void newEvent(final String name, final T aRecord, final double timestamp);
}
