/**
 * The optics Service defines the server side of a MEME EPICS V4 service for
 * accessing a relational database which contains the twiss parameters and orbit
 * response matrices (R-matrices) of a particle accelerator mathematical model.
 * 
 * @author Greg White
 * @version 2014-10-09 Added caused by to final UnableToGetDataException 
 */

package edu.stanford.slac.meme.service.optics;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

// Error handling, MEME exceptions, utilities for MEME design principles.
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.server.rpc.RPCRequestException;
// Import pvaccess Remote Procedure Call interface
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;
// pvData Data Interface
import org.epics.pvdata.factory.FieldFactory;
// pvData Introspection interface
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
// Asynchronous status messaging system
// import org.epics.pvdata.pv.Status;
// import org.epics.pvdata.pv.StatusCreate;
import org.epics.pvdata.pv.Status.StatusType;
// import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.Structure;

import Jama.Matrix;
import edu.stanford.slac.meme.support.err.Message;
import edu.stanford.slac.meme.support.err.UnableToGetDataException;
// import edu.stanford.slac.meme.support.err.MEMERequestException;
import edu.stanford.slac.meme.support.sys.DataMode; // fake or real data
import edu.stanford.slac.meme.support.sys.MemeNormativeTypes; // NTMATRIX_ID
import edu.stanford.slac.meme.support.sys.Mode; // Dev or prod

/**
 * The optics Service defines the server side of an MEME EPICS V4 service for accessing a relational database which
 * contains the twiss parameters and orbit response matrices (R-matrices) of a particle accelerator mathematical model.
 *
 * In MEME, optics database "queries" are valued as the name of a accelerator device followed by the optics data you
 * want to get for that device.
 * 
 * <pre>
 * QUAD:LI23:13/R - returns NTMatrix 
 * BPMS:UND1:32/twiss - returns NTTable
 * </pre>
 * 
 * @author Greg White, 25-Oct-2013 (greg@slac.stanford.edu)
 * @see [1] <a href="http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html#ntmatrix"> Normative
 *      Type Specification</a>
 * 
 */
public class OpticsService {
    // Acquire the logging interface
    private static final Logger logger = Logger.getLogger(OpticsService.class.getPackage().getName());

    // Get this MEME server's config - its name and which MEME network
    // it will join. The name is the PV name for pvAccess responses
    public static Integer meme_mode = null, data_mode = null;
    public static String server_name = null;

    // Factories for creating the data and introspection interfaces of
    // data exchanged by OpticsService.
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    // private static final PVDataCreate pvDataCreate = PVDataFactory
    // .getPVDataCreate();

    // private static OpticsMessage Msg;

    // vate static final int MODE_PARAM=0; // Index of MODE in Params.
    private static final int B_PARAM = 0; // Index of B in Params.
    private static final int TYPE_PARAM = 1; // Index of TYPE in Params.
    private static final int POS_PARAM = 2; // Index of POS in Params.
    private static final int POSB_PARAM = 3; // Index of POSB in Params.
    private static final int RUN_PARAM = 4; // Index of RUN in Params.
    private static final int FAKEDATA_PARAM = 5; // Index of FAKEDATA
    private static final int PSIX = 1, PSIY = 6;// Indeces in Twiss array

    // HashTable for finding optics data of sliced devices (most
    // commonly quads with 2 slices - where a BPM is embedded between
    // the two). In 2 sliced device Begin means entry, middle means
    // entry to second slice, end means exit of 2nd slice.
    private static final Map<String, Integer> indexMap;
    static {
        indexMap = new HashMap<String, Integer>(3);
        indexMap.put("BEGIN", new Integer(1));
        indexMap.put("MIDDLE", new Integer(2));
        indexMap.put("END", new Integer(3));
    }

    // Create the introspection interface of the returned Twiss
    // data. At present this is a PVStructure without a Normative
    // Type. It's a system of named field values. Maybe EV4 WG will in
    // future define this as a Normative type called say NTTuple, or
    // "NTStaticNamedValue".
    //
    // TODO: How should units be integrated?
    private final static Structure twissStructure = fieldCreate.createStructure(new String[] { "energy", "psix",
            "alphax", "betax", "etax", "etaxp", "psiy", "alphay", "betay", "etay", "etayp", "z", "leff", "sleff",
            "ordinality" },
            new Field[] { fieldCreate.createScalar(ScalarType.pvDouble), fieldCreate.createScalar(ScalarType.pvDouble),
                    fieldCreate.createScalar(ScalarType.pvDouble), fieldCreate.createScalar(ScalarType.pvDouble),
                    fieldCreate.createScalar(ScalarType.pvDouble), fieldCreate.createScalar(ScalarType.pvDouble),
                    fieldCreate.createScalar(ScalarType.pvDouble), fieldCreate.createScalar(ScalarType.pvDouble),
                    fieldCreate.createScalar(ScalarType.pvDouble), fieldCreate.createScalar(ScalarType.pvDouble),
                    fieldCreate.createScalar(ScalarType.pvDouble), fieldCreate.createScalar(ScalarType.pvDouble),
                    fieldCreate.createScalar(ScalarType.pvDouble), fieldCreate.createScalar(ScalarType.pvDouble),
                    fieldCreate.createScalar(ScalarType.pvInt) });

    // Response matrix data (Rmats) are returned in a EPICS V4
    // NTMatrix Normative Type [1].
    private final static Structure rmatStructure = fieldCreate.createStructure(MemeNormativeTypes.NTMATRIX_ID,
            new String[] { "value", "dim" }, new Field[] { fieldCreate.createScalarArray(ScalarType.pvDouble),
                    fieldCreate.createScalarArray(ScalarType.pvInt) });

    // Fake Twiss and response matrix (6x6) data set, returned for
    // param fakedata being non-null. Based on QM14, QUAD:LI21:315 of
    // model run 45845.
    private static double[] fakeTwissData = { 0.22, 20.8572411437, 29.3321341325, 12.5804392017, 0.0436279229998,
            -0.0193296502818, 15.1387492999, 31.0719654674, -31.6073259725, 0.0, 0.0, 2059.071169, 0.108, 0.054, 526 };
    private static double[] fakeRmatData = { 2.09624160473, 0.804154556226, 0.0, 0.0, -1.06385858999, -0.0112465957359,
            -0.966309173538, -0.357701041448, 0.0, 0.0, 0.474782969028, 0.00498288131746, 0.0, 0.0, -0.303419179617,
            1.28893518781, 0.0, 0.0, 0.0, 0.0, -0.314544894536, 1.24631295859, 0.0, 0.0, -0.00130328580005,
            2.17740006796E-4, 0.0, 0.0, 0.14234033399, 0.00291949986906, -0.0295967119844, -0.0246787398374, 0.0, 0.0,
            -21.9009846796, -0.257784349164 };

    /**
     * The implementation class of the OpticsService RPCService, which gets transverse beam optics data, specifically
     * Twiss params and orbit response matrices, for a given device, per user requests.
     * 
     * @author Greg White, 13-Sep-2013.
     */
    private static class OpticsServiceImpl implements RPCService {

        // The OpticeServiceConnection manages all i/o with the
        // back end database.
        private final OpticsServiceConnection connection;

        // The pvAccess connection delegate for the Optics
        // service. This service takes a backend database
        // connection delegate.
        OpticsServiceImpl(OpticsServiceConnection connection) {
            this.connection = connection;
        }

        /**
         * Construct and return the requested database data, given an NTURI that encodes the name of a relational
         * database query, as understood by this service.
         */
        @Override
        public PVStructure request(PVStructure pvUri) throws RPCRequestException {

            RPCRequestException iss = null; // Return status
            double[] twiss = null; // Raw twiss param values
            double[] rmat = null; // Raw response matrix values

            // TODO: Check whether this should be static too -
            // for performance.

            // Create the return data instance of a
            // resultStructure, using the pvData Data interface
            // methods, and the data interface to this
            // instance.
            PVStructure result = null;

            try {
                // Retrieve the query - the name of a
                // device and and whether twiss or R matrix
                // is wanted. <devicename>/twiss or
                // <devicename>/R
                //
                PVStructure pvQuery = pvUri.getStructureField("query");
                logger.fine("pvQuery received= " + pvQuery);

                PVString pvQueryName = pvQuery.getStringField("q");
                if (pvQueryName == null)
                    throw new RPCRequestException(StatusType.ERROR, String.format(Message.MISSINGREQUIREDARGLVAL, "q"));
                String pvname = pvQueryName.get();
                if (pvname == null)
                    throw new RPCRequestException(StatusType.ERROR, String.format(Message.MISSINGREQUIREDARGRVAL, "q"));
                logger.fine("pvname = " + pvname);

                // Extract the parameters of the query from
                // the request URI. These tell us
                // refinements of exactly what model data
                // is wanted for the given device (named in
                // the q subfield).
                //
                String[] params = parseArgs(pvQuery);
                Entity entity = new Entity(pvname);

                // If twiss params were requested, get
                // those, otherwise if R matrix was
                // requested, get that. Any other attribute
                // is not recognized.
                //
                if (entity.attribute().equals("TWISS")) {
                    // Get or compute the twiss
                    // parameters at the beamline
                    // entity given.
                    twiss = get_twissA(entity, params);

                    // Populate the return PVStructure
                    // through its so called "data"
                    // interface.
                    result = PVDataFactory.getPVDataCreate().createPVStructure(twissStructure);

                    // Assign Courant-Snyder params found
                    // to fields of introspection interface
                    // constructed above.
                    result.getDoubleField("energy").put(twiss[0]);
                    result.getDoubleField("psix").put(twiss[1]);
                    result.getDoubleField("alphax").put(twiss[2]);
                    result.getDoubleField("betax").put(twiss[3]);
                    result.getDoubleField("etax").put(twiss[4]);
                    result.getDoubleField("etaxp").put(twiss[5]);
                    result.getDoubleField("psiy").put(twiss[6]);
                    result.getDoubleField("alphay").put(twiss[7]);
                    result.getDoubleField("betay").put(twiss[8]);
                    result.getDoubleField("etay").put(twiss[9]);
                    result.getDoubleField("etayp").put(twiss[10]);
                    result.getDoubleField("z").put(twiss[11]);
                    result.getDoubleField("leff").put(twiss[12]);
                    result.getDoubleField("sleff").put(twiss[13]);
                    result.getIntField("ordinality").put((int) twiss[14]);
                } else if (entity.attribute().equals("R")) {
                    // TODO: Can createPVStructure be
                    // done statically?

                    // Create PVdata object to return,
                    // according to introspection
                    // interface of rmat defined above.
                    result = PVDataFactory.getPVDataCreate().createPVStructure(rmatStructure);
                    PVDoubleArray rmatpv = (PVDoubleArray) result.getScalarArrayField("value", ScalarType.pvDouble);
                    PVIntArray dimpv = (PVIntArray) result.getScalarArrayField("dim", ScalarType.pvInt);

                    if (params[B_PARAM].equals("NULL")) {
                        rmat = get_rmatA(entity, params);

                        // Populate the return PVStructure
                        // through its so called "data"
                        // interface.
                        rmatpv.put(0, 36, rmat, 0);
                        dimpv.put(0, 2, new int[] { 6, 6 }, 0);
                    } else // There is a B device, get A to B.
                    {
                        rmat = get_rmat_AtoB(entity, params);

                        // Populate the return PVStructure
                        // through its so called "data"
                        // interface.
                        rmatpv.put(0, 36, rmat, 0);
                        dimpv.put(0, 2, new int[] { 6, 6 }, 0);
                    }
                } else {
                    // query attribute not recognized
                    throw new IllegalArgumentException(OpticsMessage.INVALIDATTRIBUTE + " : " + entity.attribute());
                }

                logger.fine("Successfully retrieved model data interface of PVStruture");

            } catch (Exception ex) {
                // Throw the final RPCRequestExcetion
                // (conforming to MEME Design Principles,
                // MUST say Unable to get (kind of) data,
                // the causing exception, and subsequently
                // MUST log severe.
                iss = new UnableToGetDataException("Unable to get optics data", ex);
                logger.severe(iss.getMessage() + " caused by " + ex);
                throw iss;
            }

            if (logger.isLoggable(Level.FINE))
                logger.fine("result = " + result);

            return result;
        }

        /**
         * Examines the "parameters", passed from the client as part of the MEME query.
         * 
         * @param args
         *            String array of parameter/value pairs, even numbered elements (0indexed) are parameter names, odd
         *            numbered are the values (passed as String, even if they're numeric.
         * @return String array of parameter values, fully instantiated; each element being the value of the
         *         corresponding parameter, as index by class variable param_INDEX static constants.
         * @throws IllegalArgumentException
         *             if any parameter name or value is found to be invalid.
         */
        private String[] parseArgs(PVStructure pvQuery) throws IllegalArgumentException {
            // Parameters for query, initialized to defaults.
            String[] paramNames = { "b", "type", "pos", "posb", "run", "fakedata" };
            String[] paramValues = { "NULL", "EXTANT", "END", "BEG", "GOLD", "NULL" };

            // TODO: Add a check that an unreognized parameter (-a) is given.

            /*
             * Extract the parameters from the NTURI query PVStructure given to service, and override defaults.
             */
            for (int i = 0; i < paramNames.length; i++) {
                if (pvQuery.getSubField(paramNames[i]) != null) {
                    PVString pvParam = pvQuery.getStringField(paramNames[i]);
                    if (pvParam != null)
                        paramValues[i] = pvParam.get().trim().toUpperCase();
                }
            }

            /*
             * Fix cases where 2 params have been given in contradiction. If an explicit run number is given, then warn
             * that the* type of it is irrelevant.
             */
            if (pvQuery.getSubField(paramNames[TYPE_PARAM]) != null
                    && pvQuery.getSubField(paramNames[RUN_PARAM]) != null
                    && Pattern.matches("^(0+)?[1-9][0-9]+$", paramValues[RUN_PARAM])) {
                paramValues[TYPE_PARAM] = "NULL";
                logger.warning(OpticsMessage.WARNING_BOTHTYPEANDRUNID);
            }

            // Check that the type parameter, if specified, is valid. If
            // specified to be "DATABASE" or "COMPUTED", this is equivalent
            // to "EXTANT". "EXTANT" is default.
            if (Pattern.matches("^(COMP.*)|(DATA.*)|(EXT.*)$", paramValues[TYPE_PARAM]))
                paramValues[TYPE_PARAM] = "EXTANT";
            else if (Pattern.matches("^(DES.*)$", paramValues[TYPE_PARAM]))
                paramValues[TYPE_PARAM] = "DESIGN";
            else {
                throw new IllegalArgumentException(OpticsMessage.TYPE_PARAM_VAL_ILLEGAL);
            }

            // Check that the POS position parameter is valid.
            if (Pattern.matches("^(B|b).*$", paramValues[POS_PARAM]))
                paramValues[POS_PARAM] = "BEGIN";
            else if (Pattern.matches("^(M|m).*$", paramValues[POS_PARAM]))
                paramValues[POS_PARAM] = "MIDDLE";
            else if (Pattern.matches("^(E|e).*$", paramValues[POS_PARAM]))
                paramValues[POS_PARAM] = "END";
            else {
                throw new IllegalArgumentException(OpticsMessage.POS_PARAM_VAL_ILLEGAL);
            }

            // Check that the POSB position parameter is valid.
            if (Pattern.matches("^(B|b).*$", paramValues[POSB_PARAM]))
                paramValues[POSB_PARAM] = "BEGIN";
            else if (Pattern.matches("^(M|m).*$", paramValues[POSB_PARAM]))
                paramValues[POSB_PARAM] = "MIDDLE";
            else if (Pattern.matches("^(E|e).*$", paramValues[POSB_PARAM]))
                paramValues[POSB_PARAM] = "END";
            else {
                throw new IllegalArgumentException(OpticsMessage.POS_PARAM_VAL_ILLEGAL);
            }

            // Check that the RUNID position parameter is valid. It must be
            // valued "NULL" or 0 or "LATEST" indicating LATEST run,
            // 1 or "GOLD" indicating GOLD, or a positive non-leading-zero integer.
            if (paramValues[RUN_PARAM].equalsIgnoreCase("NULL") || paramValues[RUN_PARAM].equalsIgnoreCase("0")
                    || paramValues[RUN_PARAM].equalsIgnoreCase("LATEST")) {
                paramValues[RUN_PARAM] = "LATEST";
            } else if (paramValues[RUN_PARAM].equalsIgnoreCase("GOLD") || paramValues[RUN_PARAM].equals("1")) {
                paramValues[RUN_PARAM] = "GOLD";
            }
            // A numerical RunID is an optional one or more 0s,
            // followed by a number of at least 2 digits. This
            // distinguishes RUN IDs from 0 = latest, and 1 =
            // gold.
            else {
                try {
                    Integer.parseInt(paramValues[RUN_PARAM]);
                }
                // if ( ! Pattern.matches("^(0+)?[1-9][0-9]+$",
                // paramValues[RUN_PARAM]) )
                catch (Exception n) {
                    IllegalArgumentException ex = new IllegalArgumentException(OpticsMessage.ILLEGALRUN_PARAM);
                    throw ex;
                }
            }

            // If we're logging debug messages, log the
            // parameters understood
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Query made with following parsed parameters:");
                for (int i = 0; i < paramValues.length; i++)
                    logger.fine("\t" + paramNames[i] + " = " + paramValues[i]);
            }

            return paramValues;
        }

        /**
         * Acquires the Twiss of a given device, subject to the parameters given.
         *
         * This method is algrithmically identical to get_rmatA, but is kept separate for the sake of explicit coding.
         */
        private double[] get_twissA(Entity a, String[] params) throws UnableToGetDataException {

            double[] modeldata = null; // The twiss params of device a.
            Integer runID; // The largest (latest) run ID of a
            // model containing the device A.

            // Extract the individual parameters, as passed and defaulted.
            String typeParam = params[TYPE_PARAM], posParam = params[POS_PARAM], runIdParam = params[RUN_PARAM], fakeDataParam = params[FAKEDATA_PARAM];

            // Intercept fakedata request here, and return the static
            // fakedata.
            if (meme_mode == Mode.LOCAL || !fakeDataParam.equals("NULL")) {
                return fakeTwissData;
            }

            // If the runID has been given as a number, use that runID
            // number explicitly, otherwise find the runid number that
            // corresponds to the LATEST or GOLD, and use that number.
            //
            if (!(runIdParam.equals("LATEST") || runIdParam.equals("GOLD"))) {
                try {
                    modeldata = get_twiss(a, posParam, Integer.parseInt(runIdParam));
                    // "NULL" ); // null MODE
                } catch (Exception ex) {
                    throw new UnableToGetDataException("Check use of Run ID " + runIdParam);
                }
            } else {
                // Get run Id number of most recent GOLD or most recent
                // of any model (LATEST).
                runID = get_runID(a.instance(), typeParam,
                // posParam,
                // FULLMACHINE_MODE_DEFAULT,
                        runIdParam); // "GOLD" | "LATEST"
                if (runID == 0) {
                    throw new UnableToGetDataException(a.instance() + " optics not found in " + runIdParam);
                }
                // We have the max runID that contains the
                // device in a GOLD or LATEST run, so get the
                // model data with that runID.
                modeldata = get_twiss(a,
                // typeParam,
                        posParam, runID);
                // "NULL" ); // MODE
            }
            return modeldata;
        }

        /**
         * Acquires the R matrix data of a given device, subject to the parameters given.
         *
         * This method is algrithmically identical to get_twissA, but is kept separate for the sake of explicit coding.
         */
        private double[] get_rmatA(Entity a, String[] params) throws UnableToGetDataException {
            // 6x6 Rmat of A column order (1st 6 are 1st column)
            double[] modeldata = null;
            // The largest (latest) run ID of a model containing the device A.
            Integer runID;

            // Extract the individual parameters, as passed and defaulted.
            String typeParam = params[TYPE_PARAM], posParam = params[POS_PARAM], runIdParam = params[RUN_PARAM], fakeDataParam = params[FAKEDATA_PARAM];

            // Intercept fakedata request here, and return the static
            // fakedata.
            if (meme_mode == Mode.LOCAL || !fakeDataParam.equals("NULL")) {
                return fakeRmatData;
            }

            // If the runID has been given, use that runID;
            if (!(runIdParam.equals("LATEST") || runIdParam.equals("GOLD"))) {
                try {
                    modeldata = get_rmat(a, posParam, Integer.parseInt(runIdParam));
                    // "NULL" ); // MODE
                } catch (Exception ex) {
                    throw new UnableToGetDataException("Check use of Run ID " + runIdParam);
                }
            } else {
                // No specific Run Id given, so find Run ID of most recent
                // run uploaded of the desired run kind (GOLD or simply latest).
                runID = get_runID(a.instance(), typeParam, // "EXTANT" | "DESIGN"
                        runIdParam); // "GOLD" | "LATEST"
                if (runID == 0) {
                    throw new UnableToGetDataException(a.instance() + " optics not found in " + runIdParam);
                }
                // We have the max runID that contains the device,
                // so get the model data with that runID.
                modeldata = get_rmat(a, posParam, runID);
                // "NULL" ); // MODE
            }
            return modeldata;
        }

        // get_twiss ESS
        private double[] get_twiss(Entity q, String posParam, Integer runId) throws UnableToGetDataException {

            // SQL query for Twiss. Note %d and %s for run id and element name.
            final String GETTWISS = "select e.\"EK\", "
                    + "e.\"PSI_X\", e.\"BETA_X\", e.\"ALPHA_X\", e.\"ETA_X\", e.\"ETAP_X\", "
                    + "e.\"PSI_Y\", e.\"BETA_Y\", e.\"ALPHA_Y\", e.\"ETA_Y\", e.\"ETAP_Y\", "
                    + "e.\"ZPOS\", e.\"SUML\", e.\"LEFF\", e.\"SLEFF\", e.\"ORDINAL\" "
                    + "from \"MACHINE_MODEL\".\"ELEMENT_MODELS\" e, " + "\"MACHINE_MODEL\".\"RUNS\" r " + "where "
                    + "e.\"RUNS_ID\"=%d and e.\"ELEMENT_NAME\"='%s' " + "and e.\"RUNS_ID\" = r.\"ID\" "
                    + "ORDER BY e.\"INDEX_SLICE_CHK\"";

            // Substitute in the RunID and instance name given (eg QP1)
            String sql = String.format(GETTWISS, runId, q.instance());
            DBG("sql for twiss", sql);

            // Execute the SQL query and return resulting array of twiss values.
            return connection.executeQuery(sql, indexMap.get(posParam));
        }

        // get_rmat ESS
        private double[] get_rmat(Entity q, String posParam, Integer runId) throws UnableToGetDataException {

            // SQL query for R-matrices. Note %d and %s for run id and element name.
            final String GETRMAT_SQL = "select " + "e.\"R11\",e.\"R12\",e.\"R13\",e.\"R14\",e.\"R15\",e.\"R16\", "
                    + "e.\"R21\",e.\"R22\",e.\"R23\",e.\"R24\",e.\"R25\",e.\"R26\", "
                    + "e.\"R31\",e.\"R32\",e.\"R33\",e.\"R34\",e.\"R35\",e.\"R36\", "
                    + "e.\"R41\",e.\"R42\",e.\"R43\",e.\"R44\",e.\"R45\",e.\"R46\", "
                    + "e.\"R51\",e.\"R52\",e.\"R53\",e.\"R54\",e.\"R55\",e.\"R56\", "
                    + "e.\"R61\",e.\"R62\",e.\"R63\",e.\"R64\",e.\"R65\",e.\"R66\" "
                    + "from \"MACHINE_MODEL\".\"ELEMENT_MODELS\" e, " + "\"MACHINE_MODEL\".\"RUNS\" r " + "where "
                    + "e.\"RUNS_ID\"=%d and e.\"ELEMENT_NAME\"='%s' " + "and e.\"RUNS_ID\" = r.\"ID\" "
                    + "ORDER BY e.\"INDEX_SLICE_CHK\"";

            // Substitute in the RunID and instance name given (eg QP1)
            String sql = String.format(GETRMAT_SQL, runId, q.instance());
            DBG("sql for Rmat", sql);

            // Excecute SQL and return array of 6x6=36 doubles
            return connection.executeQuery(sql, indexMap.get(posParam));
        }

        /**
         * Computes the transfer matrix from a device A to device B, given the additional constraints specified in the
         * given parmameters.
         */
        private double[] get_rmat_AtoB(Entity q, String[] params) throws UnableToGetDataException {

            // 6x6 Rmats of A and B in column order (1st 6 are 1st column)
            double[] rmatA = null; // The 6x6 of device A
            double[] rmatB = null; // The 6x6 of device B
            double[] rmatU = null; // The 6x6 of the upstream device
            double[] rmatD = null; // The 6x6 of the downstream device

            // Extract the individual parameters, as passed and defaulted.
            String typeParam = params[TYPE_PARAM], BParam = params[B_PARAM], posParam = params[POS_PARAM], posBParam = params[POSB_PARAM], runIdParam = params[RUN_PARAM];

            // Extract the names of the devices A and B
            Entity a = q; // Device A, as given in query
            Entity b = new Entity(BParam, true);
            // Device B (as in B=<device> param
            Entity u = null; // The upstream device
            Entity d = null; // The downstream device.
            String posU = null; // The position requested for upstream entity
            String posD = null; // The position requested for downstream entity
            Integer runIdD; // The run ID of the downstream device.

            // Check which device is upstream of the other. This is needed
            // because to compute the transfer matrix, the input rmats must
            // of course come from the same model. We know that by
            // convention all models start at the cathode and go to a
            // desitinaton. Therefore, a model that contains the downsream
            // device must contain the upstream device too. Therefore, to
            // get the rmats of the two devices from the same model, we
            // first establish the runid to be used using the downstream
            // device, and then get the upstream device from the same
            // runid. To so this, we establish the upstream (u) and
            // downstream (d) device names and positions, and later assign
            // the rmats found for u and d to A and B.
            boolean AisDownstreamofB = isDownstream(a, b, params);
            if (AisDownstreamofB) {
                u = b;
                posU = posBParam;
                d = a;
                posD = posParam;
            } else {
                u = a;
                posU = posParam;
                d = b;
                posD = posBParam;
            }

            // First get the Rmat of u and the Rmat of d. Then compute
            // transfer matrix between them. If the RunID was supplied, then
            // get the Rmat using that RunID and ignore anything else.
            // If RunID was not given, look for the latest RunID
            // that contains d.
            //
            // NOTE: If at some time in the future ESS wants to model
            // different lines with different destinations, then any
            // search for a runid from which to get both the Upstream and Downstream
            // device will have to pay attention also to making sure that RunID
            // is for a common destination and possibly also a common timing
            // definition.
            //
            if (!(runIdParam.equals("LATEST") || runIdParam.equals("GOLD"))) {
                try {
                    // Get rmats of u and d devices using
                    // MODE="NULL" since RunId is given.
                    rmatU = get_rmat(u,
                    // typeParam,
                            posU, Integer.parseInt(runIdParam));
                    // "NULL" ); // MODE
                    rmatD = get_rmat(d,
                    // typeParam,
                            posD, Integer.parseInt(runIdParam));
                    // "NULL"); // MODE
                } catch (Exception ex) {
                    throw new UnableToGetDataException("Check use of Run ID " + runIdParam);
                }
            } else {
                // RunId has not been given, so we must find the max
                // runID (using get_RunID) that contains the downstream
                // device d.
                //
                // NOTE: Let's assume that if the model run contains D it
                // will contain U too. If we were not to assume that, and
                // instead look explicitly for a model run which contained
                // both, its possible that a typo in giving the name of U
                // could result in successfully getting a very old model.
                // Better just to fail to get the model if the most recent
                // run containg D does not also contain U.
                runIdD = get_runID(d.instance(), typeParam,
                // posD,
                // modeParam,
                        runIdParam);
                if (runIdD == 0) {
                    throw new UnableToGetDataException(d.instance() + " was not found in a model of type " + typeParam);
                }
                // Now we have a runId for d, get both rmats for
                // both u and d device (A and B) using that
                // runID (ignoring now MODE).
                rmatD = get_rmat(d, posD, runIdD);
                // "NULL"); // MODE
                // get U with downstream's runID
                rmatU = get_rmat(u, posU, runIdD);
                // "NULL"); // MODE
            } // endif RunID given switch

            // Assign rmats for A and B depending on which was, in fact,
            // upstream and downstream.
            if (AisDownstreamofB) {
                rmatA = rmatD;
                rmatB = rmatU;
            } else {
                rmatA = rmatU;
                rmatB = rmatD;
            }

            // Now we have the Rmat of A and of B, compute the transfer matrix.
            Matrix A = new Matrix(rmatA, 6).transpose();
            if (logger.isLoggable(Level.FINE))
                printMatrix("rmatA", A);
            Matrix B = new Matrix(rmatB, 6).transpose();
            if (logger.isLoggable(Level.FINE))
                printMatrix("rmatB", B);
            Matrix RmatAB = B.times(A.inverse());
            if (logger.isLoggable(Level.FINE))
                printMatrix("RmatAB", RmatAB);

            return RmatAB.getRowPackedCopy();
        }

        /**
         * get_runID returns the model upload RunID of the chronologically most recently uploaded model for the device
         * matching the input arguments, if one exists, and 0 otherwise; in this signature, the run param can be used to
         * confine the search to GOLD models.
         *
         * @param instance
         *            An EPICS channel access addressable device name, or more specifically an XAL element EPICS name,
         *            which should match an Element in the model database.
         * @param type
         *            A model upload "type" - valid values are "EXTANT" or "DESIGN".
         * @param run
         *            Specifies whether the search should be for the "LATEST" or "GOLD" model.
         * 
         *            <pre>
         * type      | run    | gets id matching
         * -----------------------------------------------------------------
         * EXTANT    | LATEST | most recent run on real pv settings 
         * EXTANT    | GOLD   | most recent gold designated run on real pv settings 
         * DESIGN    | LATEST | most recent run on design pv settings 
         * DESIGN    | GOLD   | most recent gold designated run on design pv settings
         * </pre>
         *
         * @return An int giving the highest valued Run ID which matched the input arguments, or 0 if no model matching
         *         the input arguments was found.
         */
        private int get_runID(String instance, String type,
        // String pos,
        // String mode,
                String run) {
            // The basic query strings for the 2 cases of whether simply the
            // latest run id is wanted of extant or design, or specfically the
            // golden model. Note the %s for the EXTANT or DESIGN to be substituted.
            final String SQL_GETLATESTRUNID = "select max(e.\"RUNS_ID\") from "
                    + "\"MACHINE_MODEL\".\"ELEMENT_MODELS\" e, \"MACHINE_MODEL\".\"RUNS\""
                    + " r where r.\"RUN_SOURCE_CHK\"='%s' and e.\"RUNS_ID\" = r.\"ID\"";
            final String SQL_GETLATESTRUNID_GOLD = "select max(e.\"RUNS_ID\") from "
                    + "\"MACHINE_MODEL\".\"ELEMENT_MODELS\" e, \"MACHINE_MODEL\".\"RUNS\" r, "
                    + "\"MACHINE_MODEL\".\"GOLD\" g where "
                    + "r.\"RUN_SOURCE_CHK\"='%s' and e.\"RUNS_ID\" = r.\"ID\" and " + "g.\"RUNS_ID\" = r.\"ID\"";

            String sql = null; // The sql to execute to get run id.
            double[] dbresult = null; // The result of the SQL query.

            if (run.equals("LATEST"))
                sql = String.format(SQL_GETLATESTRUNID, type);
            else
                sql = String.format(SQL_GETLATESTRUNID_GOLD, type);

            DBG("get runID sql", sql);

            try {
                dbresult = connection.executeQuery(sql, 1);
            } catch (Exception ex) {
                logger.warning("Problem running database query for getting run id: " + ex.toString());
                return 0;
            }

            // We use the same basic query wrapper for all db I/O, which returns
            // an array of double, but the run id is one int. So convert it.
            return (new Double(dbresult[0])).intValue();
        }

        /**
         * get_runID returns the model upload RunID of the chronologically most recently uploaded model for the device
         * matching the input arguments, if one exists, and 0 otherwise.
         *
         * @param instance
         *            An EPICS channel access addressible device name, or more sepcifically an XAL element EPICS name,
         *            which should match an Element in the model upload/Symbols table.
         * @param type
         *            A model upload type - valid values are "EXTANT" or "DESIGN".
         * @return An int giving the highest valued Run ID which matched the input arguments, or 0 if no model matching
         *         the input arguments was found.
         */
        private int get_runID(String instance, String type)
        // String pos,
        // String mode )
        {
            return get_runID(instance, type, "LATEST");
        }

        /**
         * Is element A downstream of element B, established by comparison of phase advances.
         *
         * @throws UnableToGetDataException
         *             if inconsistent phase advances across X and Y.
         */
        private boolean isDownstream(Entity a, Entity b, String[] params) throws UnableToGetDataException {

            // Ensure that POS param used for B, is that whch was passed in
            // POSB. Especially important when a and b are in fact the same
            // device!
            double[] A_twiss = get_twissA(a, params);
            String[] paramsB = params;
            paramsB[POS_PARAM] = params[POSB_PARAM];
            double[] B_twiss = get_twissA(b, paramsB);

            // Check that A's X and Y phases advances are > or < B in both
            // planes consistently.
            if (A_twiss[PSIX] > B_twiss[PSIX] && A_twiss[PSIY] < B_twiss[PSIY] || A_twiss[PSIX] < B_twiss[PSIX]
                    && A_twiss[PSIY] > B_twiss[PSIY])
                throw new UnableToGetDataException(OpticsMessage.INCONSISTENT_PSI);

            // If consistent, any one of these will do to establish
            // downstreamness.
            return (A_twiss[PSIX] > B_twiss[PSIX]);
        }

        /**
         * Static method for issuing messages if static "debug" flag is on. This method should be inlined by the
         * compiler.
         */
        private static final void DBG(String context, Object Msg) {
            if (logger.isLoggable(Level.FINE))
                logger.fine(context + " : " + Msg.toString());
        }

        // TODO: redirect stdout to log.
        private static void printMatrix(String label, Matrix M) {
            System.out.println(label);
            M.print(8, 4);
        }

    }

    public static void main(String[] args) throws PVAException {

        // Optics service's connecton to its backend database.
        OpticsServiceConnection opticsConnection = null;

        // Get service name from property if given.
        String server_name = System.getProperty("SERVER_NAME", "meme_optics");

        // Initialize logging - now done through properties file.
        // ConsoleLogHandler.defaultConsoleLogging( LOG_LEVEL_DEFAULT );
        // ConsoleLogHandler.defaultConsoleLogging( Level.FINE) ;
        logger.info("SERVICES OF SERVER \"" + server_name + "\" is/are initializing...");

        // Get MEME runtime config for this execution of this service.
        String prop = System.getProperty("MEME_MODE", "PROD").toUpperCase();
        meme_mode = Mode.modeMap.get(prop);
        if (meme_mode != null)
            logger.info("MEME_MODE :" + prop);
        else {
            String _msg = "MEME_MODE property value given \"" + prop + "\" is not valid;" + " valid MEME_MODES are: "
                    + Mode.modeMap.keySet();
            logger.severe(_msg);
            logger.severe("Aborting, previous errors prohibit execution");
            throw new PVAException(_msg);
        }

        // Get Data mode - now just whether it's real or fake, but in future
        // may be more sophisticated
        prop = System.getProperty("DATA_MODE", "REAL").toUpperCase();
        ;
        data_mode = DataMode.modeMap.get(prop);
        if (data_mode != null)
            logger.info("DATA_MODE :" + prop);
        else {
            String _msg = "DATA_MODE property value given \"" + prop + "\" is not valid;" + " valid DATA_MODES are: "
                    + DataMode.modeMap.keySet();
            logger.severe(_msg);
            logger.severe("Aborting, previous errors prohibit execution");
            throw new PVAException(_msg);
        }

        // Initialize backend database connection unless MEME server is run in
        // in fake_data mode
        final String OPTICS_SERVICE_NAME = "optics";
        if (data_mode != DataMode.FAKE)
            opticsConnection = new OpticsServiceConnection(OPTICS_SERVICE_NAME);

        // Instantiate a service instance.
        RPCServer server = new RPCServer();

        // Register optics service, giving it the database connection.
        server.registerService(OPTICS_SERVICE_NAME, new OpticsServiceImpl(opticsConnection));
        logger.info("SERVICE \"" + OPTICS_SERVICE_NAME + "\" is operational in " + Mode.modes[meme_mode]
                + " mode, returning " + DataMode.datamode_names[data_mode] + " data.");

        // Print server startup details.
        server.printInfo();

        // Start the service.
        server.run(0);
    }

}
