/*
 * Created on May 19, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package xal.tools.beam.optics;

import xal.model.IProbe;
import xal.tools.beam.Constants;

/**
 * @author Chris Allen
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BendingMagnet {

    private BendingMagnet() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Compute the path curvature of a magnetic dipole for the given probe. The
     * path curvature is 1/R where R is the bending radius of the dipole (radius
     * of curvature). Note that for zero fields the radius of curvature is
     * infinite while the path curvature is zero.
     *
     * @param probe probe object to be deflected
     * @param dblFld constant field strength of magnet
     *
     * @return dipole path curvature for given probe (in
     * <strong>1/meters</strong>)
     */
    public static double compCurvature(IProbe probe, double dblFld) {
        // Get  parameters
        double b0 = dblFld;
        double c = Constants.LIGHT_SPEED;

        double e = probe.getSpeciesCharge();
        double eR = probe.getSpeciesRestEnergy();
        double gamma = probe.getGamma();
        double beta = probe.getBeta();

        // Compute the equilibrium curvature h=1/R
        return (e * c * b0) / (beta * gamma * eR);
    }
}
