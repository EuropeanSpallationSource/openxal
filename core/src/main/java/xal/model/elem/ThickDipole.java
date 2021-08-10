/*
 * Quadrupole.java
 *
 * Created on Dec. 19 , 2003
 */
package xal.model.elem;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.ElementaryFunction;

/**
 * Represents a thick magnetic dipole magnet for a beam transport/accelerator
 * system.
 *
 * NOTE: - !!! Bending is assumed to be horizontal for now !!!!
 *
 * It has provisions for a general wedge magnet, with arbitrary entrance / exit
 * angles. The MAD convention for sector magnets is followed for coordinates,
 * signs, and lengths. The formulation from D. Carey's Optics book + Transport
 * manual are used.
 *
 * TODO - Add "tilt" angle of the dipole, and add charge of the probe to get the
 * right bend radius.
 *
 * @author jdg
 */
public class ThickDipole extends ThickElectromagnet {

    private static final Logger LOGGER = Logger.getLogger(ThickDipole.class.getName());

    /*
     *  Global Attributes
     */
    /**
     * string type identifier for all ThickDipole objects
     */
    public static final String TYPE = "ThickDipole";

    /**
     * Parameters for XAL MODEL LATTICE dtd
     */
    /**
     * all thick elements have length - CKA
     */
    public static final String PATH_LENGTH = "PathLength";
    public static final String FIELD = "MagField";
    public static final String ENTRANCE_ANGLE = "EntranceAngle";
    public static final String EXIT_ANGLE = "ExitAngle";
    public static final String QUAD_COMPONENT = "QuadComponent";

    /*
     *  Attributes
     */
    /**
     * Path length in the magnet (m)
     */
    private double pathLength = 0.0;

    /**
     * Entrance angle (rad)
     */
    private double entranceAngle = 0.0;

    /**
     * Exit angle (rad)
     */
    private double exitAngle = 0.0;

    /**
     * The gap height (m)
     */
    private double gapHeight = 0.;

    /**
     * The dimensionless integral term for the fringe field focusing Should be =
     * 1/6 for linear drop off, ~ 0.4 for clamped Rogowski coil or 0.7 for an
     * unclamped Rogowski coil.
     */
    private double fringeIntegral = 0.;

    /**
     * The quadrupole term = 1/(B-rho) * dB_y/dx
     */
    private double k1 = 0.;

    //hs alignment
    private double alignx = 0.0;
    private double aligny = 0.0;
    private double alignz = 0.0;

    @Override
    public void setAlignX(double x) {
        alignx = x;
    }

    @Override
    public void setAlignY(double y) {
        aligny = y;
    }

    @Override
    public void setAlignZ(double z) {
        alignz = z;
    }

    @Override
    public double getAlignX() {
        return alignx;
    }

    @Override
    public double getAlignY() {
        return aligny;
    }

    @Override
    public double getAlignZ() {
        return alignz;
    }
    //hs bend angle
    private double bendAngle = 0.0;
    private double fieldPathFlag = 0.0;

    public void setPathLength(double pl) {
        pathLength = pl;
    }

    public void setBendAngle(double ba) {
        bendAngle = ba;
    }

    public void setFieldPathFlag(double ba) {
        fieldPathFlag = ba;
    }

    public double getPathLength() {
        return pathLength;
    }

    public double getBendAngle() {
        return bendAngle;
    }

    public double getFieldPathFlag() {
        return fieldPathFlag;
    }


    /*
     * Initialization
     */
    /**
     * Creates a new instance of ThickDipole
     *
     * @param strId identifier for this ThickDipole object
     * @param fld field gradient strength (in <strong>Tesla</strong>)
     * @param len pathLength of the dipole (in m)
     * @param entAng entrance angle of the dipole (in rad)
     * @param exitAng exit angle of the dipole (in rad)
     * @param gap full pole gap of the dipole (in m)
     * @param fInt The dimensionless integral term for the extended fringe field
     * focsing, Should be = 1/6 for linear drop off, ~ 0.4 for clamped Rogowski
     * coil, or 0.7 for an unclamped Rogowski coil. (dimensionless)
     *
     */
    public ThickDipole(
            String strId, double fld, double len, double entAng, double exitAng, double gap, double fInt) {
        super(TYPE, strId, len);
        this.setMagField(fld);
        entranceAngle = entAng;
        exitAngle = exitAng;
        gapHeight = gap;
        fringeIntegral = fInt;
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of
     * ThickDipole * This is the constructor called in automatic lattice
     * generation. Thus, all element properties are set following construction.
     *
     * <strong>BE CAREFUL</strong>
     */
    public ThickDipole() {
        super(TYPE);
    }

    /**
     * Sets the entrance angle of the beam into the dipole.
     *
     * @param dblAng entrance angle in <strong>radians</strong>
     *
     * @author Christopher K. Allen
     */
    public void setEntranceAngle(double dblAng) {
        this.entranceAngle = dblAng;
    }

    /**
     * Gets the entrance angle of the beam into the dipole.
     *
     * @author J. Galambos
     */
    public double getEntranceAngle() {
        return entranceAngle;
    }

    /**
     * Sets the entrance angle of the beam into the dipole.
     *
     * @param dblAng exit angle in <strong>radians</strong>
     * @author J. Galambos
     */
    public void setExitAngle(double dblAng) {
        this.exitAngle = dblAng;
    }

    /**
     * Gets the exit angle of the beam into the dipole.
     *
     * @author J. Galambos
     */
    public double getExitAngle() {
        return exitAngle;
    }

    /**
     * Sets the quad. field index term
     *
     * @param k = 1/B-rho) * d B_y/dx
     */
    public void setKQuad(double k) {
        this.k1 = k;
    }

    /**
     * Gets the quad. field index term = 1/B-rho * d B_y/dx
     */
    public double getKQuad() {
        return k1;
    }

    /*
     *  ThickElement Abstract Functions
     */
    /**
     * Returns the time taken for the probe to drift through part of the
     * element.
     *
     * @param probe propagating probe
     * @param dblLen length of subsection to propagate through
     * <strong>meters</strong>
     *
     * @return the elapsed time through section<strong>Units: seconds</strong>
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return super.compDriftingTime(probe, dblLen);
    }

    /**
     * Return the energy gain imparted to a particular probe. For an ideal
     * quadrupole magnet this value is always zero.
     *
     * @param dblLen dummy argument
     * @param probe dummy argument
     *
     * @return returns a zero value
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return 0.0;
    }

    /**
     * set the fringe field integral factor
     *
     * @param fint the field integral a la MAD = 1/6 for linear drop off = 0.4
     * for clamped Rogowski coil = 0.7 for unclamped Rogowski coil = 0.45 for
     * square edge - non saturating magnet
     */
    public void setFieldIntegral(double fint) {
        fringeIntegral = fint;
    }

    /**
     * set the gap height
     *
     * @param gap = full gap height (m)
     */
    public void setGapHeight(double gap) {
        gapHeight = gap;
    }

    /**
     * get field index nQ
     *
     */
    public double getFieldIndex(IProbe probe) {
        // Get  parameters
        // opposite
        double b = this.getMagField();

        //hs
        double path = this.getPathLength();
        double alpha = this.getBendAngle() / 180. * Math.PI;

        double bPathFlag = this.getFieldPathFlag();

        double rho = -1;
        if (alpha != 0) {
            rho = Math.abs(path / alpha);
        }

        double hrho = 0;
        if (rho != 0) {
            //sign of alpha = sign of h
            if (alpha < 0) {
                hrho = -1. / rho;
            } else {
                hrho = 1. / rho;
            }
        }

        double charge = probe.getSpeciesCharge();
        double eTotal = probe.getSpeciesRestEnergy() * probe.getGamma();

        double beta = probe.getBeta();

        double h = 0.2998e9 * b / (eTotal * beta * charge);

        //hs 
        LOGGER.log(Level.INFO, "h, hrho = {0} {1}", new Object[]{h, hrho});

        if (bPathFlag == 1.) {
            //if fieldPathFlag=1, use hrho (calculated from rho) instead of h(calculated from p and B)
            h = hrho;
        }
        double n = 0.;
        if (h != 0.) {
            // transform to transport notation - simpler for coding
            n = -getKQuad() / (h * h);
        }

        return n;
    }

    /**
     * Compute the partial transfer map of an ideal quadrupole for the
     * particular probe. Computes transfer map for a section of quadrupole
     * <code>dblLen</code> meters in length.
     *
     * @param dL compute transfer matrix for section of this path length
     * @param probe uses the rest and kinetic energy parameters from the probe
     *
     * @return transfer map of ideal quadrupole for particular probe
     *
     * @exception ModelException unknown quadrupole orientation
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dL)
            throws ModelException {

        double nQ = 0.;

        // Get  parameters
        // opposite
        double b = this.getMagField();

        //hs
        double path = this.getPathLength();
        double alpha = this.getBendAngle() / 180. * Math.PI;

        double bPathFlag = this.getFieldPathFlag();

        double hrho = 0;
        if (path != 0) {
            hrho = alpha / path;
        }

        double charge = probe.getSpeciesCharge();
        double eTotal = probe.getSpeciesRestEnergy() * probe.getGamma();

        double beta = probe.getBeta();

        // Compute the bending constant h  == 1/ bend radius (1/meter)
        //was default    
        double h = 0.2998e9 * b / (eTotal * beta * charge);
        LOGGER.log(Level.INFO, "h, hrho = {0} {1}", new Object[]{h, hrho});

        if (bPathFlag == 1.) {
            //if fieldPathFlag=1, use hrho (calculated from rho) instead of h(calculated from p and B)
            h = hrho;
        }

        if (h != 0.) {
            // transform to transport notation - simpler for coding
            nQ = -getKQuad() / (h * h);
        }

        double kx = Math.sqrt(1 - nQ) * h;
        //ohkawa
        if (nQ >= 1) {
            kx = Math.sqrt(nQ - 1) * h;
        }
        double ky = Math.sqrt(Math.abs(nQ)) * h;

        // The fringe field angle from the extended field:
        double entranceAnglePhi = gapHeight * h * (1. + Math.pow(Math.sin(entranceAngle), 2.)) / Math.cos(entranceAngle) * fringeIntegral;

        double exitAnglePhi = gapHeight * h * (1. + Math.pow(Math.sin(exitAngle), 2.)) / Math.cos(exitAngle) * fringeIntegral;

        // Compute the transfer matrix components
        double[][] arrB = {
            {Math.cos(kx * dL), kx != 0 ? Math.sin(kx * dL) / kx : dL},
            {-Math.sin(kx * dL) * kx, Math.cos(kx * dL)},};

        // Build the diople body tranfer matrix
        PhaseMatrix matBody = PhaseMatrix.identity();

        // the H bend
        matBody.setSubMatrix(0, 1, 0, 1, arrB);

        if (nQ < 1) {
            matBody.setElem(0, 5, (1. - Math.cos(kx * dL)) * h / (kx * kx));
            matBody.setElem(1, 5, Math.sin(kx * dL) * h / kx);
            matBody.setElem(4, 0, -Math.sin(kx * dL) * h / kx);
            matBody.setElem(4, 1, -(1. - Math.cos(kx * dL)) * h / (kx * kx));
            matBody.setElem(4, 5, -(kx * dL * beta * beta - Math.sin(kx * dL)) * ((h / kx) * (h / kx)) / kx + dL * (1. - h * h / (kx * kx)) * (1. - beta * beta));
        } else {
            matBody.setElem(0, 0, ElementaryFunction.cosh(kx * dL));
            matBody.setElem(0, 1, ElementaryFunction.sinh(kx * dL) / kx);
            matBody.setElem(1, 0, ElementaryFunction.sinh(kx * dL) * kx);
            matBody.setElem(1, 1, ElementaryFunction.cosh(kx * dL));
            matBody.setElem(0, 5, -(1. - ElementaryFunction.cosh(kx * dL)) * h / (kx * kx));
            matBody.setElem(1, 5, ElementaryFunction.sinh(kx * dL) * h / kx);
            matBody.setElem(4, 0, -ElementaryFunction.sinh(kx * dL) * h / kx);
            matBody.setElem(4, 1, (1. - ElementaryFunction.cosh(kx * dL)) * h / (kx * kx));
            matBody.setElem(4, 5, (kx * dL * beta * beta - ElementaryFunction.sinh(kx * dL)) * Math.pow((h / kx), 2.) / kx + dL * (1. + h * h / (kx * kx)) * (1. - beta * beta));
        }

        // focusing in vertical
        if (nQ >= 0) {
            matBody.setElem(2, 2, Math.cos(ky * dL));
            // = l* sin(kl)/kl
            matBody.setElem(2, 3, dL * ElementaryFunction.sinc(ky * dL));
            matBody.setElem(3, 2, -ky * Math.sin(ky * dL));
            matBody.setElem(3, 3, Math.cos(ky * dL));
            // defocusing in vertical
        } else {
            matBody.setElem(2, 2, ElementaryFunction.cosh(ky * dL));
            // = l* sin(kl)/kl
            matBody.setElem(2, 3, dL * ElementaryFunction.sinch(ky * dL));
            matBody.setElem(3, 2, ky * ElementaryFunction.sinh(ky * dL));
            matBody.setElem(3, 3, ElementaryFunction.cosh(ky * dL));
        }

        double hStar = h;
        // The entrance pole face matrix 
        PhaseMatrix matEntrance = PhaseMatrix.identity();
        matEntrance.setElem(1, 0, hStar * Math.tan(entranceAngle));
        matEntrance.setElem(3, 2, -hStar * Math.tan(entranceAngle - entranceAnglePhi));

        // The exit pole face matrix 
        PhaseMatrix matExit = PhaseMatrix.identity();
        matExit.setElem(1, 0, hStar * Math.tan(exitAngle));
        matExit.setElem(3, 2, -hStar * Math.tan(exitAngle - exitAnglePhi));

        // Multiply the 3 matrices together, starting at entrance side
        PhaseMatrix matProd1 = matBody.times(matEntrance);
        PhaseMatrix matProd2 = matExit.times(matProd1);

        return new PhaseMap(matProd2);
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

        os.println("  magnetic field     : " + this.getMagField());
        os.println("  magnet orientation : " + this.getOrientation());
    }

}
