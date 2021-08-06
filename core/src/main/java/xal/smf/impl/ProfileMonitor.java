/*
 * ProfileMonitor.java
 *  This class contains the interfaces to the wirescanner instruments.
 *  This includes basic settings, fitted data, and access to raw data.
 *
 * Created on January 25, 2002, 5:56 PM tap, revised by jdg 9/25/02
 */
package xal.smf.impl;

import xal.smf.*;
import xal.smf.impl.qualify.*;
import xal.ca.correlator.*;
import xal.ca.*;
import xal.tools.correlator.*;

/**
 * Represents the wire scanner device using the original API
 *
 * @author tap
 */
public class ProfileMonitor extends AcceleratorNode {

    /**
     * identifies instances of this ProfileMonitor class in contrast to the
     * WireScanner class
     */
    public static final String PROFILE_MONITOR_TYPE = "profilemonitor";

    /**
     * software type for the Profile Monitor class
     */
    public static final String SOFTWARE_TYPE = "Version 1.0.0";

    /*
     *  Constants
     */
    public static final String TYPE = "WS";

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(ProfileMonitor.class, TYPE, "wirescanner", PROFILE_MONITOR_TYPE);
    }

    /**
     * Override to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Overriden to provide the software type
     */
    @Override
    public String getSoftType() {
        return SOFTWARE_TYPE;
    }

    /**
     * Primary Constructor
     */
    public ProfileMonitor(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public ProfileMonitor(final String strId) {
        this(strId, null);
    }

    // real time readback signals     
    public static final String POS_HANDLE = "position";
    public final AccessibleProperty position = new AccessibleProperty(POS_HANDLE);
    private Channel posC = null;

    public static final String RT_GRAPH_HANDLE = "RTGraph";
    public final AccessibleProperty rtGraph = new AccessibleProperty(RT_GRAPH_HANDLE);

    // action signals
    public static final String ABORT_SCAN_HANDLE = "abortScan";
    public final AccessibleProperty abortScan = new AccessibleProperty(ABORT_SCAN_HANDLE);
    private Channel abortScanC = null;

    public static final String BEGIN_SCAN_HANDLE = "beginScan";
    public final AccessibleProperty beginScan = new AccessibleProperty(BEGIN_SCAN_HANDLE);
    private Channel beginScanC = null;

    public static final String CHANGE_PARAMS_HANDLE = "ChangeParams";
    public final AccessibleProperty changeParams = new AccessibleProperty(CHANGE_PARAMS_HANDLE);

    public static final String ACCEPT_PARAMS_HANDLE = "AcceptParams";
    public final AccessibleProperty acceptParams = new AccessibleProperty(ACCEPT_PARAMS_HANDLE);

    public static final String STAT_ARRAD_HANDLE = "statusArray";
    public final AccessibleProperty statusArray = new AccessibleProperty(STAT_ARRAD_HANDLE);
    private Channel statArrayC = null;

    public static final String VDATA_ARRAD_HANDLE = "vDataArray";
    public final AccessibleProperty vDataArray = new AccessibleProperty(VDATA_ARRAD_HANDLE);
    private Channel vDataArrayC = null;

    public static final String DDATA_ARRAD_HANDLE = "dDataArray";
    public final AccessibleProperty dDataArray = new AccessibleProperty(DDATA_ARRAD_HANDLE);
    private Channel dDataArrayC = null;

    public static final String HDATA_ARRAD_HANDLE = "hDataArray";
    public final AccessibleProperty hDataArray = new AccessibleProperty(HDATA_ARRAD_HANDLE);
    private Channel hDataArrayC = null;

    public static final String POS_ARRAD_HANDLE = "positionArray";
    public final AccessibleProperty positionArray = new AccessibleProperty(POS_ARRAD_HANDLE);
    private Channel posArrayC = null;

    // wire setting signals
    public static final String STEPS_HANDLE = "nSteps";
    public final AccessibleProperty nSteps = new AccessibleProperty(STEPS_HANDLE);
    private Channel stepsC = null;

    public static final String STEP1_POS_HANDLE = "Step1Pos";
    public final AccessibleProperty step1Pos = new AccessibleProperty(STEP1_POS_HANDLE);
    private Channel step1PosC = null;

    public static final String POS_SPACING_HANDLE = "PosSpacing";
    public final AccessibleProperty posSpacing = new AccessibleProperty(POS_SPACING_HANDLE);

    public static final String NO_MEAS_HANDLE = "NoMeas";
    public final AccessibleProperty noMeas = new AccessibleProperty(NO_MEAS_HANDLE);
    private Channel noMeasC = null;

    public static final String SCAN_LEN_HANDLE = "scanLength";
    public final AccessibleProperty scanLength = new AccessibleProperty(SCAN_LEN_HANDLE);
    private Channel scanLengthC = null;

    public static final String BIAS_HANDLE = "Bias";
    public final AccessibleProperty bias = new AccessibleProperty(BIAS_HANDLE);
    private Channel biasC = null;

    // Fitted data signals
    public static final String V_AREA_F_HANDLE = "vAreaF";
    public final AccessibleProperty vAreaF = new AccessibleProperty(V_AREA_F_HANDLE);
    private Channel vAreaFC = null;

    public static final String V_AMP_F_HANDLE = "vAmpF";
    public final AccessibleProperty vAmpF = new AccessibleProperty(V_AMP_F_HANDLE);
    private Channel vAmpFC = null;

    public static final String V_MEAN_F_HANDLE = "vMeanF";
    public final AccessibleProperty vMeanF = new AccessibleProperty(V_MEAN_F_HANDLE);
    private Channel vMeanFC = null;

    public static final String V_SIGMA_F_HANDLE = "vSigmaF";
    public final AccessibleProperty vSigmaF = new AccessibleProperty(V_SIGMA_F_HANDLE);
    private Channel vSigmaFC = null;

    public static final String V_OFFST_F_HANDLE = "vOffstF";
    public final AccessibleProperty vOffstF = new AccessibleProperty(V_OFFST_F_HANDLE);
    private Channel vOffstFC = null;

    public static final String V_SLOPE_F_HANDLE = "vSlopeF";
    public final AccessibleProperty vSlopeF = new AccessibleProperty(V_SLOPE_F_HANDLE);
    private Channel vSlopeFC = null;

    public static final String V_AREA_M_HANDLE = "vAreaM";
    public final AccessibleProperty vAreaM = new AccessibleProperty(V_AREA_M_HANDLE);
    private Channel vAreaMC = null;

    public static final String V_AMP_M_HANDLE = "vAmpM";
    public final AccessibleProperty vAmpM = new AccessibleProperty(V_AMP_M_HANDLE);
    private Channel vAmpMC = null;

    public static final String V_MEAN_M_HANDLE = "vMeanM";
    public final AccessibleProperty vMeanM = new AccessibleProperty(V_MEAN_M_HANDLE);
    private Channel vMeanMC = null;

    public static final String V_SIGMA_M_HANDLE = "vSigmaM";
    public final AccessibleProperty vSigmaM = new AccessibleProperty(V_SIGMA_M_HANDLE);
    private Channel vSigmaMC = null;

    public static final String V_OFFST_M_HANDLE = "vOffstM";
    public final AccessibleProperty vOffstM = new AccessibleProperty(V_OFFST_M_HANDLE);
    private Channel vOffstMC = null;

    public static final String V_SLOPE_M_HANDLE = "vSlopeM";
    public final AccessibleProperty vSlopeM = new AccessibleProperty(V_SLOPE_M_HANDLE);
    private Channel vSlopeMC = null;

    public static final String D_AREA_F_HANDLE = "dAreaF";
    public final AccessibleProperty dAreaF = new AccessibleProperty(D_AREA_F_HANDLE);
    private Channel dAreaFC = null;

    public static final String D_AMP_F_HANDLE = "dAmpF";
    public final AccessibleProperty dAmpF = new AccessibleProperty(D_AMP_F_HANDLE);
    private Channel dAmpFC = null;

    public static final String D_MEAN_F_HANDLE = "dMeanF";
    public final AccessibleProperty dMeanF = new AccessibleProperty(D_MEAN_F_HANDLE);
    private Channel dMeanFC = null;

    public static final String D_SIGMA_F_HANDLE = "dSigmaF";
    public final AccessibleProperty dSigmaF = new AccessibleProperty(D_SIGMA_F_HANDLE);
    private Channel dSigmaFC = null;

    public static final String D_OFFST_F_HANDLE = "dOffstF";
    public final AccessibleProperty dOffstF = new AccessibleProperty(D_OFFST_F_HANDLE);
    private Channel dOffstFC = null;

    public static final String D_SLOPE_F_HANDLE = "dSlopeF";
    public final AccessibleProperty dSlopeF = new AccessibleProperty(D_SLOPE_F_HANDLE);
    private Channel dSlopeFC = null;

    public static final String D_AREA_M_HANDLE = "dAreaM";
    public final AccessibleProperty dAreaM = new AccessibleProperty(D_AREA_M_HANDLE);
    private Channel dAreaMC = null;

    public static final String D_AMP_M_HANDLE = "dAmpM";
    public final AccessibleProperty dAmpM = new AccessibleProperty(D_AMP_M_HANDLE);
    private Channel dAmpMC = null;

    public static final String D_MEAN_M_HANDLE = "dMeanM";
    public final AccessibleProperty dMeanM = new AccessibleProperty(D_MEAN_M_HANDLE);
    private Channel dMeanMC = null;

    public static final String D_SIGMA_M_HANDLE = "dSigmaM";
    public final AccessibleProperty dSigmaM = new AccessibleProperty(D_SIGMA_M_HANDLE);
    private Channel dSigmaMC = null;

    public static final String D_OFFST_M_HANDLE = "dOffstM";
    public final AccessibleProperty dOffstM = new AccessibleProperty(D_OFFST_M_HANDLE);
    private Channel dOffstMC = null;

    public static final String D_SLOPE_M_HANDLE = "dSlopeM";
    public final AccessibleProperty dSlopeM = new AccessibleProperty(D_SLOPE_M_HANDLE);
    private Channel dSlopeMC = null;

    public static final String H_AREA_F_HANDLE = "hAreaF";
    public final AccessibleProperty hAreaF = new AccessibleProperty(H_AREA_F_HANDLE);
    private Channel hAreaFC = null;

    public static final String H_AMP_F_HANDLE = "hAmpF";
    public final AccessibleProperty hAmpF = new AccessibleProperty(H_AMP_F_HANDLE);
    private Channel hAmpFC = null;

    public static final String H_MEAN_F_HANDLE = "hMeanF";
    public final AccessibleProperty hMeanF = new AccessibleProperty(H_MEAN_F_HANDLE);
    private Channel hMeanFC = null;

    public static final String H_SIGMA_F_HANDLE = "hSigmaF";
    public final AccessibleProperty hSigmaF = new AccessibleProperty(H_SIGMA_F_HANDLE);
    private Channel hSigmaFC = null;

    public static final String H_OFFST_F_HANDLE = "hOffstF";
    public final AccessibleProperty hOffstF = new AccessibleProperty(H_OFFST_F_HANDLE);
    private Channel hOffstFC = null;

    public static final String H_SLOPE_F_HANDLE = "hSlopeF";
    public final AccessibleProperty hSlopeF = new AccessibleProperty(H_SLOPE_F_HANDLE);
    private Channel hSlopeFC = null;

    public static final String H_AREA_M_HANDLE = "hAreaM";
    public final AccessibleProperty hAreaM = new AccessibleProperty(H_AREA_M_HANDLE);
    private Channel hAreaMC = null;

    public static final String H_AMP_M_HANDLE = "hAmpM";
    public final AccessibleProperty hAmpM = new AccessibleProperty(H_AMP_M_HANDLE);
    private Channel hAmpMC = null;

    public static final String H_MEAN_M_HANDLE = "hMeanM";
    public final AccessibleProperty hMeanM = new AccessibleProperty(H_MEAN_M_HANDLE);
    private Channel hMeanMC = null;

    public static final String H_SIGMA_M_HANDLE = "hSigmaM";
    public final AccessibleProperty hSigmaM = new AccessibleProperty(H_SIGMA_M_HANDLE);
    private Channel hSigmaMC = null;

    public static final String H_OFFST_M_HANDLE = "hOffstM";
    public final AccessibleProperty hOffstM = new AccessibleProperty(H_OFFST_M_HANDLE);
    private Channel hOffstMC = null;

    public static final String H_SLOPE_M_HANDLE = "hSlopeM";
    public final AccessibleProperty hSlopeM = new AccessibleProperty(H_SLOPE_M_HANDLE);
    private Channel hSlopeMC = null;

    public static final String V_FIT_HANDLE = "vFit";
    public final AccessibleProperty vFit = new AccessibleProperty(V_FIT_HANDLE);
    private Channel vFitC = null;

    public static final String D_FIT_HANDLE = "dFit";
    public final AccessibleProperty dFit = new AccessibleProperty(D_FIT_HANDLE);
    private Channel dFitC = null;

    public static final String H_FIT_HANDLE = "hFit";
    public final AccessibleProperty hFit = new AccessibleProperty(H_FIT_HANDLE);
    private Channel hFitC = null;

    // wire position signals
    public static final String V_POS_HANDLE = "vPos";
    public final AccessibleProperty vPos = new AccessibleProperty(V_POS_HANDLE);
    private Channel vPosC = null;

    public static final String D_POS_HANDLE = "dPos";
    public final AccessibleProperty dPos = new AccessibleProperty(D_POS_HANDLE);
    private Channel dPosC = null;

    public static final String H_POS_HANDLE = "hPos";
    public final AccessibleProperty hPos = new AccessibleProperty(H_POS_HANDLE);
    private Channel hPosC = null;

    // raw data signals
    public static final String V_RAW_HANDLE = "vRaw";
    public final AccessibleProperty vRaw = new AccessibleProperty(V_RAW_HANDLE);
    private Channel vRawC = null;

    public static final String D_RAW_HANDLE = "dRaw";
    public final AccessibleProperty dRaw = new AccessibleProperty(D_RAW_HANDLE);
    private Channel dRawC = null;

    public static final String H_RAW_HANDLE = "hRaw";
    public final AccessibleProperty hRaw = new AccessibleProperty(H_RAW_HANDLE);
    private Channel hRawC = null;

    public static final String V_REAL_DATA_HANDLE = "vRealData";
    public final AccessibleProperty vRealData = new AccessibleProperty(V_REAL_DATA_HANDLE);
    private Channel vVRealDataC = null;

    public static final String D_REAL_DATA_HANDLE = "dRealData";
    public final AccessibleProperty dRealData = new AccessibleProperty(D_REAL_DATA_HANDLE);
    private Channel dRealDataC = null;

    public static final String H_REAL_DATA_HANDLE = "hRealData";
    public final AccessibleProperty hRealData = new AccessibleProperty(H_REAL_DATA_HANDLE);
    private Channel hRealDataC = null;

    /**
     * the container for horizontal fitted data
     */
    private ProfileFit xFit;

    /**
     * the container for vertical fitted data
     */
    private ProfileFit yFit;

    /**
     * the container for diagonal fitted data
     */
    private ProfileFit zFit;

    /**
     * the container for horizontal fitted data, moment method
     */
    private ProfileFit xFitM;

    /**
     * the container for vertical fitted data, moment method
     */
    private ProfileFit yFitM;

    /**
     * the container for diagonal fitted data, moment method
     */
    private ProfileFit zFitM;

    /*
     *  public process variable accessors
     */
    /**
     * get the array with v (vertical) positions in mm
     */
    public double[] getVPos() throws GetException {
        vPosC = this.lazilyGetAndConnect(V_POS_HANDLE, vPosC);
        return vPosC.getArrDbl();
    }

    /**
     * get the array with d (diagonal) positions in mm
     */
    public double[] getDPos() throws GetException {
        dPosC = this.lazilyGetAndConnect(D_POS_HANDLE, dPosC);
        return dPosC.getArrDbl();
    }

    /**
     * get the array with h (horizontal) positions in mm
     */
    public double[] getHPos() throws GetException {
        hPosC = this.lazilyGetAndConnect(H_POS_HANDLE, hPosC);
        return hPosC.getArrDbl();
    }

    /**
     * get the raw vertical intensity array [AU]
     */
    public double[] getVRaw() throws GetException {
        vRawC = this.lazilyGetAndConnect(V_RAW_HANDLE, vRawC);
        return vRawC.getArrDbl();
    }

    /**
     * get the raw diagonal intensity array [AU]
     */
    public double[] getDRaw() throws GetException {
        dRawC = this.lazilyGetAndConnect(D_RAW_HANDLE, dRawC);
        return dRawC.getArrDbl();
    }

    /**
     * get the raw horizontal intensity array [AU]
     */
    public double[] getHRaw() throws GetException {
        hRawC = this.lazilyGetAndConnect(H_RAW_HANDLE, hRawC);
        return hRawC.getArrDbl();
    }

    /**
     * set the number of steps to take
     *
     * @param numSteps = number of Steps to take
     */
    public void setNSteps(int numSteps) throws PutException {
        stepsC = this.lazilyGetAndConnect(STEPS_HANDLE, stepsC);
        stepsC.putVal(numSteps);
    }

    /**
     * set the number of pulses to average over at each wire position
     */
    public void setNAvgPulses(int numAvgs) throws PutException {
        noMeasC = this.lazilyGetAndConnect(NO_MEAS_HANDLE, noMeasC);
        noMeasC.putVal(numAvgs);
    }

    /**
     * set the starting wire position [mm]
     */
    public void setNAvgPulses(double startPos) throws PutException {
        step1PosC = this.lazilyGetAndConnect(STEP1_POS_HANDLE, step1PosC);
        step1PosC.putVal(startPos);
    }

    /**
     * Set the bias voltage on the wire
     *
     * @param newBias = bias [volts]
     */
    public void setBias(double newBias) throws PutException {
        biasC = this.lazilyGetAndConnect(BIAS_HANDLE, biasC);
        biasC.putVal(newBias);
    }

    /**
     * use this to get the real time position of the wire [mm]
     */
    public double getPos() throws GetException {
        posC = this.lazilyGetAndConnect(POS_HANDLE, posC);
        return posC.getValDbl();
    }

    public void connectPos() {
        posC = this.lazilyGetAndConnect(POS_HANDLE, posC);
    }

    /**
     * use this to get the length of the scan [mm]
     */
    public double getScanLength() throws GetException {
        scanLengthC = this.lazilyGetAndConnect(SCAN_LEN_HANDLE, scanLengthC);
        return scanLengthC.getValDbl();
    }

    /**
     * use this to get the number of steps of the scan
     */
    public int getNSteps() throws GetException {
        stepsC = this.lazilyGetAndConnect(STEPS_HANDLE, stepsC);
        return stepsC.getValInt();
    }

    /**
     * tells the wire scanner to actually perform a scan
     */
    public void doScan() throws PutException {
        beginScanC = this.lazilyGetAndConnect(BEGIN_SCAN_HANDLE, beginScanC);
        beginScanC.putVal(1);
    }

    /**
     * tells the wire scanner to stop a scan
     */
    public void stopScan() throws PutException {
        abortScanC = this.lazilyGetAndConnect(ABORT_SCAN_HANDLE, abortScanC);
        abortScanC.putVal(1);
    }

    /**
     * get the status array []
     */
    public double[] getStatusArray() throws GetException {
        statArrayC = this.lazilyGetAndConnect(STAT_ARRAD_HANDLE, statArrayC);
        return statArrayC.getArrDbl();
    }

    /**
     * connect the status array []
     */
    public void connectStatArray() {
        statArrayC = this.lazilyGetAndConnect(STAT_ARRAD_HANDLE, statArrayC);
    }

    /**
     * get the v data array []
     */
    public double[] getVDataArray() throws GetException {
        vDataArrayC = this.lazilyGetAndConnect(VDATA_ARRAD_HANDLE, vDataArrayC);
        return vDataArrayC.getArrDbl();
    }

    /**
     * connect the v data array []
     */
    public void connectVDataArray() {
        vDataArrayC = this.lazilyGetAndConnect(VDATA_ARRAD_HANDLE, vDataArrayC);
    }

    /**
     * get the d data array []
     */
    public double[] getDDataArray() throws GetException {
        dDataArrayC = this.lazilyGetAndConnect(DDATA_ARRAD_HANDLE, dDataArrayC);
        return dDataArrayC.getArrDbl();
    }

    /**
     * connect the d data array []
     */
    public void connectDDataArray() {
        dDataArrayC = this.lazilyGetAndConnect(DDATA_ARRAD_HANDLE, dDataArrayC);
    }

    /**
     * get the h data array []
     */
    public double[] getHDataArray() throws GetException {
        hDataArrayC = this.lazilyGetAndConnect(HDATA_ARRAD_HANDLE, hDataArrayC);
        return hDataArrayC.getArrDbl();
    }

    /**
     * connect the h data array []
     */
    public void connectHDataArray() {
        hDataArrayC = this.lazilyGetAndConnect(HDATA_ARRAD_HANDLE, hDataArrayC);
    }

    /**
     * get the position data array []
     */
    public double[] getPosArray() throws GetException {
        posArrayC = this.lazilyGetAndConnect(POS_ARRAD_HANDLE, posArrayC);
        return posArrayC.getArrDbl();
    }

    /**
     * connect the position data array []
     */
    public void connectPosArray() {
        posArrayC = this.lazilyGetAndConnect(POS_ARRAD_HANDLE, posArrayC);
    }

    /**
     * get the v fit array []
     */
    public double[] getVFitArray() throws GetException {
        vFitC = this.lazilyGetAndConnect(V_FIT_HANDLE, vFitC);
        return vFitC.getArrDbl();
    }

    /**
     * connect the v fit array []
     */
    public void connectVFitArray() {
        vFitC = this.lazilyGetAndConnect(V_FIT_HANDLE, vFitC);
    }

    /**
     * get the d fit array []
     */
    public double[] getDFitArray() throws GetException {
        dFitC = this.lazilyGetAndConnect(D_FIT_HANDLE, dFitC);
        return dFitC.getArrDbl();
    }

    /**
     * connect the d fit array []
     */
    public void connectDFitArray() {
        dFitC = this.lazilyGetAndConnect(D_FIT_HANDLE, dFitC);
    }

    /**
     * get the h fit array []
     */
    public double[] getHFitArray() throws GetException {
        hFitC = this.lazilyGetAndConnect(H_FIT_HANDLE, hFitC);
        return hFitC.getArrDbl();
    }

    /**
     * connect the h fit array []
     */
    public void connectHFitArray() {
        hFitC = this.lazilyGetAndConnect(H_FIT_HANDLE, hFitC);
    }

    /**
     * use this to get the v area fit
     */
    public double getVAreaF() throws GetException {
        vAreaFC = this.lazilyGetAndConnect(V_AREA_F_HANDLE, vAreaFC);
        return vAreaFC.getValDbl();
    }

    /**
     * use this to get the d area fit
     */
    public double getDAreaF() throws GetException {
        dAreaFC = this.lazilyGetAndConnect(D_AREA_F_HANDLE, dAreaFC);
        return dAreaFC.getValDbl();
    }

    /**
     * use this to get the h area fit
     */
    public double getHAreaF() throws GetException {
        hAreaFC = this.lazilyGetAndConnect(H_AREA_F_HANDLE, hAreaFC);
        return hAreaFC.getValDbl();
    }

    /**
     * use this to get the v area rms
     */
    public double getVAreaM() throws GetException {
        vAreaMC = this.lazilyGetAndConnect(V_AREA_M_HANDLE, vAreaMC);
        return vAreaMC.getValDbl();
    }

    /**
     * use this to get the d area rms
     */
    public double getDAreaM() throws GetException {
        dAreaMC = this.lazilyGetAndConnect(D_AREA_M_HANDLE, dAreaMC);
        return dAreaMC.getValDbl();
    }

    /**
     * use this to get the h area rms
     */
    public double getHAreaM() throws GetException {
        hAreaMC = this.lazilyGetAndConnect(H_AREA_M_HANDLE, hAreaMC);
        return hAreaMC.getValDbl();
    }

    /**
     * use this to get the v sigma fit
     */
    public double getVSigmaF() throws GetException {
        vSigmaFC = this.lazilyGetAndConnect(V_SIGMA_F_HANDLE, vSigmaFC);
        return vSigmaFC.getValDbl();
    }

    /**
     * use this to get the d sigma fit
     */
    public double getDSigmaF() throws GetException {
        dSigmaFC = this.lazilyGetAndConnect(D_SIGMA_F_HANDLE, dSigmaFC);
        return dSigmaFC.getValDbl();
    }

    /**
     * use this to get the h sigma fit
     */
    public double getHSigmaF() throws GetException {
        hSigmaFC = this.lazilyGetAndConnect(H_SIGMA_F_HANDLE, hSigmaFC);
        return hSigmaFC.getValDbl();
    }

    /**
     * use this to get the v sigma rms
     */
    public double getVSigmaM() throws GetException {
        vSigmaMC = this.lazilyGetAndConnect(V_SIGMA_M_HANDLE, vSigmaMC);
        return vSigmaMC.getValDbl();
    }

    /**
     * use this to get the d sigma rms
     */
    public double getDSigmaM() throws GetException {
        dSigmaMC = this.lazilyGetAndConnect(D_SIGMA_M_HANDLE, dSigmaMC);
        return dSigmaMC.getValDbl();
    }

    /**
     * use this to get the h sigma rms
     */
    public double getHSigmaM() throws GetException {
        hSigmaMC = this.lazilyGetAndConnect(H_SIGMA_M_HANDLE, hSigmaMC);
        return hSigmaMC.getValDbl();
    }

    /**
     * use this to get the v amp fit
     */
    public double getVAmplF() throws GetException {
        vAmpFC = this.lazilyGetAndConnect(V_AMP_F_HANDLE, vAmpFC);
        return vAmpFC.getValDbl();
    }

    /**
     * use this to get the d amp fit
     */
    public double getDAmplF() throws GetException {
        dAmpFC = this.lazilyGetAndConnect(D_AMP_F_HANDLE, dAmpFC);
        return dAmpFC.getValDbl();
    }

    /**
     * use this to get the h amp fit
     */
    public double getHAmplF() throws GetException {
        hAmpFC = this.lazilyGetAndConnect(H_AMP_F_HANDLE, hAmpFC);
        return hAmpFC.getValDbl();
    }

    /**
     * use this to get the v amp rms
     */
    public double getVAmplM() throws GetException {
        vAmpMC = this.lazilyGetAndConnect(V_AMP_M_HANDLE, vAmpMC);
        return vAmpMC.getValDbl();
    }

    /**
     * use this to get the d amp rms
     */
    public double getDAmplM() throws GetException {
        dAmpMC = this.lazilyGetAndConnect(D_AMP_M_HANDLE, dAmpMC);
        return dAmpMC.getValDbl();
    }

    /**
     * use this to get the h amp rms
     */
    public double getHAmplM() throws GetException {
        hAmpMC = this.lazilyGetAndConnect(H_AMP_M_HANDLE, hAmpMC);
        return hAmpMC.getValDbl();
    }

    /**
     * use this to get the v mean fit
     */
    public double getVMeanF() throws GetException {
        vMeanFC = this.lazilyGetAndConnect(V_MEAN_F_HANDLE, vMeanFC);
        return vMeanFC.getValDbl();
    }

    /**
     * use this to get the d mean fit
     */
    public double getDMeanF() throws GetException {
        dMeanFC = this.lazilyGetAndConnect(D_MEAN_F_HANDLE, dMeanFC);
        return dMeanFC.getValDbl();
    }

    /**
     * use this to get the h mean fit
     */
    public double getHMeanF() throws GetException {
        hMeanFC = this.lazilyGetAndConnect(H_MEAN_F_HANDLE, hMeanFC);
        return hMeanFC.getValDbl();
    }

    /**
     * use this to get the v mean rms
     */
    public double getVMeanM() throws GetException {
        vMeanMC = this.lazilyGetAndConnect(V_MEAN_M_HANDLE, vMeanMC);
        return vMeanMC.getValDbl();
    }

    /**
     * use this to get the d mean rms
     */
    public double getDMeanM() throws GetException {
        dMeanMC = this.lazilyGetAndConnect(D_MEAN_M_HANDLE, dMeanMC);
        return dMeanMC.getValDbl();
    }

    /**
     * use this to get the h mean rms
     */
    public double getHMeanM() throws GetException {
        hMeanMC = this.lazilyGetAndConnect(H_MEAN_M_HANDLE, hMeanMC);
        return hMeanMC.getValDbl();
    }

    /**
     * use this to get the v offset fit
     */
    public double getVOffsetF() throws GetException {
        vOffstFC = this.lazilyGetAndConnect(V_OFFST_F_HANDLE, vOffstFC);
        return vOffstFC.getValDbl();
    }

    /**
     * use this to get the d offset fit
     */
    public double getDOffsetF() throws GetException {
        dOffstFC = this.lazilyGetAndConnect(D_OFFST_F_HANDLE, dOffstFC);
        return dOffstFC.getValDbl();
    }

    /**
     * use this to get the h offset fit
     */
    public double getHOffsetF() throws GetException {
        hOffstFC = this.lazilyGetAndConnect(H_OFFST_F_HANDLE, hOffstFC);
        return hOffstFC.getValDbl();
    }

    /**
     * use this to get the v offset rms
     */
    public double getVOffsetM() throws GetException {
        vOffstMC = this.lazilyGetAndConnect(V_OFFST_M_HANDLE, vOffstMC);
        return vOffstMC.getValDbl();
    }

    /**
     * use this to get the d offset rms
     */
    public double getDOffsetM() throws GetException {
        dOffstMC = this.lazilyGetAndConnect(D_OFFST_M_HANDLE, dOffstMC);
        return dOffstMC.getValDbl();
    }

    /**
     * use this to get the h offset rms
     */
    public double getHOffsetM() throws GetException {
        hOffstMC = this.lazilyGetAndConnect(H_OFFST_M_HANDLE, hOffstMC);
        return hOffstMC.getValDbl();
    }

    /**
     * use this to get the v slope fit
     */
    public double getVSlopeF() throws GetException {
        vSlopeFC = this.lazilyGetAndConnect(V_SLOPE_F_HANDLE, vSlopeFC);
        return vSlopeFC.getValDbl();
    }

    /**
     * use this to get the d slope fit
     */
    public double getDSlopeF() throws GetException {
        dSlopeFC = this.lazilyGetAndConnect(D_SLOPE_F_HANDLE, dSlopeFC);
        return dSlopeFC.getValDbl();
    }

    /**
     * use this to get the h slope fit
     */
    public double getHSlopeF() throws GetException {
        hSlopeFC = this.lazilyGetAndConnect(H_SLOPE_F_HANDLE, hSlopeFC);
        return hSlopeFC.getValDbl();
    }

    /**
     * use this to get the v slope rms
     */
    public double getVSlopeM() throws GetException {
        vSlopeMC = this.lazilyGetAndConnect(V_SLOPE_M_HANDLE, vSlopeMC);
        return vSlopeMC.getValDbl();
    }

    /**
     * use this to get the d slope rms
     */
    public double getDSlopeM() throws GetException {
        dSlopeMC = this.lazilyGetAndConnect(D_SLOPE_M_HANDLE, dSlopeMC);
        return dSlopeMC.getValDbl();
    }

    /**
     * use this to get the h slope rms
     */
    public double getHSlopeM() throws GetException {
        hSlopeMC = this.lazilyGetAndConnect(H_SLOPE_M_HANDLE, hSlopeMC);
        return hSlopeMC.getValDbl();
    }

    /**
     * connect the v real data stream
     */
    public void connectVData() {
        vVRealDataC = this.lazilyGetAndConnect(V_REAL_DATA_HANDLE, vVRealDataC);
    }

    /**
     * get the v real data stream
     */
    public double getVData() throws GetException {
        vVRealDataC = this.lazilyGetAndConnect(V_REAL_DATA_HANDLE, vVRealDataC);
        return vVRealDataC.getValDbl();
    }

    /**
     * connect the d real data stream
     */
    public void connectDData() {
        dRealDataC = this.lazilyGetAndConnect(D_REAL_DATA_HANDLE, dRealDataC);
    }

    /**
     * get the d real data stream
     */
    public double getDData() throws GetException {
        dRealDataC = this.lazilyGetAndConnect(D_REAL_DATA_HANDLE, dRealDataC);
        return dRealDataC.getValDbl();
    }

    /**
     * connect the h real data stream
     */
    public void connectHData() {
        hRealDataC = this.lazilyGetAndConnect(H_REAL_DATA_HANDLE, hRealDataC);
    }

    /**
     * get the h real data stream
     */
    public double getHData() throws GetException {
        hRealDataC = this.lazilyGetAndConnect(H_REAL_DATA_HANDLE, hRealDataC);
        return hRealDataC.getValDbl();
    }

    /**
     * this method updates the horizontal profile polynomial fitted information
     * from the instrument
     */
    public void updateFits() {

        // create a correlator with a 1 second correlation time span
        // to grab all the fit parameters at once.
        // The wire scanner grabs points from multiple pulses, and when
        // done, does the fit calculation. The time-stamp on the fit PVs
        // is the time of the calculation. Use 1 sec. window for safety.
        ChannelCorrelator correlator = new ChannelCorrelator(1.0);
        correlator.addChannel(vAreaFC);
        correlator.addChannel(vAmpFC);
        correlator.addChannel(vMeanFC);
        correlator.addChannel(vSigmaFC);
        correlator.addChannel(vOffstFC);
        correlator.addChannel(vSlopeFC);

        correlator.addChannel(vAreaMC);
        correlator.addChannel(vAmpMC);
        correlator.addChannel(vMeanMC);
        correlator.addChannel(vSigmaMC);
        correlator.addChannel(vOffstMC);
        correlator.addChannel(vSlopeMC);

        correlator.addChannel(dAreaFC);
        correlator.addChannel(dAmpFC);
        correlator.addChannel(dMeanFC);
        correlator.addChannel(dSigmaFC);
        correlator.addChannel(dOffstFC);
        correlator.addChannel(dSlopeFC);

        correlator.addChannel(dAreaMC);
        correlator.addChannel(dAmpMC);
        correlator.addChannel(dMeanMC);
        correlator.addChannel(dSigmaMC);
        correlator.addChannel(dOffstMC);
        correlator.addChannel(dSlopeMC);

        correlator.addChannel(hAreaFC);
        correlator.addChannel(hAmpFC);
        correlator.addChannel(hMeanFC);
        correlator.addChannel(hSigmaFC);
        correlator.addChannel(hOffstFC);
        correlator.addChannel(hSlopeFC);

        correlator.addChannel(hAreaMC);
        correlator.addChannel(hAmpMC);
        correlator.addChannel(hMeanMC);
        correlator.addChannel(hSigmaMC);
        correlator.addChannel(hOffstMC);
        correlator.addChannel(hSlopeMC);

        // get all the values at once:
        Correlation<ChannelTimeRecord> correlation = correlator.fetchCorrelationWithTimeout(10.0);

        xFit.setMean(correlation.getRecord(V_MEAN_F_HANDLE).doubleValue());
        xFit.setSigma(correlation.getRecord(V_SIGMA_F_HANDLE).doubleValue());
        xFit.setAmp(correlation.getRecord(V_AMP_F_HANDLE).doubleValue());
        xFit.setArea(correlation.getRecord(V_AREA_F_HANDLE).doubleValue());
        xFit.setOffset(correlation.getRecord(V_OFFST_F_HANDLE).doubleValue());
        xFit.setSlope(correlation.getRecord(V_SLOPE_F_HANDLE).doubleValue());

        xFitM.setMean(correlation.getRecord(V_MEAN_M_HANDLE).doubleValue());
        xFitM.setSigma(correlation.getRecord(V_SIGMA_M_HANDLE).doubleValue());
        xFitM.setAmp(correlation.getRecord(V_AMP_M_HANDLE).doubleValue());
        xFitM.setArea(correlation.getRecord(V_AREA_M_HANDLE).doubleValue());
        xFitM.setOffset(correlation.getRecord(V_OFFST_M_HANDLE).doubleValue());
        xFitM.setSlope(correlation.getRecord(V_SLOPE_M_HANDLE).doubleValue());

        yFit.setMean(correlation.getRecord(D_MEAN_F_HANDLE).doubleValue());
        yFit.setSigma(correlation.getRecord(D_SIGMA_F_HANDLE).doubleValue());
        yFit.setAmp(correlation.getRecord(D_AMP_F_HANDLE).doubleValue());
        yFit.setArea(correlation.getRecord(D_AREA_F_HANDLE).doubleValue());
        yFit.setOffset(correlation.getRecord(D_OFFST_F_HANDLE).doubleValue());
        yFit.setSlope(correlation.getRecord(D_SLOPE_F_HANDLE).doubleValue());

        yFitM.setMean(correlation.getRecord(D_MEAN_M_HANDLE).doubleValue());
        yFitM.setSigma(correlation.getRecord(D_SIGMA_M_HANDLE).doubleValue());
        yFitM.setAmp(correlation.getRecord(D_AMP_M_HANDLE).doubleValue());
        yFitM.setArea(correlation.getRecord(D_AREA_M_HANDLE).doubleValue());
        yFitM.setOffset(correlation.getRecord(D_OFFST_M_HANDLE).doubleValue());
        yFitM.setSlope(correlation.getRecord(D_SLOPE_M_HANDLE).doubleValue());

        zFit.setMean(correlation.getRecord(H_MEAN_F_HANDLE).doubleValue());
        zFit.setSigma(correlation.getRecord(H_SIGMA_F_HANDLE).doubleValue());
        zFit.setAmp(correlation.getRecord(H_AMP_F_HANDLE).doubleValue());
        zFit.setArea(correlation.getRecord(H_AREA_F_HANDLE).doubleValue());
        zFit.setOffset(correlation.getRecord(H_OFFST_F_HANDLE).doubleValue());
        zFit.setSlope(correlation.getRecord(H_SLOPE_F_HANDLE).doubleValue());

        zFitM.setMean(correlation.getRecord(H_MEAN_M_HANDLE).doubleValue());
        zFitM.setSigma(correlation.getRecord(H_SIGMA_M_HANDLE).doubleValue());
        zFitM.setAmp(correlation.getRecord(H_AMP_M_HANDLE).doubleValue());
        zFitM.setArea(correlation.getRecord(H_AREA_M_HANDLE).doubleValue());
        zFitM.setOffset(correlation.getRecord(H_OFFST_M_HANDLE).doubleValue());
        zFitM.setSlope(correlation.getRecord(H_SLOPE_M_HANDLE).doubleValue());
    }
}
