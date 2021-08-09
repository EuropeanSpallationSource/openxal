package xal.plugin.mysql;

import xal.tools.database.*;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabaseAdaptor extends DatabaseAdaptor {

    @Override
    public Array getArray(String type, Connection connection, Object array)
            throws DatabaseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Connection getConnection(String urlSpec, String user, String password)
            throws DatabaseException {
        try {
            return DriverManager.getConnection(urlSpec, user, password);
        } catch (SQLException exception) {
            throw new DatabaseException("Exception connecting to the database at URL: \"" + urlSpec + "\" as user: " + user, this, exception);
        }
    }

    /**
     * Fetch all schemas from the connected database. MySQL adaptor returns
     * catalogs instead of schemas.
     *
     * @return list of all schemas in the database
     * @exception DatabaseException
     * @throws xal.tools.database.DatabaseException if the schema fetch fails
     */
    @Override
    public List<String> fetchAllSchemas(final Connection connection) throws DatabaseException {
        try {
            final List<String> schemas = new ArrayList<>();
            final DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet result = metaData.getCatalogs()) {
                while (result.next()) {
                    schemas.add(result.getString("TABLE_CAT"));
                }
            }
            return schemas;
        } catch (SQLException exception) {
            throw new DatabaseException("Database exception while fetching schemas.", this, exception);
        }
    }

    /**
     * Get the result set for tables for the specified meta data and schema.
     * MySQL adaptor uses the catalog in place of schema.
     */
    @Override
    public ResultSet getTablesResultSet(final DatabaseMetaData metaData, final String schema) throws SQLException {
        return metaData.getTables(schema, null, null, null);
    }

    /**
     * Get the result set of columns for the specified meta data, schema and
     * table. MySQL adaptor uses the catalog in place of schema.
     */
    @Override
    public ResultSet getColumnsResultSet(final DatabaseMetaData metaData, final String schema, final String table) throws SQLException {
        return metaData.getColumns(schema, null, table, null);
    }

    /**
     * Get the result set of primary keys for the specified meta data, schema
     * and table. MySQL adaptor uses the catalog in place of schema.
     */
    @Override
    public ResultSet getPrimaryKeysResultSet(final DatabaseMetaData metaData, final String schema, final String table) throws SQLException {
        return metaData.getPrimaryKeys(schema, null, table);
    }
}
