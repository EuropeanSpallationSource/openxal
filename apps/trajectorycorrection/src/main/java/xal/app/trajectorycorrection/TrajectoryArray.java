/*
 * TrajectoryArray.java
 *
 * Created by Natalia Milas on 07.07.2017
 *
 * Copyright (c) 2017 European Spallation Source ERIC
 * Tunavägen 20
 * Lund, Sweden
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xal.app.trajectorycorrection;

/**
 * Defines a quantity to store on the entity Trajectory Array.
 * Can store the current orbit and the reference orbit at once.
 * It is possible to reset the values, read from file and also read from OpenXAL
 * once can also define how long is the array as you program
 * also performs the calculation of the difference orbit
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

public class TrajectoryArray {

/* ------------------------
  Class variables
 * ------------------------ */


    /** Hashmaps with the BPM and position.
        @serial internal array storage.
    */

    HashMap<xal.smf.impl.BPM, Double> Pos = new HashMap();
    HashMap<xal.smf.impl.BPM, Double> X = new HashMap();
    HashMap<xal.smf.impl.BPM, Double> Y = new HashMap();
    HashMap<xal.smf.impl.BPM, Double> XDiff = new HashMap();
    HashMap<xal.smf.impl.BPM, Double> YDiff = new HashMap();
    HashMap<xal.smf.impl.BPM, Double> XRef = new HashMap();
    HashMap<xal.smf.impl.BPM, Double> YRef = new HashMap();

    /** Number of BPMs in the sequence.
        @serial number of BPMs.
    */
    protected int BPMnum = 98;


/* ------------------------
 Constructors
* ------------------------ */

    /** Reads the trajectory for a given sequence and accelerator (xml file)
    @param accl   accelerator.
    @param Seq    name of the sequence.
    @throws xal.ca.ConnectionException
    @throws xal.ca.GetException
    */

    public void readTrajectory(xal.smf.Accelerator accl, String Seq) throws ConnectionException, GetException{

        List<xal.smf.impl.BPM> BPM = new ArrayList<>();
        String Sequence = accl.getSequences().toString();
        String ComboSequence = accl.getComboSequences().toString();

        if(Sequence.contains(Seq)){
            BPM = accl.getSequence(Seq).getAllNodesOfType("BPM");
        } else if (ComboSequence.contains(Seq)){
            BPM = accl.getComboSequence(Seq).getAllNodesOfType("BPM");
        } else {
            BPM = accl.getAllNodesOfType("BPM");
        }

        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();

        for(xal.smf.impl.BPM item:BPM){
            Pos.put(item, item.getSDisplay());
            X.put(item, item.getXAvg());
            Y.put(item, item.getYAvg());
            if(XRef.containsKey(item)){
                XDiff.put(item, X.get(item) - XRef.get(item));
                YDiff.put(item, Y.get(item) - YRef.get(item));
            } else {
                XDiff.put(item,X.get(item));
                YDiff.put(item,Y.get(item));
            }
        }

        BPMnum = BPM.size();

    }

    /** Reads the trajectory for a given set of BPMs
    @param BPMList   List of BPMs to read the trajectory from.
    @throws xal.ca.ConnectionException
    @throws xal.ca.GetException
    */
    public void readBPMListTrajectory(List<xal.smf.impl.BPM> BPMList) throws ConnectionException, GetException{

        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();

        for(xal.smf.impl.BPM item:BPMList){
            Pos.put(item, item.getSDisplay());
            X.put(item, item.getXAvg());
            Y.put(item, item.getYAvg());
            if(XRef.containsKey(item)){
                XDiff.put(item, X.get(item) - XRef.get(item));
                YDiff.put(item, Y.get(item) - YRef.get(item));
            } else {
                XDiff.put(item,X.get(item));
                YDiff.put(item,Y.get(item));
            }
        }

        BPMnum = BPMList.size();

    }

    /** Set the current trajectory at a given sequence as reference
    @param accl   accelerator.
    @throws xal.ca.ConnectionException
    @throws xal.ca.GetException
    */

    public void readReferenceTrajectory(xal.smf.Accelerator accl) throws ConnectionException, GetException{

        List<xal.smf.impl.BPM> BPM = new ArrayList<>();
        BPM = accl.getAllNodesOfType("BPM");
        
        XRef.clear();
        YRef.clear();

        for(xal.smf.impl.BPM item:BPM){
            XRef.put(item, item.getXAvg());
            YRef.put(item, item.getYAvg());
        }

    }   

    /** Reads the reference trajectory from file
    @param accl     accelerator.
    @param filename name of the file (full path).
    @throws java.io.FileNotFoundException
    */

    public void readReferenceTrajectoryFromFile(xal.smf.Accelerator accl, File filename) throws FileNotFoundException, IOException{
        DataAdaptor readAdp = null;
        String[] bpmNames;
        double[] posS;
        double[] posX;
        double[] posY;
        List<String> listBPMname = new ArrayList<>();
        List<xal.smf.impl.BPM> BPMList = accl.getAllNodesOfType("BPM");

        XRef.clear();
        YRef.clear();

        readAdp = XmlDataAdaptor.adaptorForFile(filename, false);
        DataAdaptor header = readAdp.childAdaptor("ReferenceTrajectory");
        DataAdaptor trajData =  header.childAdaptor("TrajectoryData");
        DataAdaptor BPMData =  trajData.childAdaptor("BPM");
        bpmNames = BPMData.stringValue("data").split(",");
        for(int k=0; k<bpmNames.length ; k+=1){
            listBPMname.add(bpmNames[k]);
        }
        DataAdaptor PosData =  trajData.childAdaptor("Position");
        posS = PosData.doubleArray("data");
        DataAdaptor XData =  trajData.childAdaptor("Horizontal");
        posX = XData.doubleArray("data");
        DataAdaptor YData =  trajData.childAdaptor("Vertical");
        posY = YData.doubleArray("data");
        
        BPMList.forEach(item -> {            
            XRef.put(item,posX[listBPMname.indexOf(item.toString())]);
            YRef.put(item,posY[listBPMname.indexOf(item.toString())]);           
        });
        
    }

    /** Saves the trajectory to file (saves the full machine)
    @param accl   accelerator.
    @param filename name of the file (full path).
    @throws xal.ca.ConnectionException
    */

    public void saveTrajectory(xal.smf.Accelerator accl, File filename) throws ConnectionException{
        //Saves the data into the file and set as reference
        XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor trajectoryAdaptor =  da.createChild("ReferenceTrajectory");
        trajectoryAdaptor.setValue("title", filename.getAbsolutePath());
        trajectoryAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        List<xal.smf.impl.BPM> BPMList = accl.getAllNodesOfType("BPM"); 
        String BPMnames = "";
        double[] posS = new double[BPMList.size()];
        double[] posX = new double[BPMList.size()];
        double[] posY = new double[BPMList.size()];
        int k = 0;
        
        XRef.clear();
        YRef.clear();
        
        for(xal.smf.impl.BPM bpm: BPMList){            
            posS[k] = bpm.getSDisplay();
            try {
                posX[k] = bpm.getXAvg();
                posY[k] = bpm.getYAvg();
                XRef.put(bpm,posX[k]);
                YRef.put(bpm,posY[k]);
            } catch (GetException ex) {
                Logger.getLogger(TrajectoryArray.class.getName()).log(Level.SEVERE, null, ex);
            }            
            k++;
            if(k<BPMList.size()){
                BPMnames+=bpm.toString()+",";
            } else {
                BPMnames+=bpm.toString();        
            }
        }        
        
        DataAdaptor trajData =  trajectoryAdaptor.createChild("TrajectoryData");
        DataAdaptor BPMData =  trajData.createChild("BPM");
        BPMData.setValue("data", BPMnames);
        DataAdaptor PosData =  trajData.createChild("Position");
        PosData.setValue("data", posS);
        DataAdaptor XData =  trajData.createChild("Horizontal");
        XData.setValue("data", posX);
        DataAdaptor YData =  trajData.createChild("Vertical");
        YData.setValue("data", posY);
        
        try {        
            da.writeTo(filename.getAbsoluteFile());
        } catch (IOException ex) {
            Logger.getLogger(TrajectoryArray.class.getName()).log(Level.SEVERE, null, ex);
        }
                       
    }

    public void saveTrajectory(xal.smf.Accelerator accl, File filename, DataAdaptor da) throws ConnectionException{
        //Saves the data into the file and set as reference        
        DataAdaptor trajectoryAdaptor =  da.createChild("ReferenceTrajectory");
        trajectoryAdaptor.setValue("title", filename);
        List<xal.smf.impl.BPM> BPMList = accl.getAllNodesOfType("BPM"); 
        String BPMnames = "";
        double[] posS = new double[BPMList.size()];
        double[] posX = new double[BPMList.size()];
        double[] posY = new double[BPMList.size()];
        int k = 0;
        
        XRef.clear();
        YRef.clear();
        
        for(xal.smf.impl.BPM bpm: BPMList){            
            posS[k] = bpm.getSDisplay();
            try {
                posX[k] = bpm.getXAvg();
                posY[k] = bpm.getYAvg();
                XRef.put(bpm,posX[k]);
                YRef.put(bpm,posY[k]);
            } catch (GetException ex) {
                Logger.getLogger(TrajectoryArray.class.getName()).log(Level.SEVERE, null, ex);
            }            
            k++;
            if(k<BPMList.size()){
                BPMnames+=bpm.toString()+",";
            } else {
                BPMnames+=bpm.toString();        
            }
        }        
        
        DataAdaptor trajData =  trajectoryAdaptor.createChild("TrajectoryData");
        DataAdaptor BPMData =  trajData.createChild("BPM");
        BPMData.setValue("data", BPMnames);
        DataAdaptor PosData =  trajData.createChild("Position");
        PosData.setValue("data", posS);
        DataAdaptor XData =  trajData.createChild("Horizontal");
        XData.setValue("data", posX);
        DataAdaptor YData =  trajData.createChild("Vertical");
        YData.setValue("data", posY);                
                       
    }    
    
    /** Resets the trajectory to zero
    */

    public void resetTrajectory(){

        Pos.keySet().stream().forEachOrdered((item) -> {
            X.put(item,0.0);
            Y.put(item,0.0);
        });
    }

    /** Calculates the horizontal rms spread of the trajectory
    @return rmsX value
    */

    public double getXrms(){
        double rms = 0.0;
        for(xal.smf.impl.BPM item : X.keySet()){
            rms = rms + XDiff.get(item)*XDiff.get(item);
        }
        rms = Math.sqrt(1.0/XDiff.size()*rms);

        return rms;
    }

    /** Calculates the vertical rms spread of the trajectory
    @return rmsY value
    */

    public double getYrms(){
        double rms = 0.0;
        for(xal.smf.impl.BPM item : Y.keySet()){
            rms = rms + YDiff.get(item)*YDiff.get(item);
        }
        rms = Math.sqrt(1.0/YDiff.size()*rms);

        return rms;

    }

    /** Calculates the maximum trajectory displacement (absolute value)
     * @return double
    */
    
    public double getXmax(){
        List<Double> Xval =  new ArrayList<>();
                     
        XDiff.keySet().forEach(bpm -> Xval.add(X.get(bpm)));
               
        return Xval.stream().max(Comparator.comparing(i -> Math.abs(i))).orElse(0.0);
    }
    
    /** Calculates the maximum trajectory displacement (absolute value)
     * @return double
    */
    
    public double getYmax(){
        List<Double> Yval =  new ArrayList<>();
                     
        YDiff.keySet().forEach(bpm -> Yval.add(Y.get(bpm)));
               
        return Yval.stream().max(Comparator.comparing(i -> Math.abs(i))).orElse(0.0);
        
    }
    
    /** Sets the number of BPMs in a sequence
    @param accl   accelerator.
    @param Seq    sequence name.
    @throws xal.ca.ConnectionException
    @throws xal.ca.GetException
    */

    public void setBPMnum(xal.smf.Accelerator accl, String Seq) throws ConnectionException, GetException{
        BPMnum = accl.getSequence(Seq).getAllNodesOfType("BPM").size();
    }

    /** Sets value of the horizontal reference trajectory at a BPM to a given value
    @param bpm  beam position monitor.
    @param val  trajectory value at the bpm.
    */

    public void setRefPositionX(xal.smf.impl.BPM bpm, double val){
        XRef.put(bpm, val);
    }

    /** Sets value of the vertical reference trajectory at a BPM to a given value
    @param bpm  beam position monitor.
    @param val  trajectory value at the bpm.
    */

    public void setRefPositionY(xal.smf.impl.BPM bpm, double val){
        YRef.put(bpm, val);
    }

}
