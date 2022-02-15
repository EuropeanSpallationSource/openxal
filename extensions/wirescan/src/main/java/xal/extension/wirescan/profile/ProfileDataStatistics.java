/**
 * ProfileDataStatistics.java
 *
 * Created      : September, 2007
 * Author       : Christopher K. Allen
 */
package xal.extension.wirescan.profile;

/**
 * <p>
 * Computes statistical properties of profile data.</p>
 *
 * <p>
 * Define the weighted, central summation <em>S<sub>n</sub></em>(<em>&mu;</em>)
 * as
 * <br>
 * <br>&nbsp;&nbsp;<em>S<sub>n</sub></em>(<em>&mu;</em>) =
 * &Sigma;<sub><em>k</em></sub>(<em>k -
 * &mu;</em>)<sup><em>n</em></sup><em>f<sub>k</sub></em>
 * <br>
 * <br>
 * where {<em>f<sub>k</sub></em>} is the set of discrete samples for a
 * projection view (that is, the discrete function representing the projection).
 * Then the quantities provided by this class are typically some ratio of the
 * <em>S<sub>n</sub></em>(<em>&mu;</em>) for a combination of parameters
 * (<em>n,&mu;</em>). For example, the <em>n</em><sup>th</sup> central moment
 * moment, denoted &lt;(<em>x - &mu;</em>)<em><sup>n</sup></em>&gt;, is defined
 * as
 * <br>
 * <br>&nbsp;&nbsp; &lt;(<em>x - &mu;</em>)<em><sup>n</sup></em>&gt; =
 * <em>S<sub>n</sub></em>(<em>&mu;</em>)/<em>S</em><sub>0</sub>(0)
 * <br>
 * <br>
 * Note that the units of the moments will be in terms of
 * <strong>samples</strong> (see below). For example, the value returned by the
 * method <code>getCenter(ProfileData.Angle)</code> is the index location of the
 * center of mass. Note further that although the indices are integer valued,
 * the statistic values in units of <strong>samples</strong>
 * need not be.
 * </p>
 *
 * <p>
 * NOTE:
 * <br>- The quantities provided by this class are for discrete system. When
 * considering these systems as approximations for continuous systems (sampled
 * data systems) then we must "unnormalized" the results by the sampling
 * interval <em>h</em>. For example, to convert <(<em>x -
 * &mu;</em>)<em><sup>n</sup></em>> to the continuous approximation the value
 * must be multiplied by <em>h<sup>n</sup></em>.
 * <br>- The above conversions are <strong>not</strong> provided by methods in
 * this class. There are, however, methods for computing the sampling length
 * <em>h</em> for the various axes and actuator positions. The rationale for not
 * providing this service at present is the profile data sets might not contain
 * the axis positions, as these are version-sensitive data. Thus, such methods
 * have the potential for returning erroneous results.
 * <br>- Higher-order moments are highly sensitive to signal noise.
 * <br>- Strongly peaked distributions create numerically unstable computations.
 * In general, for accurate numerical results the distribution should have a
 * standard deviation <em>&sigma;</em> &gt; 2. Standard deviations smaller than
 * this value imply that the sampling interval is too large.
 * </p>
 *
 * @author Christopher K. Allen
 *
 */
public class ProfileDataStatistics {

    /*
     * Local Attributes
     */
    /**
     * profile data to analyze
     */
    private ProfileData pdoData = null;

    /**
     * zero-order moments (i.e., the total mass)
     */
    private double[] arrMass;

    /**
     * first-order moments (i.e., the centers)
     */
    private double[] arrCentr;

    /**
     * Create a new <code>ProfileDataStatistics</code> object and attach it to
     * the given profile data set.
     */
    public ProfileDataStatistics(final ProfileData pdoData) {
        this.pdoData = pdoData;

        this.initialize();
    }

    /*
     * Operations
     */
    /**
     * <p>
     * Return the total mass (i.e., the first integral, or zeroth moment) of the
     * given projection.
     * </p>
     *
     * <p>
     * NOTE: <br>
     * - The returned value is for a discrete function and must be treated as a
     * "normalized" quantity when considering sampled data from a continuous
     * system. To convert to the continuous approximation the returned value
     * must be multiplied by <em>h</em> where <em>h</em> is the sampling
     * interval.
     * </p>
     *
     * @param view projection data
     * @return the value <em>S<sub>0</sub></em>(<em>0</em>)
     */
    public double getMass(ProfileData.Angle view) {
        return this.arrMass[view.getIndex()];
    }

    /**
     * <p>
     * Return the (normalized) center of mass (i.e., the first moment) for the
     * given projection.
     * </p>
     *
     * <p>
     * NOTE:
     * <br>- The returned value is for a discrete function and must be treated
     * as a "normalized" quantity when considering sampled data from a
     * continuous system. To convert to the continuous approximation for
     * <<em>x</em>> the returned value must be multiplied by <em>h</em> where
     * <em>h</em> is the sampling interval.
     * <br>- To convert to the center of the projection axis indicated by
     * <var>view</var>, the returned value must be multiply by <em>h</em> then
     * offset by <em>x</em><sub>0</sub>, the left-hand axis end-point.
     * </p>
     *
     * @param view projection data
     *
     * @return the value
     * <em>S<sub>1</sub></em>(<em>0</em>)/<em>S<sub>0</sub></em>(<em>0</em>) in
     */
    public double getCenter(ProfileData.Angle view) {
        return this.arrCentr[view.getIndex()];
    }

    /**
     * Return the initial actuator position, that is the actuator location
     * <em>x</em><sub>0</sub> of the first sample.
     *
     * @return initial position of the actuator
     */
    public double getActuatorOffset() {
        return this.pdoData.getActuatorPositionAt(0);
    }

    /**
     * Return the initial axis position for the given projection angle, that is
     * the axis location <em>x</em><sub>0</sub> of the first sample.
     *
     * @return initial axis position for the given viewing angle
     */
    public double getAxisOffset(ProfileData.Angle view) {
        return this.pdoData.getAxisPositionAt(view, 0);
    }

    /**
     * <p>
     * Compute and return the average step size between actuator positions for
     * the entire sample projection sample set. This value is given by
     * <br>
     * <br>&nbsp;&nbsp; (<em>x<sub>N-</em>1</sub> -
     * <em>x</em><sub>0</sub>)/(<em>N</em> - 1)
     * <br>
     * <br>
     * where <em>N</em> is the number of sample points and
     * {<em>x<sub>k</sub></em>} are the actuator positions.
     * </p>
     * <p>
     * NOTE:
     * <br> - The above result is the same as computing the steps size
     * <em>h<sub>k</sub></em> = <em>x<sub>k</sub></em> -
     * <em>x<sub>k-</em>1</sub>
     * between each sample then averaging the set.
     *
     * @return average step length between actuator positions
     */
    public double compAveActuatorStepSize() {
        double[] arrPos = this.pdoData.getActuatorPositions();
        int N = arrPos.length;
        double x1 = arrPos[N - 1];
        double x0 = arrPos[0];

        return (x1 - x0) / (N - 1.0);
    }

    /**
     * <p>
     * Compute and return the average step size between axis sample positions
     * for the given projection view. This value is given by
     * <br>
     * <br>&nbsp;&nbsp; (<em>x<sub>N-</em>1</sub> -
     * <em>x</em><sub>0</sub>)/(<em>N</em> - 1)
     * <br>
     * <br>
     * where <em>N</em> is the number of sample points and
     * {<em>x<sub>k</sub></em>} are the axis positions.
     * </p>
     * <p>
     * NOTE:
     * <br> - The above result is the same as computing the steps size
     * <em>h<sub>k</sub></em> = <em>x<sub>k</sub></em> -
     * <em>x<sub>k-</em>1</sub>
     * between each sample then averaging the set.
     *
     * @return average step length between axis sample positions
     */
    public double compAveAxisStepSize(ProfileData.Angle view) {
        double[] arrPos = pdoData.getAxisPositions(view);
        int n = arrPos.length;
        double x1 = arrPos[n - 1];
        double x0 = arrPos[0];

        return (x1 - x0) / (n - 1.0);
    }

    /**
     * <p>
     * Return the (normalized) standard deviation <em>&sigma;</em> of the given
     * projection. The standard deviation is defined as
     * <br>
     * <br>&nbsp;&nbsp; <em>&sigma;</em> = &lt;(<em>x -
     * &mu;</em>)<sup>2</sup>&gt;<sup>&frac12;</sup>
     * <br>
     * <br>
     * </p>
     * <p>
     * NOTE: <br>
     * - The returned value is for a discrete function and must be treated as a
     * "normalized" quantity when considering sampled data from a continuous
     * system. To convert to the continuous approximation for <em>&sigma;</em>
     * the returned value must be multiplied by <em>h</em> where <em>h</em> is
     * the sampling interval.
     * <br>
     * - Because of signal noise and numerical rounding it is possible to
     * compute a value for &lt;(<em>x - &mu;</em>)<sup>2</sup>&gt; which is less
     * than zero. Such an occurrence indicates either that sampling interval
     * <em>h</em> is too large, or that the signal-to-noise ratio is too small
     * to permit meaningful results.  <strong>A zero value is returned</strong>
     * in this situation.
     * </p>
     *
     * @param view project data to analyze
     *
     * @return standard deviation of given view, or zero if undefined
     */
    public double compStdDev(ProfileData.Angle view) {
        double std = 0.0;

        double mm2 = computeMoment(2, view);
        double mu = getCenter(view);

        double sig2 = mm2 - mu * mu;

        if (sig2 > 0.0) {
            std = Math.sqrt(sig2);
        }

        return std;
    }

    /**
     * <p>
     * Compute and return the indicated moment for the given projection data.
     * The value of the returned moment <<em>x<sup>n</sup></em>> is defined as
     * <br>
     * <br>&nbsp;&nbsp; <<em>x<sup>n</sup></em>> =
     * <em>S<sub>n</sub></em>(0)/<em>S<sub>0</sub></em>(0)
     * <br>
     * </p>
     * <p>
     * NOTE: <br>
     * - The returned value is for a discrete function and must be treated as a
     * "normalized" quantity when considering sampled data from a continuous
     * system. To convert to the continuous approximation for
     * <<em>x<sup>n</sup></em>> the returned value must be multiplied by
     * <em>h<sup>n</sup></em> where <em>h</em> is the sampling interval.
     * </p>
     *
     * @param intOrder moment order
     * @param view project data to analyze
     *
     * @return moment of given view
     */
    public double computeMoment(int intOrder, ProfileData.Angle view) {
        double mass = getMass(view);
        double sum = computeWeightedSum(intOrder, 0.0, view);

        return sum / mass;
    }

    /**
     * <p>
     * Compute and return the indicated central moment for the given projection
     * data. The value of the returned moment <(<em>x -
     * &mu;</em>)<em><sup>n</sup></em>> is defined as
     * <br>
     * <br>&nbsp;&nbsp; <(<em>x - &mu;</em>)<em><sup>n</sup></em>> =
     * <em>S<sub>n</sub></em>(<em>&mu;</em>)/<em>S<sub>0</sub></em>(0)
     * <br>
     * </p>
     * <p>
     * NOTE: <br>
     * - The returned value is for a discrete function and must be treated as a
     * "normalized" quantity when considering sampled data from a continuous
     * system. To convert to the continuous approximation for <(<em>x -
     * &mu;</em>)<em><sup>n</sup></em>> the returned value must be multiplied by
     * <em>h<sup>n</sup></em> where <em>h</em> is the sampling interval.
     * </p>
     *
     * @param intOrder moment order
     * @param view project data to analyze
     *
     * @return moment of given view
     */
    public double computeCentralMoment(int intOrder, ProfileData.Angle view) {
        double mass = this.getMass(view);
        double cntr = this.getCenter(view);
        double sum = this.computeWeightedSum(intOrder, cntr, view);

        return sum / mass;
    }

    /**
     * <p>
     * Compute and return the weighted central summation
     * <em>S<sub>n</sub></em>(<em>&mu;</em>) which is defined
     * <br>
     * <br>&nbsp;&nbsp;<em>S<sub>n</sub></em>(<em>&mu;</em>) =
     * &Sigma;<sub><em>k</em></sub>(<em>k -
     * &mu;</em>)<sup><em>n</em></sup><em>f<sub>k</sub></em>
     * <br>
     * </p>
     *
     * @param intOrder order of the summation weight (i.e., <em>n</em>)
     * @param dblCntr center of the summation weight (i.e., <em>&mu;</em>)
     * @param view projection view angle (i.e., the set
     * {<em>f<sub>k</sub></em>})
     *
     * @return the value <em>S<sub>n</sub></em>(<em>&mu;</em>) as defined above
     */
    public double computeWeightedSum(int intOrder, double dblCntr, ProfileData.Angle view) {
        // Get the projection data
        double[] arrPrj = this.getProjection(view);
        int n = arrPrj.length;

        // Initialize and begin summation loop
        double sum = 0.0;
        for (int k = 0; k < n; k++) {
            double wgt = this.exponentiate(k - dblCntr, intOrder);
            double smp = arrPrj[k];

            sum += wgt * smp;
        }

        return sum;
    }

    /*
     *  Support Methods 
     */
    /**
     * Returns the projection data for the given viewing angle.
     *
     * @param view viewing angle
     *
     * @return projection data for given viewing angle
     */
    private double[] getProjection(ProfileData.Angle view) {
        return this.pdoData.getProjection(view);
    }

    /**
     * Initialize the local variables for the given projection data set.
     * Specifically, compute the mass and center for each projection in the set.
     */
    private void initialize() {

        // Initial the projection masses and centers
        this.arrMass = new double[ProfileData.Angle.getCount()];
        this.arrCentr = new double[ProfileData.Angle.getCount()];

        for (ProfileData.Angle view : ProfileData.Angle.values()) {
            double dblS0 = this.computeWeightedSum(0, 0.0, view);
            double dblS1 = this.computeWeightedSum(1, 0.0, view);

            this.arrMass[view.getIndex()] = dblS0;
            this.arrCentr[view.getIndex()] = dblS1 / dblS0;
        }
    }

    /**
     * <p>
     * Exponentiates the given base by the given <em>integer</em> exponent.</p>
     *
     * <p>
     * NOTE: <br>
     * This method should be faster than <code>Math.pow()</code> since the
     * exponent is restricted to integer values.
     * <p>
     *
     * @param dblBase value to be exponentiated
     * @param intExp integer exponent
     *
     * @return the value <em>dblBase<sup>intExp</sup></em>
     */
    private double exponentiate(double dblBase, int intExp) {

        double dblProd = 1.0;
        for (int k = 0; k < intExp; k++) {
            dblProd *= dblBase;
        }

        return dblProd;
    }
}
