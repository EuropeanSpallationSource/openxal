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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.ca.Timestamp;

/**
 *
 * @todo It would be nice to refactor this and not do static stuff so much (ugh)
 *
 * @author yngvelevinsen
 */
public class MainFunctions {

    public static ScannerDocument mainDocument;

    // Holds the progress of the execution
    public static SimpleDoubleProperty runProgress;

    // True if combos list is up to date
    public static SimpleBooleanProperty isCombosUpdated;

    public static SimpleBooleanProperty pauseTask;
    public static SimpleBooleanProperty stopTask;

    private static InvalidationListener changeListener;

    public static void initialize(ScannerDocument scannerDocument) {

        mainDocument = scannerDocument;

        isCombosUpdated = new SimpleBooleanProperty(false);
        runProgress = new SimpleDoubleProperty(-1.0);
        pauseTask = new SimpleBooleanProperty(false);
        stopTask = new SimpleBooleanProperty(false);
        changeListener = observable -> {
            Logger.getLogger(MainFunctions.class.getName()).log(Level.FINEST, "ChangeListener triggered, combos must be recalculated");
            isCombosUpdated.set(false);
        };
    }


    /**
     * This should be called when the channel is selected in the GUI.
     *
     * @param cWrapper The channel to add
     * @param read Add the channel to readbacks
     * @param write Add the channel to writeables
     * @return true if the channel was added (ie was not already in list)
     */
    public static boolean actionAddPV(ChannelWrapper cWrapper, Boolean read, Boolean write) {
        if (read)  {
            Logger.getLogger(MainFunctions.class.getName()).log(Level.FINER, "Added channel {0} to readable list", cWrapper);
            mainDocument.setHasChanges(true);
            return true;
            }
        if (write) {
            Logger.getLogger(MainFunctions.class.getName()).log(Level.FINER, "Added channel {0} to writeable list", cWrapper);
            cWrapper.npointsProperty().addListener(changeListener);
            cWrapper.minProperty().addListener(changeListener);
            cWrapper.maxProperty().addListener(changeListener);
            mainDocument.setHasChanges(true);
            return true;
        }
        return false;
    }

    /**
     * This should be called when the channel is unselected in the GUI.
     * It currently only have a function for writeable channels, but please
     * always call this when a channel is unselected (in case we need some logic
     * in the future).
     *
     * @param cWrapper The channel to remove
     * @param read Remove the channel from readbacks
     * @param write Remove the channel from writeables
     */
    public static void actionRemovePV(ChannelWrapper cWrapper, Boolean read, Boolean write) {
        if (read)  {
            Logger.getLogger(MainFunctions.class.getName()).log(Level.FINER, "Removing channel {0} from readable list",cWrapper);
        }
        if (write) {
            Logger.getLogger(MainFunctions.class.getName()).log(Level.FINER, "Removing channel {0} from writeable list",cWrapper);
            cWrapper.npointsProperty().removeListener(changeListener);
            cWrapper.minProperty().removeListener(changeListener);
            cWrapper.maxProperty().removeListener(changeListener);
        }
    }

    /*
     * This function updates combos with the correct combinations of settings
     * for each variable.
     *
     * Note that combos.get(0) always returns the INITIAL SETTINGS
     */
    public static int calculateCombos() {
        mainDocument.calculateCombos();
        isCombosUpdated.set(true);
        return mainDocument.combos.size();
    }

    /**
     * This calculates the total number of measurement points for this scan
     */
    public static int calculateNumMeas() {
        if(!isCombosUpdated.getValue())
            calculateCombos();
        return 2+(mainDocument.combos.size()-2)*mainDocument.numberMeasurementsPerCombo.get();
    }

    /*
     * Set a specific combo
     *
     */
    private static void setCombo(double[] combo) {
        for (int i=0;i<combo.length;i++) {
            ChannelWrapper cw = mainDocument.getActivePVwriteback(i);
            try {
                if (cw.getChannel().connectAndWait(5))
                    cw.getChannel().putVal(combo[i]);
            } catch (ConnectionException | PutException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static ArrayList<ChannelTimeRecord> makeReading() {
        ArrayList<ChannelTimeRecord> readings = new ArrayList((int) mainDocument.getActivePVreadables().count());
        for (int i=0;i<(int) mainDocument.getActivePVreadables().count();i++) {
            try {
                // Here you insert an actual reading of the PV value..
                readings.add(mainDocument.getActivePVreadable(i).getChannel().getTimeRecord());
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return readings;
    }

    public static String getTimeString(int npoints) {
        int seconds = (int) (npoints*mainDocument.delayBetweenMeasurements.get()/1000);
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

        if (mainDocument.nCombosDone == 0) {
            mainDocument.currentMeasurement = new double[2+(mainDocument.combos.size()-2)*mainDocument.numberMeasurementsPerCombo.get()][((int) mainDocument.getActivePVwritebacks().count())+((int) mainDocument.getActivePVreadables().count())];
            mainDocument.currentTimestamps = new Timestamp[2+(mainDocument.combos.size()-2)*mainDocument.numberMeasurementsPerCombo.get()][(int) mainDocument.getActivePVreadables().count()];
        }

        // Warning, this only saves if document is defined (user has saved before)
        MainFunctions.mainDocument.saveDocument();

        pauseTask.set(false);
        stopTask.set(false);

        Task runTask = new Task<Void>() {


            @Override
            public void run() {

                Logger.getLogger(MainFunctions.class.getName()).log(Level.INFO, "Starting a new scan {0}",mainDocument.nCombosDone);

                int nMeasThisCombo=1;
                int nMeasDone=0;
                if (mainDocument.nCombosDone>0) {
                    Logger.getLogger(MainFunctions.class.getName()).log(Level.FINER, "Continuing old measurement, {0} combos already done", mainDocument.nCombosDone);
                    nMeasDone=1+(mainDocument.nCombosDone-1)*mainDocument.numberMeasurementsPerCombo.get();
                }

                while (mainDocument.nCombosDone<mainDocument.combos.size()) {
                    while (pauseTask.get()) {
                        try {
                            Thread.sleep(mainDocument.delayBetweenMeasurements.get());
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, "Sleep thread interrupted", ex);
                        }
                    }

                    setCombo(mainDocument.combos.get(mainDocument.nCombosDone));
                    try {
                        Thread.sleep(mainDocument.delayBetweenMeasurements.get());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, "Sleep thread interrupted", ex);
                    }

                    // The first and last measurement is a check which we only do once
                    if (mainDocument.nCombosDone!=0 && mainDocument.nCombosDone!=mainDocument.combos.size()-1)
                        nMeasThisCombo=mainDocument.numberMeasurementsPerCombo.get();
                    else
                        nMeasThisCombo=1;
                    Logger.getLogger(MainFunctions.class.getName()).log(Level.FINEST, "Number of measurements with this combo {0}", nMeasThisCombo);
                    for (int j=0;j<nMeasThisCombo;j++) {
                        ArrayList<ChannelTimeRecord> readings = makeReading();
                        System.arraycopy(mainDocument.combos.get(mainDocument.nCombosDone),
                                0,
                                mainDocument.currentMeasurement[nMeasDone],
                                0,
                                (int) mainDocument.getActivePVwritebacks().count());
                        System.arraycopy(readings.stream().mapToDouble(m -> m.doubleValue()).toArray(),
                                0,
                                mainDocument.currentMeasurement[nMeasDone],
                                (int) mainDocument.getActivePVwritebacks().count(),
                                (int) mainDocument.getActivePVreadables().count());
                        System.arraycopy(readings.stream().map(m -> m.getTimestamp()).toArray(),
                                0,
                                mainDocument.currentTimestamps[nMeasDone],
                                0,
                                (int) mainDocument.getActivePVreadables().count());
                        updateProgress(mainDocument.nCombosDone+1, mainDocument.combos.size());
                        mainDocument.saveCurrentMeas(nMeasDone);
                        nMeasDone++;
                    }
                    mainDocument.nCombosDone++;
                    // if a stop is requested, break the task loop
                    if (stopTask.get())
                        break;
                }

                // Make sure we are back to initial settings!
                setCombo(mainDocument.combos.get(0));

                // If we finished the measurement, store the new data.
                if (mainDocument.nCombosDone == mainDocument.combos.size()) {
                    String setName = "Measurement "+(mainDocument.numberOfScans.get()+1);
                    mainDocument.setDataSet(setName, mainDocument.currentMeasurement);
                    mainDocument.setPVreadbackData(setName);
                    mainDocument.setPVwriteData(setName);
                    mainDocument.setTimestamps(setName);
                    mainDocument.numberOfScans.set(mainDocument.numberOfScans.get()+1);
                    mainDocument.nCombosDone=0;
                    // Note that this is only saved if document is defined
                    MainFunctions.mainDocument.saveDocument();
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
