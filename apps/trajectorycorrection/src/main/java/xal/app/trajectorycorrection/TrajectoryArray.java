/*
 * Copyright (C) 2018 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.app.trajectorycorrection;

/**
 * Defines a quantity to store on the entity Trajectory Array. Can store the
 * current orbit and the reference orbit at once. It is possible to reset the
 * values, read from file and also read from OpenXAL once can also define how
 * long is the array as you program also performs the calculation of the
 * difference orbit
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.BPM;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

public class TrajectoryArray {

    /* ------------------------
  Class variables
 * ------------------------ */
    /**
     * Hashmaps with the BPM and position.
     *
     * @serial internal array storage.
     */
    HashMap<BPM, Boolean> channelConnection = new HashMap();
    HashMap<BPM, Double> Pos = new HashMap();
    HashMap<BPM, Double> X = new HashMap();
    HashMap<BPM, Double> Y = new HashMap();
    HashMap<BPM, Double> XDiff = new HashMap();
    HashMap<BPM, Double> YDiff = new HashMap();
    HashMap<BPM, Double> XRef = new HashMap();
    HashMap<BPM, Double> YRef = new HashMap();
    HashMap<BPM, Double> AvgAmpl = new HashMap();

    /**
     * Number of BPMs in the sequence.
     *
     * @serial number of BPMs.
     */
    protected int BPMnum;


    /* ------------------------
 Constructors
* ------------------------ */
    /**
     * Initializes BPMs in the machine and try to connect
     *
     * @param accl accelerator.
     */
    public void initBPMs(Accelerator accl) {

        List<BPM> BPMList = accl.getAllNodesOfType("BPM");

        //initial value for connection always false
        BPMList.forEach(bpm -> {
            channelConnection.put(bpm, false);
            X.put(bpm, 0.0);
            Y.put(bpm, 0.0);
            XDiff.put(bpm, 0.0);
            YDiff.put(bpm, 0.0);
            AvgAmpl.put(bpm, 0.0);
            Pos.put(bpm, bpm.getSDisplay());
        });

        BPMList.parallelStream().forEach(bpm -> {
            if (!(bpm.getChannel("yAvg").isConnected() && bpm.getChannel("xAvg").isConnected() && bpm.getChannel("amplitudeAvg").isConnected())) {
                channelConnection.replace(bpm, (bpm.getChannel("yAvg").connectAndWait(1.0) && bpm.getChannel("xAvg").connectAndWait(1.0) && bpm.getChannel("amplitudeAvg").connectAndWait(1.0)));
            } else {
                channelConnection.replace(bpm, true);
            }
        });

        BPMnum = BPMList.size();

    }

    /**
     * Tests and initializes BPMs in the given sequence
     *
     * @param Sequence accelerator sequence.
     */
    public void connectCheckBPMs(AcceleratorSeq Sequence) {

        List<BPM> BPMList = Sequence.getAllNodesOfType("BPM");

        BPMList.parallelStream().forEach(bpm -> {
            if (!(bpm.getChannel("yAvg").isConnected() && bpm.getChannel("xAvg").isConnected() && bpm.getChannel("amplitudeAvg").isConnected())) {
                channelConnection.replace(bpm, (bpm.getChannel("yAvg").connectAndWait(1.0) && bpm.getChannel("xAvg").connectAndWait(1.0) && bpm.getChannel("amplitudeAvg").connectAndWait(1.0)));
            } else {
                channelConnection.replace(bpm, true);
            }
        });
    }

    /**
     * Tests and initializes BPMs in the given combo sequence
     *
     * @param ComboSequence accelerator sequence.
     */
    public void connectCheckBPMs(AcceleratorSeqCombo ComboSequence) {

        List<BPM> BPMList = ComboSequence.getAllNodesOfType("BPM");

        BPMList.parallelStream().forEach(bpm -> {
            if (!(bpm.getChannel("yAvg").isConnected() && bpm.getChannel("xAvg").isConnected() && bpm.getChannel("amplitudeAvg").isConnected())) {
                channelConnection.replace(bpm, (bpm.getChannel("yAvg").connectAndWait(1.0) && bpm.getChannel("xAvg").connectAndWait(1.0) && bpm.getChannel("amplitudeAvg").connectAndWait(1.0)));
            } else {
                channelConnection.replace(bpm, true);
            }
        });
    }

    /**
     * Tests and initializes BPMs in the given combo sequence
     *
     * @param BPMList List of BPMs
     */
    public void connectCheckBPMs(List<BPM> BPMList) {

        BPMList.parallelStream().forEach(bpm -> {
            if (!(bpm.getChannel("yAvg").isConnected() && bpm.getChannel("xAvg").isConnected() && bpm.getChannel("amplitudeAvg").isConnected())) {
                channelConnection.replace(bpm, (bpm.getChannel("yAvg").connectAndWait(1.0) && bpm.getChannel("xAvg").connectAndWait(1.0) && bpm.getChannel("amplitudeAvg").connectAndWait(1.0)));
            } else {
                channelConnection.replace(bpm, true);
            }
        });
    }

    /**
     * Reads the trajectory for a given sequence and accelerator (xml file)
     *
     * @param accl accelerator.
     * @param Seq name of the sequence.
     * @throws xal.ca.ConnectionException
     * @throws xal.ca.GetException
     */
    public void readTrajectory(Accelerator accl, String Seq) throws ConnectionException, GetException {

        List<BPM> BPMList = new ArrayList<>();
        String Sequence = accl.getSequences().toString();
        String ComboSequence = accl.getComboSequences().toString();

        if (Sequence.contains(Seq)) {
            BPMList = accl.getSequence(Seq).getAllNodesOfType("BPM");
        } else if (ComboSequence.contains(Seq)) {
            BPMList = accl.getComboSequence(Seq).getAllNodesOfType("BPM");
        } else {
            BPMList = accl.getAllNodesOfType("BPM");
        }

        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();
        AvgAmpl.clear();

        for (BPM item : BPMList) {
            Pos.put(item, item.getSDisplay());
            if (channelConnection.get(item)) {
                X.put(item, item.getXAvg());
                Y.put(item, item.getYAvg());
                AvgAmpl.put(item, item.getAmpAvg());
                if (XRef.containsKey(item)) {
                    XDiff.put(item, X.get(item) - XRef.get(item));
                    YDiff.put(item, Y.get(item) - YRef.get(item));
                } else {
                    XDiff.put(item, X.get(item));
                    YDiff.put(item, Y.get(item));
                }
            } else {
                X.put(item, 0.0);
                Y.put(item, 0.0);
                XDiff.put(item, 0.0);
                YDiff.put(item, 0.0);
                AvgAmpl.put(item, 0.0);
            }
        }

        BPMnum = BPMList.size();

    }

    /**
     * Reads the trajectory for a given set of BPMs
     *
     * @param BPMList List of BPMs to read the trajectory from.
     * @throws xal.ca.ConnectionException
     * @throws xal.ca.GetException
     */
    public void readTrajectory(List<BPM> BPMList) throws ConnectionException, GetException {

        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();
        AvgAmpl.clear();

        for (BPM item : BPMList) {
            Pos.put(item, item.getSDisplay());
            if (channelConnection.get(item)) {
                X.put(item, item.getXAvg());
                Y.put(item, item.getYAvg());
                AvgAmpl.put(item, item.getAmpAvg());
                if (XRef.containsKey(item)) {
                    XDiff.put(item, X.get(item) - XRef.get(item));
                    YDiff.put(item, Y.get(item) - YRef.get(item));
                } else {
                    XDiff.put(item, X.get(item));
                    YDiff.put(item, Y.get(item));
                }
            } else {
                X.put(item, 0.0);
                Y.put(item, 0.0);
                XDiff.put(item, 0.0);
                YDiff.put(item, 0.0);
                AvgAmpl.put(item, 0.0);
            }
        }

        BPMnum = BPMList.size();

    }

    /**
     * Load a trajectory from URL
     *
     * @param filename name of the file (full path).
     * @throws java.io.FileNotFoundException
     */
    public void loadTrajectory(List<BPM> BPMList, URL filename) throws FileNotFoundException, IOException {
        DataAdaptor readAdp = null;
        String[] bpmNames;
        double[] posS;
        double[] posX;
        double[] posY;
        List<String> listBPMname = new ArrayList<>();

        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();
        AvgAmpl.clear();

        readAdp = XmlDataAdaptor.adaptorForUrl(filename, false);
        DataAdaptor header = readAdp.childAdaptor("ReferenceTrajectory");
        DataAdaptor trajData = header.childAdaptor("TrajectoryData");
        DataAdaptor BPMData = trajData.childAdaptor("BPM");
        bpmNames = BPMData.stringValue("data").split(",");
        for (int k = 0; k < bpmNames.length; k += 1) {
            listBPMname.add(bpmNames[k]);
        }
        DataAdaptor PosData = trajData.childAdaptor("Position");
        posS = PosData.doubleArray("data");
        DataAdaptor XData = trajData.childAdaptor("Horizontal");
        posX = XData.doubleArray("data");
        DataAdaptor YData = trajData.childAdaptor("Vertical");
        posY = YData.doubleArray("data");

        BPMList.forEach(item -> {
            Pos.put(item, posS[listBPMname.indexOf(item.toString())]);
            X.put(item, posX[listBPMname.indexOf(item.toString())]);
            Y.put(item, posY[listBPMname.indexOf(item.toString())]);
            if (XRef.containsKey(item)) {
                XDiff.put(item, X.get(item) - XRef.get(item));
                YDiff.put(item, Y.get(item) - YRef.get(item));
            } else {
                XDiff.put(item, X.get(item));
                YDiff.put(item, Y.get(item));
            }
            AvgAmpl.put(item, 0.0);
        });

    }

    /**
     * Loads a trajectory from file
     *
     * @param filename name of the file (full path).
     * @throws java.io.FileNotFoundException
     */
    public void loadTrajectory(List<BPM> BPMList, File filename) throws FileNotFoundException, IOException {
        DataAdaptor readAdp = null;
        String[] bpmNames;
        double[] posS;
        double[] posX;
        double[] posY;
        List<String> listBPMname = new ArrayList<>();

        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();
        AvgAmpl.clear();

        readAdp = XmlDataAdaptor.adaptorForFile(filename, false);
        DataAdaptor header = readAdp.childAdaptor("ReferenceTrajectory");
        DataAdaptor trajData = header.childAdaptor("TrajectoryData");
        DataAdaptor BPMData = trajData.childAdaptor("BPM");
        bpmNames = BPMData.stringValue("data").split(",");
        for (int k = 0; k < bpmNames.length; k += 1) {
            listBPMname.add(bpmNames[k]);
        }
        DataAdaptor PosData = trajData.childAdaptor("Position");
        posS = PosData.doubleArray("data");
        DataAdaptor XData = trajData.childAdaptor("Horizontal");
        posX = XData.doubleArray("data");
        DataAdaptor YData = trajData.childAdaptor("Vertical");
        posY = YData.doubleArray("data");

        BPMList.forEach(item -> {
            Pos.put(item, posS[listBPMname.indexOf(item.toString())]);
            X.put(item, posX[listBPMname.indexOf(item.toString())]);
            Y.put(item, posY[listBPMname.indexOf(item.toString())]);
            if (XRef.containsKey(item)) {
                XDiff.put(item, X.get(item) - XRef.get(item));
                YDiff.put(item, Y.get(item) - YRef.get(item));
            } else {
                XDiff.put(item, X.get(item));
                YDiff.put(item, Y.get(item));
            }
            AvgAmpl.put(item, 0.0);
        });

    }

    /**
     * Set the current trajectory at a given sequence as reference
     *
     * @param accl accelerator.
     * @throws xal.ca.ConnectionException
     * @throws xal.ca.GetException
     */
    public void readReferenceTrajectory(Accelerator accl) throws ConnectionException, GetException {

        List<BPM> bpm = new ArrayList<>();
        bpm = accl.getAllNodesOfType("BPM");

        XRef.clear();
        YRef.clear();

        for (BPM item : bpm) {
            if (channelConnection.get(item)) {
                XRef.put(item, item.getXAvg());
                YRef.put(item, item.getYAvg());
            } else {
                XRef.put(item, 0.0);
                YRef.put(item, 0.0);
            }
        }

    }

    /**
     * Reads the reference trajectory from URL
     *
     * @param accl accelerator.
     * @param filename name of the file (full path).
     * @throws java.io.FileNotFoundException
     */
    public void readReferenceTrajectory(Accelerator accl, URL filename) throws FileNotFoundException, IOException {
        DataAdaptor readAdp = null;
        String[] bpmNames;
        double[] posX;
        double[] posY;
        List<String> listBPMname = new ArrayList<>();
        List<BPM> BPMList = accl.getAllNodesOfType("BPM");

        XRef.clear();
        YRef.clear();

        readAdp = XmlDataAdaptor.adaptorForUrl(filename, false);
        DataAdaptor header = readAdp.childAdaptor("ReferenceTrajectory");
        DataAdaptor trajData = header.childAdaptor("TrajectoryData");
        DataAdaptor BPMData = trajData.childAdaptor("BPM");
        bpmNames = BPMData.stringValue("data").split(",");
        for (int k = 0; k < bpmNames.length; k += 1) {
            listBPMname.add(bpmNames[k]);
        }
        
        DataAdaptor XData = trajData.childAdaptor("Horizontal");
        posX = XData.doubleArray("data");
        DataAdaptor YData = trajData.childAdaptor("Vertical");
        posY = YData.doubleArray("data");

        BPMList.forEach(item -> {
            XRef.put(item, posX[listBPMname.indexOf(item.toString())]);
            YRef.put(item, posY[listBPMname.indexOf(item.toString())]);
        });

    }

    /**
     * Reads the reference trajectory from file
     *
     * @param accl accelerator.
     * @param filename name of the file (full path).
     * @throws java.io.FileNotFoundException
     */
    public void readReferenceTrajectory(Accelerator accl, File filename) throws FileNotFoundException, IOException {
        DataAdaptor readAdp = null;
        String[] bpmNames;

        double[] posX;
        double[] posY;
        List<String> listBPMname = new ArrayList<>();
        List<BPM> BPMList = accl.getAllNodesOfType("BPM");

        XRef.clear();
        YRef.clear();

        readAdp = XmlDataAdaptor.adaptorForFile(filename, false);
        DataAdaptor header = readAdp.childAdaptor("ReferenceTrajectory");
        DataAdaptor trajData = header.childAdaptor("TrajectoryData");
        DataAdaptor BPMData = trajData.childAdaptor("BPM");
        bpmNames = BPMData.stringValue("data").split(",");
        for (int k = 0; k < bpmNames.length; k += 1) {
            listBPMname.add(bpmNames[k]);
        }
        
        DataAdaptor XData = trajData.childAdaptor("Horizontal");
        posX = XData.doubleArray("data");
        DataAdaptor YData = trajData.childAdaptor("Vertical");
        posY = YData.doubleArray("data");

        BPMList.forEach(item -> {
            XRef.put(item, posX[listBPMname.indexOf(item.toString())]);
            YRef.put(item, posY[listBPMname.indexOf(item.toString())]);
        });

    }

    /**
     * Saves the trajectory to file (saves the full machine)
     *
     * @param accl accelerator.
     * @param filename name of the file (full path).
     * @throws xal.ca.ConnectionException
     */
    public void saveTrajectory(Accelerator accl, File filename) throws ConnectionException, GetException {
        //Saves the data into the file and set as reference
        XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor trajectoryAdaptor = da.createChild("ReferenceTrajectory");
        trajectoryAdaptor.setValue("title", filename.getAbsolutePath());
        trajectoryAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        List<BPM> BPMList = accl.getAllNodesOfType("BPM");
        String BPMnames = "";
        double[] posS = new double[BPMList.size()];
        double[] posX = new double[BPMList.size()];
        double[] posY = new double[BPMList.size()];
        int k = 0;

        XRef.clear();
        YRef.clear();

        for (BPM bpm : BPMList) {
            posS[k] = bpm.getSDisplay();
            if (channelConnection.get(bpm)) {
                posX[k] = bpm.getXAvg();
                posY[k] = bpm.getYAvg();
                XRef.put(bpm, posX[k]);
                YRef.put(bpm, posY[k]);
            } else {
                posX[k] = 0.0;
                posY[k] = 0.0;
                XRef.put(bpm, 0.0);
                YRef.put(bpm, 0.0);
            }
            k++;
            if (k < BPMList.size()) {
                BPMnames += bpm.toString() + ",";
            } else {
                BPMnames += bpm.toString();
            }
        }

        DataAdaptor trajData = trajectoryAdaptor.createChild("TrajectoryData");
        DataAdaptor BPMData = trajData.createChild("BPM");
        BPMData.setValue("data", BPMnames);
        DataAdaptor PosData = trajData.createChild("Position");
        PosData.setValue("data", posS);
        DataAdaptor XData = trajData.createChild("Horizontal");
        XData.setValue("data", posX);
        DataAdaptor YData = trajData.createChild("Vertical");
        YData.setValue("data", posY);

        try {
            da.writeTo(filename.getAbsoluteFile());
        } catch (IOException ex) {
            Logger.getLogger(TrajectoryArray.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Saves the trajectory to file (saves the full machine)
     *
     * @param accl accelerator.
     * @param filename name of the file (full path).
     * @throws xal.ca.ConnectionException
     * @throws xal.ca.GetException
     */
    public void saveTrajectory(Accelerator accl, URL filename) throws ConnectionException, GetException {
        //Saves the data into the file and set as reference
        XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor trajectoryAdaptor = da.createChild("ReferenceTrajectory");
        trajectoryAdaptor.setValue("title", filename.getPath());
        trajectoryAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        List<BPM> BPMList = accl.getAllNodesOfType("BPM");
        String BPMnames = "";
        double[] posS = new double[BPMList.size()];
        double[] posX = new double[BPMList.size()];
        double[] posY = new double[BPMList.size()];
        int k = 0;

        XRef.clear();
        YRef.clear();

        for (BPM bpm : BPMList) {
            posS[k] = bpm.getSDisplay();
            if (channelConnection.get(bpm)) {
                posX[k] = bpm.getXAvg();
                posY[k] = bpm.getYAvg();
                XRef.put(bpm, posX[k]);
                YRef.put(bpm, posY[k]);
            } else {
                posX[k] = 0.0;
                posY[k] = 0.0;
                XRef.put(bpm, 0.0);
                YRef.put(bpm, 0.0);
            }
            k++;
            if (k < BPMList.size()) {
                BPMnames += bpm.toString() + ",";
            } else {
                BPMnames += bpm.toString();
            }
        }

        DataAdaptor trajData = trajectoryAdaptor.createChild("TrajectoryData");
        DataAdaptor BPMData = trajData.createChild("BPM");
        BPMData.setValue("data", BPMnames);
        DataAdaptor PosData = trajData.createChild("Position");
        PosData.setValue("data", posS);
        DataAdaptor XData = trajData.createChild("Horizontal");
        XData.setValue("data", posX);
        DataAdaptor YData = trajData.createChild("Vertical");
        YData.setValue("data", posY);

        da.writeToUrl(filename);

    }

    public void saveTrajectory(Accelerator accl, URL filename, DataAdaptor da) throws ConnectionException, GetException {
        //Saves the data into the file and set as reference        
        DataAdaptor trajectoryAdaptor = da.createChild("ReferenceTrajectory");
        trajectoryAdaptor.setValue("title", filename.getFile());
        List<BPM> BPMList = accl.getAllNodesOfType("BPM");
        String BPMnames = "";
        double[] posS = new double[BPMList.size()];
        double[] posX = new double[BPMList.size()];
        double[] posY = new double[BPMList.size()];
        int k = 0;

        XRef.clear();
        YRef.clear();

        for (BPM bpm : BPMList) {
            posS[k] = bpm.getSDisplay();
            if (channelConnection.get(bpm)) {
                posX[k] = bpm.getXAvg();
                posY[k] = bpm.getYAvg();
                XRef.put(bpm, posX[k]);
                YRef.put(bpm, posY[k]);
            } else {
                posX[k] = 0.0;
                posY[k] = 0.0;
                XRef.put(bpm, 0.0);
                YRef.put(bpm, 0.0);
            }
            k++;
            if (k < BPMList.size()) {
                BPMnames += bpm.toString() + ",";
            } else {
                BPMnames += bpm.toString();
            }
        }

        DataAdaptor trajData = trajectoryAdaptor.createChild("TrajectoryData");
        DataAdaptor BPMData = trajData.createChild("BPM");
        BPMData.setValue("data", BPMnames);
        DataAdaptor PosData = trajData.createChild("Position");
        PosData.setValue("data", posS);
        DataAdaptor XData = trajData.createChild("Horizontal");
        XData.setValue("data", posX);
        DataAdaptor YData = trajData.createChild("Vertical");
        YData.setValue("data", posY);

    }

    public void saveTrajectory(Accelerator accl, File filename, DataAdaptor da) throws ConnectionException, GetException {
        //Saves the data into the file and set as reference        
        DataAdaptor trajectoryAdaptor = da.createChild("ReferenceTrajectory");
        trajectoryAdaptor.setValue("title", filename.getAbsolutePath());
        List<BPM> BPMList = accl.getAllNodesOfType("BPM");
        String BPMnames = "";
        double[] posS = new double[BPMList.size()];
        double[] posX = new double[BPMList.size()];
        double[] posY = new double[BPMList.size()];
        int k = 0;

        XRef.clear();
        YRef.clear();

        for (BPM bpm : BPMList) {
            posS[k] = bpm.getSDisplay();
            if (channelConnection.get(bpm)) {
                posX[k] = bpm.getXAvg();
                posY[k] = bpm.getYAvg();
                XRef.put(bpm, posX[k]);
                YRef.put(bpm, posY[k]);
            } else {
                posX[k] = 0.0;
                posY[k] = 0.0;
                XRef.put(bpm, 0.0);
                YRef.put(bpm, 0.0);
            }
            k++;
            if (k < BPMList.size()) {
                BPMnames += bpm.toString() + ",";
            } else {
                BPMnames += bpm.toString();
            }
        }

        DataAdaptor trajData = trajectoryAdaptor.createChild("TrajectoryData");
        DataAdaptor BPMData = trajData.createChild("BPM");
        BPMData.setValue("data", BPMnames);
        DataAdaptor PosData = trajData.createChild("Position");
        PosData.setValue("data", posS);
        DataAdaptor XData = trajData.createChild("Horizontal");
        XData.setValue("data", posX);
        DataAdaptor YData = trajData.createChild("Vertical");
        YData.setValue("data", posY);

    }

    /**
     * Resets the trajectory to zero
     */
    public void resetTrajectory() {

        Pos.keySet().stream().forEachOrdered((item) -> {
            X.put(item, 0.0);
            Y.put(item, 0.0);
        });
    }

    /**
     * Calculates the horizontal rms spread of the trajectory
     *
     * @return rmsX value
     */
    public double getXrms() {
        double rms = 0.0;
        for (BPM item : X.keySet()) {
            rms = rms + XDiff.get(item) * XDiff.get(item);
        }
        rms = Math.sqrt(1.0 / XDiff.size() * rms);

        return rms;
    }

    /**
     * Calculates the vertical rms spread of the trajectory
     *
     * @return rmsY value
     */
    public double getYrms() {
        double rms = 0.0;
        for (BPM item : Y.keySet()) {
            rms = rms + YDiff.get(item) * YDiff.get(item);
        }
        rms = Math.sqrt(1.0 / YDiff.size() * rms);

        return rms;

    }

    /**
     * Calculates the maximum trajectory displacement (absolute value)
     *
     * @return double
     */
    public double getXmax() {
        List<Double> Xval = new ArrayList<>();

        XDiff.keySet().forEach(bpm -> Xval.add(X.get(bpm)));

        return Xval.stream().max(Comparator.comparing(i -> Math.abs(i))).orElse(0.0);
    }

    /**
     * Calculates the maximum trajectory displacement (absolute value)
     *
     * @return double
     */
    public double getYmax() {
        List<Double> Yval = new ArrayList<>();

        YDiff.keySet().forEach(bpm -> Yval.add(Y.get(bpm)));

        return Yval.stream().max(Comparator.comparing(i -> Math.abs(i))).orElse(0.0);

    }

    /**
     * Sets the number of BPMs in a sequence
     *
     * @param accl accelerator.
     * @param Seq sequence name.
     * @throws xal.ca.ConnectionException
     * @throws xal.ca.GetException
     */
    public void setBPMnum(Accelerator accl, String Seq) throws ConnectionException, GetException {
        BPMnum = accl.getSequence(Seq).getAllNodesOfType("BPM").size();
    }

    /**
     * Sets value of the horizontal reference trajectory at a BPM to a given
     * value
     *
     * @param bpm beam position monitor.
     * @param val trajectory value at the bpm.
     */
    public void setRefPositionX(BPM bpm, double val) {
        XRef.put(bpm, val);
    }

    /**
     * Sets value of the vertical reference trajectory at a BPM to a given value
     *
     * @param bpm beam position monitor.
     * @param val trajectory value at the bpm.
     */
    public void setRefPositionY(BPM bpm, double val) {
        YRef.put(bpm, val);
    }

}
