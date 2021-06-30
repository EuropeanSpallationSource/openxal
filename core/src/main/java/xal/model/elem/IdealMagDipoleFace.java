/*
 *  IdealMagDipoleFace
 *
 * Created on May 17, 2004
 *
 */
package xal.model.elem;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.BendingMagnet;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IElectromagnet;

/**
 * Represents the action of a rotated dipole face as a thin lens effect. Note
 * that there is always an associated dipole magnet for any
 * <code>IdealMagDipoleFace</code>. The two objects should provide the same
 * values for the <code>IElectromagnet</code> interface. Note that a dipole face
 * rotation has the same effect both on beam entering the dipole or exiting the
 * dipole. The model for the pole face effect is taken from D.C. Carey's book.
 *
 * @author Christopher K. Allen
 *
 * @see "D.C. Carey, The Optics of Charged Particle Beams (Harwood, 1987)"
 *
 * @deprecated This class has been replaced by <code>IdealMagDipoleFace2</code>
 */
@Deprecated
public class IdealMagDipoleFace extends ThinElectromagnet {

    /*
     *  Global Attributes
     */
    /**
     * the string type identifier for all IdealMagSteeringDipole's
     */
    public static final String TYPE = "IdealMagDipoleFace";

    /**
     * Parameters for XAL MODEL LATTICE dtd
     */
    public static final String PARAM_LEN_EFF = "EffLength";
    public static final String PARAM_ORIENT = "Orientation";
    public static final String PARAM_FIELD = "MagField";

    /*
     *  Local Attributes
     */
    /**
     * The dipole gap height (m)
     */
    private double dblGap = 0.0;

    /**
     * internal pole face angle made with respect to the design trajectory
     */
    private double dblAngFace = 0.0;

    /**
     * second moment of fringe field defined a al Carey
     */
    private double dblMmtFrng = 0.0;

    /*
     * Initialization
     */
    /**
     * Default constructor - creates a new uninitialized instance of
     * IdealMagSectorDipole. This is the constructor called in automatic lattice
     * generation. Thus, all element properties are set following construction.
     */
    public IdealMagDipoleFace() {
        super(TYPE);
    }

    /**
     * Constructor providing the instance identifier for the element.
     *
     * @param strId string identifier for element
     */
    public IdealMagDipoleFace(String strId) {
        super(TYPE, strId);
    }

    /**
     * Set the angle between the pole face normal vector and the design
     * trajectory. This can be either at the magnet entrance or exit, the effect
     * is the same.
     *
     * @param dblAngPole pole face angle in <strong>radians</strong>
     *
     */
    public void setPoleFaceAngle(double dblAngPole) {
        this.dblAngFace = dblAngPole;
    }

    /**
     * Set the gap height between the magnet poles.
     *
     * @param dblGap gap size in <strong>meters</strong>
     */
    public void setGapHeight(double dblGap) {
        this.dblGap = dblGap;
    }

    /**
     * Set the second-order moment integral of the dipole fringe field as
     * described by D.C. Carey. The integral determines the amount of defocusing
     * caused by the fringe field. Denoting the integral <em>I2</em>
     * it has the definition
     *
     * I2 := Integral{ B(z)[B0 - B(z)]/(g B0^2) }dz
     *
     * where <em>g</em> is the gap height, <em>B0</em> is the hard edge value
     * for the magnetic field, and <em>B(z)</em> is the true magnetic field
     * along the design trajectory with path length parameter <em>z</em>. The
     * integral taken from a location <em>z0</em> within the magnet where
     * <em>B(z0)=B0</em>
     * out to <em>z</em> = infinity.
     *
     * Some examples values are the following: I2 = 0.1666 linear drop off I2 =
     * 0.4 clamped Rogowski coil I2 = 0.7 unclamped Rogoski coil
     *
     * @param dblFrngMmt field moment I2 (<strong>dimensionless</strong>)
     */
    public void setFringeIntegral(double dblFrngMmt) {
    }

    //hs bend angle
    private double bendAngle = 0.0;
    private double fieldPathFlag = 0.0;
    private double pathLength = 0.0;

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
     * Accessors
     */
    /**
     * Return distance between dipole magnet poles.
     *
     * @return gap height in <strong>meters</strong>
     */
    public double getGapHeight() {
        return this.dblGap;
    }

    /**
     * Return the angle between the pole face normal vector and the design
     * trajectory. This can be either at the magnet entrance or exit, the effect
     * is the same.
     *
     * @return pole face angle in <strong>radians</strong>
     */
    public double getPoleFaceAngle() {
        return this.dblAngFace;
    }

    /**
     * Set the second-order moment integral of the dipole fringe field as
     * described by D.C. Carey. The integral determines the amount of defocusing
     * caused by the fringe field.
     *
     * @return second-order integral of fringe field
     * (<strong>dimensionless</strong>)
     *
     * @see IdealMagDipoleFace#setFringeIntegral(double)
     */
    public double getFringeIntegral() {
        return this.dblMmtFrng;
    }

    /*
     * IElement Interface
     */
    /**
     * Returns the time taken for the probe to propagate through element.
     *
     * @param probe propagating probe
     *
     * @return value of zero
     */
    @Override
    public double elapsedTime(IProbe probe) {
        return 0.0;
    }

    /**
     * Return the energy gain for this Element.
     *
     * @param probe propagating probe
     *
     * @return value of zero
     */
    @Override
    public double energyGain(IProbe probe) {
        return 0.0;
    }

    /**
     * @param probe
     * @return
     * @throws ModelException
     *
     * @see xal.model.elem.ThinElement#transferMap(xal.model.IProbe)
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {

        // Get  parameters
        // opposite
        double B = this.getMagField();
        double g = this.getGapHeight();
        double I2 = this.getFringeIntegral();
        double h = BendingMagnet.compCurvature(probe, B);

        double bPathFlag = this.getFieldPathFlag();

        if (bPathFlag == 1.) {
            //hs calculate hrho
            double path = this.getPathLength();
            double alpha = this.getBendAngle();
            double hrho = 0;
            if (path != 0) {
                hrho = alpha / path;
            }
            //if fieldPathFlag=1, use hrho (calculated from rho) instead of h(calculated from p and B)
            h = hrho;
        }

        // The fringe field angle from the extended field:
        double dblAngFace = this.getPoleFaceAngle();
        double sin = Math.sin(dblAngFace);
        double cos = Math.cos(dblAngFace);
        double dblAngDefl = g * h * ((1. + sin * sin) / cos) * I2;

        // Compute the transfer matrix components
        double hStar = h;
        PhaseMatrix matPhi = PhaseMatrix.identity();

        switch (this.getOrientation()) {
            case IElectromagnet.ORIENT_HOR:
                matPhi.setElem(1, 0, hStar * Math.tan(dblAngFace));
                matPhi.setElem(3, 2, -hStar * Math.tan(dblAngFace - dblAngDefl));
                break;

            case IElectromagnet.ORIENT_VER:
                matPhi.setElem(1, 0, -hStar * Math.tan(dblAngFace - dblAngDefl));
                matPhi.setElem(3, 2, hStar * Math.tan(dblAngFace));
                break;

            default:
                throw new ModelException("IdealMagDipoleFace#transferMap() - bad magnet orientation.");
        }

        //Jan 2019 Apply the slice error form the ThinElement
        PhaseMatrix Phidx = applyErrors(matPhi, 0.0);
        matPhi = Phidx;

        return new PhaseMap(matPhi);

    }

}
