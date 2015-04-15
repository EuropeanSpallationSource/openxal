/**
 * RdbService defines the server side of an MEME EPICS V4 service for accessing
 * a relational database, such as ORACLE.
 *
 * @author Greg White, 12-Sep-2013, from an example for EPICS V4 also by me,
 *         from an MEME v1 service also by me.
 */
package edu.stanford.slac.meme.service.rdb;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Status.StatusType;

import edu.stanford.slac.meme.support.sys.MemeNormativeTypes;

/**
 * RdbService implements the MEME EPICS v4 service for retrieving data from a
 * relational database (rdb) like Oracle.
 *
 * This implementation of the service is based on, but is distinct from, the RdbService
 * in the exampleJava package of EPICS V4. That other rdbService is by the same author 
 ( (me, Greg White - hi!).
 * 
 * In the EPICS v4 services framework, a pure RPC service is implemented by creating a
 * class with the signature defined by org.epics.pvaccess.server.rpc.RPCService. This 
 * is the required factory Class for the Rdb service. This is the guy a service
 * developer writes.
 * 
 * As written, RdbService expects its input, the argument to the request method, to be
 * a pvStructure conforming to Normative Type NTURI (see EPICS V4 Normative Types).  
 * Specifically the NTURI must encode a single query argument, named "q", whose
 * value is the name of an rdb database query it knows how to process.
 * 
 * The service returns results as a PVStructure of normative type NTTable (as
 * NTTable was defined at the time of writing, it was in flux, as the idea was
 * being driven by this project).
 * 
 * @author Greg White, 13-Oct-2011 (greg@slac.stanford.edu)
 * @version 04-Mar-2015, Greg White (greg@slac.stanford.edu)
 *          Fix for no argument rvalue given. Check not for null but for 0 length.
 * @version 11-Nov-2014, Greg Whote (greg@slac.stanford.edu)
 *          Modifications for using new EPICS V4 Normative Tyes IDs.
 * @version 2-Jun-2014, Greg White (greg@slac.stanford.edu)
 *          Added variable replacement for entity and attribute, so can dynamically
 *          construct SQL queries with different device names or properties.
 * @version 15-Jan-2013, Greg White (greg@slac.stanford.edu) 
 *          Updated for conformance to NTTable.
 * @version 2-Nov-2012, Greg White (greg@slac.stanford.edu) 
 *          Added use of NTURI normative type. Hence rdbService is Normative Types
 *          compliant, since input is by NNTRI and output by NTTAable, all I/O is 
 *          by normative type. 
 * @version 2-Oct-2012, Greg White (greg@slac.stanford.edu) Converted to using
 *          NTTable normative type.
 * @version 7-May-2012, Greg White (greg@slac.stanford.edu) Changed calls to
 *          pvAccess api following changes in introspection API.
 * 
 */
public class RdbService
{
	// Connect to logger.
	private static final Logger logger = 
		Logger.getLogger(RdbService.class.getPackage().getName());
    
        private final static String SERVER_NAME_DEFAULT = "rdbserver";

	// The advertised name of the service - that is, the EPICS V4 PV name of
	// this RPC service.
	private static final String NAMES_SERVICE_CHANNEL_NAME = "names";
	private static final String RDB_SERVICE_CHANNEL_NAME = "rdb";
    
       	// Factories for creating the data and introspection interfaces of data
	// exchanged by RdbService.
	private static final FieldCreate fieldCreate = FieldFactory
			.getFieldCreate();
	private static final PVDataCreate pvDataCreate = PVDataFactory
			.getPVDataCreate();

	// Default console logging level.
	private static final Level LOG_LEVEL_DEFAULT = Level.INFO;
	
	private static String 
          INSTANCE_NAMESQUERY = 
		"SELECT DISTINCT INSTANCE FROM AIDA_NAMES WHERE INSTANCE LIKE \'%s\'", 
          INSTANCE_AND_ATTRIBUTE_NAMESQUERY = 
		"SELECT DISTINCT INSTANCE||\'/\'||ATTRIBUTE FROM AIDA_NAMES "+
		"WHERE INSTANCE LIKE \'%s\' AND ATTRIBUTE LIKE \'%s\'"; 
	
 
	// Error Messages
	private static final String NOTEXPECTEDNTID =
		"Expected %s id member value but found id value %s";
        private static final String MISSINGREQUIREDARGLVAL = 
		"Missing required argument %s";
        private static final String MISSINGREQUIREDARGRVAL = 
		"Missing required argument %s rvalue";
	private static final String NORETURNEDDATA = 
		"Failed to get data from the database. Check service name and arguments";

	/**
	 * The implementation class of the namesService RPCService, which
	 * gets the names of EPICS PVs matching a given pattern provided by the user.
	 *  
	 * @author Greg White, 9-Nov-2012.
	 * @version Greg White, 11-Sep-2013, modified to be MEME rdb service. 
	 */
	private static class NamesServiceImpl implements RPCService
	{

		// The pvAccess connection delegate for the RDB service.
		private final RdbServiceConnection connection;

		NamesServiceImpl(RdbServiceConnection connection)
		{
			this.connection = connection;
		}

		/**
		 * Construct and return the requested database data, given an NTURI
		 * that encodes the name of a relational database query, as
		 * understood by this service.
		 */
		public PVStructure request(PVStructure pvUri)
				throws RPCRequestException
		{
			// Retrieve the (required) pattern argument 
			// TODO: Make efficient by extracting query and acting on that.
			//
			PVString pvPatternArg =
			    pvUri.getStructureField("query").getStringField("pattern");
			if (pvPatternArg == null)
				throw new RPCRequestException(StatusType.ERROR,
			     	      String.format(MISSINGREQUIREDARGLVAL,"pattern"));
			String pattern = pvPatternArg.get();
			if (pattern == null)
				throw new RPCRequestException(StatusType.ERROR,
			      	      String.format(MISSINGREQUIREDARGRVAL,"pattern"));

			// Retieve the (optional) service name argument, if present
			String serviceName = null;
                        if ( pvUri.getStructureField("query").getSubField("service")!=null )
			{
				PVString pvServiceNameArg =
			      	pvUri.getStructureField("query").getStringField("service");
				if (pvServiceNameArg != null)
					serviceName = pvServiceNameArg.get();
			}


			// Parse the name to see if we were given both an instance 
			// part (device name) and an attribute part or not. Then lookup
			// matching names appropriately in AIDA_NAMES.
			//
			PVStructure pvTop = null;			
			try
			{
				int firstSlash = pattern.indexOf('/');
				int parts = (firstSlash == -1) ? 1 : 2;
				
				String query = null;
				if ( firstSlash == -1 && serviceName == null)
					query = String.format(INSTANCE_NAMESQUERY,pattern);
				else 
				{
					String instance=pattern.substring(0,firstSlash);
					String attribute=pattern.substring(firstSlash+1);
					query = String.format(
						INSTANCE_AND_ATTRIBUTE_NAMESQUERY,
						instance, attribute );
				}

				// All gone well, so, pass the pvTop introspection
				// interface and the query string to getData, which
				// will populate the pvTop for us with the data in
				// Oracle.
				//
				pvTop = connection.getData(query);

				logger.finer("pvTop = " + pvTop);

				// Return the data from Oracle, in the pvTop, to the client.
				return pvTop;
				
			} catch (UnableToGetDataException ex)
			{
				
				String issueMsg = 
					"Failed to get data from the database; "+
					ex.getMessage();
				logger.info(String.format(
					"Server request returns: [%s]\'%s\'", 
					StatusType.ERROR, issueMsg)); 
				throw new RPCRequestException(StatusType.ERROR, issueMsg);
			}
		}
	}

	/**
	 * The implementation class of the namesService RPCService, which
	 * gets the names of EPICS PVs matching a given pattern provided by the user.
	 *  
	 * @author Greg White, 9-Nov-2012.
	 * @author Greg White, 11-Sep-2013, modified to be MEME rdb service. 
	 */
	private static class RdbServiceImpl implements RPCService
	{
		// Acquire the logging interface
		private static final Logger logger = 
		    Logger.getLogger(RdbServiceImpl.class.getName());

		// The pvAccess connection delegate for the RDB service.
		private final RdbServiceConnection connection;

		RdbServiceImpl(RdbServiceConnection connection)
		{
			this.connection = connection;
		}

		/**
		 * Construct and return the requested database data, given an NTURI
		 * that encodes the name of a relational database query, as
		 * understood by this service.
		 */
		public PVStructure request(PVStructure pvUri)
				throws RPCRequestException
		{
			String msg = null; // Server messages logged and returned.

			String type = pvUri.getStructure().getID();
			if (!type.equals(MemeNormativeTypes.NTURI_ID))	
			{
				msg = "Unable to get data. Bad argument to server: "+
				  String.format(NOTEXPECTEDNTID, MemeNormativeTypes.NTURI_ID, 
						type);
				logger.info(msg);
				throw new RPCRequestException(StatusType.ERROR, msg);
			}

			// Retrieve the pattern argument, assuming pvUri is
		        // is a PVStructure conforming to NTURI.
			//
			PVString pvRbbQueryName =
			    pvUri.getStructureField("query").getStringField("q");
			if (pvRbbQueryName == null)
				throw new RPCRequestException(StatusType.ERROR,
			     	      String.format(MISSINGREQUIREDARGLVAL,"q"));
			String rdbqueryname = pvRbbQueryName.get();
			if (rdbqueryname == null || rdbqueryname.length()==0)
				throw new RPCRequestException(StatusType.ERROR,
			      	      String.format(MISSINGREQUIREDARGRVAL,"q"));

			// Look up the SQL query keyed by the rdbqueryname, and then
			// get the data in Oracle for that SQL query.
			//
			PVStructure pvTop = null;
			try
			{
				// Look up sql query for queryname given.
				String sqlquery = connection.instanceToQuery( rdbqueryname );

				// Execute sql query and serialize to pvTop PVStructure
				pvTop = connection.getData(sqlquery);
				logger.finer("pvTop = " + pvTop);

				// Return the data from Oracle, in the pvTop, to the client.
				return pvTop;
				
			} catch (UnableToGetDataException ex)
			{
				msg = NORETURNEDDATA + ": " + ex.getMessage();
				logger.info(String.format(
						    "Server request returns: [%s] \'%s\'", 
						    StatusType.ERROR, msg)); 
				throw new RPCRequestException( StatusType.ERROR, msg  );
			}
		}
	}

	public static void main(String[] args) throws PVAException
	{
		// Get service name from property if given.
		String server_name = System.getProperty( "SERVER_NAME",
							  SERVER_NAME_DEFAULT );

		// Initialize console logging.
		logger.info("SERVICES OF \""+server_name +"\" is/are initializing...");

		// Initialize database connection.
		RdbServiceConnection namesConnection =
		    new RdbServiceConnection(NAMES_SERVICE_CHANNEL_NAME);
		RdbServiceConnection rdbConnection = 
		    new RdbServiceConnection(RDB_SERVICE_CHANNEL_NAME);

		// Instantiate ChannelRPC service instances of this pvAccess server.
		RPCServer server = new RPCServer();
		logger.info("SERVICES OF \""+server_name +"\" is/are initializing...");

		// Register channels to which this service should respond over pvAccess.
		server.registerService(NAMES_SERVICE_CHANNEL_NAME, 
				       new NamesServiceImpl(namesConnection));
		logger.info("SERVICE \""+NAMES_SERVICE_CHANNEL_NAME+"\" is operational.");
	    
		server.registerService(RDB_SERVICE_CHANNEL_NAME, 
				       new RdbServiceImpl(rdbConnection));
		logger.info("SERVICE \""+RDB_SERVICE_CHANNEL_NAME+"\" is operational.");

		server.printInfo();
		
		// Start the service.
		server.run(0);
	}

}
