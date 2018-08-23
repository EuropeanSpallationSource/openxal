/*
 * Copyright (c) 2017, Open XAL Collaboration
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xal.app.scanner;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.Timestamp;
import xal.smf.Accelerator;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

import xal.extension.fxapplication.XalFxDocument;

/**
 *
 * @author yngvelevinsen
 */
public class ScannerDocument extends XalFxDocument {
    /**
     * A dictionary of the created datasets..
     */
    private Map<String, double[][]> dataSets;
    /**
     * For every measurement, store the channels read for this measurement..
     */
    private Map<String, List<Channel>> allPVrb;
    /**
     * For every measurement, store the channels written to for this measurement..
     */
    private Map<String, List<Channel>> allPVw;
    /**
     * For every measurement, store time stamps of all read variables here
     */
    private Map<String, Timestamp[][]> allTimestamps;

    /**
     * The number of different scans that have been done
     */
    public SimpleIntegerProperty numberOfScans;

    public double[][] currentMeasurement;
    public Timestamp[][] currentTimestamps;

    // Use this as a trigger to update the GUI so that we can continue a loaded half-finished measurement
    public SimpleBooleanProperty currentMeasurementWasLoaded;

    // The current number of measurement points done
    public int nCombosDone;

    /**
     * To calculate constraints, we need to know the short hand variable name
     * for each variable..
     */
    public ObservableList<String> constraints;

    /**
     * The delay between successive measurements in milliseconds
     */
    public SimpleLongProperty delayBetweenMeasurements;

    /**
     * The number of measurements to take at each location
     */
    public SimpleIntegerProperty numberMeasurementsPerCombo;

    // The channels that may be scanned or only read
    public ObservableList<ChannelWrapper> pvChannels;
    // The combination of scan points (each double[] is equal to number of writeables)
    public List<double[]> combos;

    // Save/restore parameters..
    private final String SCANNER_SR;
    private final String CHANNELS_SR;
    private final String MEASUREMENTS_SR;
    private final String CONSTRAINTS_SR;
    private final String CURRENTMEAS_SR;
    private final String SETTINGS_SR;
    private final String TIMESTAMPS_SR;
    private final String TITLE_SR;
    private final String ACTIVE_SCAN_SR;
    private final String ACTIVE_READ_SR;
    private final String NAME_SR;

    private final SimpleDateFormat TIMEFORMAT_SR;

    private XmlDataAdaptor da;
    private DataAdaptor currentMeasAdaptor;

    // -- Constructors --

    /**
     *  Create a new empty ScanDocument1D
     *
     * @param stage The stage for this application
     */
    public ScannerDocument(Stage stage) {
        super(stage);

        SCANNER_SR = "ScannerData";
        CHANNELS_SR = "Channels";
        MEASUREMENTS_SR = "measurements";
        CONSTRAINTS_SR = "constraints";
        CURRENTMEAS_SR = "currentMeasurement";
        SETTINGS_SR = "settings";
        TIMESTAMPS_SR = "timestamps";
        TITLE_SR = "title";
        ACTIVE_SCAN_SR = "active_scan";
        ACTIVE_READ_SR = "active_read";
        NAME_SR = "name";
        TIMEFORMAT_SR = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        dataSets = new HashMap<>();
        allPVrb = new HashMap<>();
        allPVw = new HashMap<>();
        allTimestamps = new HashMap<>();
        pvChannels = FXCollections.observableArrayList();
        combos = new ArrayList<>();
        constraints = FXCollections.observableArrayList("", "", "", "");
        numberOfScans = new SimpleIntegerProperty(0);

        numberMeasurementsPerCombo = new SimpleIntegerProperty(1);
        delayBetweenMeasurements = new SimpleLongProperty(1500);

        currentMeasurementWasLoaded = new SimpleBooleanProperty(false);

        DEFAULT_FILENAME="Data.scan.xml";
        WILDCARD_FILE_EXTENSION = "*.scan.xml";
        HELP_PAGEID="227688413";
    }


    /**
     *  Create a new document loaded from the URL file
     *
     *@param  url  The URL of the file to load into the new document.
     */
    public ScannerDocument(URL url, Stage stage) {
        this(stage);
        if (url == null) {
                return;
        }
        Logger.getLogger(ScannerDocument.class.getName()).log(Level.FINER, "Loading {0}", url);

        setSource(new File(url.getFile()));
        readScanDocument(url);

        //super class method - will show "Save" menu active
        if (url.getProtocol().equals("jar")) {
                return;
        }
        setHasChanges(false);
    }

    /**
     *  Reads the content of the document from the specified URL.
     *
     *@param  url  Description of the Parameter
     */
    public final void readScanDocument(URL url) {

        DataAdaptor readAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
        if (readAdaptor != null) {
            Logger.getLogger(ScannerDocument.class.getName()).log(Level.INFO, "Will read document {0}", url.getFile());
        }

    }


    // -- Getters/setters --

    public Map<String, double[][]> getDataSets() {
        return dataSets;
    }

    public double[][] getDataSet(String setName) {
        return dataSets.get(setName);
    }

    public void setDataSet(String setName, double[][] data) {
        dataSets.put(setName, data);
    }


    public Map<String, List<Channel>> getAllPVrbData() {
        return allPVrb;
    }

    public List<Channel> getPVrbData(String setName) {
        return allPVrb.get(setName);
    }

    public void setPVreadbackData(String setName, List<Channel> data) {
        allPVrb.put(setName, data);
    }

    public void setPVreadbackData(String setName) {
        setPVreadbackData(setName, getActivePVreadables().map(cw -> cw.getChannel()).collect(Collectors.toList()));
    }


    public Map<String, List<Channel>> getAllPVWriteData() {
        return allPVw;
    }

    public List<Channel> getPVWriteData(String setName) {
        return allPVw.get(setName);
    }

    public void setPVwriteData(String setName, List<Channel> data) {
        allPVw.put(setName, data);
    }

    public void setPVwriteData(String setName) {
        setPVwriteData(setName, getActivePVwritebacks().map(cw -> cw.getChannel()).collect(Collectors.toList()));
    }


    public Map<String, Timestamp[][]> getAllTimestamps() {
        return allTimestamps;
    }

    public Timestamp[][] getTimestamps(String setName) {
        return allTimestamps.get(setName);
    }

    public void setTimestamps(String setName, Timestamp[][] timestamps) {
        allTimestamps.put(setName, timestamps);
    }

    public void setTimestamps(String setName) {
        setTimestamps(setName, currentTimestamps);
    }

    // -- File I/O --

    /**
     *  Save the ScannerDocument document to the specified URL.
     *
     *  @param  url  The file URL where the data should be saved
     */
    @Override
    public void saveDocumentAs(URL url) {
        Logger.getLogger(ScannerDocument.class.getName()).log(Level.FINER, "Saving document, filename {0}", url);
        initDocumentAdaptor();
        DataAdaptor scannerAdaptor =  da.childAdaptor(SCANNER_SR);
        currentMeasAdaptor = null;
        scannerAdaptor.setValue(TITLE_SR, url.getFile());
        scannerAdaptor.setValue("date", TIMEFORMAT_SR.format(new Date()));


        // Store the settings..
        DataAdaptor settingsAdaptor = scannerAdaptor.createChild(SETTINGS_SR);
        settingsAdaptor.setValue("MeasurementDelay", delayBetweenMeasurements.get());
        settingsAdaptor.setValue("MeasurementPerCombo", numberMeasurementsPerCombo.get());

        // Store information about all measurements done..
        DataAdaptor measurementsScanner = scannerAdaptor.createChild(MEASUREMENTS_SR);
        dataSets.entrySet().forEach( measurement -> {
            // convenience variables..
            List<Channel> pvW = allPVw.get(measurement.getKey());
            List<Channel> pvR = allPVrb.get(measurement.getKey());
            Timestamp[][] tstamps = allTimestamps.get(measurement.getKey());
            DataAdaptor measurementAdaptor = measurementsScanner.createChild("measurement");
            measurementAdaptor.setValue(TITLE_SR, measurement.getKey());
            for (int i=0;i<measurement.getValue()[0].length;i++) {
                DataAdaptor channelAdaptor = measurementAdaptor.createChild("channel");
                if (i<pvW.size()) {
                    channelAdaptor.setValue(NAME_SR, pvW.get(i).getId());
                    channelAdaptor.setValue("type", "w");
                } else {
                    channelAdaptor.setValue(NAME_SR, pvR.get(i-pvW.size()).getId());
                    channelAdaptor.setValue("type", "r");
                }
                double[] data = new double[measurement.getValue().length];
                String tstamps_str = "";
                for (int j = 0;j<measurement.getValue().length;j++) {
                    data[j]=measurement.getValue()[j][i];
                    if (i>=pvW.size()) {
                        if (j!=0)
                            tstamps_str=tstamps_str.concat(", ");
                        tstamps_str=tstamps_str.concat(tstamps[j][i-pvW.size()].toBigDecimal().toString());
                    }
                    channelAdaptor.setValue("data", data);
                }
                channelAdaptor.setValue("data", data);
                if (i>=pvW.size())
                    channelAdaptor.setValue(TIMESTAMPS_SR, tstamps_str);
            }
        });
        // Store information about current measurement setup..

        // Store list of variables to read & write.. ChannelWrapper objects
        DataAdaptor scanpvScanner = scannerAdaptor.createChild(CHANNELS_SR);
        pvChannels.forEach( pv -> {
            DataAdaptor scan_PV_name =  scanpvScanner.createChild("PV");
            scan_PV_name.setValue(NAME_SR, pv.getChannelName() );
            scan_PV_name.setValue("min", pv.minProperty().get() );
            scan_PV_name.setValue("max", pv.maxProperty().get() );
            scan_PV_name.setValue("npoints", pv.npointsProperty().get() );
            scan_PV_name.setValue("instance", pv.instanceProperty().get() );
            scan_PV_name.setValue(ACTIVE_SCAN_SR, pv.isScannedProperty().get() );
            scan_PV_name.setValue(ACTIVE_READ_SR, pv.isReadProperty().get() );
        });

        DataAdaptor constraintsAdaptor = scannerAdaptor.createChild(CONSTRAINTS_SR);
        constraints.forEach( constraint -> {
            if (! constraint.isEmpty())
                constraintsAdaptor.createChild("constraint").setValue("value", constraint);
            });

        da.writeToUrl( url );
        Logger.getLogger(ScannerDocument.class.getName()).log(Level.FINEST, "Saved document");
    }

    public void saveCurrentMeas(int nmeas) {
        if (da == null)
            initDocumentAdaptor();
        if (currentMeasAdaptor==null) {
            currentMeasAdaptor=da.childAdaptor(SCANNER_SR).createChild(CURRENTMEAS_SR);
        }

        String tstamps_str = "";
        for(Timestamp tstamp : currentTimestamps[nmeas]) {
            if (tstamps_str!="")
                tstamps_str=tstamps_str.concat(", ");
            tstamps_str=tstamps_str.concat(tstamp.toBigDecimal().toString());
        }
        DataAdaptor stepAdaptor = currentMeasAdaptor.createChild("step");
        stepAdaptor.setValue("values", currentMeasurement[nmeas]);
        stepAdaptor.setValue(TIMESTAMPS_SR, tstamps_str);

        if (source!=null)
            da.writeToUrl( source );
    };

    /**
     *  Reads the content of the document from the specified URL, and loads the information into the application.
     *
     * @param  url  The path to the XML file
     */
    public void loadDocument(URL url) {
        DataAdaptor readAdp = XmlDataAdaptor.adaptorForUrl( url, false );
        DataAdaptor scannerAdaptor =  readAdp.childAdaptor(SCANNER_SR);

        Accelerator acc = Model.getInstance().getAccelerator();

        // Load the settings
        DataAdaptor settingsAdaptor = scannerAdaptor.childAdaptor(SETTINGS_SR);
        delayBetweenMeasurements.set(settingsAdaptor.longValue("MeasurementDelay"));
        numberMeasurementsPerCombo.set(settingsAdaptor.intValue("MeasurementPerCombo"));

        // Load list of variables to read & write.. ChannelWrapper objects
        DataAdaptor channelScanner = scannerAdaptor.childAdaptor(CHANNELS_SR);
        pvChannels.clear();
        channelScanner.childAdaptors().forEach( childAdaptor -> {
            String name = childAdaptor.stringValue(NAME_SR);
            boolean active_scan = childAdaptor.booleanValue(ACTIVE_SCAN_SR);
            boolean active_read = childAdaptor.booleanValue(ACTIVE_READ_SR);
            Logger.getLogger(ScannerDocument.class.getName()).log(Level.FINER, "Loading PV {0}, scan: {1}, read: {2}", new Object[]{name, active_scan, active_read});

            double min = childAdaptor.doubleValue("min");
            double max = childAdaptor.doubleValue("max");
            int npoints = childAdaptor.intValue("npoints");
            String instance = childAdaptor.stringValue("instance");

            Channel chan = acc.channelSuite().getChannelFactory().getChannel(name);

            ChannelWrapper cWrap = new ChannelWrapper(chan);
            cWrap.isScannedProperty().set(active_scan);
            cWrap.isReadProperty().set(active_read);
            cWrap.minProperty().set(min);
            cWrap.maxProperty().set(max);
            cWrap.npointsProperty().set(npoints);
            // if instance equals x0 it means the variable was never used.
            if (!instance.equals("x0"))
                cWrap.forceInstance(instance);

            pvChannels.add(cWrap);
        });

        // Load all constraints from the file
        DataAdaptor constraintsAdaptor = scannerAdaptor.childAdaptor(CONSTRAINTS_SR);
        for(int i = 0; i<constraints.size();i++)
            constraints.set(i, "");
        for (int i = 0; i<constraintsAdaptor.childAdaptors().size();i++) {
            DataAdaptor childAdaptor = constraintsAdaptor.childAdaptors().get(i);
            constraints.set(i,childAdaptor.stringValue("value"));
        }


        // Load earlier measurements..
        if ( scannerAdaptor.childAdaptor(MEASUREMENTS_SR) != null) {
            DataAdaptor measurementsScanner = scannerAdaptor.childAdaptor(MEASUREMENTS_SR);
            measurementsScanner.childAdaptors().forEach( measAdaptor -> {
                Logger.getLogger(ScannerDocument.class.getName()).log(Level.FINEST, "Loading measurement {0}", measAdaptor.stringValue(TITLE_SR));
                List<Channel> pvW = new ArrayList<>();
                List<Channel> pvR = new ArrayList<>();

                int numCombos = measAdaptor.childAdaptors().get(0).doubleArray("data").length;
                int numChannels = measAdaptor.childAdaptors().size();
                int numReadChannels = (int) measAdaptor.childAdaptors().stream().filter( childAdaptor -> ("r".equals(childAdaptor.stringValue("type"))) ).count();

                double[][] data = new double[numCombos][numChannels];
                Timestamp[][] tstamps = new Timestamp[numCombos][numReadChannels];
                int iReadChan = 0;
                for (int ichan=0;ichan<numChannels;ichan++) {
                    DataAdaptor chanAdaptor = measAdaptor.childAdaptors().get(ichan);
                    boolean isRead = "r".equals(chanAdaptor.stringValue("type"));
                    boolean isWrite = "w".equals(chanAdaptor.stringValue("type"));
                    double[] channelData = chanAdaptor.doubleArray("data");
                    for (int icombo=0;icombo<numCombos;icombo++) {
                        data[icombo][ichan] = channelData[icombo];
                    }
                    if (isRead) {
                        String[] tstampData = chanAdaptor.stringValue(TIMESTAMPS_SR).split(", ");
                        for (int icombo=0;icombo<numCombos;icombo++) {
                            tstamps[icombo][iReadChan] = new Timestamp(new BigDecimal(tstampData[icombo]));
                        }
                        iReadChan+=1;
                    }
                    Channel chan = acc.channelSuite().getChannelFactory().getChannel(chanAdaptor.stringValue(NAME_SR));
                    if (isWrite) {
                        pvW.add(chan);
                    } else if (isRead) {
                        pvR.add(chan);
                    }
                }
                dataSets.put(measAdaptor.stringValue(TITLE_SR), data);
                allPVw.put(measAdaptor.stringValue(TITLE_SR), pvW);
                allPVrb.put(measAdaptor.stringValue(TITLE_SR), pvR);
                allTimestamps.put(measAdaptor.stringValue(TITLE_SR), tstamps);
                numberOfScans.set(numberOfScans.get()+1);

            });
        }

        currentMeasAdaptor = scannerAdaptor.childAdaptor(CURRENTMEAS_SR);
        if ( currentMeasAdaptor != null) {
            // Need to calculate nmeas (or ncombos if you want)
            int nStepsTotal = calculateCombos();
            nCombosDone=currentMeasAdaptor.childAdaptors().size();
            Logger.getLogger(ScannerDocument.class.getName()).log(Level.FINEST, "Loading unfinished measurement, found {0} measurement points", nCombosDone);
            int nActiveWrites = (int) getActivePVwritebacks().count();
            int nActiveReads = (int) getActivePVreadables().count();
            int nActiveChannels = nActiveWrites + nActiveReads;
            currentMeasurement = new double[nStepsTotal][nActiveChannels];
            currentTimestamps = new Timestamp[2+(nStepsTotal-2)*numberMeasurementsPerCombo.get()][(int) getActivePVreadables().count()];
            for(int i = 0;i<nCombosDone;i++) {
                double [] values = currentMeasAdaptor.childAdaptors().get(i).doubleArray("values");
                String[] tstamps = currentMeasAdaptor.childAdaptors().get(i).stringValue(TIMESTAMPS_SR).split(", ");
                for (int j=0;j<nActiveChannels;j++) {
                    currentMeasurement[i][j] = values[j];
                    if (j>=getActivePVwritebacks().count()) {
                        currentTimestamps[i][j-nActiveWrites] = new Timestamp(new BigDecimal(tstamps[j-nActiveWrites]));
                    }
                }
            }
            currentMeasurementWasLoaded.set(true);
            // TODO Is there a way to trigger plot of the current measurement here?
        }

    }

    // -- Functions --


    private void initDocumentAdaptor() {
        da = XmlDataAdaptor.newEmptyDocumentAdaptor();
        da.createChild(SCANNER_SR);

    }

    public Stream<ChannelWrapper> getActivePVreadables() {
        return pvChannels.stream().filter( channel -> (channel.getIsRead()));
    }

    public Stream<ChannelWrapper> getActivePVwritebacks() {
        return pvChannels.stream().filter( channel -> (channel.getIsScanned()));
    }

    /**
     *
     * @param i the channel index (counting active only)
     * @return Return the i'th active readable channel
     */
    public ChannelWrapper getActivePVreadable(int i) {
        int j=-1;
        for (ChannelWrapper cw : pvChannels) {
            if (cw.getIsRead())
                j++;
            if (j==i)
                return cw;
        }
        return null;
    }

    /**
     *
     * @param i the channel index (counting active only)
     * @return Return the i'th active scannable channel
     */
    public ChannelWrapper getActivePVwriteback(int i) {
        int j=-1;
        for (ChannelWrapper cw : pvChannels) {
            if (cw.getIsScanned())
                j++;
            if (j==i)
                return cw;
        }
        return null;
    }

    private boolean hasConstraints() {
        return constraints.stream().anyMatch( constraint -> (constraint.trim().length()>0));
    }

    /**
     * Check a combo for any of the potentially defined constraints
     *
     * TODO there are probably better/safer ways to do this?
     *
     * @param combo
     * @return whether or not combo pass all constraints
     * @throws ScriptException
     */
    public boolean checkConstraints(double[] combo) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        for(int i=0;i<combo.length;i++) {
            engine.eval(pvChannels.get(i).instanceProperty().get()+"="+combo[i]);
        }
        for(String constraint:constraints) {
            if (constraint.trim().length()>0)
                if (!(boolean)engine.eval(constraint))
                    return false;
        }
        return true;
    }

    // Return the current reading of the i'th ACTIVE pvWriteable
    private double getPVsetting(int i) throws ConnectionException, GetException {
        int j = -1;
        for (ChannelWrapper cw : pvChannels) {
            if (cw.getIsScanned())
                j++;
            if (i==j)
                return cw.getChannel().getRawValueRecord().doubleValue();
       }
       return Double.NaN;
    }

    public int calculateCombos() {

        Logger.getLogger(ScannerDocument.class.getName()).log(Level.FINER, "Recalculating steps");

         combos.clear();
         if(currentMeasurementWasLoaded.get()) {
             currentMeasurementWasLoaded.set(false);
         } else {
             nCombosDone = 0;
         }

        // Calculate the correct amount of combos..
        int ncombos=1;
        ncombos = getActivePVwritebacks().map( cw -> cw.getNpoints()).reduce(ncombos, (accumulator, _item) -> accumulator * _item);
        for (int i = 0;i<ncombos+2;i++)
            combos.add(new double[(int) getActivePVwritebacks().count()]);

        // Read in all settings before any modifications..
        // First and last measurement is at initial parameters
        for (int i=0;i<(int) getActivePVwritebacks().count();i++) {
             try {
                 combos.get(0)[i] = getPVsetting(i);
                 combos.get(ncombos+1)[i] = combos.get(0)[i];
             } catch (ConnectionException | GetException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.WARNING, null, ex);
                combos.get(0)[i] = 0.0;
                combos.get(ncombos+1)[i] = 0.0;
             }
        }

        // Insert all numbers..
        // n1 will say how many times each number should currently be repeated
        int n1 = ncombos;
        // n2 will say how many times we should loop the current PV
        int n2 = 1;
        // Write out one parameter at the time
        for (int i=0; i<(int) getActivePVwritebacks().count();i++) {
            // The combo index we are currently inserting
            int m = 1;
            ChannelWrapper cw = getActivePVwriteback(i);
            n1/=cw.getNpoints();
            for (int l=0;l<n2;l++) {
                for ( double sp : cw.getScanPoints()) {
                    for (int k=0;k<n1;k++) {
                        combos.get(m)[i]=sp;
                        m+=1;
                    }
                }
            }
            n2*=cw.getNpoints();
         }

        // Now we check if any of the combos are invalid..
        if (hasConstraints()) {
            int i=0;
            while(i<combos.size())
            {
                try {
                    if (!checkConstraints(combos.get(i))) {
                        combos.remove(i);
                        continue;
                    }
                } catch (ScriptException ex) {
                    Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
                }
                i+=1;
            }
        }
        return combos.size();
    }

}
