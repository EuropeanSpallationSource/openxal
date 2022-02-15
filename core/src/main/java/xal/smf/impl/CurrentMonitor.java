package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.impl.qualify.*;

/**
 * The implementation of the Current Monitor class. This class contains the
 * methods members, attributes, and signal sets pertinent to modeling Current
 * Monitors.
 *
 * @author J. Galambos (jdg@ornl.gov)
 */
public class CurrentMonitor extends AcceleratorNode {

    /**
     * standard type for instances of this class
     */
    public static final String TYPE = "BCM";

    static {
        registerType();
    }


    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(CurrentMonitor.class, TYPE);
    }

    /**
     * Override to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Primary Constructor
     */
    public CurrentMonitor(final String strId, final ChannelFactory channeFactory) {
        super(strId, channeFactory);
    }

    /**
     * Constructor
     */
    public CurrentMonitor(final String strId) {
        this(strId, null);
    }

    public static final String Q_INTEGRAL_HANDLE = "Particles";
    public final AccessibleProperty particles = new AccessibleProperty(Q_INTEGRAL_HANDLE);
    private Channel qIntegralC = null;

    public static final String T_AVG_LEN_HANDLE = "DisplayLength";
    public final AccessibleProperty displayLength = new AccessibleProperty(T_AVG_LEN_HANDLE);
    private Channel tAvgLenC = null;

    public static final String I_TBT_HANDLE = "currentTBT";
    public final AccessibleProperty currentTBT = new AccessibleProperty(I_TBT_HANDLE);
    private Channel iTBTC = null;

    public static final String T_DELAY_HANDLE = "tDelay";
    public final AccessibleProperty tDelay = new AccessibleProperty(T_DELAY_HANDLE);
    private Channel tDelayC = null;

    public static final String I_AVG_HANDLE = "currentAvg";
    public final AccessibleProperty currentAvg = new AccessibleProperty(I_AVG_HANDLE);
    private Channel iAvgC = null;

    public static final String I_MAX_HANDLE = "currentMax";
    public final AccessibleProperty currentMax = new AccessibleProperty(I_MAX_HANDLE);
    private Channel iMaxC = null;

    /**
     * Integrated current over macropulse
     */
    public double getQIntegral() throws GetException {
        qIntegralC = this.lazilyGetAndConnect(Q_INTEGRAL_HANDLE, qIntegralC);
        return qIntegralC.getValDbl();
    }

    /**
     * Averaged pulse length
     */
    public double getTAvgLen() throws GetException {
        tAvgLenC = this.lazilyGetAndConnect(T_AVG_LEN_HANDLE, tAvgLenC);
        return tAvgLenC.getValDbl();
    }

    /**
     * Turn by turn current
     */
    public double[] getITBT() throws GetException {
        iTBTC = this.lazilyGetAndConnect(I_TBT_HANDLE, iTBTC);
        return iTBTC.getArrDbl();
    }

    /**
     * Get the portion of ITBT array with beam on only. This method is useful
     * for rf cavity beam loading calculation
     *
     * @return part of the beam current array with non-zero beam only
     * @throws ConnectionException
     * @throws GetException
     */
    public double[] getITBTWithBeamOnly() throws GetException {
        double[] fullArray = getITBT();
        double iMax = -100.;
        for (int i = 0; i < fullArray.length; i++) {
            if (fullArray[i] > iMax) {
                iMax = fullArray[i];
            }
        }
        double[] beamArray;
        // do calculation only if > 1.mA of peak current
        if (iMax > 1.) {
            int start;
            int end;
            int counter = 0;
            while (fullArray[counter] < iMax / 10.) {
                counter++;
            }
            start = counter;
            while (fullArray[counter] > iMax / 5.) {
                counter++;
            }
            end = counter;
            if (start != end) {
                beamArray = new double[end - start];
                System.arraycopy(fullArray, start, beamArray, 0, end - start);
                // if cannot find obvious beam, set this array to size 1, and value=0.
            } else {
                beamArray = new double[1];
            }
            // if < 1mA, just return 0.
        } else {
            beamArray = new double[1];
        }

        return beamArray;
    }

    /**
     * Time delay
     */
    public double getTDelay() throws GetException {
        tDelayC = this.lazilyGetAndConnect(T_DELAY_HANDLE, tDelayC);
        return tDelayC.getValDbl();
    }

    /**
     * Average beam current
     */
    public double getIAvg() throws GetException {
        iAvgC = this.lazilyGetAndConnect(I_AVG_HANDLE, iAvgC);
        return iAvgC.getValDbl();
    }

    /**
     * Maximum beam current
     */
    public double getIMax() throws GetException {
        iMaxC = this.lazilyGetAndConnect(I_MAX_HANDLE, iMaxC);
        return iMaxC.getValDbl();
    }
}
