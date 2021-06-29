/*
 * TimeAdaptor.java
 *
 * Created on August 27, 2002, 9:25 AM
 */
package xal.ca;

import java.math.BigDecimal;

/**
 *
 * @author tap
 */
public interface TimeAdaptor extends StatusAdaptor {

    /**
     * Time stamp in seconds since the epoch used by Java
     *
     * @return timestamp
     */
    public BigDecimal getTimestamp();
}
