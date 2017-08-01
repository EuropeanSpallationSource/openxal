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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
    public static Map<Channel, String> constraintsVars;
    public static List<String> constraints;

    // The Readback channels
    public static List<Channel> pvReadbacks;
    // The Set channels
    public static List<Channel> pvWriteables;
    // The scan points for each channel in pvWriteAbles
    public static Map<Channel, double[]> pvScanPoints;
    // The combination of scan points (each double[] is equal to number of writeables)
    public static List<double[]> combos;

    private static RunSimulationService simulationWorker;
    private static boolean simulationServiceUpdated;

    public static void initialize() {
        dataSets = new HashMap<>();
        allPVrb = new HashMap<>();
        allPVw = new HashMap<>();
        constraintsVars = new HashMap<>();
        pvReadbacks = new ArrayList<>();
        pvWriteables = new ArrayList<>();
        pvScanPoints = new HashMap<>();
        combos = new ArrayList<>();
        constraints = new ArrayList<>();
        simulationWorker = new RunSimulationService();
        simulationServiceUpdated = false;
    }

    /**
     *
     * @param channel The channel to add
     * @param shorthand The short hand name of variable for constraint view
     * @param read Add the channel to readbacks
     * @param write Add the channel to writeables
     */
    public static void actionScanAddPV(Channel channel, String shorthand, Boolean read, Boolean write) {

        Accelerator acc = Model.getInstance().getAccelerator();

        if (read)
            if (! pvReadbacks.contains(channel))
                pvReadbacks.add(channel);
        if (write) {
            if (! pvWriteables.contains(channel)) {
                pvWriteables.add(channel);
                constraintsVars.put(channel,shorthand);
                // TODO we should of course allow the user to set the range!
                pvScanPoints.put(channel,new double[] {-0.5,-0.2,0.2,0.5});
            }
        }


    }

    /**
     *
     * @param channel The channel to remove
     * @param read Remove the channel from readbacks
     * @param write Remove the channel from writeables
     */
    public static void actionScanRemovePV(Channel channel, Boolean read, Boolean write) {
        Accelerator acc = Model.getInstance().getAccelerator();
        if (read) {
            pvReadbacks.remove(channel);
        }
        if (write) {
            pvWriteables.remove(channel);
            pvScanPoints.remove(channel);
            constraintsVars.remove(channel);
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

    // Return the current reading of the i'th pvWriteables
    private static double getPVsetting(int i) throws ConnectionException, GetException {
        return pvWriteables.get(i).getRawValueRecord().doubleValue();
    }
    /*
     * This function updates combos with the correct combinations of settings
     * for each variable.
     *
     * Note that combos.get(0) always returns the INITIAL SETTINGS
     */
    private static void calculateCombos() {
         combos.clear();

        // Calculate the correct amount of combos..
        int ncombos=1;
        for (Channel ch : pvWriteables)
            ncombos*=pvScanPoints.get(ch).length;
        for (int i = 0;i<ncombos+1;i++)
            combos.add(new double[pvScanPoints.size()]);

        // Read in all settings before any modifications..
        for (int i=0;i<pvScanPoints.size();i++) {
             try {
                 combos.get(0)[i] = getPVsetting(i);
             } catch (ConnectionException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.WARNING, null, ex);
                combos.get(0)[i] = 0.0;
             } catch (GetException ex) {
                 Logger.getLogger(MainFunctions.class.getName()).log(Level.WARNING, null, ex);
                combos.get(0)[i] = 0.0;
             }
        }

        // Insert all numbers..
        // n1 will say how many times each number should currently be repeated
        int n1 = ncombos;
        // n2 will say how many times we should loop the current PV
        int n2 = 1;
        // Write out one parameter at the time
        for (int i=0; i<pvScanPoints.size();i++) {
            // The combo index we are currently inserting
            int m = 1;
            n1/=pvScanPoints.get(pvWriteables.get(i)).length;
            for (int l=0;l<n2;l++) {
                for ( double sp : pvScanPoints.get(pvWriteables.get(i))) {
                    for (int k=0;k<n1;k++) {
                        combos.get(m)[i]=sp+combos.get(m)[0];
                        m+=1;
                    }
                }
            }
            n2*=pvScanPoints.get(pvWriteables.get(i)).length;
         }

        // Now we check if any of the combos are invalid..
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

    /*
     * Set a specific combo
     *
     */
    private static void setCombo(double[] combo) {
        for (int i=0;i<combo.length;i++) {
            try {
                System.out.println("DBG "+pvWriteables.get(i).channelName()+": "+pvWriteables.get(i).getRawValueRecord().doubleValue()+", "+combo[i]);
                if (pvWriteables.get(i).connectAndWait(5))
                    pvWriteables.get(i).putVal(combo[i]);
            } catch (ConnectionException | GetException | PutException ex) {
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

    public static double[][] actionExecute() {
        pvReadbacks.forEach(pv -> System.out.println("Read PV: "+pv.channelName()));
        pvWriteables.forEach(pv -> System.out.println("Write PV: "+pv.channelName()));
        calculateCombos();
        double[][] measurement = new double[combos.size()][pvWriteables.size()+pvReadbacks.size()];


        for(int i=0;i<combos.size();i++) {
            System.out.println("DBG, execute "+i);
            setCombo(combos.get(i));
            try {
                sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
            double[] readings = makeReading();
            System.arraycopy(combos.get(i), 0, measurement[i], 0, pvWriteables.size());
            System.arraycopy(readings, 0, measurement[i], pvWriteables.size(), pvReadbacks.size());
        }
        setCombo(combos.get(0));
        int measNum = dataSets.size()+1;
        dataSets.put("Measurement "+measNum, measurement);
        allPVrb.put("Measurement "+measNum, pvReadbacks.stream().collect(Collectors.toList()));
        allPVw.put("Measurement "+measNum, pvWriteables.stream().collect(Collectors.toList()));

        System.out.println("DBG There are " + dataSets.size() + " data sets now.");
        return measurement;
    }

}
