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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Window;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.Accelerator;
import xal.smf.impl.BPM;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.VDipoleCorr;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 *
 * @author nataliamilas
 */
public class CorrectionBlock {

    private final StringProperty blockName = new SimpleStringProperty();

    private final BooleanProperty blockchanged = new SimpleBooleanProperty();

    private final BooleanProperty okSVD = new SimpleBooleanProperty();

    private final BooleanProperty ok1to1 = new SimpleBooleanProperty();

    private List<BPM> blockBPM;
    private List<HDipoleCorr> blockHC;
    private List<VDipoleCorr> blockVC;
    private CorrectionMatrix Correction1to1;
    private CorrectionSVD CorrectionMatrixSVD;

    // Save variables
    private final String blockData = "block";
    private final String inputData = "input_variables";
    private final String blockSVD = "dataSVD";
    private final String block1to1 = "data1to1";

    public final StringProperty blockNameProperty() {
        return this.blockName;
    }

    public final String getblockName() {
        return this.blockNameProperty().get();
    }

    public final void setblockName(final String name) {
        this.blockNameProperty().set(name);
    }

    public final BooleanProperty blockChangedProperty() {
        return blockchanged;
    }

    public final boolean isBlockChanged() {
        return blockChangedProperty().get();
    }

    public final void setBlockChanged(boolean change) {
        blockChangedProperty().set(change);
    }

    public BooleanProperty oKSVDProperty() {
        return okSVD;
    }

    public final boolean isOkSVD() {
        return oKSVDProperty().get();
    }

    public final void setOkSVD(boolean change) {
        oKSVDProperty().set(change);
    }

    public BooleanProperty oK1to1Property() {
        return ok1to1;
    }

    public final boolean isOk1to1() {
        return oK1to1Property().get();
    }

    public final void setOk1to1(boolean change) {
        oK1to1Property().set(change);
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

    public CorrectionMatrix getCorrection1to1() {
        return Correction1to1;
    }

    public void setCorrection1to1(CorrectionMatrix Correction1to1) {
        this.Correction1to1 = Correction1to1;
    }

    public CorrectionSVD getCorrectionSVD() {
        return CorrectionMatrixSVD;
    }

    public void setCorrectionSVD(CorrectionSVD CorrectionMatrixSVD) {
        this.CorrectionMatrixSVD = CorrectionMatrixSVD;
    }

    public void saveBlock(DataAdaptor da) {
        String bpmNames = "";
        String hcNames = "";
        String vcNames = "";
        String paramVal = "";
        int index = 0;
        double[] row;
        DataAdaptor blockAdaptor = da.createChild(blockData);
        blockAdaptor.setValue("name", this.getblockName());

        //Save data relative to the block (general)
        DataAdaptor blockInputData = blockAdaptor.createChild(inputData);
        DataAdaptor blockFlags = blockInputData.createChild("flags");
        blockFlags.setValue("okSVD", this.isOk1to1());
        blockFlags.setValue("ok1to1", this.isOk1to1());
        DataAdaptor blockBPMData = blockInputData.createChild("BPM");
        for (BPM bpm : blockBPM) {
            bpmNames += bpm.toString();
            if (blockBPM.indexOf(bpm) < blockBPM.size() - 1) {
                bpmNames += ",";
            }
        }
        blockBPMData.setValue("data", bpmNames);
        DataAdaptor blockHCData = blockInputData.createChild("HCorrector");
        for (HDipoleCorr hcor : blockHC) {
            hcNames += hcor.toString();
            if (blockHC.indexOf(hcor) < blockHC.size() - 1) {
                hcNames += ",";
            }
        }
        blockHCData.setValue("data", hcNames);
        DataAdaptor blockVCData = blockInputData.createChild("VCorrector");
        for (VDipoleCorr vcor : blockVC) {
            vcNames += vcor.toString();
            if (blockVC.indexOf(vcor) < blockVC.size() - 1) {
                vcNames += ",";
            }
        }
        blockVCData.setValue("data", vcNames);

        //Save data from SVD
        if (this.isOkSVD()) {
            DataAdaptor blockSVDData = blockAdaptor.createChild(blockSVD);
            DataAdaptor blockSVDCut = blockSVDData.createChild("SVDcut");
            blockSVDCut.setValue("value", CorrectionMatrixSVD.getCutSVD());
            blockBPMData = blockSVDData.createChild("BPM");
            bpmNames = "";
            for (BPM bpm : CorrectionMatrixSVD.bpm) {
                bpmNames += bpm.toString();
                if (CorrectionMatrixSVD.bpm.indexOf(bpm) < CorrectionMatrixSVD.bpm.size() - 1) {
                    bpmNames += ",";
                }
            }
            blockBPMData.setValue("data", bpmNames);
            blockHCData = blockSVDData.createChild("HCorrector");
            hcNames = "";
            for (HDipoleCorr hcor : CorrectionMatrixSVD.HC) {
                hcNames += hcor.toString();
                if (CorrectionMatrixSVD.HC.indexOf(hcor) < CorrectionMatrixSVD.HC.size() - 1) {
                    hcNames += ",";
                }
            }
            blockHCData.setValue("data", hcNames);
            blockVCData = blockSVDData.createChild("VCorrector");
            vcNames = "";
            for (VDipoleCorr vcor : CorrectionMatrixSVD.VC) {
                vcNames += vcor.toString();
                if (CorrectionMatrixSVD.VC.indexOf(vcor) < CorrectionMatrixSVD.VC.size() - 1) {
                    vcNames += ",";
                }
            }
            blockVCData.setValue("data", vcNames);
            DataAdaptor blockMatrix = blockSVDData.createChild("matrix");
            DataAdaptor blockMatrixElements = blockMatrix.createChild("elements");
            blockMatrixElements.setValue("plane", "horizontal");
            blockMatrixElements.setValue("row", CorrectionMatrixSVD.TRMhorizontal.getRowDimension());
            blockMatrixElements.setValue("column", CorrectionMatrixSVD.TRMhorizontal.getColumnDimension());
            blockMatrixElements.setValue("values", CorrectionMatrixSVD.TRMhorizontal.getColumnPackedCopy());

            blockMatrixElements = blockMatrix.createChild("elements");
            blockMatrixElements.setValue("plane", "vertical");
            blockMatrixElements.setValue("row", CorrectionMatrixSVD.TRMvertical.getRowDimension());
            blockMatrixElements.setValue("column", CorrectionMatrixSVD.TRMvertical.getColumnDimension());
            blockMatrixElements.setValue("values", CorrectionMatrixSVD.TRMvertical.getColumnPackedCopy());
        }

        //Save data from 1to1
        if (this.isOk1to1()) {
            DataAdaptor block1to1Data = blockAdaptor.createChild(block1to1);
            DataAdaptor blockPair = block1to1Data.createChild("pair");
            blockPair.setValue("plane", "horizontal");
            bpmNames = "";
            hcNames = "";
            for (BPM bpm : Correction1to1.HC.keySet()) {
                bpmNames += bpm.toString();
                hcNames += Correction1to1.HC.get(bpm).toString();
                paramVal += Correction1to1.HorParam.get(bpm)[0] + ":" + Correction1to1.HorParam.get(bpm)[1];
                index += 1;
                if (index < Correction1to1.HC.keySet().size()) {
                    bpmNames += ",";
                    hcNames += ",";
                    paramVal += ",";
                }
            }
            blockBPMData = blockPair.createChild("BPM");
            blockBPMData.setValue("data", bpmNames);
            DataAdaptor blockCorrector = blockPair.createChild("corrector");
            blockCorrector.setValue("data", hcNames);
            DataAdaptor blockCoeff = blockPair.createChild("coefficients");
            blockCoeff.setValue("coeff", paramVal);

            blockPair = block1to1Data.createChild("pair");
            blockPair.setValue("plane", "vertical");
            bpmNames = "";
            vcNames = "";
            paramVal = "";
            index = 0;
            for (BPM bpm : Correction1to1.VC.keySet()) {
                bpmNames += bpm.toString();
                vcNames += Correction1to1.VC.get(bpm).toString();
                paramVal += Correction1to1.VertParam.get(bpm)[0] + ":" + Correction1to1.VertParam.get(bpm)[1];
                index += 1;
                if (index < Correction1to1.VC.keySet().size()) {
                    bpmNames += ",";
                    vcNames += ",";
                    paramVal += ",";
                }
            }
            blockBPMData = blockPair.createChild("BPM");
            blockBPMData.setValue("data", bpmNames);
            blockCorrector = blockPair.createChild("corrector");
            blockCorrector.setValue("data", vcNames);
            blockCoeff = blockPair.createChild("coefficients");
            blockCoeff.setValue("coeff", paramVal);
        }

    }

    public void loadBlock(File filename, Accelerator accl) {
        DataAdaptor readAdp = null;
        String[] bpmNames;
        String[] hcNames;
        String[] vcNames;
        List<Integer> validBPMs = new ArrayList<>();
        List<Integer> validHC = new ArrayList<>();
        List<Integer> validVC = new ArrayList<>();
        List<BPM> listBPM = new ArrayList<>();
        List<HDipoleCorr> listHC = new ArrayList<>();
        List<VDipoleCorr> listVC = new ArrayList<>();
        List<BPM> existsBPM = accl.getAllNodesOfType("BPM");
        List<HDipoleCorr> existsHC = accl.getAllNodesOfType("DCH");
        List<VDipoleCorr> existsVC = accl.getAllNodesOfType("DCV");

        try {
            readAdp = XmlDataAdaptor.adaptorForFile(filename, false);
            DataAdaptor blockheader = readAdp.childAdaptor("CorrectionData");
            DataAdaptor blockAdaptor = blockheader.childAdaptor(blockData);
            this.setblockName(blockAdaptor.stringValue("name"));
            DataAdaptor blockInputData = blockAdaptor.childAdaptor(inputData);
            DataAdaptor blockFlags = blockInputData.childAdaptor("flags");
            this.setOkSVD(blockFlags.booleanValue("okSVD"));
            this.setOk1to1(blockFlags.booleanValue("ok1to1"));
            DataAdaptor blockBPMData = blockInputData.childAdaptor("BPM");
            bpmNames = blockBPMData.stringValue("data").split(",");
            for (int i = 0; i < bpmNames.length; i += 1) {
                if (existsBPM.contains((BPM) accl.getNode(bpmNames[i]))) {
                    listBPM.add((BPM) accl.getNode(bpmNames[i]));
                }
            }
            this.setBlockBPM(listBPM);
            DataAdaptor blockHCData = blockInputData.childAdaptor("HCorrector");
            hcNames = blockHCData.stringValue("data").split(",");
            for (int i = 0; i < hcNames.length; i += 1) {
                if (existsHC.contains((HDipoleCorr) accl.getNode(hcNames[i]))) {
                    listHC.add((HDipoleCorr) accl.getNode(hcNames[i]));
                }
            }
            this.setBlockHC(listHC);
            DataAdaptor blockVCData = blockInputData.childAdaptor("VCorrector");
            vcNames = blockVCData.stringValue("data").split(",");
            for (int i = 0; i < vcNames.length; i += 1) {
                if (existsVC.contains((VDipoleCorr) accl.getNode(vcNames[i]))) {
                    listVC.add((VDipoleCorr) accl.getNode(vcNames[i]));
                }
            }
            this.setBlockVC(listVC);

            if (this.isOkSVD()) {
                this.CorrectionMatrixSVD = new CorrectionSVD();
                DataAdaptor blockSVDData = blockAdaptor.childAdaptor(blockSVD);
                DataAdaptor blockSVDCut = blockSVDData.childAdaptor("SVDcut");
                this.CorrectionMatrixSVD.setCutSVD(blockSVDCut.doubleValue("value"));
                blockBPMData = blockSVDData.childAdaptor("BPM");
                bpmNames = blockBPMData.stringValue("data").split(",");
                listBPM.clear();
                for (int i = 0; i < bpmNames.length; i += 1) {
                    if (existsBPM.contains((BPM) accl.getNode(bpmNames[i]))) {
                        listBPM.add((BPM) accl.getNode(bpmNames[i]));
                        validBPMs.add(i);
                    }
                }
                this.CorrectionMatrixSVD.setBPM(listBPM);
                blockHCData = blockSVDData.childAdaptor("HCorrector");
                hcNames = blockHCData.stringValue("data").split(",");
                listHC.clear();
                for (int i = 0; i < hcNames.length; i += 1) {
                    if (existsHC.contains((HDipoleCorr) accl.getNode(hcNames[i]))) {
                        listHC.add((HDipoleCorr) accl.getNode(hcNames[i]));
                        validHC.add(i);
                    }
                }
                this.CorrectionMatrixSVD.setHC(listHC);
                blockVCData = blockSVDData.childAdaptor("VCorrector");
                vcNames = blockVCData.stringValue("data").split(",");
                listVC.clear();
                for (int i = 0; i < vcNames.length; i += 1) {
                    if (existsVC.contains((VDipoleCorr) accl.getNode(vcNames[i]))) {
                        listVC.add((VDipoleCorr) accl.getNode(vcNames[i]));
                        validVC.add(i);
                    }
                }
                this.CorrectionMatrixSVD.setVC(listVC);
                DataAdaptor blockMatrix = blockSVDData.childAdaptor("matrix");
                blockMatrix.childAdaptors().forEach((DataAdaptor childAdaptor) -> {
                    int rows = childAdaptor.intValue("row");
                    int columns = childAdaptor.intValue("column");
                    double[] elements = childAdaptor.doubleArray("values");
                    Matrix response = new Matrix(elements, rows);
                    if (childAdaptor.stringValue("plane").equals("horizontal")) {
                        if (rows == listBPM.size() && columns == listHC.size()) {
                            this.CorrectionMatrixSVD.setTRMhorizontal(response);
                            this.CorrectionMatrixSVD.setM(rows);
                            this.CorrectionMatrixSVD.setNh(columns);
                        } else {
                            this.CorrectionMatrixSVD.setTRMhorizontal(response.getMatrix(validBPMs.stream().mapToInt(i -> i).toArray(), validHC.stream().mapToInt(i -> i).toArray()));
                            this.CorrectionMatrixSVD.setM(listBPM.size());
                            this.CorrectionMatrixSVD.setNh(listHC.size());
                        }
                    } else if (childAdaptor.stringValue("plane").equals("vertical")) {
                        if (rows == listBPM.size() && columns == listVC.size()) {
                            this.CorrectionMatrixSVD.setTRMvertical(response);
                            this.CorrectionMatrixSVD.setM(rows);
                            this.CorrectionMatrixSVD.setNv(columns);
                        } else {
                            this.CorrectionMatrixSVD.setTRMvertical(response.getMatrix(validBPMs.stream().mapToInt(i -> i).toArray(), validVC.stream().mapToInt(i -> i).toArray()));
                            this.CorrectionMatrixSVD.setM(listBPM.size());
                            this.CorrectionMatrixSVD.setNh(listVC.size());
                        }
                    }
                });
            } else {
                this.initializeSVDCorrection(accl);
            }
            if (this.isOk1to1()) {
                this.Correction1to1 = new CorrectionMatrix();
                DataAdaptor block1to1Data = blockAdaptor.childAdaptor(block1to1);
                block1to1Data.childAdaptors().forEach((childAdaptor) -> {
                    HashMap<BPM, double[]> Param = new HashMap();
                    HashMap<BPM, HDipoleCorr> HCpair = new HashMap();
                    HashMap<BPM, VDipoleCorr> VCpair = new HashMap();
                    double[] elements1to1 = new double[2];
                    String[] bpmNames1to1;
                    String[] hcNames1to1;
                    String[] vcNames1to1;
                    String[] coeff;
                    DataAdaptor blockBPMData1to1;
                    DataAdaptor blockCorrData1to1;
                    if (childAdaptor.stringValue("plane").equals("horizontal")) {
                        blockBPMData1to1 = childAdaptor.childAdaptor("BPM");
                        bpmNames1to1 = blockBPMData1to1.stringValue("data").split(",");
                        blockCorrData1to1 = childAdaptor.childAdaptor("corrector");
                        hcNames1to1 = blockCorrData1to1.stringValue("data").split(",");
                        DataAdaptor blockCoeff = childAdaptor.childAdaptor("coefficients");
                        coeff = blockCoeff.stringValue("coeff").split(",");
                        for (int j = 0; j < bpmNames1to1.length; j += 1) {
                            if (existsBPM.contains((BPM) accl.getNode(bpmNames1to1[j])) && existsHC.contains((HDipoleCorr) accl.getNode(hcNames1to1[j]))) {
                                HCpair.put((BPM) accl.getNode(bpmNames1to1[j]), (HDipoleCorr) accl.getNode(hcNames1to1[j]));
                                elements1to1[0] = Double.parseDouble(coeff[j].split(":")[0]);
                                elements1to1[1] = Double.parseDouble(coeff[j].split(":")[1]);
                                Param.put((BPM) accl.getNode(bpmNames1to1[j]), elements1to1.clone());
                            }
                        }
                        this.Correction1to1.setHC(HCpair);
                        this.Correction1to1.setHorParam(Param);

                    } else if (childAdaptor.stringValue("plane").equals("vertical")) {
                        blockBPMData1to1 = childAdaptor.childAdaptor("BPM");
                        bpmNames1to1 = blockBPMData1to1.stringValue("data").split(",");
                        blockCorrData1to1 = childAdaptor.childAdaptor("corrector");
                        vcNames1to1 = blockCorrData1to1.stringValue("data").split(",");
                        DataAdaptor blockCoeff = childAdaptor.childAdaptor("coefficients");
                        coeff = blockCoeff.stringValue("coeff").split(",");
                        for (int j = 0; j < bpmNames1to1.length; j += 1) {
                            if (existsBPM.contains((BPM) accl.getNode(bpmNames1to1[j])) && existsVC.contains((VDipoleCorr) accl.getNode(vcNames1to1[j]))) {
                                VCpair.put((BPM) accl.getNode(bpmNames1to1[j]), (VDipoleCorr) accl.getNode(vcNames1to1[j]));
                                elements1to1[0] = Double.parseDouble(coeff[j].split(":")[0]);
                                elements1to1[1] = Double.parseDouble(coeff[j].split(":")[1]);
                                Param.put((BPM) accl.getNode(bpmNames1to1[j]), elements1to1.clone());
                            }
                        }
                        this.Correction1to1.setVC(VCpair);
                        this.Correction1to1.setVertParam(Param);
                    }
                });
            } else {
                this.initialize1to1Correction(accl);
            }
        } catch (MalformedURLException | XmlDataAdaptor.ParseException | XmlDataAdaptor.ResourceNotFoundException ex) {
            Logger.getLogger(CorrectionBlock.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void loadBlock(DataAdaptor readAdp, Accelerator accl) {
        String[] bpmNames;
        String[] hcNames;
        String[] vcNames;
        List<Integer> validBPMs = new ArrayList<>();
        List<Integer> validHC = new ArrayList<>();
        List<Integer> validVC = new ArrayList<>();
        List<BPM> listBPM = new ArrayList<>();
        List<HDipoleCorr> listHC = new ArrayList<>();
        List<VDipoleCorr> listVC = new ArrayList<>();
        List<BPM> existsBPM = accl.getAllNodesOfType("BPM");
        List<HDipoleCorr> existsHC = accl.getAllNodesOfType("DCH");
        List<VDipoleCorr> existsVC = accl.getAllNodesOfType("DCV");

        try {
            this.setblockName(readAdp.stringValue("name"));
            DataAdaptor blockInputData = readAdp.childAdaptor(inputData);
            DataAdaptor blockFlags = blockInputData.childAdaptor("flags");
            this.setOkSVD(blockFlags.booleanValue("okSVD"));
            this.setOk1to1(blockFlags.booleanValue("ok1to1"));
            DataAdaptor blockBPMData = blockInputData.childAdaptor("BPM");
            bpmNames = blockBPMData.stringValue("data").split(",");
            for (int i = 0; i < bpmNames.length; i += 1) {
                if (existsBPM.contains((BPM) accl.getNode(bpmNames[i]))) {
                    listBPM.add((BPM) accl.getNode(bpmNames[i]));
                }
            }
            this.setBlockBPM(listBPM);
            DataAdaptor blockHCData = blockInputData.childAdaptor("HCorrector");
            hcNames = blockHCData.stringValue("data").split(",");
            for (int i = 0; i < hcNames.length; i += 1) {
                if (existsHC.contains((HDipoleCorr) accl.getNode(hcNames[i]))) {
                    listHC.add((HDipoleCorr) accl.getNode(hcNames[i]));
                }
            }
            this.setBlockHC(listHC);
            DataAdaptor blockVCData = blockInputData.childAdaptor("VCorrector");
            vcNames = blockVCData.stringValue("data").split(",");
            for (int i = 0; i < vcNames.length; i += 1) {
                if (existsVC.contains((VDipoleCorr) accl.getNode(vcNames[i]))) {
                    listVC.add((VDipoleCorr) accl.getNode(vcNames[i]));
                }
            }
            this.setBlockVC(listVC);

            if (this.isOkSVD()) {
                this.CorrectionMatrixSVD = new CorrectionSVD();
                DataAdaptor blockSVDData = readAdp.childAdaptor(blockSVD);
                DataAdaptor blockSVDCut = blockSVDData.childAdaptor("SVDcut");
                this.CorrectionMatrixSVD.setCutSVD(blockSVDCut.doubleValue("value"));
                blockBPMData = blockSVDData.childAdaptor("BPM");
                bpmNames = blockBPMData.stringValue("data").split(",");
                listBPM.clear();
                for (int i = 0; i < bpmNames.length; i += 1) {
                    if (existsBPM.contains((BPM) accl.getNode(bpmNames[i]))) {
                        listBPM.add((BPM) accl.getNode(bpmNames[i]));
                        validBPMs.add(i);
                    }
                }
                this.CorrectionMatrixSVD.setBPM(listBPM);
                blockHCData = blockSVDData.childAdaptor("HCorrector");
                hcNames = blockHCData.stringValue("data").split(",");
                listHC.clear();
                for (int i = 0; i < hcNames.length; i += 1) {
                    if (existsHC.contains((HDipoleCorr) accl.getNode(hcNames[i]))) {
                        listHC.add((HDipoleCorr) accl.getNode(hcNames[i]));
                        validHC.add(i);
                    }
                }
                this.CorrectionMatrixSVD.setHC(listHC);
                blockVCData = blockSVDData.childAdaptor("VCorrector");
                vcNames = blockVCData.stringValue("data").split(",");
                listVC.clear();
                for (int i = 0; i < vcNames.length; i += 1) {
                    if (existsVC.contains((VDipoleCorr) accl.getNode(vcNames[i]))) {
                        listVC.add((VDipoleCorr) accl.getNode(vcNames[i]));
                        validVC.add(i);
                    }
                }
                this.CorrectionMatrixSVD.setVC(listVC);
                DataAdaptor blockMatrix = blockSVDData.childAdaptor("matrix");
                blockMatrix.childAdaptors().forEach((DataAdaptor childAdaptor) -> {
                    int rows = childAdaptor.intValue("row");
                    int columns = childAdaptor.intValue("column");
                    double[] elements = childAdaptor.doubleArray("values");
                    Matrix response = new Matrix(elements, rows);
                    if (childAdaptor.stringValue("plane").equals("horizontal")) {
                        if (rows == listBPM.size() && columns == listHC.size()) {
                            this.CorrectionMatrixSVD.setTRMhorizontal(response);
                            this.CorrectionMatrixSVD.setM(rows);
                            this.CorrectionMatrixSVD.setNh(columns);
                        } else {
                            this.CorrectionMatrixSVD.setTRMhorizontal(response.getMatrix(validBPMs.stream().mapToInt(i -> i).toArray(), validHC.stream().mapToInt(i -> i).toArray()));
                            this.CorrectionMatrixSVD.setM(listBPM.size());
                            this.CorrectionMatrixSVD.setNh(listHC.size());
                        }
                    } else if (childAdaptor.stringValue("plane").equals("vertical")) {
                        if (rows == listBPM.size() && columns == listVC.size()) {
                            this.CorrectionMatrixSVD.setTRMvertical(response);
                            this.CorrectionMatrixSVD.setM(rows);
                            this.CorrectionMatrixSVD.setNv(columns);
                        } else {
                            this.CorrectionMatrixSVD.setTRMvertical(response.getMatrix(validBPMs.stream().mapToInt(i -> i).toArray(), validVC.stream().mapToInt(i -> i).toArray()));
                            this.CorrectionMatrixSVD.setM(listBPM.size());
                            this.CorrectionMatrixSVD.setNh(listVC.size());
                        }
                    }
                });
            } else {
                this.initializeSVDCorrection(accl);
            }
            if (this.isOk1to1()) {
                this.Correction1to1 = new CorrectionMatrix();
                DataAdaptor block1to1Data = readAdp.childAdaptor(block1to1);
                block1to1Data.childAdaptors().forEach((childAdaptor) -> {
                    HashMap<BPM, double[]> Param = new HashMap();
                    HashMap<BPM, HDipoleCorr> HCpair = new HashMap();
                    HashMap<BPM, VDipoleCorr> VCpair = new HashMap();
                    double[] elements1to1 = new double[2];
                    String[] bpmNames1to1;
                    String[] hcNames1to1;
                    String[] vcNames1to1;
                    String[] coeff;
                    DataAdaptor blockBPMData1to1;
                    DataAdaptor blockCorrData1to1;
                    if (childAdaptor.stringValue("plane").equals("horizontal")) {
                        blockBPMData1to1 = childAdaptor.childAdaptor("BPM");
                        bpmNames1to1 = blockBPMData1to1.stringValue("data").split(",");
                        blockCorrData1to1 = childAdaptor.childAdaptor("corrector");
                        hcNames1to1 = blockCorrData1to1.stringValue("data").split(",");
                        DataAdaptor blockCoeff = childAdaptor.childAdaptor("coefficients");
                        coeff = blockCoeff.stringValue("coeff").split(",");
                        for (int j = 0; j < bpmNames1to1.length; j += 1) {
                            if (existsBPM.contains((BPM) accl.getNode(bpmNames1to1[j])) && existsHC.contains((HDipoleCorr) accl.getNode(hcNames1to1[j]))) {
                                HCpair.put((BPM) accl.getNode(bpmNames1to1[j]), (HDipoleCorr) accl.getNode(hcNames1to1[j]));
                                elements1to1[0] = Double.parseDouble(coeff[j].split(":")[0]);
                                elements1to1[1] = Double.parseDouble(coeff[j].split(":")[1]);
                                Param.put((BPM) accl.getNode(bpmNames1to1[j]), elements1to1.clone());
                            }
                        }
                        this.Correction1to1.setHC(HCpair);
                        this.Correction1to1.setHorParam(Param);

                    } else if (childAdaptor.stringValue("plane").equals("vertical")) {
                        blockBPMData1to1 = childAdaptor.childAdaptor("BPM");
                        bpmNames1to1 = blockBPMData1to1.stringValue("data").split(",");
                        blockCorrData1to1 = childAdaptor.childAdaptor("corrector");
                        vcNames1to1 = blockCorrData1to1.stringValue("data").split(",");
                        DataAdaptor blockCoeff = childAdaptor.childAdaptor("coefficients");
                        coeff = blockCoeff.stringValue("coeff").split(",");
                        for (int j = 0; j < bpmNames1to1.length; j += 1) {
                            if (existsBPM.contains((BPM) accl.getNode(bpmNames1to1[j])) && existsVC.contains((VDipoleCorr) accl.getNode(vcNames1to1[j]))) {
                                VCpair.put((BPM) accl.getNode(bpmNames1to1[j]), (VDipoleCorr) accl.getNode(vcNames1to1[j]));
                                elements1to1[0] = Double.parseDouble(coeff[j].split(":")[0]);
                                elements1to1[1] = Double.parseDouble(coeff[j].split(":")[1]);
                                Param.put((BPM) accl.getNode(bpmNames1to1[j]), elements1to1.clone());
                            }
                        }
                        this.Correction1to1.setVC(VCpair);
                        this.Correction1to1.setVertParam(Param);
                    }
                });
            } else {
                this.initialize1to1Correction(accl);
            }
        } catch (XmlDataAdaptor.ParseException | XmlDataAdaptor.ResourceNotFoundException ex) {
            Logger.getLogger(CorrectionBlock.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void initializeSVDCorrection(Accelerator accl) {
        this.CorrectionMatrixSVD = new CorrectionSVD();
        this.CorrectionMatrixSVD.defineKnobs(accl, blockBPM, blockHC, blockVC);
    }

    public void initialize1to1Correction(Accelerator accl) {
        this.Correction1to1 = new CorrectionMatrix();
        try {
            this.Correction1to1.setPairs(accl, blockBPM, blockHC, blockVC);
        } catch (ConnectionException | GetException | IOException ex) {
            Logger.getLogger(CorrectionBlock.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void check1to1CorrectionPairs(Window owner, Accelerator accl) {
        boolean aux;
        try {
            aux = (this.Correction1to1.checkPairs(owner, accl, blockBPM, blockHC, blockVC));
            if (this.isOk1to1()) {
                this.setOk1to1(aux);
            }
        } catch (ConnectionException | GetException | IOException ex) {
            Logger.getLogger(CorrectionBlock.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
