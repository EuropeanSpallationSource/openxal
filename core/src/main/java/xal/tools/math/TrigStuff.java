/*
 * Unwrap.java
 *
 *  Created on January 12,2005
 *  jdg: stolen from Andrei's GraphDataOperationms class.
 *   This is usful lots of places.
 */
package xal.tools.math;

/**
 * general purpose trig stuff not found elsewhere
 */
public class TrigStuff {

    private TrigStuff() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * this method shifts the first argument by a multiple of 2*PI to produce
     * the nearest point to another specified point
     *
     * @param y = input number
     * @param yIn = number to get the input close to (within 2pi)
     */
    public static double unwrap(double y, double yIn) {
        if (y == yIn) {
            return y;
        }
        int n = 0;
        double diff = yIn - y;
        double diffMin = Math.abs(diff);
        double sign = diff / diffMin;
        int nCurr = n + 1;
        double diffMinCurr = Math.abs(y + sign * nCurr * 360. - yIn);
        while (diffMinCurr < diffMin) {
            n = nCurr;
            diffMin = Math.abs(y + sign * n * 360. - yIn);
            nCurr++;
            diffMinCurr = Math.abs(y + sign * nCurr * 360. - yIn);
        }
        return (y + sign * n * 360.);
    }
}
