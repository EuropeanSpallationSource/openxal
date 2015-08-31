/**
 * opticsServicConnection defines how the Optics service accesses
 * a relational database, such as ORACLE.
 */

package edu.stanford.slac.meme.service.optics;

// Java DataBase Connection (JDBC) and other standard java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

// Oracle's native interface that supports passwordless authentication
// import oracle.jdbc.pool.OracleDataSource;
// EPICS
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;

import edu.stanford.slac.meme.support.err.UnableToGetDataException;

/**
 * opticsServicConnection defines how the Optics service accesses a relational database, such as ORACLE, from which it
 * gets model data.
 *
 * @author Greg White, 3-Sep-2014
 * @version Greg White, 10-Apr-2015: Converted to Postgres from Oracle
 */
public class OpticsServiceConnection {
    private static final Logger logger = Logger.getLogger(OpticsServiceConnection.class.getName());

    // TODO: Externalize and protect access username/passwords and strings

    // Oracle JDBC connection URI and ID stuff.
    //
    private static volatile Connection m_Conn = null; // JDBC connection for queries
    private static final String CONNECTION_URI_DEFAULT = "jdbc:oracle:thin:@youopticss.host.name:1521:YOURDBNAME";
    private static final String NORESULTSETMETADATA = "No ResultSet metadata available, so can not continue to get data";
    private static final String UNABLETOPROCESSSQL = "Unable to execute SQL query successfully";
    private static final String NOMATCH = "No matching model data for query ";
    private static final String WHEN_EXECUTINGSQL = "when executing SQL query \'%s\'. Retrying with new Connection";
    private static final int MAX_RETRIES = 2; // Try a SQL query at most 2 times
    // before reinit and requery.
    /**
     * Initialize for an acquisition.
     * 
     * Note: we use a pattern where the initialization is done on server startup, and the important part (getConnection)
     * can be redone at any time if the connection to the backend optics goes bad.
     */
    OpticsServiceConnection(String service_name) {
        init(service_name);
    }

    /**
     * Init loads JDBC and initializes connection to the db, Oracle in this case.
     */
    private void init(final String service_name) {
        // Load JDBC.
        try {
            logger.info("Initializing " + service_name);

            // Establish connection to the db instance defined in private members.
            getConnection();

        } catch (Throwable ex) {
            RuntimeException runtimeException = new RuntimeException("Failed to initialize service: " + service_name,
                    ex);
            logger.severe(runtimeException.getMessage());
            throw runtimeException;
        }
    }

    /**
     * Initializes a database connection to the Oracle Database.
     * 
     * If the connection is non-null, then the old connection is first closed. This part is include so that this routine
     * can be used to renew a stale connection.
     */
    private synchronized void getConnection() {
        // If we already have a connection dispose of it.
        closeConnection();

        // Having dealt with a possible stale connection, get a new one.
        try {
            // Retrieve db connection configuration from properties, or use
            // defaults if none given.
        	final String url = System.getProperty("CONNECTION_URI_PROPERTY", CONNECTION_URI_DEFAULT);
        	final String user = System.getProperty("CONNECTION_USERID_PROPERTY");
        	final String pwd = System.getProperty("CONNECTION_PWD");

            // Make connection to Database.
            logger.info("Initializing database connection: " + url + " username:" + user);
            m_Conn = DriverManager.getConnection(url, user, pwd);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Failed to initialize connection to database.", ex);
        }
        if (m_Conn != null)
            logger.info("Database connection completed successfully.");
    }

    /**
     * Disposes of existing Db connection.
     */
    private synchronized void closeConnection() {
        // If we have a connection, dispose of it.
        try {
            if (m_Conn != null) {
                if (!m_Conn.isClosed()) {
                    logger.config("Closing connection to database...");
                    m_Conn.close();
                    m_Conn = null;
                }
            }
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Failed to close connection to database.", ex);
        }
    }

    /**
     * Queries the MEME Name Server database with the query in sqlString. This is a wrapper to give appropriate error
     * handling and retry logic.
     * 
     * @param sqlString
     *            The SQL query, in "ascii" (actually UTF-16 or whatever java String is).
     * @param position
     *            The position within a device composed of more than 1 modelled element, for which the model data is
     *            required; regognized values are 1 (for beginning), 2 (for middle), or 3 (for END). A mapping is made
     *            in this method from these 3 values, to any number of elements, 1, 2, 3 or more.
     * @return The ResultSet given by stmt.executeQuery(sqlString)
     * @version 1.0 19-Jun-2005, Greg White
     */
    protected double[] executeQuery(final String sqlString, final Integer position) throws UnableToGetDataException {

    	Statement stmt = null; // Db SQL query object.
    	ResultSet rs = null; // ResultSet receiving SQL results.
    	int nRetries = 0; // Tracks retries of failing queries.
    	boolean bRetry = false; // Whether to retry db query.
    	double[] modelData = null; // Optics data from database as array.
    	String message = null; // Diagnostic messages;

        logger.fine("sqlString: " + sqlString);
        logger.fine("Position: " + position.toString());

        try {
            // Create a jdbc Statement and execute the given
            // query. If the query fails to execute for whatever
            // reason, try to get a new connection and loop -
            // re-creating the statement and re-executing the
            // query. Try up to 3 times.
            do {
                try {
                    // Create a statement with "Scrollable"
                    // ResultSet, as necessary for
                    // processing each column as a unit in
                    // the get method.
                    stmt = m_Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    rs = stmt.executeQuery(sqlString);
                    bRetry = false;
                } catch (Exception ex) {
                    // We encountered an error in the
                    // execution of the sql query, so try to
                    // fix this by getting a new
                    // connection and set logic so we'll go
                    // through the do loop again.
                    message = String.format(WHEN_EXECUTINGSQL, sqlString);
                    if (nRetries < MAX_RETRIES) {
                        logger.warning(ex.getMessage() + ":" + message);
                        getConnection();
                        bRetry = true;
                        nRetries++;
                    } else {
                        bRetry = false;
                        if (ex.getClass().getName() == "java.sql.SQLException")
                            message.concat(": " + ((SQLException) ex).getSQLState());
                        logger.severe(ex.getMessage() + " " + message);
                    }
                }
            } while (bRetry);

            if (rs == null || nRetries >= MAX_RETRIES)
                throw new UnableToGetDataException(UNABLETOPROCESSSQL);

            // Extract the data from the (single column) ResulSet.
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rsmd == null)
                throw new UnableToGetDataException(NORESULTSETMETADATA);

            // Get number of rows in ResultSet. Check if got too few
            // (<1)
            rs.last();
            final int rowsM = rs.getRow();
            if (rowsM < 1)
                throw new UnableToGetDataException(NOMATCH + sqlString);

            // Decide the rowset we're going to return. Normally
            // (for all 0-length devices, and devices in more than 1
            // element whose multi-elementness is handled by the SQL
            // procedure (eg QUADs) only 1 rowset will have been
            // retuned. However, some devices correspond to >1
            // element and the SQL query has returned >1 row. This
            // is normal for KLYS, ACCL etc, corresponding to eg
            // elements K21_1D1, K21_1D2, K21_1D3, K21_1D4. For
            // those, decide by checking the pos argument. The pos
            // default is 3 (for END). For sliced waveguides (whether
            // with 1 or more structures), both the begin and end of
            // each "slice" is returned by the SQL. So we simply
            // choose the middle one.
            //
            Integer m = rowsM; // Default is to take the last returned
            // rowset.
            if (m > 1) {
                // If we got >1 row there was >1 modelled element
                // corresponding to the device.
                switch (position) {
                case 1: // BEG, return the 1st rowset.
                    m = 1;
                    break;
                case 2: // MID, return middle rowset.
                    m = (int) Math.ceil(rowsM / 2.0);
                    break;
                case 3: // END, return the last rowset.
                    m = rowsM;
                    break;
                default:
                    m = rowsM; // shouldn't ever happen
                }
            }
            logger.fine("Advance to ResultSet # " + m.toString());

            // Actually advance to the mth rowset (often just the
            // 1st).
            rs.beforeFirst();
            // Get number of columns in ResultSet
            final int columnsN = rsmd.getColumnCount();
            modelData = new double[columnsN];
            logger.fine("Num Columns:" + columnsN);

            // For each column, extract all the rows of the column
            // from the ResultSet and add the whole column to what
            // we return.
            for (int colj = 1; colj <= columnsN; colj++) {
                modelData[colj - 1] = rs.getDouble(colj);
                logger.fine("Data " + (colj - 1) + " = " + modelData[colj - 1]);
            }
        } catch (Exception ex) {
            throw new UnableToGetDataException(UNABLETOPROCESSSQL, ex);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (Exception ex) {
                logger.warning(ex.getMessage() + " when freeing JDBC resources");
            }
        }

        return modelData;
    }

}
