/**
 * RdbServiceConnection manages the JDBC connection for rdbService,
 * the server side of an EPICS V4 service for accessing a relational
 * database, such as ORACLE.
 *
 * @author  Greg White, 2-Sep-2014
 * @version Greg White 04-Mar-2015
 *          Make queryname search case insensitive; and replace split at '/' with 
 *          with split at last ':' to conform to SLAC naming (maybe also ESS naming!). 
 * @todo: Remove check that a given name has syntax inst/attr, since that is a leftover
 * of AIDA syntax. Fixing this means moving the rdb names of out the MEME_NAMES table
 * and into their own table - one that doesn't distinguish instance and attr, or simply
 * into a file loaded at runtime.
 */
package edu.stanford.slac.meme.service.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import edu.stanford.slac.meme.support.sys.MemeConstants;

/**
 * RdbServiceConnection implements JDBC connection logic.
 *
 * @author Greg White, 2-Sep-2014
 * @author Blaž Kranjc 30-Aug-2015
 */
public class RdbServiceConnection {
	private static final Logger logger = Logger.getLogger(RdbServiceConnection.class.getName());

	// Oracle JDBC connection URI and ID stuff.
	private static volatile Connection m_Conn = null;
	private static final String CONNECTION_URI_DEFAULT = "jdbc:oracle:thin:@yourdbs.host.name:1521:YOURDBNAME";
	private static final int MAX_RETRIES = 2;

	// Index of the column of eida.eida_names that contains the query string.
	private static final int QRYCOLUMNNUM = 1;

	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

	/*
	 * Query Translation Patterns
	 * 
	 * Pattern for recognizing "&instance" in a query translation. This syntax
	 * is chosen to look like SQLplus macro substitution to a user adding names
	 * and transforms to the MEME db, although the substitution is of course
	 * done entirely in java. NOTE: the name "instance" for the substitution is
	 * really wrong, it should be called "entity".
	 */
	private static final Pattern entityPattern = Pattern.compile("(\\&instance)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);;
	/** Pattern for recognizing "&attribute" in a query translation. */
	private static final Pattern attributePattern = Pattern.compile("(\\&attribute)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	/**
	 * Initialize for an acquisition.
	 * 
	 * Note: we use a pattern where the initialization is done on server
	 * startup, and the important part (getConnection) can be redone at any time
	 * if the connection to the backend rdb goes bad.
	 */
	RdbServiceConnection(final String service_name) {
		init(service_name);
	}

	/**
	 * Init loads JDBC and initializes connection to the db, Oracle in this
	 * case.
	 */
	private void init(final String service_name) {
		try {
			logger.info("Initializing " + service_name);

			// Establish connection to the db instance defined in private
			// members.
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
	 * If the connection is non-null, then the old connection is first closed.
	 * This part is include so that this routine can be used to renew a stale
	 * connection.
	 */
	private synchronized void getConnection() {
		// If we already have a connection dispose of it.
		closeConnection();

		// Having dealt with a possible stale connection, get a new one.
		try {
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
		else
			logger.info("Unable to establish connection to database");

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
	 * Get the SQL query (probably a SELECT statement) identified by the given
	 * query name, as it is given in the data source to which the server
	 * connects (probably a releational database).
	 * 
	 * @param queryName
	 *            identifier of the SQL query; what the end user entered
	 * @return the SQL select statement that corresponds to the queryName input
	 *         parameter
	 */
	public String instanceToQuery(final String queryName) throws UnableToGetDataException {
		logger.finer("Understood users named query to be: " + queryName);

		// Presently, we use the AIDA table AIDA_NAMES as the store for
		// queryname to SQL query mapping, and AIDA split all names into
		// Entity (aka instance) part and Attribute part. We'll supercede
		// this latter when we figure out how to manage all v4 PV names at
		// SLAC.
		String sqlQuery = null;
		final int firstSlash = queryName.lastIndexOf(':');
		if (firstSlash == -1)
			throw new UnableToGetDataException(String.format(MemeConstants.INVALIDRDBQUERYNAME, queryName));
		final String entity = queryName.substring(0, firstSlash);
		final String attribute = queryName.substring(firstSlash + 1);
		final String queryQuery = String.format(MemeConstants.SQLSELECT, entity.toUpperCase(), attribute.toUpperCase());

		ResultSet sqlqueryResultSet = null;
		try {
			sqlqueryResultSet = executeQuery(queryQuery);
			final ResultSetMetaData rsmd = sqlqueryResultSet.getMetaData();
			if (rsmd == null)
				throw new UnableToGetDataException(MemeConstants.NORESULTSETMETADATA);

			// Make assumption that only 1 row is returned, so we
			// don't waste time error checking for a very rare
			// occurrence.
			sqlqueryResultSet.last();
			final int rowsM = sqlqueryResultSet.getRow();
			logger.finer("Num sql queries matching name returned : " + rowsM);
			if (rowsM > 1) {
				logger.severe(MemeConstants.TOOMANYROWMATCHES + " Aborting." + " Lookup query used: " + queryQuery);
				throw new UnableToGetDataException(MemeConstants.TOOMANYROWMATCHES);
			}
			if (rowsM < 1) {
				logger.info(MemeConstants.ZEROROWMATCHES + " Lookup query used: " + queryQuery);
				throw new UnableToGetDataException(MemeConstants.ZEROROWMATCHES);
			}
			sqlqueryResultSet.beforeFirst();
			sqlqueryResultSet.next();
			sqlQuery = sqlqueryResultSet.getString(QRYCOLUMNNUM);

			// Check if the sql query has requested substitutions (begins /).
			// If so, look for '&' markers saying replace these
			// with values from the entity, attribute (or even other args,
			// though that's TODO).
			if (sqlqueryResultSet.getString(QRYCOLUMNNUM).charAt(0) == '/')
				sqlQuery = substitute(sqlqueryResultSet.getString(QRYCOLUMNNUM).substring(1), entity, attribute);
			else
				sqlQuery = sqlqueryResultSet.getString(QRYCOLUMNNUM);

			return sqlQuery;

		} catch (Throwable e) {
			throw new UnableToGetDataException(MemeConstants.UNABLETOTRANSFORM + ": " + queryName);
		} finally {
			// Free JDBC resources.
			try {
				if (sqlqueryResultSet != null) {
					sqlqueryResultSet.close();
				}
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Failed to free JDBC resources for query '" + queryQuery + "'.", e);
			}
		}
	}

	public PVStructure getData(final String query) throws UnableToGetDataException {
		if (logger.isLoggable(Level.FINER))
			logger.entering(this.getClass().getName(), "getData");
		logger.finer("SQL query to be executed: " + query);
		ResultSet rs = null;
		PVStructure pvTop = null;

		try {
			// Replace values of any passed arguments for matched arg names
			// in the query
			// String query = substituteArgs( args, query );
			rs = executeQuery(query);
			final ResultSetMetaData rsmd = rs.getMetaData();
			if (rsmd == null)
				throw new UnableToGetDataException(MemeConstants.NORESULTSETMETADATA);

			// Get number of rows in ResultSet
			rs.last();
			int rowsM = rs.getRow();
			logger.finer("Num rows returned : " + rowsM);

			// Get number of columns in ResultSet
			final int columnsN = rsmd.getColumnCount();
			final String[] columnNames = new String[columnsN];
			// PVField[] pvFields = new PVField[columnsN];
			final Field[] fields = new Field[columnsN];
			logger.finer("Num Columns returned = " + columnsN);

			// Construct introspection interface with appropriate
			// column data types, prior to population.
			for (int colj = 1; colj <= columnsN; colj++) {
				rs.beforeFirst(); // Reset cursor to first row.
				columnNames[colj - 1] = rsmd.getColumnName(colj);
				logger.finer("Column Name = " + columnNames[colj - 1]);

				switch (rsmd.getColumnType(colj)) {
				case java.sql.Types.DECIMAL:
				case java.sql.Types.DOUBLE:
				case java.sql.Types.REAL:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.FLOAT: {
					fields[colj - 1] = fieldCreate.createScalarArray(ScalarType.pvDouble);
					break;
				}
				case java.sql.Types.INTEGER:
				case java.sql.Types.SMALLINT:
				case java.sql.Types.BIGINT: {
					fields[colj - 1] = fieldCreate.createScalarArray(ScalarType.pvInt);
					break;
				}

				case java.sql.Types.TINYINT:
				case java.sql.Types.BIT: {
					fields[colj - 1] = fieldCreate.createScalarArray(ScalarType.pvByte);
					break;
				}
				case java.sql.Types.VARCHAR:
				case java.sql.Types.CHAR:
				case java.sql.Types.LONGVARCHAR: {
					fields[colj - 1] = fieldCreate.createScalarArray(ScalarType.pvString);
					break;
				}
				default: {
					fields[colj - 1] = fieldCreate.createScalarArray(ScalarType.pvString);
					break;
				}
				} // Column type

			} // For each column

			// Construct the Data interface and populate it.
			final String[] topNames = new String[] { "labels", "value" };
			final Field[] topFields = new Field[] { fieldCreate.createScalarArray(ScalarType.pvString),
					fieldCreate.createStructure(columnNames, fields) };
			final Structure top = fieldCreate.createStructure(MemeConstants.NTTABLE_ID, topNames, topFields);
			pvTop = pvDataCreate.createPVStructure(top);
			final PVStructure pvValue = pvTop.getStructureField("value");
			final PVStringArray labelsArray = (PVStringArray) pvTop.getScalarArrayField("labels", ScalarType.pvString);

			// First, just add the labels
			logger.finer("Adding labels");
			labelsArray.put(0, columnNames.length, columnNames, 0);

			// Now add the ResultSet data.
			// For each column, extract all the rows of the column from the
			// ResultSet and add the whole column to what we return. So we're
			// transposing the ResultSet where the slow moving index is row,
			// to a PVStructure.
			final PVField[] pvFields = pvValue.getPVFields();
			for (int colj = 1; colj <= columnsN; colj++) {
				rs.beforeFirst(); // Reset cursor to first row.
				int i = 0; // Reset row indexer.
				// ScalarArray colField = null;
				columnNames[colj - 1] = rsmd.getColumnName(colj);
				logger.finer("Column Name = " + columnNames[colj - 1]);

				switch (rsmd.getColumnType(colj)) {
				case java.sql.Types.DECIMAL:
				case java.sql.Types.DOUBLE:
				case java.sql.Types.REAL:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.FLOAT: {
					final PVDoubleArray valuesArray = (PVDoubleArray) pvFields[colj - 1];

					final double[] coldata = new double[rowsM];
					while (rs.next()) {
						coldata[i++] = rs.getDouble(colj);
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}
				case java.sql.Types.INTEGER:
				case java.sql.Types.SMALLINT:
				case java.sql.Types.BIGINT: {
					final PVLongArray valuesArray = (PVLongArray) pvFields[colj - 1];

					final long[] coldata = new long[rowsM];
					while (rs.next()) {
						coldata[i++] = rs.getLong(colj);
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}

				case java.sql.Types.TINYINT:
				case java.sql.Types.BIT: {
					// colField =
					// fieldCreate.createScalarArray(ScalarType.pvByte);
					// myArr.add(colField);
					final PVByteArray valuesArray = (PVByteArray) pvFields[colj - 1];
					// pvDataCreate.createPVScalarArray(colField);
					// pvFields[colj-1] = valuesArray;

					final byte[] coldata = new byte[rowsM];
					while (rs.next()) {
						coldata[i++] = rs.getByte(colj);
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}
				case java.sql.Types.VARCHAR:
				case java.sql.Types.CHAR:
				case java.sql.Types.LONGVARCHAR: {
					// colField = fieldCreate.createScalarArray(
					// ScalarType.pvString);
					// myArr.add(colField);
					final PVStringArray valuesArray = (PVStringArray) pvFields[colj - 1];
					// pvDataCreate.createPVScalarArray(colField);
					// pvFields[colj-1] = valuesArray;

					final String[] coldata = new String[rowsM];
					while (rs.next()) {
						String d = rs.getString(colj);
						coldata[i++] = (d == null || d.length() == 0) ? " " : d;
						logger.finer("coldata = '" + coldata[i - 1] + "'");
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}
				default: {
					// colField =
					// fieldCreate.createScalarArray(
					// ScalarType.pvString);
					// myArr.add(colField);
					final PVStringArray valuesArray = (PVStringArray) pvFields[colj - 1];
					// pvDataCreate.createPVScalarArray(
					// colField);
					// pvFields[colj-1] = valuesArray;

					final String[] coldata = new String[rowsM];
					while (rs.next()) {
						String d = rs.getString(colj);
						coldata[i++] = (d == null || d.length() == 0) ? " " : d;
						logger.finer("coldata = '" + coldata[i - 1] + "'");
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}
				} // column type

			} // For each column

			// Append all the fields we created for each column, to
			// the top level structure to be returned.
			// pvValue.appendPVFields(columnNames, pvFields);
			// labelsArray.put(0, columnNames.length, columnNames, 0);

		} // try block processing ResultSet

		catch (SQLException e) {
			throw new UnableToGetDataException("Failed to process SQL query: " + query, e);
		} finally {
			// Free JDBC resources.
			try {
				if (rs != null) {
					// Close and free resources of ResultSet.
					rs.close();
				}
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Failed to free JDBC resources for query: " + query, e);
			}
		}

		return pvTop;
	}

	/**
	 * Queries the database with the query in sqlString. This is a wrapper to
	 * give appropriate error handling and retry logic.
	 * 
	 * @param sqlString
	 *            the SQL query, in "ascii" (actually UTF-16 or whatever java
	 *            String is).
	 * @return The ResultSet given by stmt.executeQuery(sqlString)
	 * @version 1.0 19-Oct-2011, Greg White
	 */
	private ResultSet executeQuery(final String sqlString) throws SQLException {
		Statement stmt = null; // The Statement on which the ResultSet is
								// acquired.
		ResultSet rs = null; // ResultSet receiving SQL results. NOTE:
								// should be closed by callers.
		int nRetries = 0;
		boolean bRetry = false;

		// Create a jdbc Statement and execute the given query. If the
		// query fails to execute for whatever reason, try to get a
		// new connection and loop, re-creating the statement and
		// re-executing the query. Try up to 3 times.
		do {
			try {
				// Create a statement with "Scrollable" ResultSet, as
				// necessary
				// for processing each column as a unit in the get method.
				stmt = m_Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(sqlString);
				bRetry = false;
			} catch (Throwable ex) {
				// We encountered an error in the
				// execution of the sql query, so try
				// to fix this by getting a new Oracle
				// connection and set logic so we'll
				// go through the do loop again.
				if (nRetries < MAX_RETRIES) {
					logger.log(Level.WARNING, MemeConstants.RETRYMSG, ex);
					getConnection();
					bRetry = true;
					nRetries++;
				} else {
					bRetry = false;
					final String suppl = "Failed to execute SQL query " + sqlString;
					if (ex instanceof SQLException)
						suppl.concat(": " + ((SQLException) ex).getSQLState());
					logger.log(Level.SEVERE, suppl, ex);
				}
			}
		} while (bRetry);

		if (rs != null && nRetries < MAX_RETRIES)
			return rs;
		else
			throw new SQLException("Unable to execute query.");
	}

	/**
	 * Processes the given transform string for regular expressions embedded in
	 * it.
	 * 
	 * The most common mapping, and all that supported for now, is replacing
	 * occurances of "&amp;instance" with the meme query instance value, and
	 * occurances of "&amp;attribute" with the attribute. This method is called
	 * if the transform string begins with "/" (in the first character), which
	 * indicates that substitutions of regular expressions found in the
	 * transform string should be made by this method.
	 *
	 * @param original
	 *            The sql query to be executed on the database before
	 *            substitutions.
	 * @param entity
	 *            The query "instance" part (eg device-name, eg "XCOR:LI21:401")
	 * @param attribute
	 *            The query "attribute" part (eg secondary, PV value name, or
	 *            simply a property of the instance, eg "Z".)
	 * @return SQL expression to be processed by the Rdb data provider.
	 */
	private String substitute(final String original, final String entity, final String attribute) {
		final String afterinstsub = entityPattern.matcher(original).replaceAll(entity);
		final String finalString = attributePattern.matcher(afterinstsub).replaceAll(attribute); // The
																									// String
																									// after
																									// all
																									// replacements

		/*
		 * You could now, if you wanted, parse final string for regexps matching
		 * the components of a device-name, PV name, etc, and do replacements of
		 * those too. Eg, replace (\w(2)[0-9](2):) with the text matching from
		 * the entity, which is likely to be an "area" aka micro name.
		 */
		return finalString;
	}

}
