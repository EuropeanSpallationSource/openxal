/*
 * ValueAdaptor.java
 *
 * Created on August 26, 2002, 5:25 PM
 */
package xal.plugin.jca;

import xal.ca.ValueAdaptor;
import xal.tools.ArrayValue;

import gov.aps.jca.dbr.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrap a jca.dbr.DBR record for high level access
 *
 * @author tap
 */
class DbrValueAdaptor implements ValueAdaptor {

    private static final Logger LOGGER = Logger.getLogger(DbrValueAdaptor.class.getName());

    protected DBR dbr;

    /**
     * Creates a new instance of ValueAdaptor
     */
    public DbrValueAdaptor(final DBR dbr) {
        this.dbr = dbr;
    }

    /**
     * get the Java class corresponding to the dbr type
     */
    static Class<?> elementType(final DBRType dbrType) {
        if (dbrType.isBYTE()) {
            return Byte.TYPE;
        } else if (dbrType.isENUM()) {
            return Short.TYPE;
        } else if (dbrType.isSHORT()) {
            return Short.TYPE;
        } else if (dbrType.isINT()) {
            return Integer.TYPE;
        } else if (dbrType.isFLOAT()) {
            return Float.TYPE;
        } else if (dbrType.isDOUBLE()) {
            return Double.TYPE;
        } else if (dbrType.isSTRING()) {
            return String.class;
        } else {
            return null;
        }
    }

    /**
     * get the Java class of a single element of the record
     */
    static Class<?> elementType(final DBR record) {
        DBRType dbrType = record.getType();
        return elementType(dbrType);
    }

    /**
     * Get the array value as an ArrayStore. All DBR classes have a value()
     * method but the interface does not since the method returns different
     * array types. So, we use the Selector to call the method.
     */
    @Override
    public ArrayValue getStore() {
        try {
            Object array = dbr.getValue();
            return ArrayValue.arrayValueFromArray(array);
        } catch (IllegalArgumentException excpt) {
            LOGGER.log(Level.SEVERE, null, excpt);
        }

        return null;
    }
}
