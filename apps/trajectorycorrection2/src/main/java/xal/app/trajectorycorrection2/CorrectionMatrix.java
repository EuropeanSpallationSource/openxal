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
package xal.app.trajectorycorrection2;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import xal.smf.AcceleratorSeq;
import xal.smf.impl.BPM;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.VDipoleCorr;

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
        xal.smf.AcceleratorSeq parentSequence;

        //Populate the Corrector maps
        double fim = 0.0;
        double ini = 0.0;
        double posCorrector=0.0;

        //finds initial position for search
        int bpmIndex = 0;
        if(BPMList.get(0) == allBPMs.get(0)){
                ini = 0.0;
        } else { 
            for(xal.smf.impl.BPM item: allBPMs){
                if(BPMList.get(0) == item){
                    parentSequence = allBPMs.get(bpmIndex-1).getParent();
                    ini = allBPMs.get(bpmIndex-1).getPosition()+parentSequence.getPosition();
                }
                bpmIndex++;
            }
        }

        //Start search
        for(xal.smf.impl.BPM bpm : BPMList){
            parentSequence = bpm.getParent();
            fim = bpm.getPosition()+parentSequence.getPosition();
            //search for horizontal corrector
            for(xal.smf.impl.HDipoleCorr hcor : HCList){
                parentSequence = hcor.getParent();
                posCorrector = hcor.getPosition()+parentSequence.getPosition();
                if(posCorrector>ini && posCorrector<fim){
                    HC.put(bpm, hcor);
                    HorParam.put(bpm,new double[2]);
                    break;
                }
            }
            //search for vertical corrector
            for(xal.smf.impl.VDipoleCorr vcor : VCList){
                parentSequence = vcor.getParent();
                posCorrector = vcor.getPosition()+parentSequence.getPosition();
                if(posCorrector>ini && posCorrector<fim){
                    VC.put(bpm, vcor);
                    VertParam.put(bpm,new double[2]);
                    break;
                }
            }
            parentSequence = bpm.getParent();
            ini = bpm.getPosition()+parentSequence.getPosition();
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
        if (bpmKey.getParent()!=HC.get(bpmKey).getParent()){
            xal.smf.Accelerator accl = bpmKey.getAccelerator();
            List<AcceleratorSeq> newCombo = new ArrayList<>();
            newCombo.add(HC.get(bpmKey).getParent());
            newCombo.add(bpmKey.getParent());
            xal.smf.AcceleratorSeqCombo Sequence = new xal.smf.AcceleratorSeqCombo("calcMatrix",newCombo); 
            simulService = new RunSimulationService(Sequence);
        } else { 
            xal.smf.AcceleratorSeq Sequence = bpmKey.getParent();
            simulService = new RunSimulationService(Sequence);
        }
        simulService.setSynchronizationMode("DESIGN");
        
        HC.keySet().forEach(bpm -> BPMList.add(bpm));
        BPMList.sort((bpm1,bpm2) -> Double.compare(bpm1.getPosition()+bpm1.getParent().getPosition(),bpm2.getPosition()+bpm2.getParent().getPosition()));
        
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
        if (bpmKey.getParent()!=HC.get(bpmKey).getParent()){
            List<AcceleratorSeq> newCombo = new ArrayList<>();
            newCombo.add(HC.get(bpmKey).getParent());
            newCombo.add(bpmKey.getParent());
            xal.smf.AcceleratorSeqCombo Sequence = new xal.smf.AcceleratorSeqCombo("calcMatrix",newCombo); 
            simulService = new RunSimulationService(Sequence);
        } else { 
            xal.smf.AcceleratorSeq Sequence = bpmKey.getParent();
            simulService = new RunSimulationService(Sequence);
        }
        simulService.setSynchronizationMode("DESIGN");
        
        HC.keySet().forEach(bpm -> BPMList.add(bpm));
        BPMList.sort((bpm1,bpm2) -> Double.compare(bpm1.getPosition()+bpm1.getParent().getPosition(),bpm2.getPosition()+bpm2.getParent().getPosition()));
                
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
