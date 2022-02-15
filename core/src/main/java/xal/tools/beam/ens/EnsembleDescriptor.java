/*
 * BeamDescriptor.java
 *
 * Created on November 12, 2002, 5:58 PM
 */
package xal.tools.beam.ens;

/**
 *
 * @author CKAllen
 */
public final class EnsembleDescriptor {

    /*
     *  Enumeration of Supported Distributions
     */
    /**
     * No distribution profile specified - usually indicates error condition
     */
    public static final int DIST_NONE = 0;

    /**
     * Kapchinskij-Vladimirskij (or canonical) distribution - uniformly
     * distributed on phase-space surface
     */
    public static final int DIST_KV = 1;

    /**
     * Waterbag distribution - uniform in 6D phase space
     */
    public static final int DIST_WATERBAG = 2;

    /**
     * Parabolic distribution - parabolic in 6D phase space
     */
    public static final int DIST_PARABOLIC = 3;

    /**
     * Semi-Gaussian distribution - uniform in 3D configuration, Gaussian in
     * momentum w/ 3 std cutoff
     */
    public static final int DIST_SEMIGAUSSIAN_3 = 4;

    /**
     * Semi-Gaussian distribution - uniform in 3D configuration, Gaussian in
     * momentum w/ 4 std cutoff
     */
    public static final int DIST_SEMIGAUSSIAN_4 = 5;

    /**
     * Gaussian distribution - Gaussian in 6D phase space w/ 3 standard
     * deviations cutoff
     */
    public static final int DIST_GAUSSIAN_3 = 6;

    /**
     * Gaussian distribution - Gaussian in 6D phase space w/ 3 standard
     * deviations cutoff
     */
    public static final int DIST_GAUSSIAN_4 = 7;

    /*
     *  Public Attributes
     */
    /**
     * statistical distribution of particle phase coordinates in ensemble
     */
    private int enmProfile = DIST_NONE;

    /**
     * number of particles in ensemble
     */
    private int nCnt = 0;

    /**
     * Twiss alpha parameter in x plane
     */
    private double ax = 0.0;

    /**
     * Twiss beta parameter in x plane
     */
    private double bx = 0.0;

    /**
     * beam rms emittance in x plane
     */
    private double ex = 0.0;

    /**
     * Twiss alpha parameter in y plane
     */
    private double ay = 0.0;

    /**
     * Twiss beta parameter in y plane
     */
    private double by = 0.0;

    /**
     * beam rms emittance in y plane
     */
    private double ey = 0.0;

    /**
     * Twiss alpha parameter in z plane
     */
    private double az = 0.0;

    /**
     * Twiss beta parameter in z plane
     */
    private double bz = 0.0;

    /**
     * beam rms emittance in z plane
     */
    private double ez = 0.0;

    public int getEnmProfile() {
        return enmProfile;
    }

    public void setEnmProfile(int enmProfile) {
        this.enmProfile = enmProfile;
    }

    public int getnCnt() {
        return nCnt;
    }

    public void setnCnt(int nCnt) {
        this.nCnt = nCnt;
    }

    public double getAx() {
        return ax;
    }

    public void setAx(double ax) {
        this.ax = ax;
    }

    public double getBx() {
        return bx;
    }

    public void setBx(double bx) {
        this.bx = bx;
    }

    public double getEx() {
        return ex;
    }

    public void setEx(double ex) {
        this.ex = ex;
    }

    public double getAy() {
        return ay;
    }

    public void setAy(double ay) {
        this.ay = ay;
    }

    public double getBy() {
        return by;
    }

    public void setBy(double by) {
        this.by = by;
    }

    public double getEy() {
        return ey;
    }

    public void setEy(double ey) {
        this.ey = ey;
    }

    public double getAz() {
        return az;
    }

    public void setAz(double az) {
        this.az = az;
    }

    public double getBz() {
        return bz;
    }

    public void setBz(double bz) {
        this.bz = bz;
    }

    public double getEz() {
        return ez;
    }

    public void setEz(double ez) {
        this.ez = ez;
    }

}
