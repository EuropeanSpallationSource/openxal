/*
 *  IdealMagDipoleFace
 * 
 * Created on May 17, 2004
 *
 */
package xal.extension.jels.model.elem;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IElectromagnet;
import xal.model.elem.ThinElectromagnet;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * <p>
 * Represents the action of a rotated dipole face as a thin lens effect. Note
 * that there is always an associated dipole magnet for any
 * <code>IdealMagDipoleFace</code>. The two objects should provide the same
 * values for the <code>IElectromagnet</code> interface. Note that a dipole face
 * rotation has the same effect both on beam entering the dipole or exiting the
 * dipole.
 * </p>
 * <p>
 * The model for the pole face effect is taken from D.C. Carey's book.
 * <br/>
 * <br/>
 * D.C. Carey, <i>The Optics of Charged Particle Beams</i> (Harwood, 1987)
 * </p>
 *
 *
 * @author Christopher K. Allen
 */
public class IdealMagDipoleFace extends ThinElectromagnet {

    /*
     *  Global Attributes
     */
    /**
     * Parameters for XAL MODEL LATTICE dtd
     */
    /**
     * the string type identifier for all IdealMagSteeringDipole's
     */
    public static final String s_strType = "IdealMagDipoleFace";

    /**
     * Tags for parameters in the XML configuration file
     */
    public static final String s_strParamLenEff = "EffLength";

    /**
     * Tags for parameters in the XML configuration file
     */
    public static final String s_strParamOrient = "Orientation";

    /**
     * Tags for parameters in the XML configuration file
     */
    public static final String s_strParamField = "MagField";

    /*
     *  Local Attributes
     */
    /**
     * K0 (no length)
     */
    private double K0 = 0;

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
    private double dblFringeInt = 0.0;

    /**
     * additional fringe field coefficient
     */
    private double dblFringeInt2 = 0.0;

    /**
     * flag to use design field from bending angle and path instead of bfield
     */
    private double fieldPathFlag = 0.0;
    /**
     * design orbit path length through magnet
     */
    private double dblPathLen = 0.0;

    /**
     * design orbit bend angle (radians)
     */
    private double dblBendAng = 0.0;

    /*
     * Initialization
     */
    /**
     * Default constructor - creates a new uninitialized instance of
     * IdealMagSectorDipole. This is the constructor called in automatic lattice
     * generation. Thus, all element properties are set following construction.
     */
    public IdealMagDipoleFace() {
        super(s_strType);
    }

    /**
     * Constructor providing the instance identifier for the element.
     *
     * @param strId string identifier for element
     */
    public IdealMagDipoleFace(String strId) {
        super(s_strType, strId);
    }

    /**
     * Set the angle between the pole face normal vector and the design
     * trajectory. This can be either at the magnet entrance or exit, the effect
     * is the same.
     *
     * @param dblAngPole pole face angle in <b>radians</b>
     *
     */
    public void setPoleFaceAngle(double dblAngPole) {
        this.dblAngFace = dblAngPole;
    }

    /**
     * Set the gap height between the magnet poles.
     *
     * @param dblGap gap size in <b>meters</b>
     */
    public void setGapHeight(double dblGap) {
        this.dblGap = dblGap;
    }

    /**
     * Set the second-order moment integral of the dipole fringe field as
     * described by D.C. Carey. The integral determines the amount of defocusing
     * caused by the fringe field. Denoting the integral <i>I2</i>
     * it has the definition
     *
     * I2 := Integral{ B(z)[B0 - B(z)]/(g B0^2) }dz
     *
     * where <i>g</i> is the gap height, <i>B0</i> is the hard edge value for
     * the magnetic field, and <i>B(z)</i> is the true magnetic field along the
     * design trajectory with path length parameter <i>z</i>. The integral taken
     * from a location <i>z0</i> within the magnet where <i>B(z0)=B0</i>
     * out to <i>z</i> = infinity.
     *
     * Some examples values are the following: I2 = 0.1666 linear drop off I2 =
     * 0.4 clamped Rogowski coil I2 = 0.7 unclamped Rogoski coil
     *
     * @param dblFringeInt field moment I2 (<b>dimensionless</b>)
     */
    public void setFringeIntegral(double dblFringeInt) {
        this.dblFringeInt = dblFringeInt;
    }

    public void setFringeIntegral2(double dblFringeInt2) {
        this.dblFringeInt2 = dblFringeInt2;
    }

    /**
     * sako to set field path flag
     *
     * @param ba
     */
    public void setFieldPathFlag(double ba) {
        fieldPathFlag = ba;
    }

    /**
     * Set the reference (design) orbit path-length through the magnet.
     *
     * @param dblPathLen path length of design trajectory (meters)
     *
     */
    public void setDesignPathLength(double dblPathLen) {
        this.dblPathLen = dblPathLen;
    }

    /**
     * Set the bending angle of the reference (design) orbit.
     *
     * @param dblBendAng design trajectory bending angle (radians)
     */
    public void setDesignBendAngle(double dblBendAng) {
        this.dblBendAng = dblBendAng;
    }

    /**
     * Set the design curvature <i>h</i> of the bending magnet.
     *
     * @param dbl design curvature <i>h</i> = 1/<i>R</i><sub>0</sub> where
     * <i>R</i><sub>0</sub> is the design path radius.
     *
     * @author Christopher K. Allen
     * @since Apr 19, 2011
     */
    public void setK0(double dbl) {
        K0 = dbl;
    }

    /*
     * Property Query
     */
    /**
     * This is the design bending curvature <i>h</i> = 1/<i>R</i><sub>0</sub>
     * where
     * <i>R</i><sub>0</sub> is the design bending radius.
     *
     * @return the design curvature of the bending magnet
     *
     * @author Christopher K. Allen
     * @since Apr 19, 2011
     */
    public double getK0() {
        return K0;
    }

    /**
     * Return distance between dipole magnet poles.
     *
     * @return gap height in <b>meters</b>
     */
    public double getGapHeight() {
        return this.dblGap;
    }

    /**
     * Return the angle between the pole face normal vector and the design
     * trajectory. This can be either at the magnet entrance or exit, the effect
     * is the same.
     *
     * @return pole face angle in <b>radians</b>
     */
    public double getPoleFaceAngle() {
        return this.dblAngFace;
    }

    /**
     * Set the second-order moment integral of the dipole fringe field as
     * described by D.C. Carey. The integral determines the amount of defocusing
     * caused by the fringe field.
     *
     * @return second-order integral of fringe field (<b>dimensionless</b>)
     *
     * @see IdealMagDipoleFace#setFringeIntegral(double)
     */
    public double getFringeIntegral() {
        return this.dblFringeInt;
    }

    public double getFringeIntegral2() {
        return this.dblFringeInt2;
    }

    /*
     *  IElectromagnet Interface
     */
    /**
     * Return the field path flag.
     *
     * @return field path flag = 1 (use design field) or 0 (use bField
     * parameter)
     */
    public double getFieldPathFlag() {
        return fieldPathFlag;
    }

    /**
     * Return the path length of the design trajectory through the magnet.
     *
     * @return design trajectory path length (in meters)
     */
    public double getDesignPathLength() {
        return this.dblPathLen;
    }

    /**
     * Return the bending angle of the magnet's design trajectory.
     *
     * @return design trajectory bending angle (in radians)
     */
    public double getDesignBendingAngle() {
        return this.dblBendAng;
    }

    /**
     * Compute and return the curvature of the design orbit through the magnet.
     * Note that this value is the inverse of the design curvature radius R0.
     *
     * @return the design curvature 1/R0 (in 1/meters)
     *
     * @see IdealMagSectorDipole2#compDesignBendingRadius()
     */
    public double compDesignCurvature() {
        double L0 = this.getDesignPathLength();
        double theta0 = this.getDesignBendingAngle();

        return theta0 / L0;
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
     * Compute and return the transfer map for this dipole magnet pole face
     * element.
     *
     * @param probe
     * @return
     * @throws ModelException
     *
     * @see
     * xal.sim.slg.sns.xal.model.elem.ThinElement#transferMap(gov.sns.xal.model.IProbe)
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {
        double B = -getPoleFaceAngle();
        if (B == 0) {
            return PhaseMap.identity();
        }

        double K1 = getFringeIntegral();
        double K2 = getFringeIntegral2();

        double G = getGapHeight();
        double rho = 1 / compDesignCurvature();

        double psi;

        psi = K1 * G / Math.abs(rho) * ((1 + Math.pow(Math.sin(B), 2)) / Math.cos(B)) * (1 - K1 * K2 * G / Math.abs(rho) * Math.tan(B));

        PhaseMatrix matPhi = new PhaseMatrix();

        matPhi.setElem(0, 0, 1);
        matPhi.setElem(1, 1, 1);

        matPhi.setElem(2, 2, 1);
        matPhi.setElem(3, 3, 1);

        switch (this.getOrientation()) {
            case IElectromagnet.ORIENT_HOR:
                matPhi.setElem(1, 0, Math.tan(B) / Math.abs(rho));
                matPhi.setElem(3, 2, -Math.tan(B - psi) / Math.abs(rho));
                break;

            case IElectromagnet.ORIENT_VER:
                matPhi.setElem(1, 0, -Math.tan(B - psi) / Math.abs(rho));
                matPhi.setElem(3, 2, Math.tan(B) / Math.abs(rho));
                break;

            default:
                throw new ModelException("IdealMagDipoleFace#transferMap() - bad magnet orientation.");
        }

        matPhi.setElem(4, 4, 1);
        matPhi.setElem(5, 5, 1);
        matPhi.setElem(6, 6, 1);
        
        //Jan 2019 Apply the slice error form the ThinElement
        PhaseMatrix Phidx = applyErrors(matPhi,0.0);
        matPhi = Phidx;

        return new PhaseMap(matPhi);
    }
}
