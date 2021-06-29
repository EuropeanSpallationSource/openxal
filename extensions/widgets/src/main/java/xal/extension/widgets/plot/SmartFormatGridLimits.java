package xal.extension.widgets.plot;

import java.text.*;

/**
 * This is subclass of grid limits class (GridLimits) with non-empty
 * setSmartLimitsX() and setSmartLimitsY() methods. These methods will redefine
 * format to beautify the axes markers
 *
 * @author shishlo
 * @version 1.0
 */
public class SmartFormatGridLimits extends GridLimits {

    /**
     * The array with scale on x, xMin and xMax
     */
    protected double[] xSR = new double[3];

    /**
     * The scale on x, xMin and xMax are defined
     */
    protected boolean sucessX = false;

    /**
     * The array with scale on y, yMin and yMax
     */
    protected double[] ySR = new double[3];

    /**
     * The scale on y, yMin and yMax are defined
     */
    protected boolean sucessY = false;

    /**
     * The simple formats
     */
    protected static NumberFormat[] simpleFormats = new NumberFormat[5];

    /**
     * The scientific formats
     */
    protected static NumberFormat[] scientificFormats = new NumberFormat[5];

    /**
     * The universal format
     */
    protected static NumberFormat univFormat = new DecimalFormat("#.###E0");

    static {
        simpleFormats[0] = new DecimalFormat("###0");
        simpleFormats[1] = new DecimalFormat("###0.#");
        simpleFormats[2] = new DecimalFormat("###0.##");
        simpleFormats[3] = new DecimalFormat("###0.###");
        simpleFormats[4] = new DecimalFormat("###0.####");

        scientificFormats[0] = new DecimalFormat("#.E0");
        scientificFormats[1] = new DecimalFormat("#.#E0");
        scientificFormats[2] = new DecimalFormat("#.##E0");
        scientificFormats[3] = new DecimalFormat("#.###E0");
        scientificFormats[4] = new DecimalFormat("#.####E0");

    }

    /**
     * Constructor for the SmartFormatGridLimits object
     */
    public SmartFormatGridLimits() {
        super();
    }

    /**
     * Sets the limits by using smart procedure for x-axis
     */
    @Override
    public void setSmartLimitsX() {
        calculateScalesAndLimitsX();
        if (!sucessX) {
            return;
        }
        //we want extra digit, because it is for zoom
        setNumberFormatX(getSmartFormat(xSR, 1));
    }

    /**
     * Sets the limits by using smart procedure for x-axis
     */
    @Override
    public void setSmartLimitsY() {
        calculateScalesAndLimitsY();
        if (!sucessY) {
            return;
        }
        //we want extra digit, because it is for zoom
        setNumberFormatY(getSmartFormat(ySR, 1));
    }

    /**
     * Calculates smart limits for the X axis
     */
    protected void calculateScalesAndLimitsX() {
        sucessX = false;
        double vMin = getMinX();
        double vMax = getMaxX();
        double range = vMax - vMin;
        double scale = 0.;
        if (range > 0.) {
            scale = Math.pow(10., Math.floor(1.000001 * Math.log(range) / Math.log(10.0)));
        }
        xSR[0] = scale;
        if (scale == 0.) {
            xSR[1] = vMin;
            xSR[2] = vMax;
            return;
        }
        xSR[1] = scale * Math.floor(vMin / scale);
        xSR[2] = scale * Math.ceil(vMax / scale);
        if (xSR[1] * xSR[2] == 0. && (scale == Math.abs(xSR[2]) || scale == Math.abs(xSR[1]))) {
            scale = scale / 5.0;
            xSR[0] = scale;
            xSR[1] = scale * Math.floor(vMin / scale);
            xSR[2] = scale * Math.ceil(vMax / scale);
        }
        sucessX = true;
        //System.out.println( "debug smGL X min,max=" + vMin + " "
        //    + vMax + " scale=" + scale + " min,max=" + xSR[1] + " " + xSR[2] );
    }

    /**
     * Calculates smart limits for the Y axis
     */
    protected void calculateScalesAndLimitsY() {
        sucessY = false;
        double vMin = getMinY();
        double vMax = getMaxY();
        double range = vMax - vMin;
        double scale = 0.;
        if (range > 0.) {
            scale = Math.pow(10., Math.floor(1.000001 * Math.log(range) / Math.log(10.0)));
        }
        ySR[0] = scale;
        if (scale == 0.) {
            ySR[1] = vMin;
            ySR[2] = vMax;
            return;
        }
        ySR[1] = scale * Math.floor(vMin / scale);
        ySR[2] = scale * Math.ceil(vMax / scale);
        if (ySR[1] * ySR[2] == 0. && (scale == Math.abs(ySR[2]) || scale == Math.abs(ySR[1]))) {
            scale = scale / 5.0;
            ySR[0] = scale;
            ySR[1] = scale * Math.floor(vMin / scale);
            ySR[2] = scale * Math.ceil(vMax / scale);
        }
        sucessY = true;
        //System.out.println( "debug smGL Y min,max=" + vMin + " "
        //     + vMax + " scale=" + scale + " min,max=" + ySR[1] + " " + ySR[2] );
    }

    /**
     * Returns the new format that will be suitable for given limits
     *
     * @param arr The array with scale, vMin, vMax
     * @param nExtraDigits The additional number of digits in mantissa
     * @return The format
     */
    protected static NumberFormat getSmartFormat(double[] arr, int nExtraDigits) {

        NumberFormat frmt = univFormat;

        double vMax = Math.max(Math.abs(arr[1]), Math.abs(arr[2]));
        int nV = (int) (Math.floor(1.0001 * Math.log(vMax) / Math.log(10.0)));
        if (nV >= 0) {
            nV += 1;
        } else {
            nV -= 1;
        }

        vMax = vMax / Math.abs(arr[0]);
        int nD = (int) (Math.floor(1.0001 * Math.log(vMax) / Math.log(10.0)));
        if (nD >= 0) {
            nD += 1;
        }

        //This is for zoom, so we want to increase number of significant digits
        nD = nD + 1 + nExtraDigits;

        //System.out.println( "debug format scale=" + arr[0] +
        //   " min,max=" + arr[1] + " " + arr[2] + " nV,nD=" + nV + " " + nD );
        if (nV >= 4) {
            int n = Math.min(4, Math.abs(nD));
            frmt = scientificFormats[n];
            //System.out.println( "debug case 0 n=" + n + " format=" + frmt.toPattern() );
            return frmt;
        }

        if (nV > 0 && nV < 4) {
            if (nV >= nD) {
                frmt = simpleFormats[0];
                //System.out.println( "debug case 1 format=" + frmt.toPattern() );
                return frmt;
            } else {
                int n = Math.min(4, Math.abs(nV - nD));
                frmt = simpleFormats[n];
                //System.out.println( "debug case 2 n=" + n + " format=" + frmt.toPattern() );
                return frmt;
            }
        }

        if (nV < 0 && nV > -4) {
            int n = Math.abs(nV) + Math.abs(nD) - 2;
            if (n <= 4) {
                frmt = simpleFormats[n];
                //System.out.println( "debug case 3 n=" + n + " format=" + frmt.toPattern() );
                return frmt;
            }
        }

        //System.out.println( "debug default case format=" + frmt.toPattern() );
        return frmt;
    }

}
