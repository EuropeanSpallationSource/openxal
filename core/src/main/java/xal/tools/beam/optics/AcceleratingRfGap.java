/**
 * AcceleratingRfGap.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 1, 2015
 */
package xal.tools.beam.optics;

import xal.tools.math.Complex;
import xal.tools.beam.EnergyVector;
import xal.tools.beam.em.AxialFieldSpectrum;

/**
 * Class for modeling an accelerating RF gap as a thin lens.
 *
 *
 * @author Christopher K. Allen
 * @since Oct 1, 2015
 */
public class AcceleratingRfGap {

    /*
     * Global Constants
     */
    /**
     * the value of 2&pi;
     */
    private static final double DBL_2PI = 2.0 * Math.PI;

    /**
     * Speed of light in a vacuum (meters/second)
     */
    private static final double DBL_LGHT_SPD = 299792458.0;

    /**
     * default error tolerance when searching for consistent gain parameters
     */
    private static final double DBL_ERR_TOL = 1.0e-16;

    /**
     * maximum number of allowable iterations when searching for consistent gain
     * parameters
     */
    private static final int CNT_MAX_ITER = 100;

    /*
     * Internal Classes
     */
    /**
     * Enumeration specifying the upstream location or downstream locations for
     * the RF accelerating gap.
     *
     *
     * @author Christopher K. Allen
     * @since Nov 2, 2015
     */
    public enum LOC {

        /**
         * Constant identifying the pre-gap (upstream) location
         */
        PREGAP,
        /**
         * Constant identifying the post-gap (downstream) location
         */
        POSTGAP;

        /*
         * Operations
         */
        /**
         * Compute and return the value of the generalized Hamiltonian function
         * <em>H</em> at the given longitudinal phase coordinates for the given
         * accelerating RF gap.
         *
         * @param gap Hamiltonian function for this gap
         * @param phi particle phase location &phi; (radians)
         * @param k particle wave number <em>k</em> (radians/meter)
         *
         * @return Hamiltonian function <em>H</em>(&phi;,<em>k</em>)
         *
         * @see AcceleratingRfGap#preGapHamiltonain(double, double)
         * @see AcceleratingRfGap#postGapHamiltonain(double, double)
         *
         * @since Nov 3, 2015, Christopher K. Allen
         */
        public Complex gapHamiltonian(AcceleratingRfGap gap, double phi, double k) {
            if (this.equals(PREGAP)) {
                return gap.preGapHamiltonain(phi, k);
            } else {
                return gap.postGapHamiltonain(phi, k);
            }
        }

        /**
         * Compute and return the derivative of the generalized Hamiltonian
         * function
         * <em>dH</em>/<em>d</em>&phi; with respect to particle phase location
         * &phi; for the given accelerating RF gap.
         *
         * @param gap gap for which the parameters are computed
         * @param phi particle phase location &phi; (radians)
         * @param k particle wave number <em>k</em> (radians/meter)
         *
         * @return value of derivative
         * <em>dH</em>(&phi;,<em>k</em>)/<em>d</em>&phi;
         *
         * @see AcceleratingRfGap#dphiPreGapHamiltonian(double, double)
         * @see AcceleratingRfGap#dphiPostGapHamiltonian(double, double)
         *
         * @since Nov 3, 2015, Christopher K. Allen
         */
        public Complex dphiGapHamiltonian(AcceleratingRfGap gap, double phi, double k) {
            if (this.equals(PREGAP)) {
                return gap.dphiPreGapHamiltonian(phi, k);
            } else {
                return gap.dphiPostGapHamiltonian(phi, k);
            }
        }

        /**
         * Compute and return the derivative of the generalized Hamiltonian
         * function
         * <em>dH</em>/<em>dk</em> with respect to particle wave number
         * <em>k</em>
         * for the given accelerating RF gap.
         *
         * @param gap gap for which the parameters are computed
         * @param phi particle phase location &phi; (radians)
         * @param k particle wave number <em>k</em> (radians/meter)
         *
         * @return value of derivative <em>dH</em>(&phi;,<em>k</em>)/<em>dk</em>
         *
         * @see AcceleratingRfGap#dkPreGapHamiltonian(double, double)
         * @see AcceleratingRfGap#dkPostGapHamiltonian(double, double)
         *
         * @since Nov 3, 2015, Christopher K. Allen
         */
        public Complex dkGapHamiltonian(AcceleratingRfGap gap, double phi, double k) {
            if (this.equals(PREGAP)) {
                return gap.dkPreGapHamiltonian(phi, k);
            } else {
                return gap.dkPostGapHamiltonian(phi, k);
            }
        }

    }

    /**
     * Class <code>NoConvergenceException</code>. This exception class is thrown
     * in the case of a search algorithm that does not converge.
     *
     *
     * @author Christopher K. Allen
     * @since Oct 15, 2015
     */
    public static class NoConvergenceException extends RuntimeException {

        /*
         * Global Constants
         */
        /**
         * Java serialization version
         */
        private static final long serialVersionUID = 1L;

        /**
         * Zero argument constructor for NoConvergenceException.
         *
         * @since Oct 15, 2015, Christopher K. Allen
         */
        public NoConvergenceException() {
            super();
        }

        /**
         * Initializing constructor for NoConvergenceException.
         *
         * @param message string message describing exception and/or cause
         * @param cause source of this exception an exception chain
         *
         * @since Oct 15, 2015, Christopher K. Allen
         */
        public NoConvergenceException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Initializing constructor for NoConvergenceException.
         *
         * @param message string message describing exception and/or cause
         *
         * @since Oct 15, 2015, Christopher K. Allen
         */
        public NoConvergenceException(String message) {
            super(message);
        }

        /**
         * Initializing constructor for NoConvergenceException.
         *
         * @param cause source of this exception an exception chain
         *
         * @since Oct 15, 2015, Christopher K. Allen
         */
        public NoConvergenceException(Throwable cause) {
            super(cause);
        }

    }

    /*
     * Local Attributes
     */
    /**
     * total potential drop <em>V</em><sub>0</sub> across accelerating gap
     */
    private double dblFldMag;

    /**
     * time-harmonic frequency of the gap RF field
     */
    private final double dblFldFrq;

    /**
     * spectrum of the accelerating fields along the design axis
     */
    private final AxialFieldSpectrum spcFldSpc;

    //
    // Consistent Parameters
    //
    /**
     * wave number of the gap accelerating field in free space
     */
    private final double dblRfWvNm;

    //
    //  Numeric Parameters
    //
    /**
     * maximum number of iterations allowable in iterative search for gain
     * parameters
     */
    private int cntIterMax;

    /**
     * acceptable error tolerance when iterating for consistent gain parameters
     */
    private double dblErrTol;

    /*
     * Initialization
     */
    /**
     * Initializing constructor for AcceleratingRfGap. All parameters needed for
     * defining the RF accelerating gap are provided.
     *
     * @param f time-harmonic frequency of the accelerating field (Hz)
     * @param v0 total potential drop across gap axial field (Volts)
     * @param spcRfFld spectrum of the RF field along design axis
     *
     * @since Oct 1, 2015, Christopher K. Allen
     */
    public AcceleratingRfGap(double f, double v0, AxialFieldSpectrum spcRfFld) {
        dblFldFrq = f;
        dblFldMag = v0;
        spcFldSpc = spcRfFld;

        dblRfWvNm = DBL_2PI * f / DBL_LGHT_SPD;

        cntIterMax = CNT_MAX_ITER;
        dblErrTol = DBL_ERR_TOL;
    }

    /**
     * Set the maximum number of allowable iterations during a search for the
     * consistent gain parameters &Delta;&phi; and &Delta;<em>W</em>. The
     * default value for this quantity is given by the constant
     * <code>{@link #CNT_MAX_ITER}</code>.
     *
     * @param cntIterMax new value for maximum iteration count in gain
     * computations
     *
     * @since Oct 13, 2015, Christopher K. Allen
     */
    public void setMaxIterations(int cntIterMax) {
        this.cntIterMax = cntIterMax;
    }

    /**
     * Sets the acceptable error tolerance when iterating for consistent gain
     * parameters &Delta;<em>W</em> and &Delta;&phi;. The default value for this
     * quantity is given by the constant <code>{@link #DBL_ERR_TOL}</code>.
     *
     * @param dblErrTol new value for the error tolerance in gain computations
     *
     * @since Oct 9, 2015, Christopher K. Allen
     */
    public void setErrorTolerance(double dblErrTol) {
        this.dblErrTol = dblErrTol;
    }

    /**
     * Resets the total potential gain across the accelerating gap.
     *
     * @param v0 the integral &int;<em>E<sub>z</em></em>(<em>z<em>) <em>dz</em>
     * (in Volts)
     *
     * @since Oct 16, 2015, Christopher K. Allen
     */
    public void setRfFieldPotential(double v0) {
        dblFldMag = v0;
    }

    /*
     * Attributes
     */
    /**
     * Returns the maximum number of allowable iterations during the iterative
     * search for RF gap gain parameters &Delta;&phi; and &Delta;<em>W</em>.
     *
     * @return maximum iteration count in (&Delta;&phi;,&Delta;<em>W</em>) phase
     * jump, energy gain computations
     *
     * @since Oct 13, 2015, Christopher K. Allen
     *
     * @see AcceleratingRfGap#setMaxIterations()
     */
    public int getMaxIterations() {
        return cntIterMax;
    }

    /**
     * Returns the maximum allowable error, the distance between iterates of
     * (&Delta;&phi;,&Delta;<em>W</em>), before the current iterate is
     * considered a valid solution. Once the <em>L</em><sub>2</sub> distance
     * between the current iterate of phase jump &Delta;&phi; and energy gain
     * &Delta;<em>W</em> and the previous iterate is less than this value, then
     * the iteration stops with a valid solution.
     *
     * @return maximum distance between solution iterations for valid solution
     *
     * @since Oct 13, 2015, Christopher K. Allen
     */
    public double getErrorTolerance() {
        return dblErrTol;
    }

    /**
     * Returns the time-harmonic frequency of gap accelerating field.
     *
     * @return the RF frequency of the electric field (Hz)
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public double getRfFrequency() {
        return dblFldFrq;
    }

    /**
     * Get the free space wave number <em>k</em><sub>0</sub> of the gap
     * accelerating fields. This quantity has the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>k</em><sub>0</sub> = 2&pi;/&lambda;
     * <br/>
     * <br/>
     * where &lambda; = <em>c</em>/<em>f</em> is the wave length of the RF in
     * free space.
     *
     * @return free space wave number <em>k</em><sub>0</sub> of gap RF
     *
     * @since Oct 1, 2015, Christopher K. Allen
     */
    public double getRfWaveNumber() {
        return dblRfWvNm;
    }

    /**
     * Returns the total (integrated) potential gain of the RF field across the
     * accelerating gap. This is the value given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>V</em><sub>0</sub> &trie;
     * &int;<em>E<sub>z</sub></em>(0,<em>z</em>)<em>dz</em> ,
     * <br/>
     * <br/>
     * where the integral is taken over the entire real line <em>z</em> &in;
     * (-&infin;,+&infin;). This value represents the total available
     * accelerating RF energy and an upper limit for energy gain.
     *
     * @return the total potential <em>V</em><sub>0</sub> across the
     * accelerating gap (in Volts)
     *
     * @since Oct 2, 2015, Christopher K. Allen
     */
    public double getRfFieldPotential() {
        return dblFldMag;
    }

    /**
     * Returns the spectrum of the gap's axial electric field.
     *
     * @return spectrum object describing the electric field along the gap axis
     *
     * @since Oct 9, 2015, Christopher K. Allen
     */
    public AxialFieldSpectrum getFieldSpectrum() {
        return spcFldSpc;
    }

    /*
     * Operations
     */
    /**
     * <p>
     * Computes and returns the phase jump &Delta;&phi; and energy gain
     * &Delta;<em>W</em>
     * imparted to a particle with the the given particle charge <em>Q</em> and
     * rest energy <em>E<sub>r</sub></em> for the given gap region. The particle
     * enters this region with initial gap phase &phi;<sub>0</sub> and initial
     * kinetic energy <em>W<sub>i</sub></em>. The returned values represent the
     * effects of the first have of this gap upon the so described particle.
     * This method uses an iterative technique to compute the results in order
     * to maintain a self-consistent set of expressions. That is, the modeling
     * expressions are a set of transcendental equations which must be solved
     * self-consistently. Thus, we are relegated to iterative methods.
     * </p>
     * <p>
     * The provided phase &phi;<sub>0</sub> is assumed to be the phase of the
     * particle at the center of the gap, if it were coasting through the gap
     * with kinetic energy <em>W<sub>i</sub></em> for the pre-gap computation,
     * and after the pre-gap phase jump &Delta;&phi;<sup>-</sup> for the
     * post-gap computation. The phase value returned by this method, either
     * &Delta;&phi;<sup>-</sup> or &Delta;&phi;<sup>+</sup> for the pre-gap or
     * post-gap calculation, respectively, is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi;<sup>-</sup> = +&phi;<sub>0</sub> -
     * &phi;<sub>0</sub><sup>-</sup> ,
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi;<sup>+</sup> = -&phi;<sub>0</sub> +
     * &phi;<sub>0</sub><sup>+</sup> ,
     * <br/>
     * <br/>
     * where &phi;<sub>0</sub><sup>-</sup> is the thin-lens model gap center
     * intercept of the incoming coasting particle,
     * &phi;<sub>0</sub><sup>+</sup> is the thin-lens model gap center intercept
     * of the outgoing coasting particle, and &phi;<sub>0</sub> is the actual
     * particle phase at the gap center. The total phase jump at the gap center
     * &Delta;&phi; is the sum of these two phase jumps,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi; = &Delta;&phi;<sup>-</sup> +
     * &Delta;&phi;<sup>+</sup> = &phi;<sub>0</sub><sup>+</sup> -
     * &phi;<sub>0</sub><sup>-</sup> ,
     * <br/>
     * </p>
     * <p>
     * The returned energy gain, either &Delta;<em>W</em><sup>-</sup> for the
     * pre-gap location or &Delta;<em>W</em><sup>+</sup> for the post-gap
     * location, is the energy gained by the particle in those respective
     * locations. The total energy gained by a particle through the gap is the
     * sum of these energies
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;<em>W</em> = &Delta;<em>W</em><sup>-</sup> +
     * &Delta;<em>W</em><sup>+</sup> .
     * </p>
     *
     * @param locHam location of longitudinal gain calculations, with respect to
     * gap center
     * @param q charge of the incoming particles in terms of fundamental charge
     * <em>q</em> (unitless)
     * @param eR rest energy of the incoming particles (electron-Volts)
     * @param vecInit initial phase and energy pair
     * (&phi;<sub>0</sub>,<em>W<sub>i</sub></em>) into specified gap region
     * (radians, electron-Volts)
     *
     * @return the corrective phase jump and energy gain pair
     * (&Delta;&phi;,&Delta;<em>W</em>) after impulse at specified location,
     * pre- or post-gap (radians, electron-Volts)
     *
     * @throws NoConvergenceException the iterative search for
     * (&Delta;&phi;,&Delta;<em>W</em>) failed to converge
     *
     * @since Oct 15, 2015, Christopher K. Allen
     */
    public EnergyVector computeGapGains(LOC locHam, double q, double eR, EnergyVector vecInit) throws NoConvergenceException {

        // Variable scaling constants (scales the "Hamiltonian")
        final double v0 = q * this.getRfFieldPotential();
        final double ki = q * this.computeNormWaveNumber(vecInit.getEnergy(), eR);

        // For the asymptotic model, get the phase intercept, the initial kinetic energy, 
        //  and the initial wave number 
        double phi0 = vecInit.getPhase();
        double wi = vecInit.getEnergy();

        // Initialize the search variables.
        //  Use the phase intercept and initial energy as starting values
        // the synchronous phase at the gap center
        double phi = phi0;
        // the energy gained up to the gap center
        double w = wi;
        double k = this.computeWaveNumber(w, eR);

        // Compute the starting values for phase jump and energy gain
        double dW = -v0 * locHam.dphiGapHamiltonian(this, phi, k).imaginary();
        double dphi = +ki * locHam.dkGapHamiltonian(this, phi, k).imaginary();

        // Initialize the search loop
        int cntIter = 0;
        double dblErr = 10.0 * this.getErrorTolerance();
        while (cntIter < this.getMaxIterations()) {

            // Compute the new phase and energy from the previously computed
            //  phase jump and energy gain
            phi = phi0 + dphi;
            w = wi + dW;
            k = this.computeWaveNumber(w, eR);

            // Compute the new phase jump and energy gain from the new phase 
            //  and energies
            double dphiI = +ki * locHam.dkGapHamiltonian(this, phi, k).imaginary();
            double dWi = -v0 * locHam.dphiGapHamiltonian(this, phi, k).imaginary();

            // Compute stopping criteria values 
            cntIter++;
            dblErr = (dphiI - dphi) * (dphiI - dphi) + (dW - dWi) * (dW - dWi) / (wi * wi);

            // Update the values of the dependent variables wave number, phase jump, 
            //  and energy gain 
            dphi = dphiI;
            dW = dWi;

            // Check for Cauchy sequence convergence.  Stop and return gains if passed. 
            if (dblErr < this.getErrorTolerance()) {
                return new EnergyVector(dphi, dW);
            }
        }

        // If we made it outside the loop then there was no converge.
        String strMsg = locHam.name() + ": failed to compute gap gain values after "
                + cntIter
                + " iterations.  Error = "
                + dblErr;

        throw new NoConvergenceException(strMsg);
    }

    /**
     * <p>
     * Computes and returns the phase jump &Delta;&phi; and energy gain
     * &Delta;<em>W</em>
     * imparted to a particle with the the given rest energy
     * <em>E<sub>r</sub></em>, gap phase &phi;<sub>0</sub><sup>-</sup>, and
     * initial kinetic energy <em>W<sub>i</sub></em>. The returned values
     * represent the effects of the first have of this gap upon the so described
     * particle. This method uses an iterative technique to compute the results
     * in order to maintain a self-consistent set of expressions. That is, the
     * modeling expressions are a set of transcendental equations which must be
     * solved self-consistently. Thus, we are relegated to iterative methods.
     * </p>
     * <p>
     * The provided phase &phi;<sub>0</sub><sup>-</sup> is assumed to be the
     * phase of the particle at the center of the gap if it were coasting
     * through the gap with kinetic energy <em>W<sub>i</sub></em>. The actual
     * phase at the gap center &phi;<sub>0</sub> will be
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi;<sub>0</sub> = &phi;<sub>0</sub><sup>-</sup> +
     * &Delta;&phi; ,
     * <br/>
     * <br/>
     * where &Delta;&phi; is the phase jump returned by this method.
     * </p>
     * <p>
     * The returned energy gain &Delta;<em>W</em> is that for the particle at
     * the gap center. That is, the effects of the gap on the particle include
     * an energy gain of &Delta;<em>W</em> up to the gap center. The total
     * kinetic energy <em>W</em><sub>0</sub>
     * at gap center is then
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>W</em><sub>0</sub> = <em>W<sub>i</em> +
     * &Delta;<em>W</em>.
     * </p>
     *
     * @param eR rest energy of the incoming particles (electron-Volts)
     * @param vecInit gap phase and initial energy pair
     * (&phi;<sub>0</sub><sup>-</sup>,<em>W<sub>i</sub></em>) (radians,
     * electron-Volts)
     *
     * @return the corrective phase jump and energy gain pair
     * (&Delta;&phi;,&Delta;<em>W</em>) at gap center (radians, electron-Volts)
     *
     * @throws NoConvergenceException the iterative search for
     * (&Delta;&phi;,&Delta;<em>W</em>) failed to converge
     *
     * @since Oct 15, 2015, Christopher K. Allen
     */
    @Deprecated
    public EnergyVector computePreGapGains(double eR, EnergyVector vecInit) throws NoConvergenceException {

        // Variable scaling constants (scales the "Hamiltonian")
        final double v0 = this.getRfFieldPotential();
        final double ki = this.computeNormWaveNumber(vecInit.getEnergy(), eR);

        // For the asymptotic model, get the phase intercept, the initial kinetic energy, 
        //  and the initial wave number 
        double phi0 = vecInit.getPhase();
        double wi = vecInit.getEnergy();

        // Initialize the search variables.
        //  Use the phase intercept and initial energy as starting values
        // the synchronous phase at the gap center
        double phi = phi0;
        // the energy gained up to the gap center
        double w = wi;
        double k = this.computeWaveNumber(w, eR);

        // Compute the starting values for phase jump and energy gain
        double dW = -v0 * this.dphiPreGapHamiltonian(phi, k).imaginary();
        double dphi = +ki * this.dkPreGapHamiltonian(phi, k).imaginary();

        // Initialize the search loop
        int cntIter = 0;
        double dblErr = 10.0 * this.getErrorTolerance();
        while (cntIter < this.getMaxIterations()) {

            // Compute the new phase and energy from the previously computed
            //  phase jump and energy gain
            phi = phi0 + dphi;
            w = wi + dW;
            k = computeWaveNumber(w, eR);

            // Compute the new phase jump and energy gain from the new phase 
            //  and energies
            double dphiI = +ki * this.dkPreGapHamiltonian(phi, k).imaginary();
            double dWi = -v0 * this.dphiPreGapHamiltonian(phi, k).imaginary();

            // Compute stopping criteria values 
            cntIter++;
            dblErr = (dphiI - dphi) * (dphiI - dphi) + (dW - dWi) * (dW - dWi) / (wi * wi);

            // Update the values of the dependent variables wave number, phase jump, 
            //  and energy gain 
            dphi = dphiI;
            dW = dWi;

            // Check for Cauchy sequence convergence.  Stop and return gains if passed. 
            if (dblErr < this.getErrorTolerance()) {
                return new EnergyVector(dphi, dW);
            }
        }

        // If we made it outside the loop then there was no converge.
        String strMsg = "failed to compute gap gain values after "
                + cntIter
                + " iterations.  Error = "
                + dblErr;

        throw new NoConvergenceException(strMsg);
    }

    /**
     * Computes and returns the pre-gap "Hamiltonian" function
     * <em>H</em><sup>-</sup>(&phi;, <em>k</em>). This complex function is the
     * product of the pre-envelope spectrum &Escr;<sup>-</sup>(<em>k</em>) and
     * mid-gap synchronous phase &exponentiale;<sup>-<em>i</em> &phi;</sup>.
     * That is,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>H</em><sup>-</sup>(&phi;, <em>k</em>) =
     * &Escr;<sup>-</sup>(<em>k</em>)&exponentiale;<sup>-<em>i</em> &phi;</sup>.
     * <br/>
     * <br/>
     * The phase jump &Delta;&phi;<sup>-</sup> and energy gain
     * &Delta;<em>W</em><sup>-</sup> are dependent upon this quantity.
     *
     * @param phi the mid-gap synchronous phase angle &phi; (in radians)
     * @param k the synchronous particle wave number (in radians/meter)
     *
     * @return value of the Hamiltonian <em>H</em><sup>-</sup> at &phi; and
     * <em>k</em>
     *
     * @since Oct 7, 2015, Christopher K. Allen
     */
    private Complex preGapHamiltonain(double phi, double k) {
        Complex cpxPreSpc = this.spcFldSpc.preEnvSpectrum(k);
        Complex cpxPreAng = Complex.euler(phi);
        return cpxPreSpc.times(cpxPreAng);
    }

    /**
     * Computes and returns the post-gap "Hamiltonian" function
     * <em>H</em><sup>+</sup>(&phi;, <em>k</em>). This complex function is the
     * product of the post-envelope spectrum &Escr;<sup>+</sup>(<em>k</em>) and
     * mid-gap synchronous phase &exponentiale;<sup>-<em>i</em> &phi;</sup>.
     * That is,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>H</em><sup>+</sup>(&phi;, <em>k</em>) =
     * &Escr;<sup>+</sup>(<em>k</em>)&exponentiale;<sup>-<em>i</em> &phi;</sup>.
     * <br/>
     * <br/>
     * The phase jump &Delta;&phi;<sup>+</sup> and energy gain
     * &Delta;<em>W</em><sup>+</sup> are dependent upon this quantity.
     *
     * @param phi the mid-gap synchronous phase angle &phi; (in radians)
     * @param k the synchronous particle wave number (in radians/meter)
     *
     * @return value of the Hamiltonian <em>H</em><sup>+</sup> at &phi; and
     * <em>k</em>
     *
     * @since Oct 7, 2015, Christopher K. Allen
     */
    private Complex postGapHamiltonain(double phi, double k) {
        Complex cpxPostSpc = this.spcFldSpc.postEnvSpectrum(k);
        Complex cpxPostAng = Complex.euler(phi);
        return cpxPostSpc.times(cpxPostAng);
    }

    /**
     * Computes and returns the derivative of the pre-gap "Hamiltonian" function
     * <em>H</em><sup>-</sup>(&phi;, <em>k</em>) with respect to the wave number
     * <em>k</em>, that is, the value   <em>dH</em><sup>-</sup>(&phi;,
     * <em>k</em>)/<em>dk</em>. This complex-valued function of variables &phi;
     * and
     * <em>k</em> is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>dH</em><sup>-</sup>(&phi;, <em>k</em>)/<em>dk</em> =
     * [<em>d</em>&Escr;<sup>-</sup>(<em>k</em>)/<em>dk</em>]
     * &exponentiale;<sup>-<em>i</em> &phi;</sup>.
     * <br/>
     * <br/>
     * where &Escr;<sup>-</sup>(<em>k</em>) is the pre-envelope spectrum and
     * &exponentiale;<sup>-<em>i</em> &phi;</sup> is the mid-gap synchronous
     * phase. The phase jump &Delta;&phi;<sup>-</sup> is directly proportional
     * to the imaginary part of this quantity.
     *
     * @param phi the mid-gap synchronous phase angle &phi; (in radians)
     * @param k the synchronous particle wave number (in radians/meter)
     *
     * @return value of the Hamiltonian <em>H</em><sup>-</sup> at &phi; and
     * <em>k</em>
     *
     * @since Oct 7, 2015, Christopher K. Allen
     */
    private Complex dkPreGapHamiltonian(double phi, double k) {
        Complex cpxDkPreSpc = this.spcFldSpc.dkPreEnvSpectrum(k);
        Complex cpxPreAngle = Complex.euler(phi);
        return cpxDkPreSpc.times(cpxPreAngle);
    }

    /**
     * Computes and returns the derivative of the post-gap "Hamiltonian"
     * function
     * <em>H</em><sup>+</sup>(&phi;, <em>k</em>) with respect to the wave number
     * <em>k</em>, that is, the value   <em>dH</em><sup>+</sup>(&phi;,
     * <em>k</em>)/<em>dk</em>. This complex-valued function of variables &phi;
     * and
     * <em>k</em> is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>dH</em><sup>+</sup>(&phi;, <em>k</em>)/<em>dk</em> =
     * [<em>d</em>&Escr;<sup>+</sup>(<em>k</em>)/<em>dk</em>]
     * &exponentiale;<sup>-<em>i</em> &phi;</sup>.
     * <br/>
     * <br/>
     * where &Escr;<sup>+</sup>(<em>k</em>) is the post-envelope spectrum and
     * &exponentiale;<sup>-<em>i</em> &phi;</sup> is the mid-gap synchronous
     * phase. The phase jump &Delta;&phi;<sup>+</sup> is directly proportional
     * to the imaginary part of this quantity.
     *
     * @param phi the mid-gap synchronous phase angle &phi; (in radians)
     * @param k the synchronous particle wave number (in radians/meter)
     *
     * @return value of the Hamiltonian <em>H</em><sup>+</sup> at &phi; and
     * <em>k</em>
     *
     * @since Oct 7, 2015, Christopher K. Allen
     */
    private Complex dkPostGapHamiltonian(double phi, double k) {
        Complex cpxDkPostSpc = this.spcFldSpc.dkPostEnvSpectrum(k);
        Complex cpxPostAngle = Complex.euler(phi);
        return cpxDkPostSpc.times(cpxPostAngle);
    }

    /**
     * Computes and returns the derivative of the pre-gap "Hamiltonian" function
     * <em>H</em><sup>-</sup>(&phi;, <em>k</em>) with respect to the mid-gap
     * synchronous phase &phi;, that is, the value
     * <em>dH</em><sup>-</sup>(&phi;,
     * <em>k</em>)/<em>d</em>&phi;. This complex-valued function of variables
     * &phi; and <em>k</em> is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>dH</em><sup>-</sup>(&phi;, <em>k</em>)/<em>d</em>&phi;
     * = &Escr;<sup>-</sup>(<em>k</em>) <em>d</em>&exponentiale;<sup>-<em>i</em>
     * &phi;</sup>/<em>d</em>&phi; = -<em>i</em>
     * <em>H</em><sup>-</sup>(&phi;,<em>k</em>) ,
     * <br/>
     * <br/>
     * where &Escr;<sup>-</sup>(<em>k</em>) is the pre-envelope spectrum and
     * &exponentiale;<sup>-<em>i</em> &phi;</sup> is the mid-gap synchronous
     * phase. The energy gain &Delta;<em>W</em><sup>-</sup> is proportional to
     * the imaginary part of this quantity.
     *
     * @param phi the mid-gap synchronous phase angle &phi; (in radians)
     * @param k the synchronous particle wave number (in radians/meter)
     *
     * @return value of the Hamiltonian <em>H</em><sup>-</sup> at &phi; and
     * <em>k</em>
     *
     * @since Oct 7, 2015, Christopher K. Allen
     */
    private Complex dphiPreGapHamiltonian(double phi, double k) {
        Complex cpxHamil = this.preGapHamiltonain(phi, k);
        Complex cpxRotat = Complex.IUNIT.negate();
        return cpxHamil.times(cpxRotat);
    }

    /**
     * Computes and returns the derivative of the post-gap "Hamiltonian"
     * function
     * <em>H</em><sup>+</sup>(&phi;, <em>k</em>) with respect to the mid-gap
     * synchronous phase &phi;, that is, the value
     * <em>dH</em><sup>+</sup>(&phi;,
     * <em>k</em>)/<em>d</em>&phi;. This complex-valued function of variables
     * &phi; and <em>k</em> is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>dH</em><sup>+</sup>(&phi;, <em>k</em>)/<em>d</em>&phi;
     * = &Escr;<sup>+</sup>(<em>k</em>) <em>d</em>&exponentiale;<sup>-<em>i</em>
     * &phi;</sup>/<em>d</em>&phi; = -<em>i</em>
     * <em>H</em><sup>+</sup>(&phi;,<em>k</em>) ,
     * <br/>
     * <br/>
     * where &Escr;<sup>+</sup>(<em>k</em>) is the post-envelope spectrum and
     * &exponentiale;<sup>-<em>i</em> &phi;</sup> is the mid-gap synchronous
     * phase. The energy gain &Delta;<em>W</em><sup>+</sup> is proportional to
     * the imaginary part of this quantity.
     *
     * @param phi the mid-gap synchronous phase angle &phi; (in radians)
     * @param k the synchronous particle wave number (in radians/meter)
     *
     * @return value of the Hamiltonian <em>H</em><sup>+</sup> at &phi; and
     * <em>k</em>
     *
     * @since Oct 7, 2015, Christopher K. Allen
     */
    private Complex dphiPostGapHamiltonian(double phi, double k) {
        Complex cpxHamil = this.postGapHamiltonain(phi, k);
        Complex cpxRotat = Complex.IUNIT.negate();
        return cpxHamil.times(cpxRotat);
    }

    //
    // Beam Particle Properties
    //
    /**
     * <p>
     * Compute and return the particle wave number <em>k</em> for the given
     * particle energy <em>W</em>
     * and rest energy <em>E<sub>r</sub></em> = <em>mc</em><sup>2</sup>. The
     * formula is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>k</em> = <em>k</em><sub>0</sub>/&radic;(1 -
     * 1/&gamma;<sup>2</sup>) ,
     * <br/>
     * <br/>
     * where <em>k</em><sub>0</em> = 2&pi;/&lambda; is the wave number of the RF
     * in free space and &gamma; = 1 + <em>W</em>/<em>E<sub>r</sub></em> is the
     * relativistic factor.
     * </p>
     *
     * @param w particle kinetic energy <em>W</em> (electron-Volts)
     * @param eR particle rest mass <em>mc</em><sup>2</sup>/<em>q</em>
     * (electron-Volts)
     *
     * @return particle wave number with respect to the RF (radians/meter)
     *
     * @since Oct 12, 2015 by Christopher K. Allen
     */
    private double computeWaveNumber(double w, double eR) {
        double gamma = this.computeGammaFromEnergy(w, eR);
        double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
        double k0 = this.getRfWaveNumber();

        return k0 / beta;
    }

    /**
     * <p>
     * Compute and return the normalized wave number <em>K</em>. This quantity
     * appears as a constant in the phase jump expressions. The value of
     * <em>K</em>
     * is defined by the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>K</em> &trie; (1/&beta;<sup>3</sup>&gamma;<sup>3</sup>)
     * (<em>qV</em><sub>0</sub>/<em>mc</em><sup>2</sup>)<em>k</em><sub>0</sub>
     * <br/>
     * <br/>
     * where &beta; is the normalized particle velocity, &gamma; is the
     * relativistic factor;
     * <em>V</em><sub>0</sub> is the total (integrated) RF field potential
     * across the gap,
     * <em>mc</em><sup>2</sup>/<em>q</em> is the rest mass in electron-Volts,
     * and
     * <em>k</em><sub>0</sub> &trie; 2&pi;/&lambda; is the free-space wave
     * number of the accelerating RF field with wavelength &lambda;.
     * </p>
     * <p>
     * The quantity <em>K</em> can be interpreted as the particle wave number in
     * energy space rather that momentum space.
     * </p>
     *
     * @param w particle kinetic energy <em>W</em> (electron-Volts)
     * @param eR particle rest mass <em>mc</em><sup>2</sup>/<em>q</em>
     * (electron-Volts)
     *
     * @return normalized wave number <em>K</em> (in radians/meter)
     *
     * @since Oct 1, 2015, Christopher K. Allen
     */
    public double computeNormWaveNumber(double w, double eR) {
        double gamma = computeGammaFromEnergy(w, eR);
        double bg = Math.sqrt(gamma * gamma - 1.0);
        double bg3 = bg * bg * bg;

        double v0 = getRfFieldPotential();
        double k0 = getRfWaveNumber();

        double eN = eR * bg3;
        return (v0 / eN) * k0;
    }

    /**
     * Compute and return the relativistic factor &gamma; from the given kinetic
     * energy and given rest energy <em>E<sub>r</sub></em> =
     * <em>mc</em><sup>2</sup>. This value is given by the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &gamma; = 1 + <em>W</em>/<em>mc</em><sup>2</sup> ,
     * <br/>
     * <br/>
     * where the energy quantities can be in any (consistent) units but
     * electron- volts are typically used.
     *
     * @param w kinetic energy <em>W</em> of particle (electron-Volts)
     * @param eR rest energy <em>E<sub>r</sub></em> = <em>mc</em><sup>2</sup> of
     * particle (electron-Volts)
     *
     * @return the relativistic factor &gamma; for a particle of energy
     * <em>W</em>
     * and rest energy <em>E<sub>r</sub></em>
     *
     * @since Oct 12, 2015, Christopher K. Allen
     */
    private double computeGammaFromEnergy(double w, double eR) {
        return 1.0 + w / eR;
    }
}
