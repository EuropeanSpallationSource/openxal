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
import xal.ca.Channel;
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

    /**
     * To calculate constraints, we need to know the short hand variable name
     * for each variable..
     */
    public Map<ChannelWrapper, String> constraintsVars;
    public List<String> constraints;

    // The Readback channels
    public List<Channel> pvReadbacks;
    // The Set channels
    public List<ChannelWrapper> pvWriteables;
    // The combination of scan points (each double[] is equal to number of writeables)
    public List<double[]> combos;


    // Save/restore parameters..
    private final String scanner_SR = "ScannerData";
    private final String scanPVs_SR = "scan_PVs";
    private final String measurePVs_SR = "measure_PVs";
    private final String measurements_SR = "measurements";
    private final String constraints_SR = "constraints";
    private final String currentMeas_SR = "currentMeasurement";
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
        constraintsVars = new HashMap<>();
        pvReadbacks = new ArrayList<>();
        pvWriteables = new ArrayList<>();
        combos = new ArrayList<>();
        constraints = new ArrayList<>();
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
        DataAdaptor scannerAdaptor =  da.createChild(scanner_SR);
        currentMeasAdaptor = null;
        scannerAdaptor.setValue("title", url.getFile());
        scannerAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));


        // Store information about all measurements done..
        DataAdaptor measurementsScanner = scannerAdaptor.createChild(measurements_SR);
        dataSets.entrySet().forEach(measurement -> {
            // convenience variables..
            List<Channel> pvW = allPVw.get(measurement.getKey());
            List<Channel> pvR = allPVrb.get(measurement.getKey());

            DataAdaptor measurementAdaptor = measurementsScanner.createChild("measurement");
            measurementAdaptor.setValue("title", measurement.getKey());
            DataAdaptor dataSet = measurementAdaptor.createChild("dataSet");
            for (int i=0;i<measurement.getValue()[0].length;i++) {
                DataAdaptor channelAdaptor = dataSet.createChild("channel");
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
        DataAdaptor scanpvScanner = scannerAdaptor.createChild(scanPVs_SR);
        pvWriteables.forEach((pv) -> {
            DataAdaptor scan_PV_name =  scanpvScanner.createChild("PV");
            scan_PV_name.setValue("name", pv.getChannelName() );
            scan_PV_name.setValue("min", pv.minProperty().get() );
            scan_PV_name.setValue("max", pv.maxProperty().get() );
            scan_PV_name.setValue("npoints", pv.npointsProperty().get() );
            scan_PV_name.setValue("instance", pv.instanceProperty().get() );
        });

        DataAdaptor measpvScanner = scannerAdaptor.createChild(measurePVs_SR);
        pvReadbacks.forEach((pv) -> {
            DataAdaptor meas_PV_name =  measpvScanner.createChild("PV");
            meas_PV_name.setValue("name", pv.getId() );
        });

        DataAdaptor constraintsAdaptor = scannerAdaptor.createChild(constraints_SR);
        constraints.forEach(constraint -> {
            if (! constraint.isEmpty())
                constraintsAdaptor.createChild("constraint").setValue("value", constraint);
            });

        da.writeToUrl( url );
    }

    public void saveCurrentMeas(double[][] measurement, int nmeas) {
        System.out.println("Saving meas "+nmeas);
        if (currentMeasAdaptor==null) {
            currentMeasAdaptor=da.childAdaptor(scanner_SR).createChild(currentMeas_SR).createChild("dataSet");
        }
        currentMeasAdaptor.createChild("step").setValue("values", measurement[nmeas]);
        da.writeToUrl( defaultUrl );
    };
}
