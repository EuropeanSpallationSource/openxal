/*
 * IdealMagWedgeDipole
 * 
 * Created on May 20, 2004
 *
 */
package xal.model.elem;

import xal.model.IProbe;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Bend;

/**
 * <p>
 * Represents a bending dipole magnet with arbitrary pole face angles. This is a
 * composite element constructed from three sub-elements - one
 * <code>IdealMagSectorDipole</code> sandwiched between two
 * <code>IdealDipoleFace</code> elements that provided the thin lens dynamics of
 * the tilted pole faces.
 * </p>
 * <h3>NOTE:</h3>
 * <p>
 * A rectangle dipole can be specified by setting equal exit and entrance pole
 * face angles.
 * </p>
 *
 * @author Christopher K. Allen
 *
 * @see xal.model.elem#IdealMagSectorDipole
 * @see xal.model.elem#IdealMagDipoleFace
 *
 * @deprecated This class has been replaced by <code>IdealMagWedgeDipole2</code>
 */
@Deprecated
public class IdealMagWedgeDipole extends ElectromagnetSeq {

    /*
     *  Global Attributes
     */
    /**
     * string type identifier for all IdealMagSectorDipole objects
     */
    public static final String TYPE = "IdealMagWedgeDipole";

    /**
     * storage to reserve for child components
     */
    public static final int SIZE_RESERVE = 3;


    /*
     * Local Attributes
     */
    /**
     * magnet body
     */
    private IdealMagSectorDipole magBody = new IdealMagSectorDipole();

    /**
     * magnet entrance pole face
     */
    private IdealMagDipoleFace polEntr = new IdealMagDipoleFace();

    /**
     * magnet entrance pole face
     */
    private IdealMagDipoleFace polExit = new IdealMagDipoleFace();

    /*
     * Initialization
     */
    /**
     * Default constructor - creates a new uninitialized instance of
     * <code>IdealMagWedgeDipole</code>.
     */
    public IdealMagWedgeDipole() {
        this(null);
    }

    /**
     * Create new <code>IdealMagWedgeDipole</code> object and specify its
     * instance identifier string.
     *
     * @param strId instance identifier string
     */
    public IdealMagWedgeDipole(String strId) {
        super(TYPE, strId, SIZE_RESERVE);

        this.addChild(this.polEntr);
        this.addChild(this.magBody);
        this.addChild(this.polExit);
    }

    /**
     * Set the path length of the bending dipole along the design trajectory.
     * Note that off-axis particles will experience a different path length
     * which is accounted for in the dynamics.
     *
     * @param dblLen design path length through bend in <strong>meters</strong>
     */
    public void setLength(double dblLen) {
        this.getMagBody().setLength(dblLen);
    }

    /**
     * Set the magnetic field index of the magnet evaluated at the design orbit.
     * The field index is defined as
     *
     * n := -(R0/B0)(dB/dR)
     *
     * where R0 is the radius of the design orbit, B0 is the field at the design
     * orbit (@see IdealMagSectorDipole#getField), and dB/dR is the derivative
     * of the field with respect to the path deflection - evaluated at the
     * design radius R0.
     *
     * @param dblFldInd field index of the magnet (unitless)
     */
    public void setFieldIndex(double dblFldInd) {
        this.getMagBody().setFieldIndex(dblFldInd);
    }

    /**
     * Set the gap size between the dipole magnet poles.
     *
     * @param dblGap gap size in <strong>meters</strong>
     */
    public void setGapSize(double dblGap) {
        this.getEntrFace().setGapHeight(dblGap);
        this.getMagBody().setGapHeight(dblGap);
        this.getExitFace().setGapHeight(dblGap);
    }

    /**
     * Set the entrance pole face angle with respect to the design trajectory
     *
     * @param dblAngPole pole face angle in <strong>radians</strong>
     */
    public void setEntrPoleAngle(double dblAngPole) {
        this.getEntrFace().setPoleFaceAngle(dblAngPole);
    }

    /**
     * Set the exit pole face angle with respect to the design trajectory
     *
     * @param dblAngPole pole face angle in <strong>radians</strong>
     */
    public void setExitPoleAngle(double dblAngPole) {
        this.getExitFace().setPoleFaceAngle(dblAngPole);
    }

    /**
     * Set the entrance fringe integral (a la D.C. Carey) which accounts for the
     * first-order effects of the fringing field outside the dipole magnet.
     *
     * @param dblFldInt fringe field integral (<strong>unitless</strong>)
     *
     * @see IdealMagDipoleFace#setFringeIntegral
     */
    public void setEntrFringeIntegral(double dblFldInt) {
        this.getEntrFace().setFringeIntegral(dblFldInt);
    }

    /**
     * Set the exit fringe integral (a la D.C. Carey) which accounts for the
     * first-order effects of the fringing field outside the dipole magnet.
     *
     * @param dblFldInt fringe field integral (<strong>unitless</strong>)
     *
     * @see IdealMagDipoleFace#setFringeIntegral
     */
    public void setExitFringeIntegral(double dblFldInt) {
        this.getExitFace().setFringeIntegral(dblFldInt);
    }

    /*
     * Attribute Query
     */
    /**
     * Return the entrance dipole face object of the wedge dipole.
     *
     * @return entrance pole face
     */
    public IdealMagDipoleFace getEntrFace() {
        return this.polEntr;
    }

    /**
     * Return the exit dipole face object of this wedge dipole magnet.
     *
     * @return exit pole face
     */
    public IdealMagDipoleFace getExitFace() {
        return this.polExit;
    }

    /**
     * Return the dipole magnet body object of this wedge dipole magnet. Note
     * that the body is of type <code>IdealMagSectorDipole</code> which has no
     * end effects.
     *
     * @return magnet body
     */
    public IdealMagSectorDipole getMagBody() {
        return this.magBody;
    }

    /**
     * Return the magnetic field index of the magnet evaluated at the design
     * orbit. The field index is defined as
     *
     * n := -(R0/B0)(dB/dR)
     *
     * where R0 is the radius of the design orbit, B0 is the field at the design
     * orbit (@see IdealMagSectorDipole#getField), and dB/dR is the derivative
     * of the field with respect to the path deflection - evaluated at the
     * design radius R0.
     *
     * @return field index of the magnet at the design orbit (unitless)
     */
    public double getFieldIndex() {
        return this.getMagBody().getFieldIndex();
    }

    /**
     * Return the gap size between the dipole magnet poles.
     *
     * @return gap size in <strong>meters</strong>
     */
    public double getGapHeight() {
        return this.getMagBody().getGapHeight();
    }

    /**
     * Compute the path curvature within the dipole for the given probe. The
     * path curvature is 1/R where R is the bending radius of the dipole (radius
     * of curvature). Note that for zero fields the radius of curvature is
     * infinite while the path curvature is zero.
     *
     * @param probe probe object to be deflected
     *
     * @return dipole path curvature for given probe (in
     * <strong>1/meters</strong>)
     */
    public double compCurvature(IProbe probe) {
        return this.getMagBody().compCurvature(probe);
    }

    /*
     *  IElectromagnet Interface
     */
    /**
     * Return the orientation enumeration code specifying the bending plane.
     *
     * @return ORIENT_HOR - dipole has steering action in x (horizontal) plane
     * ORIENT_VER - dipole has steering action in y (vertical) plane ORIENT_NONE
     * - error
     */
    @Override
    public int getOrientation() {
        return this.getMagBody().getOrientation();
    }

    /**
     * Get the magnetic field strength of the dipole electromagnet
     *
     * @return magnetic field (in <strong>Tesla</strong>).
     */
    @Override
    public double getMagField() {
        return this.getMagBody().getMagField();
    }

    /**
     * Set the dipole magnet bending orientation
     *
     * @param enmOrient magnet orientation enumeration code
     *
     * @see #getOrientation
     */
    @Override
    public void setOrientation(int enmOrient) {
        this.getEntrFace().setOrientation(enmOrient);
        this.getMagBody().setOrientation(enmOrient);
        this.getExitFace().setOrientation(enmOrient);
    }

    /**
     * Set the magnetic field strength of the dipole electromagnet.
     *
     * @param dblField magnetic field (in <strong>Tesla</strong>).
     */
    @Override
    public void setMagField(double dblField) {
        this.getEntrFace().setMagField(dblField);
        this.getMagBody().setMagField(dblField);
        this.getExitFace().setMagField(dblField);
    }

    /**
     * Conversion method to be provided by the user
     *
     * @param element the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);

        Bend magnet = (Bend) element.getHardwareNode();

        // Replace ThickDipole object with an IdealMagWedgeDipole2
        // First retrieve all the physical parameters for a bending dipole                
        double lenSect = element.getLength();
        double lenPath0 = magnet.getDfltPathLength();
        double angBend0 = magnet.getDfltBendAngle() * Math.PI / 180.0;
        double kQuad0 = magnet.getQuadComponent();

        // Now compute the dependent parameters
        double RBend0 = lenPath0 / angBend0;
        double fldInd0 = -kQuad0 * RBend0 * RBend0;

        double angBend = angBend0 * (lenSect / lenPath0);
        double lenPath = RBend0 * angBend;

        // Set the parameters for the new model element                
        setFieldIndex(fldInd0);

        // first piece
        if (element.isFirstSlice()) {
            setEntrPoleAngle(magnet.getEntrRotAngle() * Math.PI / 180.);
        }
        // last piece                    
        if (element.isLastSlice()) {
            setExitPoleAngle(magnet.getExitRotAngle() * Math.PI / 180.);
        }
    }
}
