/*
 * OracleDatabaseAdaptor.java
 *
 * Created on Wed Feb 18 14:02:59 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.plugin.oracle;

import xal.tools.database.*;

import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.sql.*;
// default to SQL Array instead of java.lang.reflect.Array
import java.sql.Array;

/**
 * OracleDatabaseAdaptor is a concrete subclass of DatabaseAdaptor for
 * implementing methods specifically for the Oracle database. Oracle specific
 * classes are reflected because the default Open XAL distribution does not
 * include the Oracle driver which is not available for distribution. While this
 * code will compile as is, users must download the driver from Oracle and
 * install it if they want to use this adaptor.
 *
 * @author tap
 */
public class OracleDatabaseAdaptor extends DatabaseAdaptor {

    /**
     * Table of cached array descriptors keyed by type. The value class is
     * actually oracle.sql.ArrayDescriptor, but Object is used since the Oracle
     * driver is reflected.
     */
    private final Map<String, Object> ARRAY_DESCRIPTOR_TABLE;

    /**
     * Public Constructor
     */
    public OracleDatabaseAdaptor() {
        ARRAY_DESCRIPTOR_TABLE = new HashMap<String, Object>();
    }

    /**
     * Fetch the list of nontrivial schemas.
     *
     * @param connection database connection
     * @return list of nontrivial schema names
     */
    public List<String> fetchNontrivialSchemas(final Connection connection) throws DatabaseException {
        try {
            final PreparedStatement statement = connection.prepareStatement("select owner from all_tables group by owner order by owner");
            final ResultSet resultSet = statement.executeQuery();
            final List<String> schemas = new ArrayList<String>();
            while (resultSet.next()) {
                final String schema = resultSet.getString(1);
                schemas.add(schema);
            }
            return schemas;
        } catch (SQLException exception) {
            throw new DatabaseException("Exception Fetching nontrivial schemas.", this, exception);
        }
    }

    /**
     * Instantiate an empty Blob.
     *
     * @param connection the database connection
     * @return a new instance of a Blob appropriate for this adaptor.
     */
    public Blob newBlob(final Connection connection) {

        try {
            // reflection for:
            final Class<?> blobClass = Class.forName("oracle.sql.BLOB");
            final Field durationSessionField = blobClass.getDeclaredField("DURATION_SESSION");
            // get the value of the static field
            final int durationSession = durationSessionField.getInt(null);
            // arrays (used here as argument) not compatible with Generics
            @SuppressWarnings("rawtypes")
            final Method createMethod = blobClass.getMethod("createTemporary", new Class[]{Connection.class, Boolean.TYPE, Integer.TYPE});

            return (Blob) createMethod.invoke(null, connection, true, durationSession);
        } catch (Exception exception) {
            if (exception instanceof SQLException) {
                throw new DatabaseException("Exception generating an SQL array.", this, (SQLException) exception);
            } else {
                throw new RuntimeException("Exception instantiating a Blob in OracleDatabaseAdaptor.", exception);
            }
        }
    }

    /**
     * Get an SQL Array given an SQL array type, connection and a primitive
     * array
     *
     * @param type An SQL array type identifying the type of array
     * @param connection An SQL connection
     * @param array The primitive Java array
     * @return the SQL array which wraps the primitive array
     * @throws xal.tools.database.DatabaseException if a database exception is
     * thrown
     */
    public Array getArray(final String type, final Connection connection, final Object array) throws DatabaseException {
        try {
            final Object descriptor = getArrayDescriptor(type, connection);
            // reflection for
            final Class<?> arrayDescriptorClass = Class.forName("oracle.sql.ArrayDescriptor");
            final Class<?> arrayClass = Class.forName("oracle.sql.ARRAY");

            // arrays (used here as argument) not compatible with Generics
            @SuppressWarnings("rawtypes")
            final Constructor<?> arrayConstructor = arrayClass.getConstructor(new Class[]{arrayDescriptorClass, Connection.class, Object.class});
            return (Array) arrayConstructor.newInstance(descriptor, connection, array);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error instantiating an SQL array of type: " + type, exception);
            if (exception instanceof SQLException) {
                throw new DatabaseException("Exception generating an SQL array.", this, (SQLException) exception);
            } else {
                throw new RuntimeException("Exception instantiating a Blob in OracleDatabaseAdaptor.", exception);
            }
        }
    }

    /**
     * Get the array descriptor for the specified array type
     *
     * @param type An SQL array type
     * @param connection A database connection
     * @return the array descriptor for the array type
     * @throws java.sql.SQLException if a database exception is thrown
     */
    // arrays are not compatible with Generics
    @SuppressWarnings("rawtypes")        
    private Object getArrayDescriptor(final String type, final Connection connection) throws Exception {
        // reflection for:
        if (ARRAY_DESCRIPTOR_TABLE.containsKey(type)) {
            return ARRAY_DESCRIPTOR_TABLE.get(type);
        } else {
            // reflection for
            final Class<?> arrayDescriptorClass = Class.forName("oracle.sql.ArrayDescriptor");
            final Method createMethod = arrayDescriptorClass.getMethod("createDescriptor", new Class[]{String.class, Connection.class});
            final Object descriptor = createMethod.invoke(null, type, connection);

            ARRAY_DESCRIPTOR_TABLE.put(type, descriptor);
            return descriptor;
        }
    }
}
