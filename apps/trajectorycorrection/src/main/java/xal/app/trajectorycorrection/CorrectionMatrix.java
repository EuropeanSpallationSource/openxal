/*
 * Copyright (C) 2017 European Spallation Source ERIC
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


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.extension.fit.LinearFit;
import xal.model.ModelException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.BPM;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.VDipoleCorr;
import xal.tools.math.r3.R3;

/**
 * Class that measures and calculates the corrector kick for the 1-to-1 correction
 * @author nataliamilas
 * 06-2017
 */

public class CorrectionMatrix {
    
    //result of reponse fit
    HashMap<xal.smf.impl.BPM, double[]> VertParam = new HashMap();
    HashMap<xal.smf.impl.BPM, double[]> HorParam = new HashMap();
    
    //bpm-corrector pairs
    HashMap<xal.smf.impl.BPM,xal.smf.impl.HDipoleCorr> HC = new HashMap();
    HashMap<xal.smf.impl.BPM,xal.smf.impl.VDipoleCorr> VC = new HashMap();
   
    public HashMap<BPM, double[]> getVertParam() {
        return VertParam;
    }

    public void setVertParam(HashMap<BPM, double[]> VertParam) {
        this.VertParam = VertParam;
    }

    public HashMap<BPM, double[]> getHorParam() {
        return HorParam;
    }

    public void setHorParam(HashMap<BPM, double[]> HorParam) {
        this.HorParam = HorParam;
    }

    public HashMap<BPM, HDipoleCorr> getHC() {
        return HC;
    }

    public void setHC(HashMap<BPM, HDipoleCorr> HC) {
        this.HC = HC;
    }

    public HashMap<BPM, VDipoleCorr> getVC() {
        return VC;
    }

    public void setVC(HashMap<BPM, VDipoleCorr> VC) {
        this.VC = VC;
    }
    
   public void setPairs(xal.smf.Accelerator accl, List<xal.smf.impl.BPM> BPMList, List<xal.smf.impl.HDipoleCorr> HCList, List<xal.smf.impl.VDipoleCorr> VCList) throws ConnectionException, GetException, IOException{
                
        //Get list of BPM and correctors    
        List<xal.smf.impl.BPM> allBPMs = accl.getAllNodesOfType("BPM");   
        RunSimulationService simulService; 
        AcceleratorSeq iniSeq;
        AcceleratorSeq finalSeq;
        HashMap<xal.smf.AcceleratorNode, R3> phase= new HashMap();        
        
        //remove elements from LEBT and RFQ
        for(xal.smf.impl.HDipoleCorr hc : HCList){
            if(hc.getPrimaryAncestor().toString().equals("LEBT") || hc.getPrimaryAncestor().toString().equals("RFQ")){
                HCList.remove(hc);
            }
        }
        
        for(xal.smf.impl.VDipoleCorr vc : VCList){
            if(vc.getPrimaryAncestor().toString().equals("LEBT") || vc.getPrimaryAncestor().toString().equals("RFQ")){
                VCList.remove(vc);
            }
        }
        
        for(xal.smf.impl.BPM bpm : BPMList){
            if(bpm.getPrimaryAncestor().toString().equals("LEBT") || bpm.getPrimaryAncestor().toString().equals("RFQ")){
                BPMList.remove(bpm);
            }
        }
                       
        
        //Run Simulation to get phase advance
        if (VCList.get(0).getSDisplay()<HCList.get(0).getSDisplay()){                        
            iniSeq = VCList.get(0).getPrimaryAncestor();            
        } else { 
            iniSeq = HCList.get(0).getPrimaryAncestor(); 
        }
        finalSeq = BPMList.get(BPMList.size()-1).getPrimaryAncestor();
        if(iniSeq != finalSeq){
            List<AcceleratorSeq> newCombo = new ArrayList<>();             
            for(int i=accl.getAllSeqs().indexOf(iniSeq); i<=accl.getAllSeqs().indexOf(finalSeq); i++){
                newCombo.add(accl.getAllSeqs().get(i));
            }
            AcceleratorSeqCombo Sequence = new xal.smf.AcceleratorSeqCombo("calcMatrix",newCombo);
            simulService = new RunSimulationService(Sequence); 
            simulService.setSynchronizationMode("DESIGN");
        } else {
           simulService = new RunSimulationService(iniSeq);
           simulService.setSynchronizationMode("DESIGN");
        }                     
        
        List<xal.smf.AcceleratorNode> elements = Stream.of(HCList,VCList,BPMList).flatMap(Collection::stream).collect(Collectors.toList());
        try {
            phase = simulService.runTwissSimulation(elements);
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(PairBPMandCorrectorController.class.getName()).log(Level.SEVERE, null, ex);
        }               
        
        //finds initial position for search
        AcceleratorNode bpmIni = BPMList.get(0).getPrimaryAncestor();
        for(xal.smf.impl.BPM item: allBPMs){
            if(BPMList.get(0) == item && allBPMs.indexOf(item)>0){
                bpmIni = allBPMs.get(allBPMs.indexOf(item)-1);
            } 
        }
        
        //Start search
        for(xal.smf.impl.BPM bpm: BPMList){
        //search for horizontal corrector
            for(xal.smf.impl.HDipoleCorr hcor: HCList){
                if((hcor.getSDisplay()>bpmIni.getSDisplay()) && (hcor.getSDisplay()<bpm.getSDisplay())){
                    if(HC.containsKey(bpm)){
                        if (Math.abs(phase.get(bpm).getx()-phase.get(hcor).getx()-0.5)<Math.abs(phase.get(bpm).getx()-phase.get(HC.get(bpm)).getx()-0.5)){
                           HC.put(bpm, hcor);
                        }
                    } else {
                        HC.put(bpm, hcor);
                        HorParam.put(bpm,new double[2]);
                    }
                }            
            }
            //search for vertical corrector
            for(xal.smf.impl.VDipoleCorr vcor: VCList){                
                if(vcor.getSDisplay()>bpmIni.getSDisplay() && vcor.getSDisplay()<bpm.getSDisplay()){
                    if(VC.containsKey(bpm)){
                        if (Math.abs(phase.get(bpm).gety()-phase.get(vcor).gety()-0.5)<Math.abs(phase.get(bpm).gety()-phase.get(VC.get(bpm)).gety()-0.5)){
                            VC.put(bpm, vcor);
                        }
                    } else {
                        VC.put(bpm, vcor);
                        VertParam.put(bpm,new double[2]);
                    }                    
                }            
            }
            bpmIni = bpm;
        }  
          
    }
   
    public Boolean checkPairs(Window owner,xal.smf.Accelerator accl, List<xal.smf.impl.BPM> BPMList, List<xal.smf.impl.HDipoleCorr> HCList, List<xal.smf.impl.VDipoleCorr> VCList) throws ConnectionException, GetException, IOException{
        Stage stage; 
        Parent root;
        URL    url  = null;
        BooleanProperty changedPairs = new SimpleBooleanProperty();
        String sceneFile = "/fxml/PairBPMandCorrector.fxml";
        
        changedPairs.setValue(false);
        
        try
        {
            stage = new Stage();
            url  = getClass().getResource(sceneFile);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(sceneFile));
            root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Pair BPM and Corrector");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(owner);
            PairBPMandCorrectorController loginController = loader.getController();
            loginController.setAllVariables(accl, BPMList, HCList, VCList);
            loginController.setInitialPairs(HC, VC);
            loginController.createGui();
            loginController.loggedInProperty().addListener((obs, wasLoggedIn, isNowLoggedIn) -> {
                if (isNowLoggedIn) {                    
                    if(loginController.isPairChanged()){                        
                        this.setHC(loginController.updateHPairs());                        
                        this.setVC(loginController.updateVPairs());                        
                    }  
                    changedPairs.set(!loginController.isPairChanged());
                    stage.close();
                }
            });           
            stage.showAndWait();            
        }
        catch ( IOException ex )
        {
            System.out.println( "Exception on FXMLLoader.load()" );
            System.out.println( "  * url: " + url );
            System.out.println( "  * " + ex );
            System.out.println( "    ----------------------------------------\n" );
            throw ex;
        }
        
        return changedPairs.getValue();
        
    }
   
    public void getHCalibration(xal.smf.impl.BPM bpmKey, double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
       /* make +Dk and -Dk increments in the corrector strength and 
        * calculate the offset and slope
        */

        double HC_val = 0.0;
        double BPM_val = 0.0;
        double corrector_auxval = 0.0;
        double bpm_auxval = 0.0;
        double[] fitresult = new double[2];
        LinearFit result;

        //restart the array before linear fit
        result= new LinearFit();
        //measure response 
        HC_val = HC.get(bpmKey).getField();
        BPM_val = bpmKey.getXAvg();
        result.addSample(0.0, 0.0);
        corrector_auxval = HC_val + Dk;
        HC.get(bpmKey).setField(corrector_auxval);
        //TimeUnit.SECONDS.sleep(2);
        Thread.sleep(2000);
        bpm_auxval = bpmKey.getXAvg()- BPM_val;
        result.addSample(Dk, bpm_auxval);
        corrector_auxval = HC_val - Dk;
        HC.get(bpmKey).setField(corrector_auxval);
        Thread.sleep(2000);
        bpm_auxval = bpmKey.getXAvg() - BPM_val;
        result.addSample(-Dk, bpm_auxval);
        //restore the original field
        HC.get(bpmKey).setField(HC_val);
        Thread.sleep(2000);
        //calculate parameters from line fit 
        fitresult[0] = result.getIntercept();
        fitresult[1] = result.getSlope();
        HorParam.put(bpmKey, fitresult); 
    }
   
    public void getVCalibration(xal.smf.impl.BPM bpmKey, double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
       /* make +Dk and -Dk increments in the corrector strength and 
        * calculate the offset and slope
        */
       
        double VC_val = 0.0;
        double BPM_val = 0.0;
        double corrector_auxval = 0.0;
        double bpm_auxval = 0.0;
        double[] fitresult = new double[2];
        LinearFit result;

        //restart the array before linear fit
        result= new LinearFit();
        //measure response 
        VC_val = VC.get(bpmKey).getField();
        BPM_val = bpmKey.getYAvg();
        result.addSample(0.0, 0.0);
        corrector_auxval = VC_val + Dk;
        VC.get(bpmKey).setField(corrector_auxval);
        //TimeUnit.SECONDS.sleep(2);
        Thread.sleep(2000);
        bpm_auxval = bpmKey.getYAvg()- BPM_val;
        result.addSample(Dk, bpm_auxval);
        corrector_auxval = VC_val - Dk;
        VC.get(bpmKey).setField(corrector_auxval);
        Thread.sleep(2000);
        bpm_auxval = bpmKey.getYAvg() - BPM_val;
        result.addSample(-Dk, bpm_auxval);
        //restore the original field
        VC.get(bpmKey).setField(VC_val);
        Thread.sleep(2000);
        //calculate parameters from line fit 
        fitresult[0] = result.getIntercept();
        fitresult[1] = result.getSlope();
        VertParam.put(bpmKey, fitresult);
        
    }
    
    public void simulHCalibration(xal.smf.impl.BPM bpmKey, double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
       /* make +Dk and -Dk increments in the corrector strength and 
        * simulate the offset and slope
        */

        double HC_val = 0.0;
        double BPM_val = 0.0;
        double corrector_auxval = 0.0;
        double bpm_auxval = 0.0;
        double[] fitresult = new double[2];
        LinearFit result;
        RunSimulationService simulService;
        HashMap<xal.smf.impl.BPM, Double> trajectory= new HashMap();
        List<xal.smf.impl.BPM> BPMList = new ArrayList<>();;

        //restart the array before linear fit
        result= new LinearFit();
        
        //setup simulation parameters
        if (bpmKey.getPrimaryAncestor()!=HC.get(bpmKey).getPrimaryAncestor()){
            xal.smf.Accelerator accl = bpmKey.getAccelerator();
            List<AcceleratorSeq> newCombo = new ArrayList<>();
            newCombo.add(HC.get(bpmKey).getPrimaryAncestor());
            newCombo.add(bpmKey.getPrimaryAncestor());
            xal.smf.AcceleratorSeqCombo Sequence = new xal.smf.AcceleratorSeqCombo("calcMatrix",newCombo); 
            simulService = new RunSimulationService(Sequence);
        } else { 
            xal.smf.AcceleratorSeq Sequence = bpmKey.getPrimaryAncestor();
            simulService = new RunSimulationService(Sequence);
        }
        simulService.setSynchronizationMode("DESIGN");
        
        HC.keySet().forEach(bpm -> BPMList.add(bpm));
        BPMList.sort((bpm1,bpm2) -> Double.compare(bpm1.getSDisplay(), bpm2.getSDisplay()));
        
        //measure response 
        HC_val = HC.get(bpmKey).getDfltField();        
        try {
            trajectory = simulService.runTrajectorySimulation(BPMList,"X");
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        BPM_val = 1000*trajectory.get(bpmKey);
        result.addSample(0.0, 0.0);
        //positive step
        corrector_auxval = HC_val + Dk;
        HC.get(bpmKey).setDfltField(corrector_auxval);
        try {
            trajectory = simulService.runTrajectorySimulation(BPMList,"X");
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        bpm_auxval = 1000*trajectory.get(bpmKey)- BPM_val;
        result.addSample(Dk, bpm_auxval);
        //negative step
        corrector_auxval = HC_val - Dk;
        HC.get(bpmKey).setDfltField(corrector_auxval);
        try {
            trajectory = simulService.runTrajectorySimulation(BPMList,"X");
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        bpm_auxval = 1000*trajectory.get(bpmKey)- BPM_val;
        result.addSample(-Dk, bpm_auxval);
        //restore the original field
        HC.get(bpmKey).setDfltField(HC_val);        
        //calculate parameters from line fit 
        fitresult[0] = result.getIntercept();
        fitresult[1] = result.getSlope();
        HorParam.put(bpmKey, fitresult); 
    }
   
    public void simulVCalibration(xal.smf.impl.BPM bpmKey, double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
       /* make +Dk and -Dk increments in the corrector strength and 
        * simulate the offset and slope
        */
       
        double VC_val = 0.0;
        double BPM_val = 0.0;
        double corrector_auxval = 0.0;
        double bpm_auxval = 0.0;
        double[] fitresult = new double[2];
        LinearFit result;
        RunSimulationService simulService;
        HashMap<xal.smf.impl.BPM, Double> trajectory= new HashMap();
        List<xal.smf.impl.BPM> BPMList = new ArrayList<>();
        
        //restart the array before linear fit
        result= new LinearFit();
        
        //setup simulation parameters
        if (bpmKey.getPrimaryAncestor()!=VC.get(bpmKey).getPrimaryAncestor()){
            List<AcceleratorSeq> newCombo = new ArrayList<>();
            newCombo.add(VC.get(bpmKey).getPrimaryAncestor());
            newCombo.add(bpmKey.getPrimaryAncestor());
            xal.smf.AcceleratorSeqCombo Sequence = new xal.smf.AcceleratorSeqCombo("calcMatrix",newCombo); 
            simulService = new RunSimulationService(Sequence);
        } else { 
            xal.smf.AcceleratorSeq Sequence = bpmKey.getPrimaryAncestor();
            simulService = new RunSimulationService(Sequence);
        }
        simulService.setSynchronizationMode("DESIGN");
        
        VC.keySet().forEach(bpm -> BPMList.add(bpm));
        BPMList.sort((bpm1,bpm2) -> Double.compare(bpm1.getSDisplay(),bpm2.getSDisplay()));
                
        //measure response 
        VC_val = VC.get(bpmKey).getDfltField();        
        try {
            trajectory = simulService.runTrajectorySimulation(BPMList,"Y");
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        BPM_val = 1000*trajectory.get(bpmKey);
        result.addSample(0.0, 0.0);
        //positive delta
        corrector_auxval = VC_val + Dk;
        VC.get(bpmKey).setDfltField(corrector_auxval);
        try {
            trajectory = simulService.runTrajectorySimulation(BPMList,"Y");
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        bpm_auxval = 1000*trajectory.get(bpmKey)- BPM_val;
        result.addSample(Dk, bpm_auxval);
        corrector_auxval = VC_val - Dk;
        VC.get(bpmKey).setDfltField(corrector_auxval);
        try {
            trajectory = simulService.runTrajectorySimulation(BPMList,"Y");
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        bpm_auxval = 1000*trajectory.get(bpmKey)- BPM_val;
        result.addSample(-Dk, bpm_auxval);
        //restore the original field
        VC.get(bpmKey).setDfltField(VC_val);        
        //calculate parameters from line fit 
        fitresult[0] = result.getIntercept();
        fitresult[1] = result.getSlope();
        VertParam.put(bpmKey, fitresult);
        
    }
    
   
    public double calcHCorrection(xal.smf.impl.BPM item, double pos) throws ConnectionException, GetException{
       double strength = 0.0;
       
       //calculate horizontal corrector strength
       strength=-((item.getXAvg()-pos)-HorParam.get(item)[0])/HorParam.get(item)[1];
            
       return strength;
    }
   
    public double calcVCorrection(xal.smf.impl.BPM item, double pos) throws ConnectionException, GetException{
       double strength = 0.0;
              
       //calculate vertical corrector strength
       strength=-((item.getYAvg()-pos)-VertParam.get(item)[0])/VertParam.get(item)[1];
            
       return strength;
    }
  
}
