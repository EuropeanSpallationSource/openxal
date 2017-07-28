/*
 * CorrectionMatrix.java
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


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.extension.fit.LinearFit;

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
   
    
   public void getPairs(xal.smf.Accelerator accl, List<xal.smf.impl.BPM> BPMList) throws ConnectionException, GetException, IOException{
         
        //Get list of BPM and correctors
        //List<xal.smf.impl.BPM> BPMList = accl.getSequence(Seq).getAllNodesOfType("BPM");
        List<xal.smf.impl.HDipoleCorr> HCList = accl.getAllNodesOfType("DCH");
        List<xal.smf.impl.VDipoleCorr> VCList = accl.getAllNodesOfType("DCV");
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
