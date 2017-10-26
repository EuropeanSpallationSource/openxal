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
package openxal.apps.scanner;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xal.ca.Channel;
import xal.smf.Accelerator;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 *
 * @author yngvelevinsen
 */
public class ScannerDocument {
    /**
     * A dictionary of the created datasets..
     */
    public Map<String, double[][]> dataSets;
    /**
     * For every measurement, store the channels read for this measurement..
     */
    public Map<String, List<Channel>> allPVrb;
    /**
     * For every measurement, store the channels written to for this measurement..
     */
    public Map<String, List<Channel>> allPVw;

    public SimpleIntegerProperty numberOfMeasurements;

    public double[][] currentMeasurement;

    // The current number of measurement points done
    public int nCombosDone;

    /**
     * To calculate constraints, we need to know the short hand variable name
     * for each variable..
     */
    public ObservableList<String> constraints;

    // The Readback channels
    public List<ChannelWrapper> pvReadbacks;
    // The Set channels
    public List<ChannelWrapper> pvWriteables;
    // The combination of scan points (each double[] is equal to number of writeables)
    public List<double[]> combos;


    // Save/restore parameters..
    private static final String SCANNER_SR = "ScannerData";
    private static final String SCANPVS_SR = "scan_PVs";
    private static final String MEASUREPVS_SR = "measure_PVs";
    private static final String MEASUREMENTS_SR = "measurements";
    private static final String CONSTRAINTS_SR = "constraints";
    private static final String CURRENTMEAS_SR = "currentMeasurement";
    private XmlDataAdaptor da;
    private DataAdaptor currentMeasAdaptor;

    private URL defaultUrl;

    /**
     *  Create a new empty ScanDocument1D
     */
    public ScannerDocument() {
        dataSets = new HashMap<>();
        allPVrb = new HashMap<>();
        allPVw = new HashMap<>();
        pvReadbacks = new ArrayList<>();
        pvWriteables = new ArrayList<>();
        combos = new ArrayList<>();
        constraints = FXCollections.observableArrayList("", "", "", "");
        numberOfMeasurements = new SimpleIntegerProperty(0);
    }


    /**
     *  Create a new document loaded from the URL file
     *
     *@param  url  The URL of the file to load into the new document.
     */
    public ScannerDocument(URL url) {
        this();
        if (url == null) {
                return;
        }
        setSource(url);
        readScanDocument(url);

        //super class method - will show "Save" menu active
        if (url.getProtocol().equals("jar")) {
                return;
        }
        setHasChanges(true);
    }

    /**
     * This Function should be part of abstract class..
     * @param url
     */
    public final void setSource(URL url) {

    }

    /**
     * This Function should be part of abstract class..
     * @param hasChanges
     */
    public final void setHasChanges(boolean hasChanges) {

    }

    /**
     *  Reads the content of the document from the specified URL.
     *
     *@param  url  Description of the Parameter
     */
    public final void readScanDocument(URL url) {

        DataAdaptor readAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
        if (readAdaptor != null) {
            System.out.println("Will read document "+url.getFile());
        }

    }
    /**
     *  Save the ScannerDocument document to the specified URL.
     *
     *  @param  url  The file URL where the data should be saved
     */
    public void saveDocumentAs(URL url) {
        defaultUrl = url;
        da = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor scannerAdaptor =  da.createChild(SCANNER_SR);
        currentMeasAdaptor = null;
        scannerAdaptor.setValue("title", url.getFile());
        scannerAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));


        // Store information about all measurements done..
        DataAdaptor measurementsScanner = scannerAdaptor.createChild(MEASUREMENTS_SR);
        dataSets.entrySet().forEach(measurement -> {
            // convenience variables..
            List<Channel> pvW = allPVw.get(measurement.getKey());
            List<Channel> pvR = allPVrb.get(measurement.getKey());
            DataAdaptor measurementAdaptor = measurementsScanner.createChild("measurement");
            measurementAdaptor.setValue("title", measurement.getKey());
            for (int i=0;i<measurement.getValue()[0].length;i++) {
                DataAdaptor channelAdaptor = measurementAdaptor.createChild("channel");
                if (i<pvW.size()) {
                    channelAdaptor.setValue("name", pvW.get(i).getId());
                    channelAdaptor.setValue("type", "w");
                } else {
                    channelAdaptor.setValue("name", pvR.get(i-pvW.size()).getId());
                    channelAdaptor.setValue("type", "r");
                }
                double[] data = new double[measurement.getValue().length];
                for (int j = 0;j<measurement.getValue().length;j++) {
                    data[j]=measurement.getValue()[j][i];
                }
                channelAdaptor.setValue("data", data);
            }
        });


        // Store information about current measurement setup..

        // Store list of variables to read & write.. ChannelWrapper objects
        DataAdaptor scanpvScanner = scannerAdaptor.createChild(SCANPVS_SR);
        pvWriteables.forEach((pv) -> {
            DataAdaptor scan_PV_name =  scanpvScanner.createChild("PV");
            scan_PV_name.setValue("name", pv.getChannelName() );
            scan_PV_name.setValue("min", pv.minProperty().get() );
            scan_PV_name.setValue("max", pv.maxProperty().get() );
            scan_PV_name.setValue("npoints", pv.npointsProperty().get() );
            scan_PV_name.setValue("instance", pv.instanceProperty().get() );
        });

        DataAdaptor measpvScanner = scannerAdaptor.createChild(MEASUREPVS_SR);
        pvReadbacks.forEach((pv) -> {
            DataAdaptor meas_PV_name =  measpvScanner.createChild("PV");
            meas_PV_name.setValue("name", pv.getChannel().getId() );
        });

        DataAdaptor constraintsAdaptor = scannerAdaptor.createChild(CONSTRAINTS_SR);
        constraints.forEach(constraint -> {
            if (! constraint.isEmpty())
                constraintsAdaptor.createChild("constraint").setValue("value", constraint);
            });

        da.writeToUrl( url );
    }

    public void saveCurrentMeas(int nmeas) {
        if (currentMeasAdaptor==null) {
            currentMeasAdaptor=da.childAdaptor(SCANNER_SR).createChild(CURRENTMEAS_SR);
        }
        currentMeasAdaptor.createChild("step").setValue("values", currentMeasurement[nmeas]);
        da.writeToUrl( defaultUrl );
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

        // Load list of variables to read & write.. ChannelWrapper objects
        // There is probably an issue since these variables are not read into MainFunctions.PVlist
        DataAdaptor scanpvScanner = scannerAdaptor.childAdaptor(SCANPVS_SR);
        pvWriteables.clear();
        scanpvScanner.childAdaptors().forEach( (childAdaptor) -> {
            String name = childAdaptor.stringValue("name");
            double min = childAdaptor.doubleValue("min");
            double max = childAdaptor.doubleValue("max");
            int npoints = childAdaptor.intValue("npoints");
            String instance = childAdaptor.stringValue("instance");

            Channel chan = acc.channelSuite().getChannelFactory().getChannel(name);

            ChannelWrapper cWrap = new ChannelWrapper(chan);
            cWrap.isScannedProperty().set(true);
            cWrap.minProperty().set(min);
            cWrap.maxProperty().set(max);
            cWrap.npointsProperty().set(npoints);
            cWrap.forceInstance(instance);

            pvWriteables.add(cWrap);
        });

        DataAdaptor readpvScanner = scannerAdaptor.childAdaptor(MEASUREPVS_SR);
        pvReadbacks.clear();
        readpvScanner.childAdaptors().forEach( (childAdaptor) -> {
            String name = childAdaptor.stringValue("name");

            Channel chan = acc.channelSuite().getChannelFactory().getChannel(name);
            ChannelWrapper cWrap = new ChannelWrapper(chan);
            cWrap.isReadProperty().set(true);

            pvReadbacks.add(cWrap);
        });

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
            measurementsScanner.childAdaptors().forEach( (measAdaptor) -> {
                List<Channel> pvW = new ArrayList<>();
                List<Channel> pvR = new ArrayList<>();
                int ncombos = measAdaptor.childAdaptors().get(0).doubleArray("data").length;
                int nchannels = measAdaptor.childAdaptors().size();
                double[][] data = new double[ncombos][nchannels];
                //dataSets.get(measAdaptor.stringValue("title"));
                for (int ichan=0;ichan<nchannels;ichan++) {
                    DataAdaptor chanAdaptor = measAdaptor.childAdaptors().get(ichan);
                    double[] channelData = chanAdaptor.doubleArray("data");
                    for (int icombo=0;icombo<ncombos;icombo++) {
                        data[icombo][ichan] = channelData[icombo];
                    }
                    Channel chan = acc.channelSuite().getChannelFactory().getChannel(chanAdaptor.stringValue("name"));
                    if ("w".equals(chanAdaptor.stringValue("type"))) {
                        pvW.add(chan);
                    } else if ("r".equals(chanAdaptor.stringValue("type"))) {
                        pvR.add(chan);
                    }
                }
                dataSets.put(measAdaptor.stringValue("title"), data);
                allPVw.put(measAdaptor.stringValue("title"), pvW);
                allPVrb.put(measAdaptor.stringValue("title"), pvR);
                numberOfMeasurements.set(numberOfMeasurements.get()+1);

            });
        }

        if ( scannerAdaptor.childAdaptor(CURRENTMEAS_SR) != null) {
            currentMeasAdaptor = scannerAdaptor.childAdaptor(CURRENTMEAS_SR);
            // Need to calculate nmeas (or ncombos if you want)
            nCombosDone=currentMeasAdaptor.childAdaptors().size();
            int nVars = pvWriteables.size() + pvReadbacks.size();
            currentMeasurement = new double[nCombosDone][nVars];
            for(int i = 0;i<nCombosDone;i++) {
                double [] values = currentMeasAdaptor.childAdaptors().get(i).doubleArray("values");
                for (int j=0;j<nVars;j++) {
                    currentMeasurement[i][j] = values[j];
                }
            }
        }

    }
}
