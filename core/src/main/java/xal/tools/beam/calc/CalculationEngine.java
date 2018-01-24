/**
 * CalculationEngine.java
 *
 * Author  : Christopher K. Allen
 * Since   : Aug 14, 2013
 */
package xal.tools.beam.calc;

import java.util.EnumSet;

import xal.model.probe.TwissProbe;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseMatrix.IND;
import xal.tools.beam.Twiss3D.IND_3D;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.math.r3.R3;
import xal.tools.math.r4.R4;
import xal.tools.math.r4.R4x4;
import xal.tools.math.r6.R6;
import xal.tools.math.r6.R6x6;

/**
 * Class <code>CalculationEngine</code> performs all the common numerical calculation when
 * computing machine parameters from simulation data.  There are common data produced
 * by ring simulations and linac simulation, however, the interpretation is different.
 * It is up to the child classes to interpret the results of these calculations.
 *
 * @author Christopher K. Allen
 * @author Patrick Scruggs
 * @since  Aug 14, 2013
 */
public abstract class CalculationEngine {



    /*
     * Global Constants
     */


    /** small number used to determine the conditioning of the linear system */
    static final private double         DBL_CND_MIN = 1.0e12;

    /** small number used to define a zero phase advance */
    static final private double         DBL_EPS_PHSADV = 1.0e-2;

    /** the number 2&pi; */
    static final private double         DBL_2PI = 2 * Math.PI;

    /** The zero vector that we use for comparisons for numerical checks */
    static final private R4             VEC_ZERO = R4.newZero();



    /** This is an artifact of <code>TransferMapState</code> number of (phase plane) modes */
    static final private int NUM_MODES = 3;


    /*
     * Initialization
     */

    public CalculationEngine() {
    }


    /**
     * <p>
     * Taken from <code>TransferMapState</code>.
     * </p>
     * <p>
     * Calculate the fixed point solution vector representing the closed orbit at the
     * location of this element.
     * We first attempt to find the fixed point for the full six phase space coordinates.
     * Let <b>&Phi;</b> denote the one-turn map for a ring.  The fixed point
     * <b>z</b> &in; <b>R</b><sup>6</sup>&times;{1} in homogeneous phase space coordinates
     * is that which is invariant under <b>&Phi;</b>, that is,
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;z</b> = <b>z</b> .
     * <br>
     * <br>
     * This method returns that vector <b>z</b>.
     * </p>
     * <p>
     * Recall that the <i>homogeneous</i> transfer matrix <b>&Phi;</b> for the ring
     * has final row that represents the translation <b>&Delta;</b> of the particle
     * for the circuit around the ring.  The 6&times;6 sub-matrix of <b>&Phi;</b> represents
     * the (linear) action of the bending magnetics and quadrupoles and corresponds to the
     * matrix <b>T</b> &in; <b>R</b><sup>6&times;</sup> (here <b>T</b> is linear).
     * Thus, we can write the linear operator <b>&Phi;</b>
     * as the augmented system
     * <br>
     * <br>
     * <pre>
     * &nbsp; &nbsp; <b>&Phi;</b> = |<b>T</b> <b>&Delta;</b> |,   <b>z</b> &equiv; |<b>p</b>| ,
     *         |<b>0</b> 1 |        |1|
     * </pre>
     * where <b>p</b> is the projection of <b>z</b> onto the ambient phase space
     * <b>R</b><sup>6</sup> (without homogeneous the homogeneous coordinate).
     * coordinates).
     * </p>
     * <p>
     * Putting this together we get
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;z</b> = <b>Tp</b> + <b>&Delta;</b> = <b>p</b> ,
     * <br>
     * <br>
     * to which the solution is
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>p</b> = -(<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b>
     * <br>
     * <br>
     * assuming it exists.  The question of solution existence falls upon the
     * resolvent <b>R</b> &equiv; (<b>T</b> - <b>I</b>)<sup>-1</sup> of <b>T</b>.
     * By inspection we can see that <b>p</b> is defined so long as the eigenvalues
     * of <b>T</b> are located away from 1.
     * In this case the returned value is the augmented vector
     * (<b>p</b> 1)<sup><i>T</i></sup> &in; <b>R</b><sup>6</sup> &times; {1}.
     * </p>
     * <p>
     * When the eigenvectors do contain 1, we attempt to find the solution for the
     * transverse phase space.  That is, we take vector <b>p</b> &in; <b>R</b><sup>4</sup>
     * and <b>T</b> &in; <b>R</b><sup>4&times;4</sup> where
     * <b>T</b> = proj<sub>4&times;4</sub> <b>&Phi;</b>.  The returned value is then
     * <b>z</b> = (<b>p</b> 0 0 1)<sup><i>T</i></sup>.
     *
     * @param   matPhi      the one-turn transfer matrix (full-turn matrix)
     *
     * @return              fixed point solution for the given phase matrix
     *
     * @author Thomas Pelaia
     * @author Christopher K. Allen
     * @since  Aug 14, 2013
     */
    protected PhaseVector calculateFixedPoint(PhaseMatrix matPhi) {

        R6x6    matT = matPhi.projectR6x6();
        R6x6    matI = R6x6.newIdentity();
        R6x6    matR = matI.minus( matT );

        R6      vecdZ = matPhi.projectColumn(IND.HOM);

        double  dblCndNum = matR.conditionNumber();
        if (dblCndNum < DBL_CND_MIN) {

            R6          vecp = matR.solve(vecdZ);
            PhaseVector vecP = PhaseVector.embed(vecp);

            return vecP;
        }

        // The system is indeterminant in full 6D phase space
        //   So compute the solution for the transverse phase plane
        R4x4     matTt = matPhi.projectR4x4();
        R4x4     matIt = R4x4.newIdentity();
        R4x4     matRt = matIt.minus( matTt );

        R4       vecZt = new R4();
        vecdZ.projectOnto(vecZt);

        // Solve system and return it
        R4      vecPt    = matRt.solve(vecZt);

        PhaseVector     vecP = PhaseVector.newZero();
        vecPt.embedIn(vecP);

        return  vecP;
    }


    /*
     * Beam Operations
     */


    /**
     * <p>
     * Taken from <code>TransferMapTrajectory</code>.
     * </p>
     * <p>
     * Calculates the phase advance for the given transfer map assuming
     * periodicity, that is, the given matrix represents the transfer matrix <b>&Phi;</b>
     * of at least one cell in a periodic structure.  The Courant-Snyder parameters
     * of the machine (beam) must be invariant under the action of the transfer matrix
     * (this indicates a periodic focusing structure
     * where the beam envelope is modulated by that structure).  One phase advance
     * is provided for each phase plane, i.e.,
     * (&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>z</i></sub>, &sigma;<sub><i>z</i></sub>).
     * In the case the given map
     * is the transfer map for one cell in a periodic lattice, in that case the returned
     * value is the phase advance per cell,
     * &sigma;<sub><i>cell</i></sub> of the periodic system.
     * </p>
     * <p>
     * When the given map is the full turn map of a ring then the phase advances are the
     * betatron phases for the ring.  Specifically, the sinusoidal phase that the particles
     * advance after each completion of a ring traversal, modulo 2&pi;
     * (that is, we only take the fractional part).
     * </p>
     * <p>
     * The basic computation is
     * <br>
     * <br>
     * &nbsp; &nbsp;  &sigma; = cos<sup>-1</sup>[&frac12; Tr <b>&Phi;</b><sub>&alpha;&alpha;</sub>] ,
     * <br>
     * <br>
     * where <b>&Phi;</b></b><sub>&alpha;&alpha;</sub> is the 2&times;2 block diagonal
     * of the the provided transfer matrix for the &alpha; phase plane,
     * and Tr <b>&Phi;</b></b><sub>&alpha;&alpha;</sub> indicates the trace of matrix
     * <b>&Phi;</b></b><sub>&alpha;&alpha;</sub>.
     * </p>
     *
     * @param   matPhiCell  transfer matrix for a cell in a periodic structure
     *
     * @return  vector of phase advances (&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>y</i></sub>, &sigma;<sub><i>z</i></sub>)
     *
     * @author Thomas Pelaia
     * @author Christopher K. Allen
     * @since  Aug 14, 2013
     */
    protected R3 calculatePhaseAdvPerCell(PhaseMatrix matPhiCell) {
        double[]    arrTunes = new double[NUM_MODES];

        for ( int imode = 0 ; imode < NUM_MODES ; imode++ ) {
            final int index = 2 * imode;
            double trace = matPhiCell.getElem( index, index ) + matPhiCell.getElem( index + 1, index + 1 );
            double m12   = matPhiCell.getElem( index, index+1 );

            // problem is when abs(trace)>1, then _tunes are Double.NaN
//          double sigma = Math.acos( trace / 2 );
            double trmod = (trace > 2.0) ? 2.0 : Math.max(trace, -2);
            double sigma = Math.acos( trmod/2.0 );

            // H. Sako - Look at the sign of M12, and determine the phase
            // since M12=beta*sin(mu) and beta>0, if M12>0, sin(mu)>0, 0<mu<pi
            //                                    if M12<0, sin(mu)<0, -pi<mu<0
            if (m12<0)
                sigma = -sigma;

            arrTunes[imode] = sigma;
        }

        return new R3(arrTunes);
    }

    /**
     * <p>
     * Taken from <code>TransferMapTrajectory</code>.
     * </p>
     * <p>
     * Calculates the fractional tunes for the given transfer map assuming
     * periodicity.  That is, the Courant-Snyder parameters of the machine (beam) must be invariant
     * under the action of the transfer matrix (this indicates a periodic focusing structure
     * where the beam envelope is modulated by that structure).  One tune is provided for
     * each phase plane, i.e.,
     * (&nu;<sub><i>x</i></sub>, &nu;<sub><i>z</i></sub>, &nu;<sub><i>z</i></sub>).  The given map
     * must in the least be the transfer map for one cell in a periodic
     * lattice.  In that case the returned value is the fractional phase advance per cell,
     * &sigma;<sub><i>cell</i></sub>/2&pi; of the periodic system.
     * </p>
     * <p>
     * When the given map is the full turn map of a ring then the tunes are the
     * betatron tunes for the ring.  Specifically, the fraction of a cycle that the particles
     * advance after each completion of a ring traversal, modulo 1 (that is, we only take the
     * fractional part).
     * </p>
     * <p>
     * The basic computation is
     * <br>
     * <br>
     * &nbsp; &nbsp;  &nu; = (1/2&pi;) cos<sup>-1</sup>[&frac12; Tr <b>&Phi;</b>] ,
     * <br>
     * <br>
     * where <b>&Phi;</b> is the 2&times;2 diagonal block of the transfer matrix
     * corresponding to the phase plane of interest.
     * </p>
     * <p>
     * The calculations are actually done by the method
     * <code>{@link #calculatePhaseAdvPerCell(PhaseMatrix)}</code> then normalized by
     * 2&pi;.
     * </p>
     *
     * @param   matPhiCell  transfer matrix for a cell in a periodic structure
     *
     * @return      the array of betatron tunes (&nu;<sub><i>x</i></sub>, &nu;<sub><i>y</i></sub>, &nu;<sub><i>z</i></sub>)
     *
     * @author Thomas Pelaia
     * @author Christopher K. Allen
     * @since  Aug 14, 2013
     */
    protected R3 calculateTunePerCell(PhaseMatrix matPhiCell) {

        // Compute the phase advance for the full turn matrix
        R3          vecTunes = this.calculatePhaseAdvPerCell(matPhiCell);

        for (R3.IND i : R3.IND.values()) {

            // Normalized the phase advances by 2 pi
            double  dblTune = vecTunes.getElem(i)/DBL_2PI % 1.0;

            vecTunes.setElem(i, dblTune);
        }

        return vecTunes;
    }

    /**
     * <p>
     * Taken from <code>TransferMapState</code>.
     * </p>
     * <p>
     * Compute and return the particle phase advance for under the action
     * of the given phase matrix when used as a transfer matrix.
     * </p>
     * <p>
     * Calculates the phase advances given the initial and final
     * Courant-Snyder &alpha; and &beta; values provided. This is the general
     * phase advance of the particle through the transfer matrix <b>&Phi;</b>, and no special
     * requirements are placed upon <b>&Phi;</b>.   One phase
     * advance is provided for each phase plane, i.e.,
     * (&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>z</i></sub>, &sigma;<sub><i>z</i></sub>).
     * </p>
     * <p>
     * The definition of phase advance &sigma; is given by
     * <br>
     * <br>
     * &nbsp; &nbsp; &sigma;(<i>s</i>) &equiv; &int;<sup><i>s</i></sup> [1/&beta;(<i>t</i>)]<i>dt</i> ,
     * <br>
     * <br>
     * where &beta;(<i>s</i>) is the Courant-Snyder, envelope function, and the integral
     * is taken along the interval between the initial and final Courant-Snyder
     * parameters.
     * </p>
     * <p>
     * The basic relation used to compute &sigma; is the following:
     * <br>
     * <br>
     * &nbsp; &nbsp;  &sigma; = sin<sup>-1</sup> &phi;<sub>12</sub>/(&beta;<sub>1</sub>&beta;<sub>2</sub>)<sup>&frac12;</sup> ,
     * <br>
     * <br>
     * where &phi;<sub>12</sub> is the element of <b>&Phi;</b> in the upper right corner of each
     * 2&times;2 diagonal block, &beta;<sub>1</sub> is the initial beta function value (provided)
     * and &beta;<sub>2</sub> is the final beta function value (provided).
     * </p>
     *
     * @param   matPhi    the transfer matrix that propagated the Twiss parameters
     * @param   twsInit   initial Twiss parameter before application of matrix
     * @param   twsFinal  final Twiss parameter after application of matrix
     *
     * @return      the array of betatron tunes (&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>z</i></sub>, &sigma;<sub><i>z</i></sub>)
     *
     * @author  Christopher K. Allen
     * @author  Thomas Pelaia
     * @since   Jun, 2004
     * @version Oct, 2013
     */
    protected R3 calculatePhaseAdvance(PhaseMatrix matPhi, Twiss[] twsInit, Twiss[] twsFinal) {

        final Twiss[] twsFnl = twsFinal;
        final Twiss[] twsInt = twsInit;

        final double[] arrPhsAdv = new double[3];

        for ( int mode = 0 ; mode < 3; mode++ ) {
            final double dblAlphInt = twsInt[mode].getAlpha();
            final double dblBetaInt = twsInt[mode].getBeta();
            final double dblBetaFnl = twsFnl[mode].getBeta();

            final double dblM11 = matPhi.getElem( 2*mode, 2*mode );
            final double dblM12 = matPhi.getElem( 2*mode, 2*mode + 1 );

            // Compute the sine and cosine of phase advance for this plane
            double  dblRtBetas = Math.sqrt( dblBetaFnl * dblBetaInt );

            double dblSinPhs = dblM12 / dblRtBetas;
            double dblCosPhs = (dblM11*dblBetaInt - dblM12*dblAlphInt) / dblRtBetas;

            dblSinPhs = Math.max( Math.min( dblSinPhs, 1.0 ), -1.0 );     // make sure it is in the range [-1, 1]
            dblCosPhs = Math.max( Math.min( dblCosPhs, 1.0 ), -1.0);

            // atan() returns a value phi in the domain [-pi,pi]
            //  Put it in the range [0, 2pi]
            double   dblPhsAdv  = Math.atan2(dblSinPhs, dblCosPhs);

            if (dblPhsAdv<0.0 && dblPhsAdv>-DBL_EPS_PHSADV)
                    dblPhsAdv = 0.0;
            if (dblPhsAdv < 0.0)
                dblPhsAdv += DBL_2PI;

            arrPhsAdv[mode] = dblPhsAdv;
        }

        // Pack into vector format and return
        R3 vecPhases = new R3( arrPhsAdv );

        return vecPhases;
    }

    /**
     * <p>
     * Taken from <code>TransferMapState</code>.
     * </p>
     * <p>
     * Compute and return the particle phase advance for under the action
     * of the given phase matrix when used as a transfer matrix.
     * </p>
     * <p>
     * Calculates the phase advances given the initial and final
     * Courant-Snyder &alpha; and &beta; values provided. This is the general
     * phase advance of the particle through the transfer matrix <b>&Phi;</b>, and no special
     * requirements are placed upon <b>&Phi;</b>.   One phase
     * advance is provided for each phase plane, i.e.,
     * (&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>z</i></sub>, &sigma;<sub><i>z</i></sub>).
     * </p>
     * <p>
     * The definition of phase advance &sigma; is given by
     * <br>
     * <br>
     * &nbsp; &nbsp; &sigma;(<i>s</i>) &equiv; &int;<sup><i>s</i></sup> [1/&beta;(<i>t</i>)]<i>dt</i> ,
     * <br>
     * <br>
     * where &beta;(<i>s</i>) is the Courant-Snyder, envelope function, and the integral
     * is taken along the interval between the initial and final Courant-Snyder
     * parameters.
     * </p>
     * <p>
     * The basic relation used to compute &sigma; is the following:
     * <br>
     * <br>
     * &nbsp; &nbsp;  &sigma; = sin<sup>-1</sup> &phi;<sub>12</sub>/(&beta;<sub>1</sub>&beta;<sub>2</sub>)<sup>&frac12;</sup> ,
     * <br>
     * <br>
     * where &phi;<sub>12</sub> is the element of <b>&Phi;</b> in the upper right corner of each
     * 2&times;2 diagonal block, &beta;<sub>1</sub> is the initial beta function value (provided)
     * and &beta;<sub>2</sub> is the final beta function value (provided).
     * </p>
     *
     * @param   matPhi    the transfer matrix that propagated the Twiss parameters
     * @param   twsInit   initial Twiss parameter before application of matrix
     * @param   twsFinal  final Twiss parameter after application of matrix
     *
     * @return      the array of betatron tunes (&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>z</i></sub>, &sigma;<sub><i>z</i></sub>)
     *
     * @author  Christopher K. Allen
     * @author  Thomas Pelaia
     * @since   Jun, 2004
     * @version Oct, 2013
     *
     * @deprecated  Does not determine the phase quadrants correctly and therefore cannot produce
     *              an accurate phase advance
     */
    @Deprecated
    protected R3 calculatePhaseAdvance_old(PhaseMatrix matPhi, Twiss[] twsInit, Twiss[] twsFinal) {

        final Twiss[] twsFnl = twsFinal;
        final Twiss[] twsInt = twsInit;

        final double[] arrPhsAdv = new double[3];

        for ( int mode = 0 ; mode < 3; mode++ ) {
            final double dblBetaFnl = twsFnl[mode].getBeta();
            final double dblBetaInt = twsInt[mode].getBeta();

            final double dblAlphInt = twsInt[mode].getAlpha();

            final double dblM11 = matPhi.getElem( 2*mode, 2*mode );
            final double dblM12 = matPhi.getElem( 2*mode, 2*mode + 1 );

            // Compute the phase advance for this plane
            double dblSinPhs = dblM12 / Math.sqrt( dblBetaFnl * dblBetaInt );
            dblSinPhs = Math.max( Math.min( dblSinPhs, 1.0 ), -1.0 );     // make sure it is in the range [-1, 1]

            // This returns a value phi in the domain [-pi/2,pi/2]
            final double   dblPhsAdv  = Math.asin( dblSinPhs );

            // Compute the cosine of phase advance for identifying the phase quadrant
            //    Sako - (I think the following is wrong)
            //      final double cosPhase = m11 * Math.sqrt( beta / initialBeta ) - initialAlpha * sinPhase;
            //    Replaced by
            final double cosPhase = dblM11 * Math.sqrt( dblBetaInt / dblBetaFnl) - dblAlphInt * dblSinPhs;

            // Put the phase advance in the positive real line
            if ( cosPhase >= 0 ) {
                if ( dblSinPhs >= 0 ) {

                    arrPhsAdv[mode] = dblPhsAdv;
                } else {

                    arrPhsAdv[mode] = 2 * Math.PI + dblPhsAdv;
                }

            } else {

                arrPhsAdv[mode] = Math.PI - dblPhsAdv;
            }
        }

        // Pack into vector format and return
        R3 vecPhases = new R3( arrPhsAdv );

        return vecPhases;
    }


    /**
     * <p>
     * Taken from <code>TransferMapState</code>.
     * </p>
     * <p>
     * Calculates the matched Courant-Snyder parameters for the given
     * period cell transfer matrix and phase advances.  When the given transfer matrix
     * is the full-turn matrix for a ring the computed Twiss parameters are the matched
     * envelopes for the ring at that point.
     * </p>
     * <p>
     * The given matrix <b>&Phi;</b> is assumed to be the transfer matrix through
     * at least one cell in a periodic lattice.  Internally, the array of phase advances
     * {&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>y</i></sub>, &sigma;<sub><i>x</i></sub>}
     * are assumed to be the particle phase advances through the cell for the matched
     * solution.   These are computed with the method
     * <code>{@link #calculatePhaseAdvPerCell(PhaseMatrix)}</code>.
     * </p>
     * <p>
     * The returned Courant-Snyder parameters (&alpha;, &beta;, &epsilon;) are invariant
     * under the action of the given phase matrix, that is, they are matched.  All that
     * is required are &alpha; and &beta; since &epsilon; specifies the size of the beam
     * envelope.  Consequently the returned &epsilon; is <code>NaN</code>.
     * </p>
     * The following are the calculations used for the Courant-Snyder parameters of a
     * single phase plane:
     * <br>
     * <br>
     * &nbsp; &nbsp; &alpha; &equiv; -<i>ww'</i> = (&phi;<sub>11</sub> - &phi;<sub>22</sub>)/(2 sin &sigma;) ,
     * <br>
     * <br>
     * &nbsp; &nbsp; &beta; &equiv; <i>w</i><sup>2</sup> = &phi;<sub>12</sub>/sin &sigma;
     * <br>
     * <br>
     * where &phi;<sub><i>ij</i></sub> are the elements of the 2&times;2 diagonal blocks of
     * <b>&Phi;</b> corresponding the the particular phase plane, the function <i>w</i>
     * is taken from Reiser, and &sigma; is the phase advance through the cell for the
     * particular phase plance.
     * </p>
     *
     * @param   matPhiCell  transfer matrix <b>&Phi;</b> for a periodic cell (or full turn)
     *
     * @return  array of matched Courant-Snyder parameters
     *          (&alpha;<sub><i>cell</i></sub>, &beta;<sub><i>cell</i></sub>, <code>NaN</code>)
     *          for each phase plane
     *
     *
     * @author Christopher K. Allen
     * @author Thomas Pelaia
     * @since  Jun 2004
     * @version Oct, 2013
     */
    protected Twiss[] calculateMatchedTwiss(PhaseMatrix matPhiCell /*, double[] arrPhsCell*/) {

//        final double[]  arrPhsCell = this.calculatePhaseAdvPerCell(matPhiCell);
        final R3        vecPhsCell = this.calculatePhaseAdvPerCell(matPhiCell);

        final Twiss[] arrTwsCell = new Twiss[NUM_MODES];

        for ( int mode = 0 ; mode < NUM_MODES ; mode++ ) {
            final int index = 2 * mode;

//            final double sinMu = Math.sin( DBL_2PI * vecPhsCell.getElem(mode) );// _tunes could be NaN
            final double sinMu = Math.sin( vecPhsCell.getElem(mode) );// _tunes could be NaN
            final double m11 = matPhiCell.getElem( index, index );
            final double m12 = matPhiCell.getElem( index, index + 1 );
            final double m22 = matPhiCell.getElem( index + 1, index + 1 );

            final double beta = m12 / sinMu;
            final double alpha = ( m11 - m22 ) / ( 2 * sinMu );
            final double emittance = Double.NaN;

            arrTwsCell[mode] = new Twiss( alpha, beta, emittance );
        }

        return arrTwsCell;
    }


    /**
     * <p>
     * Convenience function for returning the chromatic aberration coefficients.
     * </p>
     * <p>
     * For example, in the
     * horizontal phase plane (<i>x,x'</i>) these coefficients specify the
     * change in position &Delta;<i>x</i>
     * and the change in divergence angle &Delta;<i>x'</i>
     * due to chromatic dispersion &delta; within the beam, or
     * <br>
     * <br>
     * &nbsp; &nbsp; &Delta;<i>x</i> = (<i>dx</i>/<i>d</i>&delta;) &sdot; &delta; ,
     * <br>
     * <br>
     * &nbsp; &nbsp; &Delta;<i>x'</i> = (<i>dx'</i>/<i>d</i>&delta;) &sdot; &delta; .
     * <br>
     * <br>
     * That is, (<i>dx</i>/<i>d</i>&delta;) and (<i>dx'</i>/<i>d</i>&delta;) are the dispersion
     * coefficients for the horizontal plane position and horizontal plane divergence
     * angle, respectively.  The vector returned by this method contains all the analogous
     * coefficients for all the phase planes.
     * </p>
     * <p>The chromatic dispersion coefficient vector <b>&Delta;</b> can be built from
     * the 6<sup><i>th</i></sup>
     * column of the state response matrix <b>&Phi;</b>.  However we must pay close
     * attention to the transverse plane quantities.  Specifically, consider again
     * the horizontal phase plane (<i>x,x'</i>).  Denoting the elements of <b>&Phi;</b>
     * in the 6<sup><i>th</i></sup> column (i.e., the <i>z'</i> column) corresponding
     * to this positions as &phi;<sub>1,6</sub> and &phi;<sub>2,6</sub>, we can write
     * them as
     * <br>
     * <br>
     * &nbsp; &nbsp; &phi;<sub>1,6</sub> = &part;<i>x</i>/&part;<i>z'</i> ,
     * <br>
     * <br>
     * &nbsp; &nbsp; &phi;<sub>2,6</sub> = &part;<i>x'</i>/&part;<i>z'</i> .
     * <br>
     * <br>
     * Now consider the relationship
     * <br>
     * <br>
     * &nbsp; &nbsp; &delta; &equiv; (<i>p</i> - <i>p</i><sub>0</sub>)/<i>p</i><sub>0</sub>
     *               = &gamma;<sup>2</sup><i>z</i>'= &gamma;<sup>2</sup><i>dz</i>/<i>ds</i>
     * <br>
     * <br>
     * or
     * <br>
     * <br>
     * &nbsp; &nbsp; &part;<i>z'</i>/&part;&delta; = 1/&gamma;<sup>2</sup> .
     * <br>
     * <br>
     * Thus, it is necessary to multiply the transfer plane elements of
     * <i>Row</i><sub>6</sub>&Phi; by 1/&gamma;<sup>2</sup> to covert to
     * the conventional dispersion coefficients.  Specifically, for the horizontal plane
     * <br>
     * <br>
     * &nbsp; &nbsp; &Delta;<sub><i>x</i></sub> = (<i>dx</i>/<i>d</i>&delta;) = &phi;<sub>6,1</sub>/&gamma;<sup>2</sup> ,
     * <br>
     * <br>
     * &nbsp; &nbsp; &Delta;<sub><i>x'</i></sub> = (<i>dx'</i>/<i>d</i>&delta;) = &phi;<sub>6,2</sub>/&gamma;<sup>2</sup> ,
     * <br>
     * <br>
     * For the longitudinal plane no such normalization is necessary.
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * - Reference text D.C. Carey, "The Optics of Charged Particle Beams"
     * </p>
     *
     * @param matPhi    transfer matrix for which we are computing aberrations
     * @param dblGamma  relativistic factor for the beam particles at the location of aberration
     *
     * @return  vector of chromatic dispersion coefficients in <b>meters/radian</b>
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2013
     */
    protected R6 calculateAberration(final PhaseMatrix matPhi, final double dblGamma) {
        PhaseMatrix matResp = matPhi;
        R6          vecDelta  = matResp.projectColumn(IND.Zp);

        double      dblGamma2 = dblGamma*dblGamma;

        // Need to normalize the transverse coordinates to momentum rather than angle
        EnumSet<IND>    SET_TRNV = EnumSet.of(IND.X, IND.Xp, IND.Y, IND.Yp);

        for (IND i : SET_TRNV) {
            double      dblDspAng = vecDelta.getElem(i);
            double      dblDspMom  = dblDspAng/dblGamma2;

            vecDelta.setElem(i, dblDspMom);
        }

        return vecDelta;
    }



    /**
         * <p>
         * Taken from <code>TransferMapState</code>.
         * </p>
         * <p>
         * Calculates the differential coefficients describing change in the fixed point of the
         * closed orbit versus chromatic dispersion.
         * The given transfer matrix <b>&Phi;</b>
         * is assumed to be the one-turn map of the ring with which the fixed point is
         * caculated.  The fixed point is with regard
         * to the transverse phase plane coordinates.
         * </p>
         * <p>
         * The transverse plane dispersion vector <b>&Delta;</b> is defined
         * <br>
         * <br>
         * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; -(1/&gamma;<sup>2</sup>)[d<i>x</i>/d<i>z'</i>, d<i>x'</i>/d<i>z'</i>, d<i>y</i>/d<i>z'</i>, d<i>y'</i>/d<i>z'</i>]<sup><i>T</i></sup> .
         * <br>
         * <br>
         * It can be identified as the first 4 entries of the 6<sup><i>th</i></sup>
         * column in the transfer matrix <b>&Phi;</b>. The above vector
         * quantifies the change in the transverse particle phase
         * coordinate position versus the change in particle momentum.
         * The factor -(1/&gamma;<sup>2</sup>) is needed to convert from longitudinal divergence
         * angle <i>z'</i> used by XAL to momentum &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> used in
         * the dispersion definition.  Specifically,
         * <br>
         * <br>
         * &nbsp; &nbsp; &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> = &gamma;<sup>2</sup><i>z</i>'
         * <br>
         * <br>
         * As such, the above vector can be better described
         * <br>
         * <br>
         * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; [&Delta;<i>x</i>/&delta;<i>p</i>, &Delta;<i>x'</i>/&delta;<i>p</i>, &Delta;<i>y</i>/&delta;<i>p</i>, &Delta;<i>y'</i>/&delta;<i>p</i>]<sup><i>T</i></sup>
         * <br>
         * <br>
         * explicitly describing the change in transverse phase coordinate for fractional
         * change in momentum &delta;<i>p</i>.
         * </p>
         * <p>
         * Since we are only concerned with transverse phase space coordinates, we restrict ourselves to the
         * 4&times;4 upper diagonal block of <b>&Phi;</b>, which we denote take <b>T</b>.
         * That is, <b>T</b> = &pi; &sdot; <b>&Phi;</b>
         * where &pi; : <b>R</b><sup>6&times;6</sup> &rarr; <b>R</b><sup>4&times;4</sup> is the
         * projection operator.
         * </p>
         * <p>
         * This method finds that point <b>z</b><sub><i>t</i></sub> &equiv;
         * (<i>x<sub>t</sub></i>, <i>x'<sub>t</sub></i>, <i>y<sub>t</sub></i>, <i>y'<sub>t</sub></i>)
         * in transvse phase space that is invariant under the action of the ring for a given momentum spread
         * &delta;<i>p</i>.  That is, the particle ends up
         * in the same location each revolution. With a finite momentum spread of &delta;<i>p</i> &gt; 0
         * we require this require that
         * <br>
         * <br>
         * &nbsp; &nbsp; <b>Tz</b><sub><i>t</i></sub> + &delta;<i>p</i><b>&Delta;</b><sub><i>t</i></sub> = <b>z</b><sub><i>t</i></sub> ,
         * <br>
         * <br>
         * which can be written
         * <br>
         * <br>
         * &nbsp; <b>z</b><sub><i>t</i></sub> = &delta;<i>p</i>(<b>I</b> - <b>T</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
         * <br>
         * <br>
         * where <b>I</b> is the identity matrix.  Dividing both sides by &delta;<i>p</i> yields the final
         * result
         * <br>
         * <br>
         * &nbsp; <b>z</b><sub>0</sub> &equiv; <b>z</b><sub><i>t</i></sub>/&delta;<i>p</i> = (<b>I</b> - <b>T</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
         * <br>
         * <br>
         * which is the returned value of this method.  It is normalized by
         * &delta;<i>p</i> so that we can compute the closed orbit for any given momentum spread.
         * </p>
         * <p>
         * The eigenvalues of <b>T</b> determine conditioning of the the resolvent (<b>I</b> - <b>T</b>)<sup>-1</sup>.
         * Whenever 1 &in; &lambda;(<b>T</b>) we have problems.  Currently a value of <b>0</b> is returned
         * whenever the resolvent does not exist.
         * </p>
         *
         * @param matPhi	we are calculating the dispersion of a ring with this one-turn map
         * @param dblGamma		relativistic factor
         *
         * @return         The closed orbit fixed point <b>z</b><sub>0</sub> for finite
         *                 dispersion, normalized by momentum spread.
         *                 Returned as an array [<i>x</i><sub>0</sub>,<i>x'</i><sub>0</sub>,<i>y</i><sub>0</sub>,<i>y'</i><sub>0</sub>]/&delta;<i>p</i>
         *
         * @author Thomas Pelaia
         * @author Christopher K. Allen
         * @since  Aug 14, 2013
         */
        protected R4 calculateDispersion(final PhaseMatrix matPhi, final double dblGamma) {

            // Decompose the transfer matrix into transverse and longitudinal
            //    components.  The decomposition contains the linear algebraic
            //    system we need to solve the dispersion relations.

            // Extract the transverse divergence-angle vector
            R6    vecdP = matPhi.projectColumn(IND.Zp);

            // Convert it to transverse momentum
            R4    vecdp = new R4();
            vecdP.projectOnto(vecdp);

            vecdp.timesEquals( 1.0/(dblGamma*dblGamma) );


            // Check if there is no momemtum spread
            //  If not, return the zero vector
            if ( vecdp.isEquivalentTo(VEC_ZERO) )
                return vecdp;


            // Extract the transverse phase space transfer matrix
            //    then set up the fixed point system.
            R4x4  matT = matPhi.projectR4x4();
            R4x4  matI = R4x4.newIdentity();
            R4x4  matR = matI.minus(matT);


            // Solve for the dispersion vector and return it
            try {

                R4    vecd = matR.solve(vecdp);

                return vecd;

            } catch (Exception e) {

                System.err.println("Error in solving matrix-vector equation");

                return R4.newZero();
            }
        }


    /**
     * <p>
     * Advance the twiss parameters using the given transfer matrix based upon
     * formula 2.54 from S.Y. Lee's book.
     * </p>
     * <h3>CKA NOTES:</h3>
     * <p>
     * - This method will only work correctly for a beam that is
     * unccorrelated in the phase planes.
     * </p>
     *
     * @param probe     probe with target twiss parameters
     * @param matPhi    the transfer matrix of the element
     * @param dW        the energy gain of this element (eV)
     *
     * @return  set of new twiss parameter values
     *
     * @deprecated This is the algorithm component for a <code>TwissProbe</code> and should
     *             not be used as the dynamics elsewhere. It has been moved to the
     *             <code>TwissTracker</code> class.
     */
    @Deprecated
    protected Twiss3D computeTwiss(TwissProbe probe, PhaseMatrix matPhi, double dW) {


        // Compute relativistic parameters ratios
        double ratTran;     // emittance decrease ratio for transverse plane
        double ratLong;     // emittance decrease ratio for longitudinal plane

        if (dW == 0.0)  {
            ratTran = 1.0;
            ratLong = 1.0;

        } else {
            double  ER = probe.getSpeciesRestEnergy();
            double  W0 = probe.getKineticEnergy();
            double  W1 = W0 + dW;

            double g0 = probe.getGamma();
            double b0 = probe.getBeta();
            double g1 = RelativisticParameterConverter.computeGammaFromEnergies(W1, ER);
            double b1 = RelativisticParameterConverter.computeBetaFromGamma(g1);

            ratTran = (g0*b0)/(b1*g1);
            ratLong = ratTran*(g0*g0)/(g1*g1 );

        }


        // Twiss parameters
        Twiss3D twissEnv0 = probe.getTwiss();   // old values of Twiss parameters
        Twiss3D twissEnv1 = new Twiss3D();        // propagated values of twiss parameters

        double  alpha0, beta0, gamma0;  // old twiss parameters
        double  emit0;                  // old (unnormalized) emittance
        double  alpha1, beta1;          // new twiss parameters
        double  emit1;                  // new (unnormalized) emittance

        // Transfer matrix diagonal sub-block
        double Rjj;     // .
        double Rjjp;    //  | Rjj  Rjjp  |
        double Rjpj;    //  | Rjpj Rjpjp |
        double Rjpjp;   //                .

        int j = 0;
        for (IND_3D index : IND_3D.values()) { // for each phase plane
            j = 2 * index.val();

            // assume constant normalized emittance
            alpha0 = twissEnv0.getTwiss(index).getAlpha();
            beta0  = twissEnv0.getTwiss(index).getBeta();
            gamma0 = twissEnv0.getTwiss(index).getGamma();
            emit0  = twissEnv0.getTwiss(index).getEmittance();

            Rjj   = matPhi.getElem(j,  j);
            Rjjp  = matPhi.getElem(j,  j+1);
            Rjpj  = matPhi.getElem(j+1,j);
            Rjpjp = matPhi.getElem(j+1,j+1);

            beta1  = Rjj*Rjj*beta0 - 2.*Rjj*Rjjp*alpha0 + Rjjp*Rjjp*gamma0;
            alpha1 = -Rjj*Rjpj*beta0 + (Rjj*Rjpjp + Rjjp*Rjpj)*alpha0 - Rjjp*Rjpjp*gamma0;

            if (index==IND_3D.Z) // longitudinal plane
                emit1 = emit0 * ratLong;
            else     // transver plane
                emit1 = emit0 * ratTran;

            twissEnv1.setTwiss(index, new Twiss(alpha1, beta1, emit1) );
        }

        return twissEnv1;
    }


    /*
     * Support Methods
     */

}

