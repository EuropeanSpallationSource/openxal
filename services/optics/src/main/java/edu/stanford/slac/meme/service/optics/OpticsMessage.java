//-*-jde-*-
package edu.stanford.slac.meme.service.optics;

import edu.stanford.slac.meme.support.err.Message;

public class OpticsMessage extends Message
{
	// Message Strings

        protected static final String SET_NOT_SUPPORTED =
		"Set operation is not supported in  Model server";
	protected static final String N_DATABASE_INIT = 
		"SQLException encountered starting ";
	protected static final String INVALIDATTRIBUTE = 
		"Entity with unrecognized attribute part received";
	protected static final String NORESULTSETMETADATA = 
		"No ResultSet metadata available so " + 
		"cannot construct DaValue return members";
	protected static final String INVALIDSYNTAXOFMODE_PARAM = 
           // "See MEME query LCLS//model_modes for valid modes"
	    "Invalid MODE argument syntax, must be 1-99. "; 
	protected static final String DGRP_DEPRECATED =
	    "DGRP argument deprecated following transition from SLC Modelling";
        protected static final String TYPE_PARAM_VAL_ILLEGAL = 
	     "Specified TYPE param must be valued COMPUTED, DATABASE, EXTANT" + 
	     " (all meaning EXTANT) or DESIGN";
        protected static final String POS_PARAM_VAL_ILLEGAL =
	     "Specified position (POS or POSB) param must be valued " + 
		"BEG, BEGINNING (same as BEG), MIDDLE (same as MID), or END";
	protected static final String UNABLETOPROCESSSQL = 
	        "Unable to execute SQL query successfully";
	protected static final String UNABLETOEXTRATCTRESULTSET = 
	        "Unable to extract data from JDBC Result Set";
	protected static final String NOMATCH = 
	        "No matching model data for query ";
	protected static final String TOOMANYROWS =
		"Too many matching rows in model database " + 
		" (should be 1 exactly) for query "; 
	protected static final String INCONSISTENT_PSI =
		"Inconsistent phase advances in each plane detected "+
		"for A and B";	
	protected static final String ILLEGALRUN_PARAM =
	        "The RUNID param, if supplied, must be a positive integer, "+
	        "or 'NULL' for latest run";	
        protected static final String NOGOLD = 
	        " model not found in any GOLD model of any MODE";
	protected static final String WARNING_FOUNDINRUNID =
		"WARNING: Device model was not found in MODE "+ 
		"; data returned is from RunId ";
	protected static final String WARNING_BOTHMODEANDRUNID =
		"WARNING: Mutually exclusive params supplied: RunID and MODE. "+
		"Only RunID will be used, given MODE ignored.";
	protected static final String WARNING_BOTHTYPEANDRUNID =
		"WARNING: Mutually exclusive params supplied: RunID and TYPE. "+
		"Only RunID will be used, given TYPE ignored.";
	protected static final String NOMODELDATA = 
	    "Optics model data for %s of type %s was not found in any model mode";
	protected static final String WHEN_EXECUTINGSQL = 
		"when executing SQL query \'%s\'. Retrying with new Connection"; 
}
