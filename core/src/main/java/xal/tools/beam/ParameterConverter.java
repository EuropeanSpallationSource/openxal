/*
 * Created on Dec 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package xal.tools.beam;

/**
 * @author Christopher K. Allen
 *
 * @deprecated use <code>RelativisticParameterConverter</code>
 *
 * @see gov.sns.tool.beam#RelativisticParameterConverter
 */
@Deprecated
public class ParameterConverter {

    private ParameterConverter() {
        throw new IllegalStateException("Utility class");
    }

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
     * @param w kinetic energy of the particle
     * @param eR rest energy of particle
     *
     * @return relativistic factor gamma
     */
    public static double computeGammaFromEnergies(double w, double eR) {
        return w / eR + 1.0;
    }

    /**
     * Convenience function for computing the probe's velocity beta (w.r.t. the
     * speed of light) from the relativistic factor gamma.
     *
     * @param gamma relativistic factor gamma
     * @return speed of probe (w.r.t. speed of light)
     */
    public static double computeBetaFromGamma(double gamma) {
        return Math.sqrt(1.0 - 1.0 / (gamma * gamma));
    }

    /**
     * Convenience function for computing the probe's velocity beta (w.r.t. the
     * speed of light) from the kinetic energy.
     *
     * @param w kinetic energy (eV)
     * @param eR rest energy of particle (eV)
     * @return speed of probe (w.r.t. speed of light)
     */
    public static double computeBetaFromEnergies(double w, double eR) {
        double gamma = computeGammaFromEnergies(w, eR);
        return Math.sqrt(1.0 - 1.0 / (gamma * gamma));
    }

    /**
     * Convenience function for computing momentum from kinetic energy
     *
     * @param w kinetic energy of the particle (eV)
     * @param eR rest energy of particle (eV)
     * @return particle momentum in eV/c where c is the speed of light
     */
    public static double computeMomentumFromEnergies(double w, double eR) {
        double gamma = computeGammaFromEnergies(w, eR);
        double beta = computeBetaFromGamma(gamma);

        return beta * gamma * eR;
    }
}
