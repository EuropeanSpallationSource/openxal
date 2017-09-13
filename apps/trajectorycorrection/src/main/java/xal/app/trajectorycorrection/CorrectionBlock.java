/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorycorrection;

import java.util.List;
import xal.smf.impl.BPM;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.VDipoleCorr;

/**
 *
 * @author nataliamilas
 */
public class CorrectionBlock {
    
    private String blockName;
    private boolean blockchanged;
    private List<xal.smf.impl.BPM> blockBPM;
    private List<xal.smf.impl.HDipoleCorr> blockHC;
    private List<xal.smf.impl.VDipoleCorr> blockVC;
    private double[][] blockMatrixH;
    private double[][] blockMatrixV;

    public String getBlockName() {
        return blockName;
    }
    
     public boolean isBlockchanged() {
        return blockchanged;
    }

    public void setBlockchanged(boolean blockchanged) {
        this.blockchanged = blockchanged;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public List<BPM> getBlockBPM() {
        return blockBPM;
    }

    public void setBlockBPM(List<BPM> blockBPM) {
        this.blockBPM = blockBPM;
    }

    public List<HDipoleCorr> getBlockHC() {
        return blockHC;
    }

    public void setBlockHC(List<HDipoleCorr> blockHC) {
        this.blockHC = blockHC;
    }

    public List<VDipoleCorr> getBlockVC() {
        return blockVC;
    }

    public void setBlockVC(List<VDipoleCorr> blockVC) {
        this.blockVC = blockVC;
    }

    public double[][] getBlockMatrixH() {
        return blockMatrixH;
    }

    public void setBlockMatrixH(double[][] blockMatrixH) {
        this.blockMatrixH = blockMatrixH;
    }

    public double[][] getBlockMatrixV() {
        return blockMatrixV;
    }

    public void setBlockMatrixV(double[][] blockMatrixV) {
        this.blockMatrixV = blockMatrixV;
    }
    
}
