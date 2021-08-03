/*
 * Created on Dec 18, 2003
 *
 */
package xal.tools.beam;

/**
 * Utility class for converting between common relativistic (and related)
 * parameters used in beam physics.
 *
 * @author Christopher K. Allen
 * @since Dec 18, 2003
 * @version Oct 1, 2015 Added wave number computation
 */
public class RelativisticParameterConverter {

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

    private RelativisticParameterConverter() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * Operations
     */
    /**
     * Computes the relativistic factor gamma from the current beta value
     *
     * @param beta speed of probe w.r.t. the speed of light
     * @return relativistic factor gamma
     */
    public static double computeGammaFromBeta(double beta) {
        return 1.0 / Math.sqrt(1.0 - beta * beta);
    }

    /**
     * Convenience function for computing the relativistic factor gamma from a
     * particle's kinetic energy and rest energy.
     *
     * @param W kinetic energy of the particle
     * @param Er rest energy of particle
     *
     * @return relativistic factor gamma
     */
    public static double computeGammaFromEnergies(double W, double Er) {
        double gamma = W / Er + 1.0;

        return gamma;
    }

    /**
     * Convenience function for computing the probe's velocity beta (w.r.t. the
     * speed of light) from the relativistic factor gamma.
     *
     * @param gamma relativistic factor gamma
     * @return speed of probe (w.r.t. speed of light)
     */
    public static double computeBetaFromGamma(double gamma) {
        double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));

        return beta;
    }

    /**
     * Convenience function for computing the probe's velocity beta (w.r.t. the
     * speed of light) from the particle's kinetic and rest energies.
     *
     * @param W kinetic energy of the particle (eV)
     * @param Er rest energy of particle (eV)
     * @return speed of probe (w.r.t. speed of light)
     */
    public static double computeBetaFromEnergies(double W, double Er) {
        double gamma = computeGammaFromEnergies(W, Er);
        double beta = computeBetaFromGamma(gamma);

        return beta;
    }

    /**
     * <p>
     * Compute and return the particle wave number <em>k</em> for the given
     * normalized particle velocity &beta;. The formula is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>k</em> = 2&pi;/&beta;&lambda; ,
     * <br/>
     * <br/>
     * where &lambda; is the wavelength of the accelerating RF.
     * </p>
     *
     * @param beta normalized probe velocity
     * @param freq time-harmonic frequency <em>f</em> of surrounding RF field
     * (Hz)
     *
     * @return particle wave number with respect to the RF
     *
     * @since Feb 16, 2015 by Christopher K. Allen
     */
    public static double computeWavenumberFromBeta(double beta, double freq) {
        double lambda = DBL_LGHT_SPD / freq;
        double k = DBL_2PI / (beta * lambda);

        return k;
    }

    /**
     * Compute the normalized particle velocity &beta; for the given particle
     * wave number <em>k</em> and frequency <em>f</em>.
     *
     * @param k wave number of the particle with respect to RF frequency
     * (radians/meter)
     * @param freq time-harmonic frequency <em>f</em> of surrounding RF field
     * (Hz)
     *
     * @return the normalized velocity &beta; of the particle for the given wave
     * number <em>k</em>
     *
     * @since Sep 28, 2015 by Christopher K. Allen
     */
    public static double computeBetaFromWavenumber(double k, double freq) {
        double lambda = DBL_LGHT_SPD / freq;
        double beta = DBL_2PI / (k * lambda);

        return beta;
    }

    /**
     * Convenience function for computing momentum from kinetic energy
     *
     * @param W kinetic energy of the particle (eV)
     * @param Er rest energy of particle (eV)
     * @return particle momentum in eV where is the speed of light
     */
    public static double computeMomentumFromEnergies(double W, double Er) {
        double gamma = computeGammaFromEnergies(W, Er);
        double beta = computeBetaFromGamma(gamma);

        return beta * gamma * Er;
    }

    /**
     * Computes and returns the momentum of a particle with the given rest and
     * kinetic energies. This method returns the rest the the standard beam
     * physics units of electron-volts/<em>c</em> where <em>c</em> is the speed
     * of light. The returned value is given by the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>p</em> = &beta;&gamma;<em>mc</em>
     * <br/>
     * <br/>
     * where &beta; is the normalized particle velocity, &gamma; is the
     * relativistic factor,
     * <em>m</em> is the mass of the particle, and <em>c</em> is the speed of
     * light.
     *
     * @param W kinetic energy of the particle (eV)
     * @param Er rest energy of particle (eV)
     *
     * @return particle momentum in <em>eV</em>/<em>c</em> where <em>c</em> is
     * the speed of light
     *
     * @since Oct 1, 2015, Christopher K. Allen
     */
    public static double computeStandardMomentumFromEnergies(double W, double Er) {
        double gamma = computeGammaFromEnergies(W, Er);
        double beta = computeBetaFromGamma(gamma);

        return beta * gamma * Er / DBL_LGHT_SPD;
    }

}
