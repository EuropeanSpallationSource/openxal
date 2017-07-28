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

import java.beans.Expression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import xal.ca.Channel;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;

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

        Accelerator acc = Model.getInstance().getAccelerator();


        /*pvWriteables.add(acc.getNode("ST1-VC").getChannel("fieldRB"));
        pvScanPoints.add(new double[] {1,2,3,4});
        pvWriteables.add(acc.getNode("ST2-VC").getChannel("fieldRB"));
        pvScanPoints.add(new double[] {5,6});
        pvWriteables.add(acc.getNode("ST3-VC").getChannel("fieldRB"));
        pvScanPoints.add(new double[] {7,8,9});
        pvReadbacks.add(acc.getNode("BPM1").getChannel("yAvg"));
        pvReadbacks.add(acc.getNode("BPM2").getChannel("yAvg"));
        pvReadbacks.add(acc.getNode("BPM3").getChannel("yAvg"));
        pvReadbacks.add(acc.getNode("BPM4").getChannel("yAvg"));
        pvReadbacks.add(acc.getNode("BPM5").getChannel("yAvg"));
        */
        System.out.println("DBG --");
        System.out.println("DBG --");
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

        System.out.println("DBG You add: "+channel.getId()+", "+read+":"+write);
        if (read)
            if (! pvReadbacks.contains(channel))
                pvReadbacks.add(channel);
        if (write) {
            if (! pvWriteables.contains(channel)) {
                pvWriteables.add(channel);
                constraintsVars.put(channel,shorthand);
                pvScanPoints.put(channel,new double[] {1,2,3,4});
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
        System.out.println("DBG You remove: "+channel.getId()+", "+read+":"+write);
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
     * @throws ScriptException
     */
    public static boolean checkConstraints(double[] combo) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        for(int i=0;i<combo.length;i++) {
            engine.eval(constraintsVars.get(pvWriteables.get(i))+"="+combo[i]);
            System.out.println("DBG " + constraintsVars.get(pvWriteables.get(i))+"="+combo[i]);
        }
        for(String constraint:constraints) {
            if (constraint.length()>0)
                if (!(boolean)engine.eval(constraint)) {
                    System.out.println("Refused based on "+constraint);
                    return false;
                }

        }
        return true;
    }

    // Returns the current reading of the i'th pvWriteables
    private static double getPVsetting(int i) {
        return 0.0;
    }
    /*
     * This function updates combos with the correct combinations of settings
     * for each variable.
     *
     * Note that combos.get(0) always returns the INITIAL SETTINGS
     */
    private static void calculateCombos() {
         combos.clear();

        // Create the correct amount of combos..
        int ncombos=1;
        for(Channel ch: pvWriteables)
            ncombos*=pvScanPoints.get(ch).length;
        for (int i = 0;i<ncombos+1;i++)
            combos.add(new double[pvScanPoints.size()]);

        // Read in all settings before any modifications..
        for (int i=0;i<pvScanPoints.size();i++)
            combos.get(0)[i] = getPVsetting(i);

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
                        combos.get(m)[i]=sp;
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

    //
    private static void setCombo(double[] combo) {
        for (int i=0;i<combo.length;i++) {
            //  pvWriteables.get(i).set(combo[i]);
        }
    }

    private static double [] makeReading() {
        double[] readings = new double[pvReadbacks.size()];
        Random rnd = new Random();
        for (int i=0;i<readings.length;i++) {
            // Here you insert an actual reading of the PV value..
            readings[i]=rnd.nextDouble();
        }
        return readings;
    }

    public static double[][] actionExecute() {
        pvReadbacks.forEach(pv -> System.out.println("Read PV: "+pv.channelName()));
        pvWriteables.forEach(pv -> System.out.println("Write PV: "+pv.channelName()));
        calculateCombos();
        double[][] measurement = new double[combos.size()][pvWriteables.size()+pvReadbacks.size()];


        for(int i=0;i<combos.size();i++) {
            setCombo(combos.get(i));
            double[] readings = makeReading();
            System.arraycopy(combos.get(i), 0, measurement[i], 0, pvWriteables.size());
            System.arraycopy(readings, 0, measurement[i], pvWriteables.size(), pvReadbacks.size());
        }

        int measNum = dataSets.size()+1;
        dataSets.put("Measurement "+measNum, measurement);
        allPVrb.put("Measurement "+measNum, pvReadbacks.stream().collect(Collectors.toList()));
        allPVw.put("Measurement "+measNum, pvWriteables.stream().collect(Collectors.toList()));

        System.out.println("DBG There are " + dataSets.size() + " data sets now.");
        return measurement;
    }

}
