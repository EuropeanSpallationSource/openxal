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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.model.ModelException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.BPM;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.VDipoleCorr;

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
    private final TrajectoryArray BPMval = new TrajectoryArray();
    private int m;//matrix size
    private int nh;
    private int nv;
    private double cutSVD;

    public List<BPM> getBPM() {
        return BPM;
    }

    public void setBPM(List<BPM> BPM) {
        this.BPM = BPM;
    }

    public List<HDipoleCorr> getHC() {
        return HC;
    }

    public void setHC(List<HDipoleCorr> HC) {
        this.HC = HC;
    }

    public List<VDipoleCorr> getVC() {
        return VC;
    }

    public void setVC(List<VDipoleCorr> VC) {
        this.VC = VC;
    }

    public Matrix getTRMhorizontal() {
        return TRMhorizontal;
    }

    public void setTRMhorizontal(Matrix TRMhorizontal) {
        this.TRMhorizontal = TRMhorizontal;
    }

    public Matrix getTRMvertical() {
        return TRMvertical;
    }

    public void setTRMvertical(Matrix TRMvertical) {
        this.TRMvertical = TRMvertical;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getNh() {
        return nh;
    }

    public void setNh(int nh) {
        this.nh = nh;
    }

    public int getNv() {
        return nv;
    }

    public void setNv(int nv) {
        this.nv = nv;
    }
    
    public double getCutSVD() {
        return cutSVD;
    }

    public void setCutSVD(double cutSVD) {
        this.cutSVD = cutSVD;
    }
    
    public void defineKnobs(xal.smf.Accelerator accl, List<xal.smf.impl.BPM> BPMList, List<xal.smf.impl.HDipoleCorr> HCList, List<xal.smf.impl.VDipoleCorr> VCList){
        
        //reset arrays to zero
        BPM.clear();
        HC.clear();
        VC.clear(); 
        
        //Get list of BPM and correctors
        BPM = BPMList;   
        List<xal.smf.impl.BPM> allBPMs = accl.getAllNodesOfType("BPM");   
        xal.smf.AcceleratorSeq parentSequence;
        
        if(HCList.size()<1){
            HCList = accl.getAllNodesOfType("DCH");
        }
        if(VCList.size()<1){
            VCList = accl.getAllNodesOfType("DCV");
        }
        
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
        
    }
    
    //measure Horizontal Trajectory Response Matrix
    public void measureTRMHorizontal(Double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
        
        double HC_val = 0.0;
        double corrector_auxval = 0.0;
        int row = 0;
        int col = 0;
        
        try {
            //Store reference trajectory
            BPMval.readReferenceTrajectory(BPM.get(0).getAccelerator());
        } catch (ConnectionException | GetException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
        }
    }
    
     //calculate Horizontal Trajectory Response Matrix 
    public void calculateTRMHorizontal(Double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
        
        double HC_val = 0.0;
        double corrector_auxval = 0.0;
        int row = 0;
        int col = 0;
        HashMap<xal.smf.impl.BPM, Double> iniPosX = new HashMap();  
        HashMap<xal.smf.impl.BPM, Double> finalPosX = new HashMap();
        List<AcceleratorSeq> newCombo = new ArrayList<>();
        RunSimulationService simulService;
        AcceleratorSeq iniSeq;
        AcceleratorSeq finalSeq;

        //setup simulation parameters
        if(BPM.get(0).getParent().getPosition()<=HC.get(0).getParent().getPosition()){
            iniSeq = BPM.get(0).getParent();
        } else {
            iniSeq = HC.get(0).getParent();
        }
        
        if(BPM.get(BPM.size()-1).getParent().getPosition()>=HC.get(HC.size()-1).getParent().getPosition()){
            finalSeq = BPM.get(0).getParent();
        } else {
            finalSeq = HC.get(0).getParent();
        }
        
        if (iniSeq != finalSeq){
            Accelerator accl = BPM.get(0).getAccelerator();            
            for(int i=accl.getAllSeqs().indexOf(iniSeq); i<=accl.getAllSeqs().indexOf(finalSeq); i++){
                newCombo.add(accl.getAllSeqs().get(i));
            }
            AcceleratorSeqCombo Sequence = new xal.smf.AcceleratorSeqCombo("calcMatrix",newCombo); 
            simulService = new RunSimulationService(Sequence);
        } else { 
            xal.smf.AcceleratorSeq Sequence = BPM.get(0).getParent();
            simulService = new RunSimulationService(Sequence);
        }
        simulService.setSynchronizationMode("DESIGN");  
        
        
        try {
            //get initial position
            iniPosX = simulService.runTrajectorySimulation(BPM,"X");
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Create reponse matrix
        for(xal.smf.impl.HDipoleCorr hcorr: HC){
            HC_val = hcorr.getDfltField();
            corrector_auxval = HC_val + Dk;
            hcorr.setDfltField(corrector_auxval);
            try {    
                finalPosX = simulService.runTrajectorySimulation(BPM,"X");
            } catch (InstantiationException | ModelException ex) {
                Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(xal.smf.impl.BPM bpm: BPM){
                TRMhorizontal.set(row, col,1000*(finalPosX.get(bpm)-iniPosX.get(bpm))/Dk);
                row++;
            }
            hcorr.setDfltField(HC_val);
            col++;
            row = 0;
        }
    }
    
    //measure Vertical Trajectory Response Matrix
    public void measureTRMVertical(Double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
        
        double VC_val = 0.0;
        double corrector_auxval = 0.0;
        int row = 0;
        int col = 0;

        try {
            //Store reference trajectory
            BPMval.readReferenceTrajectory(BPM.get(0).getAccelerator());
        } catch (ConnectionException | GetException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
        }
    }
    
    //calcualte Vertical Trajectory Response Matrix
    public void calculateTRMVertical( Double Dk) throws ConnectionException, GetException, PutException, InterruptedException{
        
        double VC_val = 0.0;
        double corrector_auxval = 0.0;
        int row = 0;
        int col = 0;
        HashMap<xal.smf.impl.BPM, Double> iniPosY = new HashMap();  
        HashMap<xal.smf.impl.BPM, Double> finalPosY = new HashMap();
        List<AcceleratorSeq> newCombo = new ArrayList<>();
        RunSimulationService simulService;
        AcceleratorSeq iniSeq;
        AcceleratorSeq finalSeq;

        //setup simulation parameters
        if(BPM.get(0).getParent().getPosition()<=VC.get(0).getParent().getPosition()){
            iniSeq = BPM.get(0).getParent();
        } else {
            iniSeq = VC.get(0).getParent();
        }
        
        if(BPM.get(BPM.size()-1).getParent().getPosition()>=VC.get(VC.size()-1).getParent().getPosition()){
            finalSeq = BPM.get(0).getParent();
        } else {
            finalSeq = VC.get(0).getParent();
        }
        
        if (iniSeq != finalSeq){
            Accelerator accl = BPM.get(0).getAccelerator();            
            for(int i=accl.getAllSeqs().indexOf(iniSeq); i<=accl.getAllSeqs().indexOf(finalSeq); i++){
                newCombo.add(accl.getAllSeqs().get(i));
            }
            AcceleratorSeqCombo Sequence = new xal.smf.AcceleratorSeqCombo("calcMatrix",newCombo); 
            simulService = new RunSimulationService(Sequence);
        } else { 
            xal.smf.AcceleratorSeq Sequence = BPM.get(0).getParent();
            simulService = new RunSimulationService(Sequence);
        }
        simulService.setSynchronizationMode("DESIGN");  
        
        try {
            //get initial position
            iniPosY = simulService.runTrajectorySimulation(BPM,"Y");
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
        }
                      
        
        for(xal.smf.impl.VDipoleCorr vcorr: VC){
            VC_val = vcorr.getDfltField();
            corrector_auxval = VC_val + Dk;
            vcorr.setDfltField(corrector_auxval);
            try {    
                finalPosY = simulService.runTrajectorySimulation(BPM,"Y");
            } catch (InstantiationException | ModelException ex) {
                Logger.getLogger(CorrectionSVD.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(xal.smf.impl.BPM bpm: BPM){
                TRMvertical.set(row, col,1000*(finalPosY.get(bpm)-iniPosY.get(bpm))/Dk);
                row++;
            }                      
            vcorr.setDfltField(VC_val);
            col++;
            row = 0;
        }
    }
    
    public double[] calculateHCorrection(TrajectoryArray Traj){
        
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
            invTRM = Response.pseudoinverse(cutSVD);
            invTRM = invTRM.transpose();
        } else {
            invTRM = Response.pseudoinverse(cutSVD);
        }
        
        hcorrKick = invTRM.times(xPosition);
        hcorrKick = hcorrKick.times(-1);
        
        return hcorrKick.getColumnPackedCopy();
        
    }
    
    public double[] calculateVCorrection(TrajectoryArray Traj){
        
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
            invTRM = Response.pseudoinverse(cutSVD);
            invTRM = invTRM.transpose();
        } else {
            invTRM = Response.pseudoinverse(cutSVD);
        }
        
        vcorrKick = invTRM.times(yPosition);
        vcorrKick = vcorrKick.times(-1);
        
        return vcorrKick.getColumnPackedCopy();
        
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
