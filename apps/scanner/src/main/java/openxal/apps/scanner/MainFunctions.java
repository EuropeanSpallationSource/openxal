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

import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.Accelerator;
import xal.ca.PutException;

/**
 *
 * @author yngvelevinsen
 */
public class MainFunctions {

    /**
     * A dictionary of the created datasets..
     */
    public static Map<String, double[][]> dataSets;
    /**
     * For every measurement, store the channels read for this measurement..
     */
    public static Map<String, List<Channel>> allPVrb;
    /**
     * For every measurement, store the channels written to for this measurement..
     */
    public static Map<String, List<Channel>> allPVw;


    /**
     * To calculate constraints, we need to know the short hand variable name
     * for each variable..
     */
    public static Map<ChannelWrapper, String> constraintsVars;
    public static List<String> constraints;

    // The Readback channels
    public static List<Channel> pvReadbacks;
    // The Set channels
    public static List<ChannelWrapper> pvWriteables;
    // The combination of scan points (each double[] is equal to number of writeables)
    public static List<double[]> combos;

    // Holds the progress of the execution
    public static SimpleDoubleProperty runProgress;

    // True if combos list is up to date
    public static SimpleBooleanProperty isCombosUpdated;

    public static void initialize() {
        dataSets = new HashMap<>();
        allPVrb = new HashMap<>();
        allPVw = new HashMap<>();
        constraintsVars = new HashMap<>();
        pvReadbacks = new ArrayList<>();
        pvWriteables = new ArrayList<>();
        combos = new ArrayList<>();
        constraints = new ArrayList<>();
        isCombosUpdated = new SimpleBooleanProperty(false);
        runProgress = new SimpleDoubleProperty(-1.0);
    }

    /**
     *
     * @param cWrapper The channel to add
     * @param shorthand The short hand name of variable for constraint view
     * @param read Add the channel to readbacks
     * @param write Add the channel to writeables
     * @return true if the channel was added (ie was not already in list)
     */
    public static boolean actionScanAddPV(ChannelWrapper cWrapper, String shorthand, Boolean read, Boolean write) {

        Accelerator acc = Model.getInstance().getAccelerator();

        if (read)
            if (! pvReadbacks.contains(cWrapper.getChannel())) {
                pvReadbacks.add(cWrapper.getChannel());
                return true;
            }
        if (write) {
            if (! pvWriteables.contains(cWrapper)) {
                pvWriteables.add(cWrapper);
                cWrapper.npointsProperty().addListener((observable, oldValue, newValue) -> isCombosUpdated.set(false));
                cWrapper.minProperty().addListener((observable, oldValue, newValue) -> isCombosUpdated.set(false));
                cWrapper.maxProperty().addListener((observable, oldValue, newValue) -> isCombosUpdated.set(false));
                constraintsVars.put(cWrapper,shorthand);
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
        Accelerator acc = Model.getInstance().getAccelerator();
        if (read) {
            pvReadbacks.remove(cWrapper.getChannel());
        }
        if (write) {
            pvWriteables.remove(cWrapper);
            constraintsVars.remove(cWrapper);
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
            engine.eval(constraintsVars.get(pvWriteables.get(i))+"="+combo[i]);
        }
        for(String constraint:constraints) {
            if (constraint.trim().length()>0)
                if (!(boolean)engine.eval(constraint))
                    return false;
        }
        return true;
    }

    private static boolean hasConstraints() {
        for(String constraint:constraints)
            if (constraint.trim().length()>0)
                return true;
        return false;
    }
    // Return the current reading of the i'th pvWriteables
    private static double getPVsetting(int i) throws ConnectionException, GetException {
        return pvWriteables.get(i).getChannel().getRawValueRecord().doubleValue();
    }
    /*
     * This function updates combos with the correct combinations of settings
     * for each variable.
     *
     * Note that combos.get(0) always returns the INITIAL SETTINGS
     */
    public static int calculateCombos() {
         combos.clear();

        // Calculate the correct amount of combos..
        int ncombos=1;
        for (ChannelWrapper cw : pvWriteables) {
            ncombos*=cw.getNpoints();
        }
        for (int i = 0;i<ncombos+2;i++)
            combos.add(new double[pvWriteables.size()]);

        // Read in all settings before any modifications..
        // First and last measurement is at initial parameters
        for (int i=0;i<pvWriteables.size();i++) {
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
        for (int i=0; i<pvWriteables.size();i++) {
            // The combo index we are currently inserting
            int m = 1;
            n1/=pvWriteables.get(i).getNpoints();
            for (int l=0;l<n2;l++) {
                for ( double sp : pvWriteables.get(i).getScanPoints()) {
                    for (int k=0;k<n1;k++) {
                        combos.get(m)[i]=sp;
                        m+=1;
                    }
                }
            }
            n2*=pvWriteables.get(i).getNpoints();
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
        isCombosUpdated.set(true);
        return combos.size();
    }

    /*
     * Set a specific combo
     *
     */
    private static void setCombo(double[] combo) {
        for (int i=0;i<combo.length;i++) {
            try {
                if (pvWriteables.get(i).getChannel().connectAndWait(5))
                    pvWriteables.get(i).getChannel().putVal(combo[i]);
            } catch (ConnectionException | PutException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static double [] makeReading() {
        double[] readings = new double[pvReadbacks.size()];
        for (int i=0;i<readings.length;i++) {
            try {
                // Here you insert an actual reading of the PV value..
                readings[i]=pvReadbacks.get(i).getRawValueRecord().doubleValue();
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
                readings[i]=0.0;
            }
        }
        return readings;
    }

    public static String getTimeString(int seconds) {
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


        pvReadbacks.forEach(pv -> System.out.println("Read PV: "+pv.channelName()));
        pvWriteables.forEach(pv -> System.out.println("Write PV: "+pv.getChannel().channelName()));
        if (!isCombosUpdated.get()) calculateCombos();
        double[][] measurement = new double[combos.size()][pvWriteables.size()+pvReadbacks.size()];


        Task task = new Task<Void>() {
            @Override
            public void run() {
                for (int i=0; i<combos.size(); i++) {
                    System.out.println("DBG, execute "+i+" : "+Arrays.toString(combos.get(i)));
                    setCombo(combos.get(i));
                    try {
                        sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    double[] readings = makeReading();
                    System.arraycopy(combos.get(i), 0, measurement[i], 0, pvWriteables.size());
                    System.arraycopy(readings, 0, measurement[i], pvWriteables.size(), pvReadbacks.size());
                    updateProgress(i+1, combos.size());
                }
            }

            @Override
            protected Void call() throws Exception {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        runProgress.bind(task.progressProperty());
        new Thread(task).start();

        // Make sure we are back to initial settings!
        setCombo(combos.get(0));
        int measNum = dataSets.size()+1;
        dataSets.put("Measurement "+measNum, measurement);
        allPVrb.put("Measurement "+measNum, pvReadbacks.stream().collect(Collectors.toList()));
        allPVw.put("Measurement "+measNum, pvWriteables.stream().map(cw -> cw.getChannel()).collect(Collectors.toList()));

        return measurement;
    }
    /**
     * Check if we have selected enough parameters to do a scan
     *
     * @return true if we have at least one parameter to scan and one to read
     */
    static boolean checkSufficientParams() {
        if (pvReadbacks.isEmpty() || pvWriteables.isEmpty())
            return false;
        return true;
    }

}
