/*
 * IdealEQuad.java
 *
 * Created on October 7, 2002, 10:36 PM
 *
 * Modified:
 *      02/13/03 CKA    - refactored to new model architecture
 *      03/21/03 CKA    - added JavaBean
 *      07/18/08 MDW    - switch to MAD's longitudinal coordinates
 */
package xal.model.elem;

import java.io.PrintWriter;

import xal.model.IProbe;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.QuadrupoleLens;

/**
 * Represents an ideal electrostatic quadrupole for a beam transport/accelerator
 * system.
 *
 * @author Carla Benatti, adapted from Christopher K. Allen's IdealMagQuad.java
 */
public class IdealEQuad extends ThickElectrostatic {

    /*
     * Global Constants
     */
    /**
     * string type identifier for all IdealEQuad objects
     */
    public static final String TYPE = "IdealEQuad";

    /**
     * Parameter for XAL MODEL LATTICE dtd
     */
    public static final String PARAM_ORIENT = "Orientation";

    /**
     * Parameter for XAL MODEL LATTICE dtd
     */
    public static final String PARAM_VOLTAGE = "Voltage";

    /**
     * Parameter for XAL MODEL LATTICE dtd
     */
    public static final String PARAM_APERTURE = "ApertureRadius";

    /*
     * Local Attributes 
     */
    /**
     * Orientation of quadrupole
     */
    private int enmOrient = ORIENT_NONE;

    /**
     * Applied Voltage
     */
    private double dblVoltage = 0.0;

    /**
     * Aperture radius
     */
    private double dblAperture = 0.0;


    /*
     * Initialization
     */
    /**
     * Creates a new instance of IdealEQuad
     *
     * @param strId identifier for this IdealEQuad object
     * @param enmOrient enumeration specifying the quadrupole orientation
     * (ORIENT_HOR or ORIENT_VER)
     * @param dblVol Applied Voltage (<strong>kV</strong>)
     * @param dblLen Length of the quadrupole (<strong>m</strong>)
     * @param dblApt Aperture radius (<strong>m</strong>)
     */
    public IdealEQuad(
            String strId,
            int enmOrient,
            double dblVol,
            double dblLen,
            double dblApt) {
        super(TYPE, strId, dblLen);

        this.setOrientation(enmOrient);
        this.setVoltage(dblVol);
        this.setAperture(dblApt);
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of IdealEQuad
     *
     * <strong>BE CAREFUL</strong>
     */
    public IdealEQuad() {
        super(TYPE);
    }

    /*
     *  IElectrostatic Interface
     */
    /**
     * Return the orientation enumeration code.
     *
     * @return ORIENT_HOR - quadrupole focuses in x (horizontal) plane
     * ORIENT_VER - quadrupole focuses in y ( vertical ) plane ORIENT_NONE -
     * error
     */
    @Override
    public int getOrientation() {
        return enmOrient;
    }

    /**
     * Get the voltage applied to the electrostatic quad pole tips.
     *
     * @return Voltage (in <strong>kV</strong>).
     */
    @Override
    public double getVoltage() {
        return dblVoltage;
    }

    /**
     * Set the electrostatic quad orientation.
     *
     * @param enmOrient quad orientation enumeration code
     *
     * @see #getOrientation
     */
    @Override
    public void setOrientation(int enmOrient) {
        this.enmOrient = enmOrient;
    }

    /**
     * Set the applied Voltage on the electrostatic quad.
     *
     * @param dblVoltage Voltage (in <strong>kV</strong>).
     */
    @Override
    public void setVoltage(double dblVoltage) {
        this.dblVoltage = dblVoltage;
    }

    /**
     * Get the Aperture radius of the electrostatic quad.
     *
     * @return Aperture Radius (in <strong>m</strong>).
     */
    public double getAperture() {
        return dblAperture;
    }

    /**
     * Set the Aperture radius of the electrostatic quad.
     *
     * @param dblAperture Aperture Radius (in <strong>m</strong>).
     */
    public void setAperture(double dblAperture) {
        this.dblAperture = dblAperture;
    }

    /*
     *  ThickElement Protocol
     */
    /**
     * Returns the time taken for the probe to drift through part of the
     * element.
     *
     * @param probe propagating probe
     * @param dblLen length of subsection to propagate through <strong>meters</strong>
     *
     * @return the elapsed time through section<strong>Units: seconds</strong>
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return super.compDriftingTime(probe, dblLen);
    }

    /**
     * Return the energy gain imparted to a particular probe. For an ideal
     * Equadrupole this value is always zero.
     *
     * @param probe dummy argument
     * @param dblLen dummy argument
     * @return returns a zero value
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return 0.0;
    }

    /**
     * Compute the partial transfer map of an ideal Equadrupole for the
     * particular probe. Computes transfer map for a section of Equadrupole
     * <code>dblLen</code> meters in length. The aperture used in the
     * calculation is the aperture radius in meters.
     *
     * @param probe supplies the charge, rest and kinetic energy parameters
     * @param length compute transfer matrix for section of this length
     * @return transfer map of ideal Equadrupole for particular probe
     */
    @Override
    public PhaseMap transferMap(final IProbe probe, final double length) {
        double charge = probe.getSpeciesCharge();
        double Er = probe.getSpeciesRestEnergy();
        double T = probe.getKineticEnergy();
        double beta = probe.getBeta();
        double gamma = probe.getGamma();
        double bg = beta * gamma;
        double brho = (Er * bg) / LIGHT_SPEED;
        double dLz = length / (bg * bg);

        // mass number 1.073e-9~=10e-6/931.494
        //final double A = Math.floor(Er * 1.07354422036e-9);
        // focusing constant (radians/meter)
        final double k = (charge * ((getVoltage() * 1e3) / T)) / (getAperture() * getAperture());
        //LOGGER.log(Level.INFO, "V = " + getVoltage() * 1e3);
        //LOGGER.log(Level.INFO, "T = " + T);
        //LOGGER.log(Level.INFO, "ap = " + getAperture());
        final double kSqrt = Math.sqrt(Math.abs(k));

        // Compute the transfer matrix components
        final double[][] arrF = QuadrupoleLens.transferFocPlane(kSqrt, length);
        final double[][] arrD = QuadrupoleLens.transferDefPlane(kSqrt, length);
        double arrZ[][] = new double[][]{{1.0, dLz}, {0.0, 1.0}};

        // Build the transfer matrix from its component blocks
        PhaseMatrix matPhi = new PhaseMatrix();

        if (k >= 0.0) {
            matPhi.setSubMatrix(0, 1, 0, 1, arrF);
            matPhi.setSubMatrix(2, 3, 2, 3, arrD);
        } else if (k < 0.0) {
            matPhi.setSubMatrix(0, 1, 0, 1, arrD);
            matPhi.setSubMatrix(2, 3, 2, 3, arrF);
        }
        matPhi.setSubMatrix(4, 5, 4, 5, arrZ); // a drift space longitudinally
        matPhi.setElem(6, 6, 1.0); // homogeneous coordinates

        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors
        matPhi = applyErrors(matPhi, probe, length);

        return new PhaseMap(matPhi);
    }


    /*
     *  Testing and Debugging
     */
    /**
     * Dump current state and content to output stream.
     *
     * @param os output stream object
     */
    @Override
    public void print(PrintWriter os) {
        super.print(os);

        os.println("  Voltage     : " + this.getVoltage());
        os.println("  EQuad orientation : " + this.getOrientation());
    }

    /**
     * Conversion method to be provided by the user
     *
     * @param element the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);
        setAperture(element.getHardwareNode().getAper().getAperX()[0]);
    }
}
