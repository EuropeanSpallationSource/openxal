package edu.stanford.slac.meme.service.testservice;

import java.util.logging.Level;    // Logging
import java.util.logging.Logger;   // Logging
import org.epics.pvaccess.util.logging.ConsoleLogHandler;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.epics.pvaccess.PVAException;

// Import pvaccess Remote Procedure Call interface
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;

// Import pvData Data Interface things we need
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

// Import pvData Introspection interface we need for test archive service
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVString;
// Also need Introspection interface for PVDouble for test twiss data
import org.epics.pvdata.pv.PVDouble;

// Get the asynchronous status messaging system things we need
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.StatusCreate;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.factory.StatusFactory;


// MEME exceptions, utilities for MEME design principles.
import edu.stanford.slac.meme.support.err.MEMERequestException;
import edu.stanford.slac.meme.support.err.UnableToGetDataException;
import edu.stanford.slac.meme.support.sys.MemeNormativeTypes;

/**
 * TestService is an EPICS V4 RPC service used exclusively for testing the
 * functionality of an MEME network. All the data this service returns originates
 * right in this code, so if a client can get data from this service, the network
 * works, and if it can't the the network itself doesn't work.
 * <p>
 * TestService is a copy of the class ServiceAPIServer, distributed with EPICS V4 in
 * exampleJava/illustrations/. As ServiceAPIServer, it pretends to be an Archive
 * service, that knows only 1 PV (quad45:bdes) and over a very short time range
 * (2011-09-16T00.03.42 - 2011-09-16T19.45.50). So, the only eget queries like the
 * following will be valid:
 * </p>
 * <pre>
 * $ eget -s testarchiveservice -a entity=quad45:bdes/history
 * -a starttime=2011-09-16T02.12.00 -a endtime=2011-09-16T15.23.17
 *                 sampled time                 sampled value
 * Fri Sep 16 02:12:56 PDT 2011                          42.2
 * Fri Sep 16 04:34:03 PDT 2011                          2.76
 * Fri Sep 16 06:08:41 PDT 2011                          45.3
 * Fri Sep 16 08:34:42 PDT 2011                       85.3245
 * Fri Sep 16 10:01:02 PDT 2011                        35.234
 * Fri Sep 16 12:03:42 PDT 2011                        4.2345
 * </pre>
 * ** It's the memetestservice to distinguish it from the shipped pvAccess test
 * server which also calls itself "testserver" on the network.
 *
 * @author Greg White SLAC, 10-Oct-2013 
 *
 */
public class TestServer {

	// Connect to logger.
	private static final Logger logger = 
		Logger.getLogger(TestServer.class.getPackage().getName());

	private final static String SERVER_NAME = "memetestserver";
	
	// The example is of an Archive Service; make some fake archive data for just
	// one PV.
	//
	private static ArchiveData archiveData;
	private static String archivedPVname;
	private static DateFormat dateFormater; 
	static { archiveData = new ArchiveData(10); initFakeArchiveData(); }

	// Create the introspection interface of the returned data, which is a PV
	// data Structure of grammar "NTTable". The NTTable has prescribed field
	// names ("labels" and "value").  The value field must be a structure of
	// scalar arrays (declared above so we can use it here).  The type of these
	// arrays is not prescribed, though they must be same length.
        //
	// private final static String NTTABLE_ID="epics:nt/NTTable:1.0";
       
	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private final static Structure valueStructure = 
          fieldCreate.createStructure(
	    new String[] {"times", "readings"},
	    new Field[] { 
	      fieldCreate.createScalarArray(ScalarType.pvString),
	      fieldCreate.createScalarArray(ScalarType.pvDouble)});
	private final static Structure resultStructure = 
	  fieldCreate.createStructure( MemeNormativeTypes.NTTABLE_ID, 
	    new String[] { "labels", "value" },
	    new Field[] {
	      fieldCreate.createScalarArray(ScalarType.pvString),
	      valueStructure } );	

	// Create the introspection interface of the returned Twiss data. At
	// present this is a PVStructure without a Normative Type. It's a 
	// system of named field values. Maybe EV4 WG will in future define 
	// this as a Normative type called say NTTuple, or "NTStaticNamedValue".  
	//
	// TODO: How should units be integrated?
	private final static Structure twissStructure =
		fieldCreate.createStructure(
		  new String[] { "energy",
				 "psix","alphax", "betax","etax","etaxp",
		                 "psiy","alphay", "betay","etay","etayp",
		                 "z","leff","sleff","ordinality"},
		  new Field[] {
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvDouble),
			  fieldCreate.createScalar(ScalarType.pvInt)});

	// Fake Twiss and response matrix (6x6) data set, returned for param 
	// fakedata being non-null. Based on QM14, QUAD:LI21:315 of model run 45845.
	private static double[] fakeTwissData = { 
		0.22, 
		20.8572411437, 29.3321341325, 12.5804392017, 0.0436279229998, -0.0193296502818,
		15.1387492999, 31.0719654674, -31.6073259725, 0.0, 0.0, 
		2059.071169, 0.108, 0.054, 526};
	
	public static void main(String[] args) throws PVAException
	{
		// Get service name from property if given.
		String server_name = System.getProperty( "SERVER_NAME",
							  SERVER_NAME );

		// Initialize console logging.
		// ConsoleLogHandler.defaultConsoleLogging( LOG_LEVEL_DEFAULT );
		// ConsoleLogHandler.defaultConsoleLogging( Level.FINE) ;
		logger.info("SERVICES OF \""+server_name +"\" is/are initializing...");

		// Initialize test archive service
		RPCServer server = new RPCServer();
		String testArchServiceName = "archiveservice";
		server.registerService(testArchServiceName, new TestServiceImpl());
		logger.info("SERVICE \""+testArchServiceName +"\" is operational.");
		
		server.registerService("qm14:twiss", 
			       new TwissServiceImpl());
		logger.info("SERVICE \""+"qm14:twiss" +"\" is operational.");
		
		server.registerService("bpm21301:twiss", 
			       new TwissServiceImpl());
		logger.info("SERVICE \""+"bpm21301:twiss" +"\" is operational.");
		
		// Print server details.
		server.printInfo();
		

		// Start the service
		server.run(0);
	}
	
	static class TwissServiceImpl implements RPCService
	{
		public PVStructure request(PVStructure pvUri) throws RPCRequestException
		{
			RPCRequestException iss = null;
			// The device name for which we have been asked to get the Twiss params
			String elementname = null;           
			
			PVStructure twissParams =
					PVDataFactory.getPVDataCreate().
					createPVStructure(twissStructure);
			
			try
			{

				elementname = pvUri.getStringField("path").get();
				if ( elementname.compareToIgnoreCase("qm14:twiss") == 0 )
				{
				
					// "energy"
					// "psix","alphax", "betax","etax","etaxp",
					//  "psiy","alphay", "betay","etay","etayp",
					//  "z","leff","sleff","ordinality"
					twissParams.getDoubleField("energy").put(fakeTwissData[0]);
					twissParams.getDoubleField("psix").put(fakeTwissData[1]);
					twissParams.getDoubleField("alphax").put(fakeTwissData[2]);
					twissParams.getDoubleField("betax").put(fakeTwissData[3]);
					twissParams.getDoubleField("etax").put(fakeTwissData[4]);
					twissParams.getDoubleField("etaxp").put(fakeTwissData[5]);
					twissParams.getDoubleField("psiy").put(fakeTwissData[6]);
					twissParams.getDoubleField("alphay").put(fakeTwissData[7]);
					twissParams.getDoubleField("betay").put(fakeTwissData[8]);
					twissParams.getDoubleField("etay").put(fakeTwissData[9]);
					twissParams.getDoubleField("etayp").put(fakeTwissData[10]);
					twissParams.getDoubleField("z").put(fakeTwissData[11]);
					twissParams.getDoubleField("leff").put(fakeTwissData[12]);
					twissParams.getDoubleField("sleff").put(fakeTwissData[13]);
				    twissParams.getIntField("ordinality").put((int)fakeTwissData[14]);
				}
				else if ( elementname.compareToIgnoreCase("bpm21301:twiss") == 0 )
				{
					twissParams.getDoubleField("energy").put(fakeTwissData[0]+Math.random());
					twissParams.getDoubleField("psix").put(fakeTwissData[1]+Math.random());
					twissParams.getDoubleField("alphax").put(fakeTwissData[2]+Math.random());
					twissParams.getDoubleField("betax").put(fakeTwissData[3]+Math.random());
					twissParams.getDoubleField("etax").put(fakeTwissData[4]+Math.random());
					twissParams.getDoubleField("etaxp").put(fakeTwissData[5]+Math.random());
					twissParams.getDoubleField("psiy").put(fakeTwissData[6]+Math.random());
					twissParams.getDoubleField("alphay").put(fakeTwissData[7]+Math.random());
					twissParams.getDoubleField("betay").put(fakeTwissData[8]+Math.random());
					twissParams.getDoubleField("etay").put(fakeTwissData[9]+Math.random());
					twissParams.getDoubleField("etayp").put(fakeTwissData[10]+Math.random());
					twissParams.getDoubleField("z").put(fakeTwissData[11]+Math.random());
					twissParams.getDoubleField("leff").put(fakeTwissData[12]+Math.random());
					twissParams.getDoubleField("sleff").put(fakeTwissData[13]+Math.random());
				    twissParams.getIntField("ordinality").put((int)fakeTwissData[14]+1);
				}
				else throw new MEMERequestException(
						  String.format(
						    "Service registered for '%s' does not know how to process that name!",
						      elementname) );
				
			}
			catch (Exception ex)
			{
				 iss = new UnableToGetDataException(
						  "Unable to get twiss data for "+elementname, ex );
						logger.severe( iss.getMessage() );
						throw iss;		
			}
			return twissParams;
		}
		
	
	}
	
	static class TestServiceImpl implements RPCService
	{
		private Date calStarttime;       // The input starttime as a Date
		private Date calEndtime;         // The input enddate param as a Date

		public PVStructure request(PVStructure uri) throws RPCRequestException
		{
			RPCRequestException iss = null;

			// Create the return data instance of a resultStructure, using the
			// pvData Data interface methods, and the data interface to
			// this instance. 
			PVStructure result =
				PVDataFactory.getPVDataCreate().
				createPVStructure(resultStructure);

			try
			{
				PVStructure pvUriQuery = uri.getStructureField("query");

				// Strings that will hold the values of the arguments sent
				// from the client.
				//
				String pvname;    // PV name of wanted historical values
				String starttime; // Request from this date/time 
				String endtime;   // .. up to this date/time
				pvname = getQueryArg( pvUriQuery, "entity" ); 
				starttime = getQueryArg( pvUriQuery, "starttime" );
				endtime = getQueryArg( pvUriQuery, "endtime" );
			
				// Process the arguments to get them in a form for
				// this service.  E.g., get string arguments of
				// date/time, into Calendar form.
				//
				dateFormater.setLenient(true);
				calStarttime = dateFormater.parse(starttime);
				calEndtime = dateFormater.parse(endtime);
			
				// Retieve the data interface of the NTTable
				// PVStructure that will be returned to the user. We
				// will populate it through this data interface.
				PVStringArray labelsArray = 
				  (PVStringArray) result.getScalarArrayField(
				     "labels",ScalarType.pvString);
				// Now the value structure
				PVStructure archiveDataTbl =
                                  result.getStructureField("value"); 		    
				PVStringArray datetimesArray = 
				  (PVStringArray) archiveDataTbl.getScalarArrayField(
				     "times",ScalarType.pvString);
				PVDoubleArray readingsArray = 
				  (PVDoubleArray)  archiveDataTbl.getScalarArrayField(
   				    "readings",ScalarType.pvDouble);
				logger.fine(
				  "Successfully retrieved data interface of PVStruture");

				// Populate the return NTTable, through the data
				// interface we made to it. Start with just the
				// labels, then actually select the archive data that
				// is in the time range sent to the service.
				//
				labelsArray.put(0, 2, 
					new String[] {"sampled time","sampled value"}, 0);
				logger.fine("Successfully populated labels of PVStruture");
            
				if ( pvname.equalsIgnoreCase(archivedPVname) )
				{
					int j=0;  // Indexes archive data points found
	     				          // between start and end time.
					int startj = 0;
					for ( int i=0; i<(archiveData.date.length); i++ ) 
					{		
						if ( (( calStarttime == null) ||
						      (calStarttime.before(
							 archiveData.date[i])) &&
						      (( calEndtime == null) ||
						       (calEndtime.after(
	      						 archiveData.date[i])))))
						{	
							// Copy one item at a time
							String [] stringarray_ = 
							    new String[]
                                                        {archiveData.date[i].toString()};
							datetimesArray.put(
								   j,1,stringarray_, 0);
							if ( j==0) startj = i;
							j++;
						} 
					}
					if ( j==0 )
					{
						String msg = 
						    "No matching data beteen %s and %s";
						throw new MEMERequestException(
						  String.format(
					            msg,calStarttime,calEndtime));
				        }
			        
					// Copy in the block of archived values that
					// matched start time to end time: 0 - elem
					// at which to start writing into
					// readingsArray j - how many elements to
					// read from archiveData.value
					// archiveData.value - the source data array
					// startj - elem at which to start reading
					// from archiveData.value
					readingsArray.put(0, j, archiveData.value, startj);
				}
			}
			catch (Exception ex)
			{
			        iss = new UnableToGetDataException(
				  "Unable to get test archive data", ex );
				logger.severe( iss.getMessage() );
				throw iss;
		        }
			return result;
		}

		private String getQueryArg( PVStructure pvUriQuery, String argname ) 
			throws RPCRequestException
		{
			RPCRequestException iss = null;
			logger.setLevel(Level.FINER);
			String argValue = null;
			PVString pvQueryArg =
				pvUriQuery.getStringField(argname);

			if (pvQueryArg == null)
			{
				// throw but don't log - per MEME logging rules.
				iss = new RPCRequestException(
					StatusType.ERROR,
					  String.format(
                        "Missing required argument '%s'",argname));
                throw iss;
			}
			else
			{    
				argValue = pvQueryArg.get();
				if (argValue.length() == 0)
					throw new MEMERequestException(
					  String.format(
					    "Missing required value for argument '%s'",
					      argname));
			}
			// Tracing MUST be logged as FINE.
			logger.fine("Argument \'"+argname+"\' received"+
				    ", value = \'"+argValue+"\'");

			return argValue;
		}

	}
	

	
	/**
	 * The data type of the fake data returned by testArchiveService
	 */
	private static class ArchiveData
	{
		Date[] date;               // Datetime stamp of archive data
		double[] value;            // The value at the time of the datetime stamp
		
		ArchiveData(int points)
		{
			date = new Date[points];
			value = new double[points];
		}
	}
	
	/**
	 * Static test data initializer. 
	 * 
	 * This is fake date/time stamps and associated fake data
	 * from which client queries can be drawn.
	 */
	private static void initFakeArchiveData()
	{
                // The instance whose history is known
		archivedPVname = "quad45:bdes/history";   
		
		dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss");
		try
		{
			archiveData.date[0] = dateFormater.parse("2011-09-16T00.03.42"); 
			archiveData.value[0] = 21.2;
			archiveData.date[1] = dateFormater.parse("2011-09-16T01.04.40");
			archiveData.value[1] = 31.2;
			archiveData.date[2] = dateFormater.parse("2011-09-16T02.12.56"); //
			archiveData.value[2] = 42.2;
			archiveData.date[3] = dateFormater.parse("2011-09-16T04.34.03"); //
			archiveData.value[3] = 2.76;
			archiveData.date[4] = dateFormater.parse("2011-09-16T06.08.41"); //
			archiveData.value[4] = 45.3;
			archiveData.date[5] = dateFormater.parse("2011-09-16T08.34.42"); //
			archiveData.value[5] = 85.3245;
			archiveData.date[6] = dateFormater.parse("2011-09-16T10.01.02"); //
			archiveData.value[6] = 35.234;
			archiveData.date[7] = dateFormater.parse("2011-09-16T12.03.42"); //
			archiveData.value[7] = 4.2345;
			archiveData.date[8] = dateFormater.parse("2011-09-16T15.23.18");
			archiveData.value[8] = 45.234;
			archiveData.date[9] = dateFormater.parse("2011-09-16T19.45.50");
			archiveData.value[9] = 56.234;
		
		} catch ( Exception ex )
		{
			System.err.println(
			  "Error Constructing fake data at initialization;" + 
			  ex.toString());
			System.exit(1);
		}
	}
}
