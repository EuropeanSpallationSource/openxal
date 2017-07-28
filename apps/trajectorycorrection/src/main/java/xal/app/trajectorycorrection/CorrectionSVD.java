/*
 * CorrectionSVD.java
 *
 * Created by Natalia Milas on 14.07.2017
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;


/**
 * Measures the response matrix and invert using SVD, calculate corrector values
 * @author nataliamilas
 */

public class CorrectionSVD {
    
    List<xal.smf.impl.BPM> BPM = new ArrayList<>();
    List<xal.smf.impl.HDipoleCorr> HC = new ArrayList<>();
    List<xal.smf.impl.VDipoleCorr> VC = new ArrayList<>();
    Matrix TRMhorizontal;
    Matrix TRMvertical;
    private TrajectoryArray BPMval = new TrajectoryArray();
    private int m;//matrix size
    private int nh;
    private int nv;
    private int progress;
    
    public void defineKnobs(xal.smf.Accelerator accl, List<xal.smf.impl.BPM> BPMList){
        
        //Get list of BPM and correctors
        BPM = BPMList;
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
                    HC.add(hcor);
                }
            }
            //search for vertical corrector
            for(xal.smf.impl.VDipoleCorr vcor : VCList){
                parentSequence = vcor.getParent();
                posCorrector = vcor.getPosition()+parentSequence.getPosition();
                if(posCorrector>ini && posCorrector<fim){
                    VC.add(vcor);
                }
            }
            parentSequence = bpm.getParent();
            ini = bpm.getPosition()+parentSequence.getPosition();
        }
 
        nh = HC.size();
        nv = VC.size();
        m = BPM.size();
        
        TRMhorizontal = new Matrix(m,nh);
        TRMvertical = new Matrix(m,nv);
        
        try {
            //Store reference trajectory
            BPMval.readBPMListReferenceTrajectory(BPMList);
        } catch (ConnectionException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GetException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    //measure Horizontal Trajectory Response Matrix
    public void measureTRMHorizontal(Double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
        
        double HC_val = 0.0;
        double corrector_auxval = 0.0;
        int row = 0;
        int col = 0;
        
        for(xal.smf.impl.HDipoleCorr hcorr: HC){
            HC_val = hcorr.getField();
            corrector_auxval = HC_val + Dk;
            hcorr.setField(corrector_auxval);
            Thread.sleep(2000);
            BPMval.readBPMListTrajectory(BPM);
            hcorr.setField(HC_val);
            Thread.sleep(2000);
            for(xal.smf.impl.BPM bpm: BPM){
                TRMhorizontal.set(row, col, BPMval.XDiff.get(bpm)/Dk);
                row++;
            }
            col++;
            row = 0;
            progress = col/HC.size();
        }
    }
    
    //measure Vertical Trajectory Response Matrix
    public void measureTRMVertical(Double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
        
        double VC_val = 0.0;
        double corrector_auxval = 0.0;
        int row = 0;
        int col = 0;

        
        for(xal.smf.impl.VDipoleCorr vcorr: VC){
            VC_val = vcorr.getField();
            corrector_auxval = VC_val + Dk;
            vcorr.setField(corrector_auxval);
            Thread.sleep(2000);
            BPMval.readBPMListTrajectory(BPM);
            vcorr.setField(VC_val);
            Thread.sleep(2000);
            for(xal.smf.impl.BPM bpm: BPM){
                TRMvertical.set(row, col, BPMval.YDiff.get(bpm)/Dk);
                row++;
            }
            col++;
            row = 0;
            progress = col/VC.size();
        }
    }
    
    public double[] calculateHCorrection(TrajectoryArray Traj, double svCut){
        
        Matrix xPosition;
        Matrix invTRM;
        Matrix hcorrKick;
        Matrix Response = TRMhorizontal.copy();
               
        xPosition = new Matrix(m,1);
        for(int i=0; i<m ; i++){
            xPosition.set(i,0,Traj.XDiff.get(BPM.get(i)));
        }
 
        if (Response.getColumnDimension() > Response.getRowDimension()){
            Response = Response.transpose();
            invTRM = Response.pseudoinverse(svCut);
            invTRM = invTRM.transpose();
        } else {
            invTRM = Response.pseudoinverse(svCut);
        }
        
        hcorrKick = invTRM.times(xPosition);
        hcorrKick = hcorrKick.times(-1);
        
        return hcorrKick.getColumnPackedCopy();
        
    }
    
    public double[] calculateVCorrection(TrajectoryArray Traj, double svCut){
        
        Matrix yPosition;
        Matrix invTRM;
        Matrix vcorrKick;
        Matrix Response = TRMvertical.copy();
               
        yPosition = new Matrix(m,1);
        for(int i=0; i<m ; i++){
            yPosition.set(i,0,Traj.YDiff.get(BPM.get(i)));
        }
        
        if (Response.getColumnDimension() > Response.getRowDimension()){
            Response = Response.transpose();
            invTRM = Response.pseudoinverse(svCut);
            invTRM = invTRM.transpose();
        } else {
            invTRM = Response.pseudoinverse(svCut);
        }
        
        vcorrKick = invTRM.times(yPosition);
        vcorrKick = vcorrKick.times(-1);
        
        return vcorrKick.getColumnPackedCopy();
        
    }

    public int getProgress() {
        return progress;
    }
    
    public double[] getSigularValuesH() {
        
        Matrix Response = TRMhorizontal.copy();
        
        if (Response.getColumnDimension() > Response.getRowDimension()){
            Response = Response.transpose();
        }
        
        return Response.getSingularValues();
    }

    public double[] getSigularValuesV() {
        
        Matrix Response = TRMvertical.copy();
        
        if (Response.getColumnDimension() > Response.getRowDimension()){
            Response = Response.transpose();
        }
        
        return Response.getSingularValues();
    }
       
}
