/*
 * PhaseIndex.java
 * 
 * Created December, 2006
 * 
 * Christopher K. Allen
 */
package xal.tools.beam;

import java.util.EnumSet;
import java.util.Set;

import xal.tools.math.IIndex;

/**
 * Enumeration for the element position indices of six-dimensional phase space
 * object.
 *
 * @author Christopher K. Allen
 * @version Nov 12, 2013
 */
public enum PhaseIndex implements IIndex {

    /*
     * Enumeration Constants
     */
    // x plane spatial
    X(0, 1),
    // x plane momentum
    XP(1, 0),
    // y plane spatial
    Y(2, 3),
    // y plane momentum
    YP(3, 2),
    // z plane spatial
    Z(4, 5),
    // z plane momentum
    ZP(5, 4);

    /*
     * Global Methods
     */
    /**
     * Return the set of indices corresponding to spatial coordinates.
     *
     * @return set of spatial indices
     */
    public static Set<PhaseIndex> spatialIndices() {
        return EnumSet.of(X, Y, Z);
    }

    /**
     * Return the set of indices corresponding to momentum coordinates.
     *
     * @return set of momentum indices
     */
    public static Set<PhaseIndex> momentumIndices() {
        return EnumSet.of(XP, YP, ZP);
    }

    /*
     * Local Attributes
     */
    /**
     * index value
     */
    private final int iVal;

    /**
     * index value of conjugate coordinate
     */
    private final int iConj;

    /**
     * Default enumeration constructor
     */
    PhaseIndex(int iVal, int iConj) {
        this.iVal = iVal;
        this.iConj = iConj;
    }

    /*
     * Public Methods
     */
    /**
     * Return the integer value of the index position
     *
     * @return value of this index
     */
    @Override
    public int val() {
        return this.iVal;
    }

    /**
     * Return the conjugate variable index to this.
     *
     * NOTE: - This function is highly under-optimized.
     *
     * @return conjugate variable index
     */
    public PhaseIndex conjugate() {
        for (PhaseIndex i : values()) {
            if (i.val() == this.iConj) {
                return i;
            }
        }

        return this;
    }
}
