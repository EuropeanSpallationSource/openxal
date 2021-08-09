/*
 * PostgresDatabaseAdaptor.java
 *
 * Created on Sat Feb 09
 *
 */
package xal.plugin.postgres;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xal.tools.database.ConnectionDictionary;
import xal.tools.database.DatabaseAdaptor;
import xal.tools.database.DatabaseException;

public class PostgresDatabaseAdaptor extends DatabaseAdaptor {

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
    @Override
    public Array getArray(final String type, final Connection connection, final Object array) throws DatabaseException {
        List<Object> objectList = new ArrayList<>();

        if (array instanceof byte[]) {
            byte[] a = (byte[]) array;
            objectList.addAll(Arrays.asList(a));
        } else if (array instanceof short[]) {
            short[] a = (short[]) array;
            objectList.addAll(Arrays.asList(a));
        } else if (array instanceof int[]) {
            int[] a = (int[]) array;
            objectList.addAll(Arrays.asList(a));
        } else if (array instanceof long[]) {
            long[] a = (long[]) array;
            objectList.addAll(Arrays.asList(a));
        } else if (array instanceof float[]) {
            float[] a = (float[]) array;
            objectList.addAll(Arrays.asList(a));
        } else if (array instanceof double[]) {
            double[] a = (double[]) array;
            objectList.addAll(Arrays.asList(a));
        } else if (array instanceof boolean[]) {
            boolean[] a = (boolean[]) array;
            objectList.addAll(Arrays.asList(a));
        } else if (array instanceof char[]) {
            char[] a = (char[]) array;
            objectList.addAll(Arrays.asList(a));
        } else {
            Object[] a = (Object[]) array;
            objectList.addAll(Arrays.asList(a));
        }

        Object[] newArray = objectList.toArray(new Object[0]);

        try {
            return connection.createArrayOf(type, newArray);
        } catch (SQLException exception) {
            throw new DatabaseException("Exception generating an SQL array of type: " + type, this, exception);
        }
    }

    @Override
    public Connection getConnection(String urlSpec, String user, String password)
            throws DatabaseException {
        Connection conn = super.getConnection(urlSpec, user, password);
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseException("Unable to disable autocommit", this, e);
        }
        return conn;
    }

    @Override
    public Connection getConnection(ConnectionDictionary dictionary)
            throws DatabaseException {
        Connection conn = super.getConnection(dictionary);
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseException("Unable to disable autocommit", this, e);
        }
        return conn;
    }
}
