package xal.model.elem;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.DriftSpace;
import xal.model.IProbe;

import java.io.PrintWriter;

/**
 * <p>
 * Models an ideal solenoid magnet. I don't know who implemented this class or
 * when he or she did so. I can't really comment on details yet.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 19, 2011
 */
public class IdealMagSolenoid extends ThickElectromagnet {

    /*
     * Global Attributes
     */
    /**
     * string type identifier for all IdealMagSolenoid objects
     */
    public static final String TYPE = "IdealMagSolenoid";

    /**
     * Parameters for XAL MODEL LATTICE dtd
     */
    public static final String PARAM_FIELD = "MagField";

    /*
     * Initialization
     */
    /**
     * Creates a new instance of IdealMagSolenoid
     *
     * @param strId identifier for this IdealMagSolenoid object
     * @param dblFld field gradient strength (in <strong>Tesla</strong>)
     * @param dblLen length of the solenoid
     */
    public IdealMagSolenoid(String strId, double dblFld, double dblLen) {
        super(TYPE, strId, dblLen);

        this.setMagField(dblFld);
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of
     * IdealMagSolenoid
     *
     * <strong>BE CAREFUL</strong>
     */
    public IdealMagSolenoid() {
        super(TYPE);
    }


    /*
     *  ThickElement Protocol
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
     * solenoid magnet this value is always zero.
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
     * Compute the partial transfer map of an ideal solenoid for the particular
     * probe. Computes transfer map for a section of solenoid
     * <code>dblLen</code> meters in length.
     *
     * @param probe supplies the charge, rest and kinetic energy parameters
     * @param length compute transfer matrix for section of this length
     * @return transfer map of ideal quadrupole for particular probe
     */
    @Override
    public PhaseMap transferMap(final IProbe probe, final double length) {
        double charge = probe.getSpeciesCharge();
        double eR = probe.getSpeciesRestEnergy();
        double beta = probe.getBeta();
        double gamma = probe.getGamma();

        // focusing constant (radians/meter)
        final double k = (charge * LIGHT_SPEED * getMagField()) / (2. * eR * beta * gamma);

        // Compute the transfer matrix components
        double r12 = Math.sin(k * length) * Math.cos(k * length) / k;
        double r13 = Math.sin(k * length) * Math.cos(k * length);
        double r14 = Math.sin(k * length) * Math.sin(k * length) / k;

        final double[][] arr0 = DriftSpace.transferDriftPlane(length);

        PhaseMatrix mEntrance = new PhaseMatrix();
        PhaseMatrix mBody = new PhaseMatrix();
        PhaseMatrix mExit = new PhaseMatrix();

        //Build each matrix
        mEntrance.setElem(0, 0, 1);
        mEntrance.setElem(1, 1, 1);
        mEntrance.setElem(2, 2, 1);
        mEntrance.setElem(3, 3, 1);
        mEntrance.setElem(1, 2, k);
        mEntrance.setElem(3, 0, -k);

        mExit.setElem(0, 0, 1);
        mExit.setElem(1, 1, 1);
        mExit.setElem(2, 2, 1);
        mExit.setElem(3, 3, 1);
        mExit.setElem(1, 2, -k);
        mExit.setElem(3, 0, k);

        mBody.setElem(0, 0, 1);
        mBody.setElem(1, 1, Math.cos(2. * k * length));
        mBody.setElem(2, 2, 1);
        mBody.setElem(3, 3, Math.cos(2. * k * length));
        mBody.setElem(0, 1, r12);
        mBody.setElem(0, 2, 0.0);
        mBody.setElem(0, 3, r14);
        mBody.setElem(1, 0, 0.0);
        mBody.setElem(1, 2, 0.0);
        mBody.setElem(1, 3, 2. * r13);
        mBody.setElem(2, 0, 0.0);
        mBody.setElem(2, 1, -1. * r14);
        mBody.setElem(2, 3, r12);
        mBody.setElem(3, 0, 0.0);
        mBody.setElem(3, 1, -2. * r13);
        mBody.setElem(3, 2, 0.0);

        // Build the tranfer matrix from its component blocks
        PhaseMatrix matPhi;

        if (isFirstSubslice(probe.getPosition())) {
            matPhi = mBody.times(mEntrance);
        } else {
            matPhi = mBody;
        }

        // a drift space longitudinally       
        matPhi.setSubMatrix(4, 5, 4, 5, arr0);
        // homogeneous coordinates      
        matPhi.setElem(6, 6, 1.0);

        // apply alignment and rotation errors taking care of the thin matrix entrance and exit slices
        // 2018-07-02 Natalia Milas
        matPhi = applyErrors(matPhi, probe, length);

        if (isLastSubslice(probe.getPosition() + length)) {
            mExit = applyErrors(mExit, probe, 0);
            matPhi = mExit.times(matPhi);
        }

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

        os.println("  magnetic field     : " + this.getMagField());
        os.println("  magnet orientation : " + this.getOrientation());
    }

}
