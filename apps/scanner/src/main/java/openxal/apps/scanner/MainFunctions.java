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

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;

/**
 *
 * @author yngvelevinsen
 */
public class MainFunctions {

    public static ScannerDocument mainDocument;

    // Holds the progress of the execution
    public static SimpleDoubleProperty runProgress;

    // True if combos list is up to date
    public static SimpleBooleanProperty isCombosUpdated;

    // Sleep time in ms between setting parameters and reading back.
    private static final long sleepTime = 2000;

    public static SimpleBooleanProperty pauseTask;
    public static SimpleBooleanProperty stopTask;

    public static void initialize() {

        mainDocument = new ScannerDocument();

        isCombosUpdated = new SimpleBooleanProperty(false);
        runProgress = new SimpleDoubleProperty(-1.0);
        pauseTask = new SimpleBooleanProperty(false);
        stopTask = new SimpleBooleanProperty(false);
    }

    /**
     *
     * @param cWrapper The channel to add
     * @param read Add the channel to readbacks
     * @param write Add the channel to writeables
     * @return true if the channel was added (ie was not already in list)
     */
    public static boolean actionScanAddPV(ChannelWrapper cWrapper, Boolean read, Boolean write) {

        if (read)
            if (! mainDocument.pvReadbacks.contains(cWrapper)) {
                mainDocument.pvReadbacks.add(cWrapper);
                return true;
            }
        if (write) {
            if (! mainDocument.pvWriteables.contains(cWrapper)) {
                mainDocument.pvWriteables.add(cWrapper);
                cWrapper.npointsProperty().addListener((observable, oldValue, newValue) -> isCombosUpdated.set(false));
                cWrapper.minProperty().addListener((observable, oldValue, newValue) -> isCombosUpdated.set(false));
                cWrapper.maxProperty().addListener((observable, oldValue, newValue) -> isCombosUpdated.set(false));
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param cWrapper The channel to remove
     * @param read Remove the channel from readbacks
     * @param write Remove the channel from writeables
     */
    public static void actionScanRemovePV(ChannelWrapper cWrapper, Boolean read, Boolean write) {
        if (read) {
            mainDocument.pvReadbacks.remove(cWrapper);
        }
        if (write) {
            mainDocument.pvWriteables.remove(cWrapper);
        }
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
    public static boolean checkConstraints(double[] combo) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        for(int i=0;i<combo.length;i++) {
            engine.eval(mainDocument.pvWriteables.get(i).instanceProperty().get()+"="+combo[i]);
        }
        for(String constraint:mainDocument.constraints) {
            if (constraint.trim().length()>0)
                if (!(boolean)engine.eval(constraint))
                    return false;
        }
        return true;
    }

    private static boolean hasConstraints() {
        return mainDocument.constraints.stream().anyMatch((constraint) -> (constraint.trim().length()>0));
    }
    // Return the current reading of the i'th pvWriteables
    private static double getPVsetting(int i) throws ConnectionException, GetException {
        return mainDocument.pvWriteables.get(i).getChannel().getRawValueRecord().doubleValue();
    }
    /*
     * This function updates combos with the correct combinations of settings
     * for each variable.
     *
     * Note that combos.get(0) always returns the INITIAL SETTINGS
     */
    public static int calculateCombos() {
         mainDocument.combos.clear();
         mainDocument.nCombosDone = 0;

        // Calculate the correct amount of combos..
        int ncombos=1;
        for (ChannelWrapper cw : mainDocument.pvWriteables) {
            ncombos*=cw.getNpoints();
        }
        for (int i = 0;i<ncombos+2;i++)
            mainDocument.combos.add(new double[mainDocument.pvWriteables.size()]);

        // Read in all settings before any modifications..
        // First and last measurement is at initial parameters
        for (int i=0;i<mainDocument.pvWriteables.size();i++) {
             try {
                 mainDocument.combos.get(0)[i] = getPVsetting(i);
                 mainDocument.combos.get(ncombos+1)[i] = mainDocument.combos.get(0)[i];
             } catch (ConnectionException | GetException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.WARNING, null, ex);
                mainDocument.combos.get(0)[i] = 0.0;
                mainDocument.combos.get(ncombos+1)[i] = 0.0;
             }
        }

        // Insert all numbers..
        // n1 will say how many times each number should currently be repeated
        int n1 = ncombos;
        // n2 will say how many times we should loop the current PV
        int n2 = 1;
        // Write out one parameter at the time
        for (int i=0; i<mainDocument.pvWriteables.size();i++) {
            // The combo index we are currently inserting
            int m = 1;
            n1/=mainDocument.pvWriteables.get(i).getNpoints();
            for (int l=0;l<n2;l++) {
                for ( double sp : mainDocument.pvWriteables.get(i).getScanPoints()) {
                    for (int k=0;k<n1;k++) {
                        mainDocument.combos.get(m)[i]=sp;
                        m+=1;
                    }
                }
            }
            n2*=mainDocument.pvWriteables.get(i).getNpoints();
         }

        // Now we check if any of the combos are invalid..
        if (hasConstraints()) {
            int i=0;
            while(i<mainDocument.combos.size())
            {
                try {
                    if (!checkConstraints(mainDocument.combos.get(i))) {
                        mainDocument.combos.remove(i);
                        continue;
                    }
                } catch (ScriptException ex) {
                    Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
                }
                i+=1;
            }
        }
        isCombosUpdated.set(true);
        return mainDocument.combos.size();
    }

    /*
     * Set a specific combo
     *
     */
    private static void setCombo(double[] combo) {
        for (int i=0;i<combo.length;i++) {
            try {
                if (mainDocument.pvWriteables.get(i).getChannel().connectAndWait(5))
                    mainDocument.pvWriteables.get(i).getChannel().putVal(combo[i]);
            } catch (ConnectionException | PutException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static double [] makeReading() {
        double[] readings = new double[mainDocument.pvReadbacks.size()];
        for (int i=0;i<readings.length;i++) {
            try {
                // Here you insert an actual reading of the PV value..
                readings[i]=mainDocument.pvReadbacks.get(i).getChannel().getRawValueRecord().doubleValue();
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
                readings[i]=0.0;
            }
        }
        return readings;
    }

    public static String getTimeString(int npoints) {
        int seconds = (int) (npoints*sleepTime/1000);
        int hours = (seconds - seconds%3600)/3600;
        int min = (seconds - seconds%60)/60;
        String time = ""+(seconds%60)+" s";
        if (seconds>59)
                time=""+min+" m, "+time;
        if (seconds>3599)
                time=""+hours+" h, "+time;
        return time;
    }

    public static double[][] actionExecute() {

        if (!isCombosUpdated.get()) calculateCombos();

        if (mainDocument.nCombosDone == 0)
            mainDocument.currentMeasurement = new double[mainDocument.combos.size()][mainDocument.pvWriteables.size()+mainDocument.pvReadbacks.size()];

        try {
            MainFunctions.mainDocument.saveDocumentAs(new File("scanner.xml").toURI().toURL());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

        pauseTask.set(false);
        stopTask.set(false);

        Task runTask = new Task<Void>() {


            @Override
            public void run() {
                while (mainDocument.nCombosDone<mainDocument.combos.size()) {
                    while (pauseTask.get()) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, "Sleep thread interrupted", ex);
                        }
                    }

                    setCombo(mainDocument.combos.get(mainDocument.nCombosDone));
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, "Sleep thread interrupted", ex);
                    }
                    double[] readings = makeReading();
                    System.arraycopy(mainDocument.combos.get(mainDocument.nCombosDone), 0, mainDocument.currentMeasurement[mainDocument.nCombosDone], 0, mainDocument.pvWriteables.size());
                    System.arraycopy(readings, 0, mainDocument.currentMeasurement[mainDocument.nCombosDone], mainDocument.pvWriteables.size(), mainDocument.pvReadbacks.size());
                    updateProgress(mainDocument.nCombosDone+1, mainDocument.combos.size());
                    mainDocument.saveCurrentMeas(mainDocument.nCombosDone);
                    mainDocument.nCombosDone++;
                    // if a stop is requested, break the task loop
                    if (stopTask.get())
                        break;
                }

                // Make sure we are back to initial settings!
                setCombo(mainDocument.combos.get(0));

                // If we finished the measurement, store the new data.
                if (mainDocument.nCombosDone == mainDocument.combos.size()) {
                    mainDocument.dataSets.put("Measurement "+(mainDocument.numberOfMeasurements.get()+1), mainDocument.currentMeasurement);
                    mainDocument.allPVrb.put("Measurement "+(mainDocument.numberOfMeasurements.get()+1), mainDocument.pvReadbacks.stream().map(cw -> cw.getChannel()).collect(Collectors.toList()));
                    mainDocument.allPVw.put("Measurement "+(mainDocument.numberOfMeasurements.get()+1), mainDocument.pvWriteables.stream().map(cw -> cw.getChannel()).collect(Collectors.toList()));
                    mainDocument.numberOfMeasurements.set(mainDocument.numberOfMeasurements.get()+1);
                    mainDocument.nCombosDone=0;
                    try {
                        MainFunctions.mainDocument.saveDocumentAs(new File("scanner.xml").toURI().toURL());
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, "Failed to save document", ex);
                    }
                }
            }

            @Override
            protected Void call() throws Exception {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        runProgress.bind(runTask.progressProperty());
        new Thread(runTask).start();

        return mainDocument.currentMeasurement;
    }

    /**
     * Check if we have selected enough parameters to do a scan
     *
     * @return true if we have at least one parameter to scan and one to read
     */
    static boolean checkSufficientParams() {
        return !(mainDocument.pvReadbacks.isEmpty() || mainDocument.pvWriteables.isEmpty());
    }

    static public void triggerPause() {
        // flip the pause state true/false
        if (pauseTask.get()) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Continue triggered");
            pauseTask.set(false);
        } else {
            Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Pause triggered");
            pauseTask.set(true);
        }
    }

    static public void triggerStop() {
        Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Stop triggered");
        stopTask.set(true);
    }

}
