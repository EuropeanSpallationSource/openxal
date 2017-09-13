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

package xal.app.trajectorycorrection2;

/**
 * Defines a quantity to store on the entity Trajectory Array. 
 * Can store the current orbit and the reference orbit at once.
 * It is possible to reset the values, read from file and also read from OpenXAL
 * once can also define how long is the array as you program
 * also performs the calculation of the difference orbit
 * @author nataliamilas
 * 06-2017
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.ConnectionException;
import xal.ca.GetException;

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
    */
    
    public void readTrajectory(xal.smf.Accelerator accl, String Seq) throws ConnectionException, GetException{
        
        List<xal.smf.impl.BPM> BPM = new ArrayList<>();
        if(Seq == "ALL"){
            BPM = accl.getAllNodesOfType("BPM");
        } else {
            BPM = accl.getSequence(Seq).getAllNodesOfType("BPM");
        }
        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();
        
        for(xal.smf.impl.BPM item:BPM){
            Pos.put(item, item.getPosition()+item.getParent().getPosition());
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
    */
    public void readBPMListTrajectory(List<xal.smf.impl.BPM> BPMList) throws ConnectionException, GetException{
        
        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();
        
        for(xal.smf.impl.BPM item:BPMList){
            Pos.put(item, item.getPosition()+item.getParent().getPosition());
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
    
    /** Reads the trajectory from file 
    @param accl   accelerator.
    @param Seq    name of the sequence.
    @param filename name of the file (full path).
    */
    
    public void readTrajectoryFromFile(xal.smf.Accelerator accl, String Seq, String filename) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int numlines = 0;
        
        X.clear();
        Y.clear();
        XDiff.clear();
        YDiff.clear();
        Pos.clear();
        
        List<xal.smf.impl.BPM> BPM = new ArrayList<>();
        BPM = accl.getSequence(Seq).getAllNodesOfType("BPM");
        double bpmPosition = 0.0;
        
        try {
            String line = br.readLine();
       
            while (line != null) {
                String[] part = line.split(";");
                for(xal.smf.impl.BPM item:BPM){
                    bpmPosition = item.getPosition()+item.getParent().getPosition();
                    if((Double.parseDouble(part[0])- bpmPosition) < 10E-6){
                        Pos.put(item,Double.parseDouble(part[0]));
                        X.put(item,Double.parseDouble(part[1]));
                        Y.put(item,Double.parseDouble(part[2]));
                        if(XRef.containsKey(item)){
                            XDiff.put(item,Double.parseDouble(part[1]) - XRef.get(item));
                            YDiff.put(item,Double.parseDouble(part[2]) - YRef.get(item));
                        } else {
                            XDiff.put(item,Double.parseDouble(part[1]));
                            YDiff.put(item,Double.parseDouble(part[2]));
                        }
                    }
                }
                line = br.readLine();
            }            
        } finally {
            br.close();
        }       
        BPMnum = Pos.size();
        
    }
    
    /** Set the current trajectory at a given sequence as reference
    @param accl   accelerator.
    @param Seq    name of the sequence.
    */
    
    public void readReferenceTrajectory(xal.smf.Accelerator accl, String Seq) throws ConnectionException, GetException{
        
        List<xal.smf.impl.BPM> BPM = new ArrayList<>();
        if(Seq == "ALL"){
            BPM = accl.getAllNodesOfType("BPM");
        } else {
            BPM = accl.getSequence(Seq).getAllNodesOfType("BPM");
        }
        XRef.clear();
        YRef.clear();
        
        for(xal.smf.impl.BPM item:BPM){
            XRef.put(item, item.getXAvg());
            YRef.put(item, item.getYAvg());
        }
        
    }
    
    /** Set the current trajectory at the chosen BPMs as reference
    @param BPMList   List of BPMs to read the trajectory from.
    */
    
    public void readBPMListReferenceTrajectory(List<xal.smf.impl.BPM> BPMList) throws ConnectionException, GetException{
        
        XRef.clear();
        YRef.clear();
        
        for(xal.smf.impl.BPM item:BPMList){
            XRef.put(item, item.getXAvg());
            YRef.put(item, item.getYAvg());
        }
        
    }
    
    /** Reads the reference trajectory from file 
    @param accl     accelerator.
    @param BPMList  list of BPMs.
    @param filename name of the file (full path).
    */
    
    public void readReferenceTrajectoryFromFile(xal.smf.Accelerator accl, List<xal.smf.impl.BPM> BPMList, String filename) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int numlines = 0;
        double bpmPosition = 0.0;
        
        XRef.clear();
        YRef.clear();
          
        try {
            String line = br.readLine();
       
            while (line != null) {
                String[] part = line.split(";");
                for(xal.smf.impl.BPM item:BPMList){
                    bpmPosition = item.getPosition()+item.getParent().getPosition();
                    if((Double.parseDouble(part[0])- bpmPosition) < 10E-6){
                        XRef.put(item,Double.parseDouble(part[1]));
                        YRef.put(item,Double.parseDouble(part[2]));
                        break;
                    }
                }
                line = br.readLine();
            }            
        } finally {
            br.close();
        }       
        
    }
    
    /** Saves the trajectory to file (saves the full machine)
    @param accl   accelerator.
    @param filename name of the file (full path).
    */
    
    public void saveTrajectory(xal.smf.Accelerator accl, String filename){
        //Saves the data into the file
        List<xal.smf.impl.BPM> BPM = new ArrayList<>();
        BPM = accl.getAllNodesOfType("BPM");
        double bpmPosition = 0.0;
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename, "UTF-8");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrajectoryArray.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TrajectoryArray.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for(xal.smf.impl.BPM item:BPM){
            try {
                bpmPosition = item.getPosition()+item.getParent().getPosition();
                String line = String.format("%.4f",bpmPosition)+";"+String.format("%.4f",item.getXAvg())+";"+String.format("%.4f",item.getYAvg());    
                writer.println(line);
            } catch (ConnectionException ex) {
                Logger.getLogger(TrajectoryArray.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GetException ex) {
                Logger.getLogger(TrajectoryArray.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        writer.close();
        
    }
    
    /** Resets the trajectory to zero
    */
    
    public void resetTrajectory(){
        
        for(xal.smf.impl.BPM item : Pos.keySet()){
            X.put(item,0.0);
            Y.put(item,0.0);
        }
    }
    
    /** Calculates the horizontal rms spread of the trajectory
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
    */
    
    public double getYrms(){
        double rms = 0.0;
        for(xal.smf.impl.BPM item : Y.keySet()){
            rms = rms + YDiff.get(item)*YDiff.get(item);
        }
        rms = Math.sqrt(1.0/YDiff.size()*rms);
        
        return rms;
        
    }
    
    /** Sets the number of BPMs in a sequence
    @param accl   accelerator.
    @param Seq    sequence name.
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