/*
 * ThickMatrix.java
 *
 *
 * Created on October 18, 2002, 3:46 PM
 *
 * Modified:
 *      02/13/03 CKA    - refactored to new model architecture
 *
 */
package xal.model.elem;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * <p>
 * User element representing a general beamline element. Arbitrary beamline
 * elements are specified by providing the transfer matrix
 * <strong>generator</strong>, elapsed time, and energy gain a priori. Note that
 * for this element the transfer matrix, elapsed time, and energy gain are
 * independent of any probe objects. Note also that the generator for the
 * transfer matrix is specified, NOT the actual transfer matrix. Thus, this
 * class should be used carefully.
 * </p>
 * <p>
 * This element is derived from the ThickElement base so that space charge kicks
 * may be applied throughout the element.
 * </p>
 * <p>
 * Denoting the generator matrix as <strong>A</strong>, then the transfer matrix
 * <strong>M</strong>(<em>s</em>) for a section of length <em>s</em> is given by
 * <br>
 * <br>
 * &nbsp; &nbsp; <strong>M</strong>(<em>s</em>) =
 * <em>e</em><sup><em>s</em><strong>A</strong></sup>
 * <br>
 * <br>
 * where <em>e</em><sup><strong>A</strong></sup> is the matrix exponential.
 * </p>
 * <h3>NOTE:</h3>
 * <p>
 * Currently the class implements the matrix exponential only to second order.
 * Therefore
 * <br>
 * <br>
 * &nbsp; &nbsp;  <strong>M</strong>(<em>s</em>) = <strong>I</strong> +
 * <em>s</em><strong>A</strong>
 * + &frac12;<em>s</em><sup>2</sup><strong>A</strong><sup>2</sup>
 * ( + <em>O</em>(<em>s</em><sup>3</sup>) )
 * <br>
 * <br>
 * </p>
 *
 *
 * @author Christopher K. Allen
 */
public class ThickMatrix extends ThickElement {

    /*
     *  Global Attributes
     */
    /**
     * string type identifier for all ThickMatrix objects
     */
    public static final String TYPE = "ThickMatrix";

    /*
     *  Local Attributes
     */
    /**
     * elapsed time for all probes to propagate this element
     */
    private double dblDelT = 0.0;

    /**
     * the energy gain imparted to all probes
     */
    private double dblDelW = 0.0;

    /**
     * element transfer matrix generator for all probes
     */
    private PhaseMatrix matGen = PhaseMatrix.zero();

    /*
     *  Initialization
     */
    /**
     * Creates a new instance of ThickMatrix
     *
     * @param strId identifier of this ThickMatrix object
     * @param dblLen length of the element (<strong>in meters</strong>)
     * @param matPhiSub 7x7 transfer matrix for a subelement
     * @param dblDelW energy gain imparted of this element (<strong>in
     * electron-volts</strong>)
     */
    public ThickMatrix(String strId, double dblLen, PhaseMatrix matPhiSub, double dblDelW) {
        super(TYPE, strId, dblLen);

        this.setEnergyGain(dblDelW);
        this.setTransferMapGenerator(matPhiSub);
    }

    /**
     * Creates a new instance of ThickMatrix. Energy gain for each subelement is
     * initialized to zero.
     *
     * @param strId identifier of this ThickMatrix object
     * @param dblLen length of the element (<strong>in meters</strong>)
     * @param matPhiSub 7x7 transfer matrix for a subelement
     */
    public ThickMatrix(String strId, double dblLen, PhaseMatrix matPhiSub) {
        this(strId, dblLen, matPhiSub, 0.0);
    }

    /**
     * Creates a new instance of ThickMatrix. The sub-element energy gain is
     * initialized to zero. The sub-element transfer matrix is initialized to
     * the 7x7 identity.
     *
     * @param strId identifier of this ThickMatrix object
     * @param dblLen length of the element (<strong>in meters</strong>)
     */
    public ThickMatrix(String strId, double dblLen) {
        this(strId, dblLen, PhaseMatrix.identity(), 0.0);
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of
     * ThickMatrix
     *
     * <strong>BE CAREFUL</strong>
     */
    public ThickMatrix() {
        super(TYPE);
    }

    /**
     * Set the total elapsed time for all probes to propagate the entire
     * element.
     *
     * @param dblDelT elapsed time through element in <strong>seconds</strong>
     */
    public void setElapsedTime(double dblDelT) {
        this.dblDelT = dblDelT;
    }

    /**
     * Set the total energy gain imparted to any probe propagating through
     * entire element.
     *
     * @param dblDelW energy gain imparted to all probes (<strong>in
     * electron-volts</strong>)
     */
    public void setEnergyGain(double dblDelW) {
        this.dblDelW = dblDelW;
    }

    /**
     * Set the transfer map generator <strong>A</strong> for the element. The
     * transfer map
     * <strong>M</strong>(s) over a distance <em>s</em> is then given by
     *
     * M(s) = Exp(s*A)
     *
     * where Exp() is the matrix exponential function. Thus, the map M generated
     * by A is also a matrix.
     *
     *
     * @param matGen transfer matrix generator (probe independent)
     */
    public void setTransferMapGenerator(PhaseMatrix matGen) {
        this.matGen = matGen;
    }

    /*
     *  ThickElement Abstract Methods
     */
    /**
     * Returns the time taken for any probe to drift through part of the
     * element. The value dT(dblLen) returned by this method is given by
     *
     * dT(dblLen) = dblLen/getLength() * dblDelT
     *
     * where dblDelT is the value given to <code>#setElapsedTime</code>.
     *
     * @param probe dummy argument
     * @param dblLen length of subsection to propagate through
     * <strong>meters</strong>
     *
     * @return the elapsed time through section<strong>Units: seconds</strong>
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return (dblLen / this.getLength()) * this.dblDelT;
    }

    /**
     * Returns the energy gain imparted to any probe when going through part of
     * the element. The value dW(dblLen) returned by this method is given by
     *
     * DW(dblLen) = dblLen/getLength() * dblDelW
     *
     * where dblDelW is the value given to <code>#setEnergyGain</code>.
     *
     * @param dblLen dummy argument
     * @param probe dummy argument
     *
     * @return energy gain for each subelement (<strong>in
     * electron-volts</strong>)
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return (dblLen / this.getLength()) * this.dblDelW;
    }

    /**
     * Returns the transfer map produced by the generator matrix over the
     * distance <code>dblLen</code>.
     *
     * NOTE: Currently the transfer map returned is accurate only to order two.
     * That is the matrix exponential function is approximated by its first
     * three terms.
     *
     * @param dblLen propagation length in <strong>meters</strong>
     * @param probe dummy argument
     *
     * @return computed transfer map
     *
     * @exception ModelException this should not occur
     *
     * @see #setTransferMapGenerator
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException {
        double s = dblLen;
        PhaseMatrix A = this.matGen;
        PhaseMatrix M = PhaseMatrix.identity();

        M.plusEquals(A.times(s));
        M.plusEquals(A.times(A.times(0.5 * s * s)));

        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors
        M = applyErrors(M, probe, dblLen);

        return new PhaseMap(M);
    }

}
