/** -*-java-*- 
 * TestServerClient is a simple client of the MEME Test Service, used for
 * testing and MEME and the V4 framework.
 */
package edu.stanford.slac.meme.service.testservice;

import java.util.logging.Level;
import java.util.logging.Logger; // Prerequisite of ConsoleLogHandler pvAccess utility.

import org.epics.pvaccess.ClientFactory; // Client side constructor.
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException; // Standard EPICS service exception.
import org.epics.pvaccess.util.logging.ConsoleLogHandler; // Logging.
import org.epics.pvdata.factory.FieldFactory; // 
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.util.namedValues.NamedValues;
import org.epics.pvdata.util.namedValues.NamedValuesFormatter;
import org.epics.pvdata.util.pvDataHelper.GetHelper;

/**
 * TestServerClient is a simple client of the MEME testService.
 *
 * Usage:
 *
 * java TestServerClient quad45:bdes/history java TestServerClient quad45:bdes/history 2011-09-16T02.12.00 java
 * TestServerClient quad45:bdes/history 2011-09-16T02.12.00 2011-09-16T10.01.03
 * 
 * @author Greg White (greg@slac.stanford.edu) 22-Sep-2011
 * @version Greg White (greg@slac.stanford.edu) 15-Jan-2013 Updated for conformance to NTTable.
 *
 */
public class TestServerClient {
    // Instantiate the Java logging API.
    private static final Logger logger = Logger.getLogger(TestServerClient.class.getName());
    private static final String CHANNEL_NAME = "archiveservice"; // Name of service to contact within the test server.

    // The testServiceClient expects to get returned in a PVStructure which conforms to the
    // definition of the NTTable Normative Type. See EPICS V4 Normative Types for definition.
    private static final String NTNAMESPACE = "epics:nt";
    private static final String NTTABLE_ID = NTNAMESPACE + "/" + "NTTable:1.0";
    private static final String NTURI_ID = NTNAMESPACE + "/" + "NTURI:1.0";

    // Error exit codes
    @SuppressWarnings("unused")
    private static final int NOTNORMATIVETYPE = 1;
    private static final int NOTNTTABLETYPE = 2;
    private static final int NODATARETURNED = 3;
    private static final int NOARGS = 4;

    private final static double TIMEOUT_SEC = 5.0;

    private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private final static Structure queryStructure = fieldCreate.createStructure(new String[] { "entity", "starttime",
            "endtime" },
            new Field[] { fieldCreate.createScalar(ScalarType.pvString), fieldCreate.createScalar(ScalarType.pvString),
                    fieldCreate.createScalar(ScalarType.pvString) });
    private final static Structure uriStructure = fieldCreate.createStructure(NTURI_ID, new String[] { "scheme",
            "query" }, new Field[] { fieldCreate.createScalar(ScalarType.pvString), queryStructure });

    /**
     * Main of testService client.
     * 
     * @param args
     *            args[0] must be the name of a query that the server will understand; effectively the "key" of a SQL
     *            SELECT statement, in a lookup table mapping keys to SELECT statements maintained by the server. Eg
     *            "swissFEL:allQuads".
     */
    public static void main(String[] args) {
        // Initialize Console logging.
        // To turn on debugging messages, set this to INFO.
        ConsoleLogHandler.defaultConsoleLogging(Level.WARNING);

        if (args.length <= 0) {
            logger.log(Level.SEVERE, "No name of a PV was given; exiting.");
            System.exit(NOARGS);
        }

        ClientFactory.start();
        RPCClientImpl client = new RPCClientImpl(CHANNEL_NAME);
        PVStructure pvRequest = PVDataFactory.getPVDataCreate().createPVStructure(uriStructure);
        pvRequest.getStringField("scheme").put("pva");
        PVStructure pvQuery = pvRequest.getStructureField("query");
        if (args[0] != null)
            pvQuery.getStringField("entity").put(args[0]);
        if (args.length >1 && args[1] != null)
            pvQuery.getStringField("starttime").put(args[1]);
        if (args.length >2 && args[2] != null)
            pvQuery.getStringField("endtime").put(args[2]);

        // Execute the service request for data subject to the arguments
        // constructed above.
        //
        PVStructure pvResult = null;
        try {
            // Actual wire i/o to get the data from the server
            pvResult = client.request(pvRequest, TIMEOUT_SEC);

            // Verify result validity, unpack and display it.
            if (pvResult != null) {
                // Check the result PVStructure is of the type we
                // expect. It should be an NTTABLE.
                String type = pvResult.getStructure().getID();
                if (!type.equals(NTTABLE_ID)) {
                    logger.log(Level.SEVERE, "Unable to get data: unexpected data " + "structure returned from "
                            + CHANNEL_NAME + "; expected returned data id member value" + NTTABLE_ID
                            + " but found type = " + type);
                    System.exit(NOTNTTABLETYPE);
                }

                PVStringArray pvColumnTitles = (PVStringArray) pvResult.getScalarArrayField("labels",
                        ScalarType.pvString);
                PVStructure pvTableData = pvResult.getStructureField("value");
                int Ncolumns = pvTableData.getNumberFields();

                PVField[] pvColumns = pvTableData.getPVFields();
                if (Ncolumns <= 0 || pvColumns.length <= 0) {
                    logger.log(Level.SEVERE, "No data fields returned from " + CHANNEL_NAME + ".");
                    System.exit(NODATARETURNED);
                }

                // To print the contents of the NTTable conforming
                // PVstructure, make a NamedValues system from its
                // data.
                //
                StringArrayData data = new StringArrayData();
                int labelOffset = 0;
                NamedValues namedValues = new NamedValues();
                for (PVField pvColumnIterator : pvColumns) {
                    // Get the column name.
                    pvColumnTitles.get(labelOffset, 1, data);
                    String fieldName = data.data[labelOffset++];

                    if (pvColumnIterator.getField().getType() == Type.scalarArray) {
                        ScalarArray scalarArray = (ScalarArray) pvColumnIterator.getField();
                        // Double array
                        if (scalarArray.getElementType() == ScalarType.pvDouble) {
                            PVDoubleArray pvDoubleArray = (PVDoubleArray) pvColumnIterator;
                            namedValues.add(fieldName, GetHelper.getDoubleVector(pvDoubleArray));
                        } else if (scalarArray.getElementType() == ScalarType.pvLong) {
                            PVLongArray pvLongArray = (PVLongArray) pvColumnIterator;
                            namedValues.add(fieldName, GetHelper.getLongVector(pvLongArray));
                        } else if (scalarArray.getElementType() == ScalarType.pvByte) {
                            PVByteArray pvByteArray = (PVByteArray) pvColumnIterator;
                            namedValues.add(fieldName, GetHelper.getByteVector(pvByteArray));
                        } else if (scalarArray.getElementType() == ScalarType.pvString) {
                            PVStringArray pvStringArray = (PVStringArray) pvColumnIterator;
                            namedValues.add(fieldName, GetHelper.getStringVector(pvStringArray));
                        } else {
                            // Array types other than those
                            // above are not understood by
                            // this client.
                            //
                            logger.log(Level.SEVERE, "Unexpected array type returned from " + CHANNEL_NAME
                                    + "; only pvData scalarArray types pvDouble, pvLong, pvByte or pvString expected");
                        }
                    } else {
                        logger.log(Level.SEVERE, "Unexpected non-array field returned from " + CHANNEL_NAME
                                + ".\n Field named \'" + fieldName + "\' is not of scalarArray type, "
                                + "and so can not be interpretted as a data column.");
                    }
                }

                // Format the NTTable we got from the server as a
                // familiar looking table. To do so, set up a
                // printout formatter for NamedValues give it our
                // constructed namedValues (the data that came back
                // from the server. Then ask the formatter to print
                // it to System.out.
                //
                NamedValuesFormatter formatter = NamedValuesFormatter.create(NamedValuesFormatter.STYLE_COLUMNS);
                formatter.setWhetherDisplayLabels(true);
                formatter.assignNamedValues(namedValues);
                formatter.display(System.out);
            }
            logger.log(Level.INFO, pvResult.toString());

        } catch (RPCRequestException rre) {
            logger.log(Level.SEVERE, "Acquisition of " + args[0] + " query was not successful.\n" + "Channel "
                    + CHANNEL_NAME + " connected but issued exception. Check validity and spelling:", rre);
        } catch (IllegalStateException rre) {
            logger.log(Level.SEVERE, "Acquisition not successful, failed to connect to " + CHANNEL_NAME, rre);
        } finally {
            // Clean up
            client.destroy();
            ClientFactory.stop();
        }

        logger.log(Level.INFO, "Completed Successfully");
        System.exit(0);
    }

}
