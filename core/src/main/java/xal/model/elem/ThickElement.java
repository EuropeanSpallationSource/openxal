/*
 * ThickElement.java
 *
 * Created on September 10, 2002, 2:47 PM
 *
 */
package xal.model.elem;

import xal.model.IComposite;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IRfCavity;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.math.r3.R3x3;

/**
 * <p>
 * Base class for all modeling elements having a finite length. As such, space
 * charge will affect probes propagation through these elements.
 * </p>
 * <p>
 * The transfer matrix that is stored in the parent element is actually the
 * incremental transfer matrix of the full element. Thus, derived classes should
 * implement the method subTransferMatrix() that returns the nSecs root of the
 * full transfer matrix. Deriving beamline elements from this class allows space
 * charge kicks to be applied at nSecs equally spaced locations throughout the
 * element.
 * </p>
 *
 * @author Christopher K. Allen
 */
public abstract class ThickElement extends Element {


    /*
     *  Local Attributes
     */
    /**
     * total length of the element
     */
    private double dblLen = 0.0;

    /**
     * position of the element slice within the node
     */
    private boolean firstSlice = true;
    private boolean lastSlice = true;


    /*
     *  Initialization
     */
    /**
     * Default constructor to be used by automatic lattice generation. Creates a
     * new parameter-uninitialized instance of ThickElement.
     *
     * @param strType the string type-identifier of the element type
     */
    protected ThickElement(String strType) {
        super(strType);
    }

    /**
     * Default constructor to be used by automatic lattice generation. Creates a
     * new instance of ThickElement specifying its type identifier and its
     * instance identifier.
     *
     * @param strType the string type-identifier of the element type
     * @param strId the string identifier of the element instance
     */
    protected ThickElement(String strType, String strId) {
        super(strType, strId);
    }

    /**
     * Creates a new instance of ThickElement
     *
     * @param strType string type identifier for the element
     * @param strId string instance identifier for this element
     * @param dblLen total length of the element (<strong>in meters</strong>)
     */
    protected ThickElement(String strType, String strId, double dblLen) {
        super(strType, strId);
        this.setLength(dblLen);
    }

    /**
     * Conversion method to be provided by the user
     *
     * @param latticeElement the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);
        setLength(latticeElement.getLength());
        firstSlice = latticeElement.isFirstSlice();
        lastSlice = latticeElement.isLastSlice();
    }

    /**
     * Set the length of the element.
     *
     * @param dblLen length of element (in <strong>meters</strong>)
     *
     */
    public void setLength(double dblLen) {
        this.dblLen = dblLen;
    }


    /*
     *  IComponent Interface
     */
    /**
     * Return the total length of this element
     *
     * @return total element length (in <strong>meters</strong>)
     */
    @Override
    public double getLength() {
        return dblLen;
    }

    /**
     * Return the energy gain of the beamline element over a subsection of the
     * specified length.
     *
     * @param probe probe for which energy gain is to be determined
     * @param dblLen subsection length to calculate energyGain for
     *
     * @return the value #subEnergyGain(probe)*#getCount()
     */
    @Override
    public abstract double energyGain(IProbe probe, double dblLen);

    /**
     *
     * @see xal.model.elem.Element#elapsedTime(xal.model.IProbe, double)
     *
     * @since Jan 22, 2015 by Christopher K. Allen
     */
    @Override
    public abstract PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException;

    /**
     * Checks if this is the first subslice transfer matrix is requested for
     *
     * @param position position of the probe
     * @return is this the first subslice
     */
    protected boolean isFirstSubslice(double position) {
        return firstSlice && Math.abs(position - (getLatticePosition() - getLength() / 2.)) < 1e-6;
    }

    /**
     * Checks if this is the last subslice transfer matrix is requested for
     *
     * @param position position of the probe + subslice length
     * @return is this the last sub-slice
     */
    protected boolean isLastSubslice(double position) {
        return lastSlice && Math.abs(position - (getLatticePosition() + getLength() / 2.)) < 1e-6;
    }

    /**
     * <h2>Add Rotation and Displacement Error to Transfer Matrix</h2>
     * <p>
     * Method to add the effects of a spatial rotation and displacement to the
     * beamline element represented by the given transfer matrix.
     *
     * @param matPhi transfer matrix <strong>&Phi;</strong> to be processed
     * @param probe instance of the probe
     * @param length total think element length
     * @return transfer matrix <strong>&Phi;</strong> after applying
     * displacement and rotation
     *
     * @author Natalia Milas - January 2019
     *
     * @see PhaseMatrix
     */
    protected PhaseMatrix applyErrors(PhaseMatrix matPhi, IProbe probe, double length) {
        double px = getPhiX();
        double py = getPhiY();
        double pz = getPhiZ();
        double dx = getAlignX();
        double dy = getAlignY();
        double dz = getAlignZ();

        // Distance from the beginning of the sequence
        double positionInSequence = getLatticePosition() - getPosition();
        //Distance form the begining of the element
        double pos = probe.getPosition() - positionInSequence - (getNodePos() - getNodeLen() / 2.0);

        //check if the element is contained in a sequence which has its own misalignements
        if (this.getParent() instanceof ElementSeq) {
            double dX = (getNodePos() - this.getParent().getLength() / 2.0) * ((ElementSeq) this.getParent()).getPhiY();
            double dY = (getNodePos() - this.getParent().getLength() / 2.0) * ((ElementSeq) this.getParent()).getPhiX();

            px = px + ((ElementSeq) this.getParent()).getPhiX();
            py = py + ((ElementSeq) this.getParent()).getPhiY();
            pz = pz + ((ElementSeq) this.getParent()).getPhiZ();

            dx = dx + dX + ((ElementSeq) this.getParent()).getAlignX();
            dy = dy + dY + ((ElementSeq) this.getParent()).getAlignY();
            dz = dz + ((ElementSeq) this.getParent()).getAlignZ();
        }

        if (length != 0.0) {
            if (pz != 0.) {
                PhaseMatrix r = PhaseMatrix.rotationProduct(R3x3.newRotationZ(-pz));
                matPhi = r.transpose().times(matPhi.times(r));
            }

            if (px != 0. || py != 0. || dz != 0. || dx != 0. || dy != 0.) {
                PhaseMatrix t = PhaseMatrix.translation(new PhaseVector(py * (getNodeLen() / 2.0 - pos) - dx, -py, px * (getNodeLen() / 2.0 - pos) - dy, -px, -dz, 0.));
                matPhi = matPhi.times(t);
            }

            if (px != 0. || py != 0. || dz != 0. || dx != 0. || dy != 0.) {
                PhaseMatrix t = PhaseMatrix.translation(new PhaseVector(-py * (getNodeLen() / 2.0 - (pos + length)) + dx, py, -px * (getNodeLen() / 2.0 - (pos + length)) + dy, px, dz, 0.));
                matPhi = t.times(matPhi);
            }

            // this is to account for the focusing slice at the end of the Hard edge solenoid model
        } else if (this instanceof IdealMagSolenoid) {
            if (pz != 0.) {
                PhaseMatrix r = PhaseMatrix.rotationProduct(R3x3.newRotationZ(-pz));
                matPhi = r.transpose().times(matPhi.times(r));
            }
            if (px != 0. || py != 0. || dz != 0. || dx != 0. || dy != 0.) {
                PhaseMatrix t = PhaseMatrix.translation(new PhaseVector(-py * getNodeLen() / 2.0 - dx, -py, -px * getNodeLen() / 2.0 - dy, -px, -dz, 0.));
                matPhi = matPhi.times(t);
            }

            if (px != 0. || py != 0. || dz != 0. || dx != 0. || dy != 0.) {
                PhaseMatrix t = PhaseMatrix.translation(new PhaseVector(py * getNodeLen() / 2.0 + dx, py, px * getNodeLen() / 2.0 + dy, px, dz, 0.));
                matPhi = t.times(matPhi);
            }
        }

        return matPhi;
    }

    @Override
    public abstract double elapsedTime(IProbe probe, double dblLen);

    /**
     *
     * This is a kluge to make RF gaps work, since frequency is not defined for
     * modeling elements outside RF cavities. For such elements we simply return
     * 0 phase advance. For elements where frequency is defined, we compute the
     * phase advance as the angular frequency times the elapsed time through the
     * element (see <code>{@link #elapsedTime(IProbe, double)}</code>).
     *
     * @see xal.model.elem.Element#longitudinalPhaseAdvance(xal.model.IProbe,
     * double)
     *
     * @since Jan 22, 2015 by Christopher K. Allen
     */
    @Override
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen) {
        // We check if our parent is an RF cavity, then check all the way up the hierarchy
        IComposite cpsParent = this.getParent();
        while (cpsParent != null) {
            if (cpsParent instanceof IRfCavity) {
                IRfCavity cavParent = (IRfCavity) cpsParent;
                double f = cavParent.getCavFrequency();
                double dt = this.elapsedTime(probe, dblLen);

                return 2.0 * Math.PI * f * dt;
            }

            // Look all the way up the hierarchy until top level (i.e., parent is null)
            cpsParent = cpsParent.getParent();
        }

        return 0.0;
    }
}
