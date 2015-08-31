package edu.stanford.slac.meme.support.sys;

public class MemeConstants {
    // Error exit codes
    public static final int NOARGS = 1;
    public static final int NOTNTTABLETYPE = 2;
    public static final int NODATARETURNED = 3;

    //Error messages
    public static final String NOT_ENOUGH_ARUMENTS = "Not enough arguments given. Exiting...";
    public static final String MISSINGREQUIREDARGLVAL = "Missing required argument %s";
    public static final String NORESULTSETMETADATA = "No ResultSet metadata available, so can not continue to get data";
    //RDB error messages
	public static final String INVALIDRDBQUERYNAME = "Invalid syntax of RDB query name (%s), at least one colon expected";
	public static final String UNABLETOTRANSFORM = "Failed to find a SQL query name matching the given name";
	public static final String TOOMANYROWMATCHES = "DATABASE DATA ERROR EDETCTED: More than row matches query name.";
	public static final String ZEROROWMATCHES = "No query name found in database matching given name.";
	public static final String RETRYMSG = "Failed to execute SQL query, retrying with new java.sql.Connection.";
    public static final String NOTEXPECTEDNTID = "Expected %s id member value but found id value %s";
    public static final String MISSINGREQUIREDARGRVAL = "Missing required argument %s rvalue";
    public static final String NORETURNEDDATA = "Failed to get data from the database. Check service name and arguments";
    public static final String SERVERINIT_SUCCESSFUL = "SERVICE %s initializaton successful.";
    public static final String SERVERINIT_FAILED = "SERVICE %s initializaton FAILED.";
	//Optics error messages
    public static final int SEPARATOR = ':'; // SLAC specific
    public static final String UNEXPECTEDSEP = "PV name %s contains unexpected separator %c, expected only instance or device name in this context ";
    public static final String CONNECTION_URI_DEFAULT = "jdbc:oracle:thin:@youopticss.host.name:1521:YOURDBNAME";
    public static final String UNABLETOPROCESSSQL = "Unable to execute SQL query successfully";
    public static final String NOMATCH = "No matching model data for query ";
    public static final String WHEN_EXECUTINGSQL = "when executing SQL query \'%s\'. Retrying with new Connection";
    public static final String INVALIDATTRIBUTE = "Entity with unrecognized attribute part received";
    public static final String TYPE_PARAM_VAL_ILLEGAL = "Specified TYPE param must be valued COMPUTED, DATABASE, EXTANT (all meaning EXTANT) or DESIGN";
    public static final String POS_PARAM_VAL_ILLEGAL = "Specified position (POS or POSB) param must be valued BEG, BEGINNING (same as BEG), MIDDLE (same as MID), or END";
    public static final String INCONSISTENT_PSI = "Inconsistent phase advances in each plane detected for A and B";
	public static final String ILLEGALRUN_PARAM = "The RUNID param, if supplied, must be a positive integer, or 'NULL' for latest run";
	public static final String WARNING_BOTHTYPEANDRUNID = "WARNING: Mutually exclusive params supplied: RunID and TYPE. Only RunID will be used, given TYPE ignored.";
	
	
	
	// The basic SQL query that looks up sql expression matching given name
	public static final String SQLSELECT = "SELECT TRANSFORM FROM AIDA_NAMES WHERE UPPER(INSTANCE) = '%s' "
			+ "AND UPPER(ATTRIBUTE) = '%s'";

}
