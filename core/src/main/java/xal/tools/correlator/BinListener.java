/*
 * BinListener.java
 *
 * Created on May 5, 2003, 10:26 AM
 */
package xal.tools.correlator;

/**
 *
 * @author tap
 */
public interface BinListener<T> {

    public void newCorrelation(BinAgent<T> sender, Correlation<T> correlation);

    public void willReset(BinAgent<T> sender);
}
