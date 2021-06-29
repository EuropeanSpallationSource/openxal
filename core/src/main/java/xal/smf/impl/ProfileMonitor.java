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
    public final AccessibleProperty position = new AccessibleProperty("position", POS_HANDLE);
    public Channel PosC = null;

    public static final String RT_GRAPH_HANDLE = "RTGraph";
    public final AccessibleProperty rtGraph = new AccessibleProperty("RTGraph", RT_GRAPH_HANDLE);

    // action signals
    public static final String ABORT_SCAN_HANDLE = "abortScan";
    public final AccessibleProperty abortScan = new AccessibleProperty("abortScan", ABORT_SCAN_HANDLE);
    private Channel AbortScanC = null;

    public static final String BEGIN_SCAN_HANDLE = "beginScan";
    public final AccessibleProperty beginScan = new AccessibleProperty("beginScan", BEGIN_SCAN_HANDLE);
    private Channel BeginScanC = null;

    public static final String CHANGE_PARAMS_HANDLE = "ChangeParams";
    public final AccessibleProperty changeParams = new AccessibleProperty("ChangeParams", CHANGE_PARAMS_HANDLE);

    public static final String ACCEPT_PARAMS_HANDLE = "AcceptParams";
    public final AccessibleProperty acceptParams = new AccessibleProperty("AcceptParams", ACCEPT_PARAMS_HANDLE);

    public static final String STAT_ARRAD_HANDLE = "statusArray";
    public final AccessibleProperty statusArray = new AccessibleProperty("statusArray", STAT_ARRAD_HANDLE);
    public Channel StatArrayC = null;

    public static final String VDATA_ARRAD_HANDLE = "vDataArray";
    public final AccessibleProperty vDataArray = new AccessibleProperty("vDataArray", VDATA_ARRAD_HANDLE);
    public Channel VDataArrayC = null;

    public static final String DDATA_ARRAD_HANDLE = "dDataArray";
    public final AccessibleProperty dDataArray = new AccessibleProperty("dDataArray", DDATA_ARRAD_HANDLE);
    public Channel DDataArrayC = null;

    public static final String HDATA_ARRAD_HANDLE = "hDataArray";
    public final AccessibleProperty hDataArray = new AccessibleProperty("hDataArray", HDATA_ARRAD_HANDLE);
    public Channel HDataArrayC = null;

    public static final String POS_ARRAD_HANDLE = "positionArray";
    public final AccessibleProperty positionArray = new AccessibleProperty("positionArray", POS_ARRAD_HANDLE);
    public Channel PosArrayC = null;

    // wire setting signals
    public static final String STEPS_HANDLE = "nSteps";
    public final AccessibleProperty nSteps = new AccessibleProperty("nSteps", STEPS_HANDLE);
    private Channel StepsC = null;

    public static final String STEP1_POS_HANDLE = "Step1Pos";
    public final AccessibleProperty step1Pos = new AccessibleProperty("Step1Pos", STEP1_POS_HANDLE);
    private Channel Step1PosC = null;

    public static final String POS_SPACING_HANDLE = "PosSpacing";
    public final AccessibleProperty posSpacing = new AccessibleProperty("PosSpacing", POS_SPACING_HANDLE);

    public static final String NO_MEAS_HANDLE = "NoMeas";
    public final AccessibleProperty noMeas = new AccessibleProperty("NoMeas", NO_MEAS_HANDLE);
    private Channel NoMeasC = null;

    public static final String SCAN_LEN_HANDLE = "scanLength";
    public final AccessibleProperty scanLength = new AccessibleProperty("scanLength", SCAN_LEN_HANDLE);
    private Channel ScanLengthC = null;

    public static final String BIAS_HANDLE = "Bias";
    public final AccessibleProperty bias = new AccessibleProperty("Bias", BIAS_HANDLE);
    private Channel BiasC = null;

    // Fitted data signals
    public static final String V_AREA_F_HANDLE = "vAreaF";
    public final AccessibleProperty vAreaF = new AccessibleProperty("vAreaF", V_AREA_F_HANDLE);
    private Channel VAreaFC = null;

    public static final String V_AMP_F_HANDLE = "vAmpF";
    public final AccessibleProperty vAmpF = new AccessibleProperty("vAmpF", V_AMP_F_HANDLE);
    private Channel VAmpFC = null;

    public static final String V_MEAN_F_HANDLE = "vMeanF";
    public final AccessibleProperty vMeanF = new AccessibleProperty("vMeanF", V_MEAN_F_HANDLE);
    private Channel VMeanFC = null;

    public static final String V_SIGMA_F_HANDLE = "vSigmaF";
    public final AccessibleProperty vSigmaF = new AccessibleProperty("vSigmaF", V_SIGMA_F_HANDLE);
    private Channel VSigmaFC = null;

    public static final String V_OFFST_F_HANDLE = "vOffstF";
    public final AccessibleProperty vOffstF = new AccessibleProperty("vOffstF", V_OFFST_F_HANDLE);
    private Channel VOffstFC = null;

    public static final String V_SLOPE_F_HANDLE = "vSlopeF";
    public final AccessibleProperty vSlopeF = new AccessibleProperty("vSlopeF", V_SLOPE_F_HANDLE);
    private Channel VSlopeFC = null;

    public static final String V_AREA_M_HANDLE = "vAreaM";
    public final AccessibleProperty vAreaM = new AccessibleProperty("vAreaM", V_AREA_M_HANDLE);
    private Channel VAreaMC = null;

    public static final String V_AMP_M_HANDLE = "vAmpM";
    public final AccessibleProperty vAmpM = new AccessibleProperty("vAmpM", V_AMP_M_HANDLE);
    private Channel VAmpMC = null;

    public static final String V_MEAN_M_HANDLE = "vMeanM";
    public final AccessibleProperty vMeanM = new AccessibleProperty("vMeanM", V_MEAN_M_HANDLE);
    private Channel VMeanMC = null;

    public static final String V_SIGMA_M_HANDLE = "vSigmaM";
    public final AccessibleProperty vSigmaM = new AccessibleProperty("vSigmaM", V_SIGMA_M_HANDLE);
    private Channel VSigmaMC = null;

    public static final String V_OFFST_M_HANDLE = "vOffstM";
    public final AccessibleProperty vOffstM = new AccessibleProperty("vOffstM", V_OFFST_M_HANDLE);
    private Channel VOffstMC = null;

    public static final String V_SLOPE_M_HANDLE = "vSlopeM";
    public final AccessibleProperty vSlopeM = new AccessibleProperty("vSlopeM", V_SLOPE_M_HANDLE);
    private Channel VSlopeMC = null;

    public static final String D_AREA_F_HANDLE = "dAreaF";
    public final AccessibleProperty dAreaF = new AccessibleProperty("dAreaF", D_AREA_F_HANDLE);
    private Channel DAreaFC = null;

    public static final String D_AMP_F_HANDLE = "dAmpF";
    public final AccessibleProperty dAmpF = new AccessibleProperty("dAmpF", D_AMP_F_HANDLE);
    private Channel DAmpFC = null;

    public static final String D_MEAN_F_HANDLE = "dMeanF";
    public final AccessibleProperty dMeanF = new AccessibleProperty("dMeanF", D_MEAN_F_HANDLE);
    private Channel DMeanFC = null;

    public static final String D_SIGMA_F_HANDLE = "dSigmaF";
    public final AccessibleProperty dSigmaF = new AccessibleProperty("dSigmaF", D_SIGMA_F_HANDLE);
    private Channel DSigmaFC = null;

    public static final String D_OFFST_F_HANDLE = "dOffstF";
    public final AccessibleProperty dOffstF = new AccessibleProperty("dOffstF", D_OFFST_F_HANDLE);
    private Channel DOffstFC = null;

    public static final String D_SLOPE_F_HANDLE = "dSlopeF";
    public final AccessibleProperty dSlopeF = new AccessibleProperty("dSlopeF", D_SLOPE_F_HANDLE);
    private Channel DSlopeFC = null;

    public static final String D_AREA_M_HANDLE = "dAreaM";
    public final AccessibleProperty dAreaM = new AccessibleProperty("dAreaM", D_AREA_M_HANDLE);
    private Channel DAreaMC = null;

    public static final String D_AMP_M_HANDLE = "dAmpM";
    public final AccessibleProperty dAmpM = new AccessibleProperty("dAmpM", D_AMP_M_HANDLE);
    private Channel DAmpMC = null;

    public static final String D_MEAN_M_HANDLE = "dMeanM";
    public final AccessibleProperty dMeanM = new AccessibleProperty("dMeanM", D_MEAN_M_HANDLE);
    private Channel DMeanMC = null;

    public static final String D_SIGMA_M_HANDLE = "dSigmaM";
    public final AccessibleProperty dSigmaM = new AccessibleProperty("dSigmaM", D_SIGMA_M_HANDLE);
    private Channel DSigmaMC = null;

    public static final String D_OFFST_M_HANDLE = "dOffstM";
    public final AccessibleProperty dOffstM = new AccessibleProperty("dOffstM", D_OFFST_M_HANDLE);
    private Channel DOffstMC = null;

    public static final String D_SLOPE_M_HANDLE = "dSlopeM";
    public final AccessibleProperty dSlopeM = new AccessibleProperty("dSlopeM", D_SLOPE_M_HANDLE);
    private Channel DSlopeMC = null;

    public static final String H_AREA_F_HANDLE = "hAreaF";
    public final AccessibleProperty hAreaF = new AccessibleProperty("hAreaF", H_AREA_F_HANDLE);
    private Channel HAreaFC = null;

    public static final String H_AMP_F_HANDLE = "hAmpF";
    public final AccessibleProperty hAmpF = new AccessibleProperty("hAmpF", H_AMP_F_HANDLE);
    private Channel HAmpFC = null;

    public static final String H_MEAN_F_HANDLE = "hMeanF";
    public final AccessibleProperty hMeanF = new AccessibleProperty("hMeanF", H_MEAN_F_HANDLE);
    private Channel HMeanFC = null;

    public static final String H_SIGMA_F_HANDLE = "hSigmaF";
    public final AccessibleProperty hSigmaF = new AccessibleProperty("hSigmaF", H_SIGMA_F_HANDLE);
    private Channel HSigmaFC = null;

    public static final String H_OFFST_F_HANDLE = "hOffstF";
    public final AccessibleProperty hOffstF = new AccessibleProperty("hOffstF", H_OFFST_F_HANDLE);
    private Channel HOffstFC = null;

    public static final String H_SLOPE_F_HANDLE = "hSlopeF";
    public final AccessibleProperty hSlopeF = new AccessibleProperty("hSlopeF", H_SLOPE_F_HANDLE);
    private Channel HSlopeFC = null;

    public static final String H_AREA_M_HANDLE = "hAreaM";
    public final AccessibleProperty hAreaM = new AccessibleProperty("hAreaM", H_AREA_M_HANDLE);
    private Channel HAreaMC = null;

    public static final String H_AMP_M_HANDLE = "hAmpM";
    public final AccessibleProperty hAmpM = new AccessibleProperty("hAmpM", H_AMP_M_HANDLE);
    private Channel HAmpMC = null;

    public static final String H_MEAN_M_HANDLE = "hMeanM";
    public final AccessibleProperty hMeanM = new AccessibleProperty("hMeanM", H_MEAN_M_HANDLE);
    private Channel HMeanMC = null;

    public static final String H_SIGMA_M_HANDLE = "hSigmaM";
    public final AccessibleProperty hSigmaM = new AccessibleProperty("hSigmaM", H_SIGMA_M_HANDLE);
    private Channel HSigmaMC = null;

    public static final String H_OFFST_M_HANDLE = "hOffstM";
    public final AccessibleProperty hOffstM = new AccessibleProperty("hOffstM", H_OFFST_M_HANDLE);
    private Channel HOffstMC = null;

    public static final String H_SLOPE_M_HANDLE = "hSlopeM";
    public final AccessibleProperty hSlopeM = new AccessibleProperty("hSlopeM", H_SLOPE_M_HANDLE);
    private Channel HSlopeMC = null;

    public static final String V_FIT_HANDLE = "vFit";
    public final AccessibleProperty vFit = new AccessibleProperty("vFit", V_FIT_HANDLE);
    public Channel VFitC = null;

    public static final String D_FIT_HANDLE = "dFit";
    public final AccessibleProperty dFit = new AccessibleProperty("dFit", D_FIT_HANDLE);
    public Channel DFitC = null;

    public static final String H_FIT_HANDLE = "hFit";
    public final AccessibleProperty hFit = new AccessibleProperty("hFit", H_FIT_HANDLE);
    public Channel HFitC = null;

    // wire position signals
    public static final String V_POS_HANDLE = "vPos";
    public final AccessibleProperty vPos = new AccessibleProperty("vPos", V_POS_HANDLE);
    private Channel VPosC = null;

    public static final String D_POS_HANDLE = "dPos";
    public final AccessibleProperty dPos = new AccessibleProperty("dPos", D_POS_HANDLE);
    private Channel DPosC = null;

    public static final String H_POS_HANDLE = "hPos";
    public final AccessibleProperty hPos = new AccessibleProperty("hPos", H_POS_HANDLE);
    private Channel HPosC = null;

    // raw data signals
    public static final String V_RAW_HANDLE = "vRaw";
    public final AccessibleProperty vRaw = new AccessibleProperty("vRaw", V_RAW_HANDLE);
    private Channel VRawC = null;

    public static final String D_RAW_HANDLE = "dRaw";
    public final AccessibleProperty dRaw = new AccessibleProperty("dRaw", D_RAW_HANDLE);
    private Channel DRawC = null;

    public static final String H_RAW_HANDLE = "hRaw";
    public final AccessibleProperty hRaw = new AccessibleProperty("hRaw", H_RAW_HANDLE);
    private Channel HRawC = null;

    public static final String V_REAL_DATA_HANDLE = "vRealData";
    public final AccessibleProperty vRealData = new AccessibleProperty("vRealData", V_REAL_DATA_HANDLE);
    private Channel VRealDataC = null;

    public static final String D_REAL_DATA_HANDLE = "dRealData";
    public final AccessibleProperty dRealData = new AccessibleProperty("dRealData", D_REAL_DATA_HANDLE);
    private Channel DRealDataC = null;

    public static final String H_REAL_DATA_HANDLE = "hRealData";
    public final AccessibleProperty hRealData = new AccessibleProperty("hRealData", H_REAL_DATA_HANDLE);
    private Channel HRealDataC = null;

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
    public double[] getVPos() throws ConnectionException, GetException {
        VPosC = this.lazilyGetAndConnect(V_POS_HANDLE, VPosC);
        return VPosC.getArrDbl();
    }

    /**
     * get the array with d (diagonal) positions in mm
     */
    public double[] getDPos() throws ConnectionException, GetException {
        DPosC = this.lazilyGetAndConnect(D_POS_HANDLE, DPosC);
        return DPosC.getArrDbl();
    }

    /**
     * get the array with h (horizontal) positions in mm
     */
    public double[] getHPos() throws ConnectionException, GetException {
        HPosC = this.lazilyGetAndConnect(H_POS_HANDLE, HPosC);
        return HPosC.getArrDbl();
    }

    /**
     * get the raw vertical intensity array [AU]
     */
    public double[] getVRaw() throws ConnectionException, GetException {
        VRawC = this.lazilyGetAndConnect(V_RAW_HANDLE, VRawC);
        return VRawC.getArrDbl();
    }

    /**
     * get the raw diagonal intensity array [AU]
     */
    public double[] getDRaw() throws ConnectionException, GetException {
        DRawC = this.lazilyGetAndConnect(D_RAW_HANDLE, DRawC);
        return DRawC.getArrDbl();
    }

    /**
     * get the raw horizontal intensity array [AU]
     */
    public double[] getHRaw() throws ConnectionException, GetException {
        HRawC = this.lazilyGetAndConnect(H_RAW_HANDLE, HRawC);
        return HRawC.getArrDbl();
    }

    /**
     * set the number of steps to take
     *
     * @param numSteps = number of Steps to take
     */
    public void setNSteps(int numSteps) throws ConnectionException, PutException {
        StepsC = this.lazilyGetAndConnect(STEPS_HANDLE, StepsC);
        StepsC.putVal(numSteps);
    }

    /**
     * set the number of pulses to average over at each wire position
     */
    public void setNAvgPulses(int numAvgs) throws ConnectionException, PutException {
        NoMeasC = this.lazilyGetAndConnect(NO_MEAS_HANDLE, NoMeasC);
        NoMeasC.putVal(numAvgs);
    }

    /**
     * set the starting wire position [mm]
     */
    public void setNAvgPulses(double startPos) throws ConnectionException, PutException {
        Step1PosC = this.lazilyGetAndConnect(STEP1_POS_HANDLE, Step1PosC);
        Step1PosC.putVal(startPos);
    }

    /**
     * Set the bias voltage on the wire
     *
     * @param newBias = bias [volts]
     */
    public void setBias(double newBias) throws ConnectionException, PutException {
        BiasC = this.lazilyGetAndConnect(BIAS_HANDLE, BiasC);
        BiasC.putVal(newBias);
    }

    /**
     * use this to get the real time position of the wire [mm]
     */
    public double getPos() throws ConnectionException, GetException {
        PosC = this.lazilyGetAndConnect(POS_HANDLE, PosC);
        return PosC.getValDbl();
    }

    public void connectPos() throws ConnectionException {
        PosC = this.lazilyGetAndConnect(POS_HANDLE, PosC);
    }

    /**
     * use this to get the length of the scan [mm]
     */
    public double getScanLength() throws ConnectionException, GetException {
        ScanLengthC = this.lazilyGetAndConnect(SCAN_LEN_HANDLE, ScanLengthC);
        return ScanLengthC.getValDbl();
    }

    /**
     * use this to get the number of steps of the scan
     */
    public int getNSteps() throws ConnectionException, GetException {
        StepsC = this.lazilyGetAndConnect(STEPS_HANDLE, StepsC);
        return StepsC.getValInt();
    }

    /**
     * tells the wire scanner to actually perform a scan
     */
    public void doScan() throws ConnectionException, PutException {
        BeginScanC = this.lazilyGetAndConnect(BEGIN_SCAN_HANDLE, BeginScanC);
        BeginScanC.putVal(1);
    }

    /**
     * tells the wire scanner to stop a scan
     */
    public void stopScan() throws ConnectionException, PutException {
        AbortScanC = this.lazilyGetAndConnect(ABORT_SCAN_HANDLE, AbortScanC);
        AbortScanC.putVal(1);
    }

    /**
     * get the status array []
     */
    public double[] getStatusArray() throws ConnectionException, GetException {
        StatArrayC = this.lazilyGetAndConnect(STAT_ARRAD_HANDLE, StatArrayC);
        return StatArrayC.getArrDbl();
    }

    /**
     * connect the status array []
     */
    public void connectStatArray() throws ConnectionException {
        StatArrayC = this.lazilyGetAndConnect(STAT_ARRAD_HANDLE, StatArrayC);
    }

    /**
     * get the v data array []
     */
    public double[] getVDataArray() throws ConnectionException, GetException {
        VDataArrayC = this.lazilyGetAndConnect(VDATA_ARRAD_HANDLE, VDataArrayC);
        return VDataArrayC.getArrDbl();
    }

    /**
     * connect the v data array []
     */
    public void connectVDataArray() throws ConnectionException {
        VDataArrayC = this.lazilyGetAndConnect(VDATA_ARRAD_HANDLE, VDataArrayC);
    }

    /**
     * get the d data array []
     */
    public double[] getDDataArray() throws ConnectionException, GetException {
        DDataArrayC = this.lazilyGetAndConnect(DDATA_ARRAD_HANDLE, DDataArrayC);
        return DDataArrayC.getArrDbl();
    }

    /**
     * connect the d data array []
     */
    public void connectDDataArray() throws ConnectionException {
        DDataArrayC = this.lazilyGetAndConnect(DDATA_ARRAD_HANDLE, DDataArrayC);
    }

    /**
     * get the h data array []
     */
    public double[] getHDataArray() throws ConnectionException, GetException {
        HDataArrayC = this.lazilyGetAndConnect(HDATA_ARRAD_HANDLE, HDataArrayC);
        return HDataArrayC.getArrDbl();
    }

    /**
     * connect the h data array []
     */
    public void connectHDataArray() throws ConnectionException {
        HDataArrayC = this.lazilyGetAndConnect(HDATA_ARRAD_HANDLE, HDataArrayC);
    }

    /**
     * get the position data array []
     */
    public double[] getPosArray() throws ConnectionException, GetException {
        PosArrayC = this.lazilyGetAndConnect(POS_ARRAD_HANDLE, PosArrayC);
        return PosArrayC.getArrDbl();
    }

    /**
     * connect the position data array []
     */
    public void connectPosArray() throws ConnectionException {
        PosArrayC = this.lazilyGetAndConnect(POS_ARRAD_HANDLE, PosArrayC);
    }

    /**
     * get the v fit array []
     */
    public double[] getVFitArray() throws ConnectionException, GetException {
        VFitC = this.lazilyGetAndConnect(V_FIT_HANDLE, VFitC);
        return VFitC.getArrDbl();
    }

    /**
     * connect the v fit array []
     */
    public void connectVFitArray() throws ConnectionException {
        VFitC = this.lazilyGetAndConnect(V_FIT_HANDLE, VFitC);
    }

    /**
     * get the d fit array []
     */
    public double[] getDFitArray() throws ConnectionException, GetException {
        DFitC = this.lazilyGetAndConnect(D_FIT_HANDLE, DFitC);
        return DFitC.getArrDbl();
    }

    /**
     * connect the d fit array []
     */
    public void connectDFitArray() throws ConnectionException {
        DFitC = this.lazilyGetAndConnect(D_FIT_HANDLE, DFitC);
    }

    /**
     * get the h fit array []
     */
    public double[] getHFitArray() throws ConnectionException, GetException {
        HFitC = this.lazilyGetAndConnect(H_FIT_HANDLE, HFitC);
        return HFitC.getArrDbl();
    }

    /**
     * connect the h fit array []
     */
    public void connectHFitArray() throws ConnectionException {
        HFitC = this.lazilyGetAndConnect(H_FIT_HANDLE, HFitC);
    }

    /**
     * use this to get the v area fit
     */
    public double getVAreaF() throws ConnectionException, GetException {
        VAreaFC = this.lazilyGetAndConnect(V_AREA_F_HANDLE, VAreaFC);
        return VAreaFC.getValDbl();
    }

    /**
     * use this to get the d area fit
     */
    public double getDAreaF() throws ConnectionException, GetException {
        DAreaFC = this.lazilyGetAndConnect(D_AREA_F_HANDLE, DAreaFC);
        return DAreaFC.getValDbl();
    }

    /**
     * use this to get the h area fit
     */
    public double getHAreaF() throws ConnectionException, GetException {
        HAreaFC = this.lazilyGetAndConnect(H_AREA_F_HANDLE, HAreaFC);
        return HAreaFC.getValDbl();
    }

    /**
     * use this to get the v area rms
     */
    public double getVAreaM() throws ConnectionException, GetException {
        VAreaMC = this.lazilyGetAndConnect(V_AREA_M_HANDLE, VAreaMC);
        return VAreaMC.getValDbl();
    }

    /**
     * use this to get the d area rms
     */
    public double getDAreaM() throws ConnectionException, GetException {
        DAreaMC = this.lazilyGetAndConnect(D_AREA_M_HANDLE, DAreaMC);
        return DAreaMC.getValDbl();
    }

    /**
     * use this to get the h area rms
     */
    public double getHAreaM() throws ConnectionException, GetException {
        HAreaMC = this.lazilyGetAndConnect(H_AREA_M_HANDLE, HAreaMC);
        return HAreaMC.getValDbl();
    }

    /**
     * use this to get the v sigma fit
     */
    public double getVSigmaF() throws ConnectionException, GetException {
        VSigmaFC = this.lazilyGetAndConnect(V_SIGMA_F_HANDLE, VSigmaFC);
        return VSigmaFC.getValDbl();
    }

    /**
     * use this to get the d sigma fit
     */
    public double getDSigmaF() throws ConnectionException, GetException {
        DSigmaFC = this.lazilyGetAndConnect(D_SIGMA_F_HANDLE, DSigmaFC);
        return DSigmaFC.getValDbl();
    }

    /**
     * use this to get the h sigma fit
     */
    public double getHSigmaF() throws ConnectionException, GetException {
        HSigmaFC = this.lazilyGetAndConnect(H_SIGMA_F_HANDLE, HSigmaFC);
        return HSigmaFC.getValDbl();
    }

    /**
     * use this to get the v sigma rms
     */
    public double getVSigmaM() throws ConnectionException, GetException {
        VSigmaMC = this.lazilyGetAndConnect(V_SIGMA_M_HANDLE, VSigmaMC);
        return VSigmaMC.getValDbl();
    }

    /**
     * use this to get the d sigma rms
     */
    public double getDSigmaM() throws ConnectionException, GetException {
        DSigmaMC = this.lazilyGetAndConnect(D_SIGMA_M_HANDLE, DSigmaMC);
        return DSigmaMC.getValDbl();
    }

    /**
     * use this to get the h sigma rms
     */
    public double getHSigmaM() throws ConnectionException, GetException {
        HSigmaMC = this.lazilyGetAndConnect(H_SIGMA_M_HANDLE, HSigmaMC);
        return HSigmaMC.getValDbl();
    }

    /**
     * use this to get the v amp fit
     */
    public double getVAmplF() throws ConnectionException, GetException {
        VAmpFC = this.lazilyGetAndConnect(V_AMP_F_HANDLE, VAmpFC);
        return VAmpFC.getValDbl();
    }

    /**
     * use this to get the d amp fit
     */
    public double getDAmplF() throws ConnectionException, GetException {
        DAmpFC = this.lazilyGetAndConnect(D_AMP_F_HANDLE, DAmpFC);
        return DAmpFC.getValDbl();
    }

    /**
     * use this to get the h amp fit
     */
    public double getHAmplF() throws ConnectionException, GetException {
        HAmpFC = this.lazilyGetAndConnect(H_AMP_F_HANDLE, HAmpFC);
        return HAmpFC.getValDbl();
    }

    /**
     * use this to get the v amp rms
     */
    public double getVAmplM() throws ConnectionException, GetException {
        VAmpMC = this.lazilyGetAndConnect(V_AMP_M_HANDLE, VAmpMC);
        return VAmpMC.getValDbl();
    }

    /**
     * use this to get the d amp rms
     */
    public double getDAmplM() throws ConnectionException, GetException {
        DAmpMC = this.lazilyGetAndConnect(D_AMP_M_HANDLE, DAmpMC);
        return DAmpMC.getValDbl();
    }

    /**
     * use this to get the h amp rms
     */
    public double getHAmplM() throws ConnectionException, GetException {
        HAmpMC = this.lazilyGetAndConnect(H_AMP_M_HANDLE, HAmpMC);
        return HAmpMC.getValDbl();
    }

    /**
     * use this to get the v mean fit
     */
    public double getVMeanF() throws ConnectionException, GetException {
        VMeanFC = this.lazilyGetAndConnect(V_MEAN_F_HANDLE, VMeanFC);
        return VMeanFC.getValDbl();
    }

    /**
     * use this to get the d mean fit
     */
    public double getDMeanF() throws ConnectionException, GetException {
        DMeanFC = this.lazilyGetAndConnect(D_MEAN_F_HANDLE, DMeanFC);
        return DMeanFC.getValDbl();
    }

    /**
     * use this to get the h mean fit
     */
    public double getHMeanF() throws ConnectionException, GetException {
        HMeanFC = this.lazilyGetAndConnect(H_MEAN_F_HANDLE, HMeanFC);
        return HMeanFC.getValDbl();
    }

    /**
     * use this to get the v mean rms
     */
    public double getVMeanM() throws ConnectionException, GetException {
        VMeanMC = this.lazilyGetAndConnect(V_MEAN_M_HANDLE, VMeanMC);
        return VMeanMC.getValDbl();
    }

    /**
     * use this to get the d mean rms
     */
    public double getDMeanM() throws ConnectionException, GetException {
        DMeanMC = this.lazilyGetAndConnect(D_MEAN_M_HANDLE, DMeanMC);
        return DMeanMC.getValDbl();
    }

    /**
     * use this to get the h mean rms
     */
    public double getHMeanM() throws ConnectionException, GetException {
        HMeanMC = this.lazilyGetAndConnect(H_MEAN_M_HANDLE, HMeanMC);
        return HMeanMC.getValDbl();
    }

    /**
     * use this to get the v offset fit
     */
    public double getVOffsetF() throws ConnectionException, GetException {
        VOffstFC = this.lazilyGetAndConnect(V_OFFST_F_HANDLE, VOffstFC);
        return VOffstFC.getValDbl();
    }

    /**
     * use this to get the d offset fit
     */
    public double getDOffsetF() throws ConnectionException, GetException {
        DOffstFC = this.lazilyGetAndConnect(D_OFFST_F_HANDLE, DOffstFC);
        return DOffstFC.getValDbl();
    }

    /**
     * use this to get the h offset fit
     */
    public double getHOffsetF() throws ConnectionException, GetException {
        HOffstFC = this.lazilyGetAndConnect(H_OFFST_F_HANDLE, HOffstFC);
        return HOffstFC.getValDbl();
    }

    /**
     * use this to get the v offset rms
     */
    public double getVOffsetM() throws ConnectionException, GetException {
        VOffstMC = this.lazilyGetAndConnect(V_OFFST_M_HANDLE, VOffstMC);
        return VOffstMC.getValDbl();
    }

    /**
     * use this to get the d offset rms
     */
    public double getDOffsetM() throws ConnectionException, GetException {
        DOffstMC = this.lazilyGetAndConnect(D_OFFST_M_HANDLE, DOffstMC);
        return DOffstMC.getValDbl();
    }

    /**
     * use this to get the h offset rms
     */
    public double getHOffsetM() throws ConnectionException, GetException {
        HOffstMC = this.lazilyGetAndConnect(H_OFFST_M_HANDLE, HOffstMC);
        return HOffstMC.getValDbl();
    }

    /**
     * use this to get the v slope fit
     */
    public double getVSlopeF() throws ConnectionException, GetException {
        VSlopeFC = this.lazilyGetAndConnect(V_SLOPE_F_HANDLE, VSlopeFC);
        return VSlopeFC.getValDbl();
    }

    /**
     * use this to get the d slope fit
     */
    public double getDSlopeF() throws ConnectionException, GetException {
        DSlopeFC = this.lazilyGetAndConnect(D_SLOPE_F_HANDLE, DSlopeFC);
        return DSlopeFC.getValDbl();
    }

    /**
     * use this to get the h slope fit
     */
    public double getHSlopeF() throws ConnectionException, GetException {
        HSlopeFC = this.lazilyGetAndConnect(H_SLOPE_F_HANDLE, HSlopeFC);
        return HSlopeFC.getValDbl();
    }

    /**
     * use this to get the v slope rms
     */
    public double getVSlopeM() throws ConnectionException, GetException {
        VSlopeMC = this.lazilyGetAndConnect(V_SLOPE_M_HANDLE, VSlopeMC);
        return VSlopeMC.getValDbl();
    }

    /**
     * use this to get the d slope rms
     */
    public double getDSlopeM() throws ConnectionException, GetException {
        DSlopeMC = this.lazilyGetAndConnect(D_SLOPE_M_HANDLE, DSlopeMC);
        return DSlopeMC.getValDbl();
    }

    /**
     * use this to get the h slope rms
     */
    public double getHSlopeM() throws ConnectionException, GetException {
        HSlopeMC = this.lazilyGetAndConnect(H_SLOPE_M_HANDLE, HSlopeMC);
        return HSlopeMC.getValDbl();
    }

    /**
     * connect the v real data stream
     */
    public void connectVData() throws ConnectionException {
        VRealDataC = this.lazilyGetAndConnect(V_REAL_DATA_HANDLE, VRealDataC);
    }

    /**
     * get the v real data stream
     */
    public double getVData() throws ConnectionException, GetException {
        VRealDataC = this.lazilyGetAndConnect(V_REAL_DATA_HANDLE, VRealDataC);
        return VRealDataC.getValDbl();
    }

    /**
     * connect the d real data stream
     */
    public void connectDData() throws ConnectionException {
        DRealDataC = this.lazilyGetAndConnect(D_REAL_DATA_HANDLE, DRealDataC);
    }

    /**
     * get the d real data stream
     */
    public double getDData() throws ConnectionException, GetException {
        DRealDataC = this.lazilyGetAndConnect(D_REAL_DATA_HANDLE, DRealDataC);
        return DRealDataC.getValDbl();
    }

    /**
     * connect the h real data stream
     */
    public void connectHData() throws ConnectionException {
        HRealDataC = this.lazilyGetAndConnect(H_REAL_DATA_HANDLE, HRealDataC);
    }

    /**
     * get the h real data stream
     */
    public double getHData() throws ConnectionException, GetException {
        HRealDataC = this.lazilyGetAndConnect(H_REAL_DATA_HANDLE, HRealDataC);
        return HRealDataC.getValDbl();
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
        correlator.addChannel(VAreaFC);
        correlator.addChannel(VAmpFC);
        correlator.addChannel(VMeanFC);
        correlator.addChannel(VSigmaFC);
        correlator.addChannel(VOffstFC);
        correlator.addChannel(VSlopeFC);

        correlator.addChannel(VAreaMC);
        correlator.addChannel(VAmpMC);
        correlator.addChannel(VMeanMC);
        correlator.addChannel(VSigmaMC);
        correlator.addChannel(VOffstMC);
        correlator.addChannel(VSlopeMC);

        correlator.addChannel(DAreaFC);
        correlator.addChannel(DAmpFC);
        correlator.addChannel(DMeanFC);
        correlator.addChannel(DSigmaFC);
        correlator.addChannel(DOffstFC);
        correlator.addChannel(DSlopeFC);

        correlator.addChannel(DAreaMC);
        correlator.addChannel(DAmpMC);
        correlator.addChannel(DMeanMC);
        correlator.addChannel(DSigmaMC);
        correlator.addChannel(DOffstMC);
        correlator.addChannel(DSlopeMC);

        correlator.addChannel(HAreaFC);
        correlator.addChannel(HAmpFC);
        correlator.addChannel(HMeanFC);
        correlator.addChannel(HSigmaFC);
        correlator.addChannel(HOffstFC);
        correlator.addChannel(HSlopeFC);

        correlator.addChannel(HAreaMC);
        correlator.addChannel(HAmpMC);
        correlator.addChannel(HMeanMC);
        correlator.addChannel(HSigmaMC);
        correlator.addChannel(HOffstMC);
        correlator.addChannel(HSlopeMC);

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
