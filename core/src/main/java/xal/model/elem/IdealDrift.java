/*
 * IdealDrift.java
 *
 * Created on October 15, 2002, 5:26 PM
 *
 * Modified:
 *      02/13/03 CKA    - refactored to new model architecture 
 */
package xal.model.elem;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IElectromagnet;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.DriftSpace;
import xal.tools.beam.optics.QuadrupoleLens;

/**
 * <p>
 * Represents a drift space in a particle beam transport/accelerator system.
 * </p>
 * <p>
 * <h3>NOTE</h3>
 * <p>
 * This class has been modified to accommodate PMQs by acting as quadrupoles
 * with diminishing fields when placed next to a PMQ element. I'm not sure this
 * is the best implementation as it is tightly coupled with several other
 * elements.
 * </p>
 *
 *
 * @author Christopher Allen
 */
public class IdealDrift extends ThickElement {

    private static final Logger LOGGER = Logger.getLogger(IdealDrift.class.getName());

    /**
     * Debugging flag
     */
    final boolean debug = false;

    /*
     *  Global Constants
     */
    /**
     * string type identifier for all IdealDrift objects
     */
    public static final String TYPE = "IdealDrift";

    /*
     * Local Attributes
     */
    final boolean newMethod = false;

    private double KDrift;

    FringePMQ fringes[];

    /*
     * Initialization
     */
    /**
     * Constructor for subclasses of <code>IdealDrift</code>.
     *
     * @param strType string type identifier of the child class
     * @param strId string identifier of the child object
     * @param dblLen length of the new drift object
     *
     * @since Jan 22, 2015 by Christopher K. Allen
     */
    protected IdealDrift(String strType, String strId, double dblLen) {
        super(strType, strId, dblLen);
    }

    /**
     * Creates a new instance of IdealDrift
     *
     * @param strId string identifier for the element
     * @param dblLen length of the drift
     */
    public IdealDrift(String strId, double dblLen) {
        super(TYPE, strId, dblLen);
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of IdealDrift
     *
     * <strong>BE CAREFUL</strong>
     */
    public IdealDrift() {
        super(TYPE);
    }


    /*
     * Attribute Queries
     */
    /**
     * I guess this is the strength of the adjacent PMQ quadrupole magnet if it
     * exists.
     *
     * @return adjacent PMQ magnet strength ?
     *
     * @author Christopher K. Allen
     * @since Apr 19, 2011
     */
    public double getKDrift() {
        return KDrift;
    }

    /*
     * Operations
     */
    /**
     * I think this method looks for PMQ magnets next to this drift then stores
     * them as a private class.
     *
     * @author Christopher K. Allen
     * @since Apr 19, 2011
     */
    public void prepareFringe() {
        if (getCloseElements() == null) {
            return;
        }

        fringes = new FringePMQ[getCloseElements().size()];

        Iterator<Element> it = this.getCloseElements().iterator();

        int ipmq = 0;
        while (it.hasNext()) {
            Element elem = it.next();
            if (elem instanceof IdealPermMagQuad) {
                IdealPermMagQuad permQuad = (IdealPermMagQuad) elem;
                fringes[ipmq] = new FringePMQ(permQuad);
                ipmq++;
            }
        }
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
     * Return the energy gain imparted to a probe object.
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
     * Computes the partial transfer map for an ideal drift space. Computes the
     * transfer map for a drift of length <code>dblLen</code>.
     *
     * @param dblLen length of drift
     * @param probe requires rest and kinetic energy from probe
     *
     * @return transfer map of an ideal drift space for this probe
     *
     * @exception ModelException should not be thrown
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException {
        if ((getCloseElements() == null) || (dblLen == 0)) {
            // Build transfer matrix
            PhaseMatrix matPhi = new PhaseMatrix();

            double mat0[][] = new double[][]{{1.0, dblLen}, {0.0, 1.0}};

            matPhi.setSubMatrix(0, 1, 0, 1, mat0);
            matPhi.setSubMatrix(2, 3, 2, 3, mat0);
            matPhi.setSubMatrix(4, 5, 4, 5, mat0);
            matPhi.setElem(6, 6, 1.0);

            return new PhaseMap(matPhi);
        }

        //sako add permquad components
        final double KDriftTh = 0.0000;
        KDrift = 0;
        double dxSum = 0;
        double dySum = 0;
        double dzSum = 0;

        final double q = probe.getSpeciesCharge();

        int KDriftCount = 0;
        if (newMethod) {
            if (fringes != null) {
                for (int ipmq = 0; ipmq < fringes.length; ipmq++) {
                    FringePMQ fringe = fringes[ipmq];
                    double K = fringe.getK(probe, dblLen);

                    KDrift += K;
                    KDriftCount++;
                }
            }
        } else {

            if (this.getCloseElements() != null) {
                if (debug) {
                    LOGGER.log(Level.INFO, "xxxxxxxxxxxxxx in IdealDrift, s, dblLen  = " + this.getPosition() + " " + dblLen);
                }

                Iterator<Element> it = this.getCloseElements().iterator();

                while (it.hasNext()) {

                    Element elem = it.next();
                    if (elem instanceof IdealPermMagQuad) {
                        IdealPermMagQuad permQuad = (IdealPermMagQuad) elem;
                        double K = permQuad.calcK(probe, dblLen);

                        dxSum += permQuad.getAlignX();
                        dySum += permQuad.getAlignY();
                        dzSum += permQuad.getAlignZ();

                        if (debug) {
                            LOGGER.log(Level.INFO, "id, K = " + permQuad.getId() + " " + K);
                        }

                        double fld = permQuad.getMagField();
                        //fixed on 15 Oct 06
                        if (q * fld >= 0) {
                            KDrift += (K * K);
                        } else {
                            KDrift -= (K * K);
                        }

                        KDriftCount++;
                    }
                }
                if (debug) {
                    LOGGER.log(Level.INFO, "xxxxxxxxxxxxxx end IdealDrift (" + this.getId() + "), KDrift, KDriftCount = " + KDrift + " " + KDriftCount);
                }

                //need to check if this is correct calculations!! (probably not)
                if (KDriftCount > 0) {
                    dxSum /= KDriftCount;
                    dySum /= KDriftCount;
                    dzSum /= KDriftCount;
                }
            }
        }

        if (KDrift >= 0) {
            KDrift = Math.sqrt(KDrift);
        } else {
            KDrift = -Math.sqrt(-KDrift);
        }

        if (KDrift > KDriftTh) {
            if (debug) {
                LOGGER.log(Level.INFO, "KDrift = " + KDrift);
            }
            return (this.transferMap(probe, dblLen, KDrift, IdealPermMagQuad.ORIENT_HOR, dxSum, dySum, dzSum));
        } else if (KDrift < -KDriftTh) {
            return (this.transferMap(probe, dblLen, -KDrift, IdealPermMagQuad.ORIENT_VER, dxSum, dySum, dzSum));
        } else {
            // Build transfer matrix
            PhaseMatrix matPhi = new PhaseMatrix();

            double mat0[][] = new double[][]{{1.0, dblLen}, {0.0, 1.0}};

            matPhi.setSubMatrix(0, 1, 0, 1, mat0);
            matPhi.setSubMatrix(2, 3, 2, 3, mat0);
            matPhi.setSubMatrix(4, 5, 4, 5, mat0);
            matPhi.setElem(6, 6, 1.0);

            return new PhaseMap(matPhi);
        }

    }

    private PhaseMap transferMap(IProbe probe, double dL, double k, int orientation, double alignx, double aligny, double alignz) {

        if (debug) {
            LOGGER.log(Level.INFO, "IdeaDrift, k, dL = " + k + " " + dL);
        }
        //def=false
        boolean useApproxLens = IdealPermMagQuad.getUseApproxLens();
        if (debug) {
            LOGGER.log(Level.INFO, "useApproxLens(IdealDrift) = " + useApproxLens);
        }
        // Compute the transfer matrix components
        double[][] arrF;
        if (useApproxLens) {
            arrF = QuadrupoleLens.transferFocPlaneApproxSandWitch(k, dL);
        } else {
            arrF = QuadrupoleLens.transferFocPlane(k, dL);
        }

        double[][] arrD;
        if (useApproxLens) {
            arrD = QuadrupoleLens.transferDefPlaneApproxSandWitch(k, dL);
        } else {
            arrD = QuadrupoleLens.transferDefPlane(k, dL);
        }

        double[][] arr0 = DriftSpace.transferDriftPlane(dL);

        // Build the tranfer matrix from its component blocks
        PhaseMatrix matPhi = new PhaseMatrix();

        // a drift space longitudinally
        matPhi.setSubMatrix(4, 5, 4, 5, arr0);
        // homogeneous coordinates
        matPhi.setElem(6, 6, 1.0);

        try {
            switch (orientation) {

                // focusing in x, defocusing in y
                case IElectromagnet.ORIENT_HOR:
                    matPhi.setSubMatrix(0, 1, 0, 1, arrF);
                    matPhi.setSubMatrix(2, 3, 2, 3, arrD);
                    break;

                // defocusing in x, focusing in y
                case IElectromagnet.ORIENT_VER:
                    matPhi.setSubMatrix(0, 1, 0, 1, arrD);
                    matPhi.setSubMatrix(2, 3, 2, 3, arrF);
                    break;

                default:
                    throw new ModelException("IdealMagQuad::computeTransferMatrix() - Bad magnet orientation.");
            }
        } catch (ModelException e) {
            LOGGER.log(Level.SEVERE, null, e);
            System.exit(-1);
        }

        matPhi = applyAlignErrorStatic(matPhi, alignx, aligny, alignz);

        return new PhaseMap(matPhi);
    }

    private PhaseMatrix applyAlignErrorStatic(PhaseMatrix matPhi, double delx, double dely, double delz) {

        if ((delx == 0) && (dely == 0) && (delz == 0)) {
            return matPhi;
        }
        PhaseMatrix T = new PhaseMatrix();

        //T = Translation Matrix by Chris Allen
        // |1 0 0 0 0 0 dx|
        // |0 1 0 0 0 0  0|
        // |0 0 1 0 0 0 dy|
        // |0 0 0 1 0 0  0|
        // |0 0 0 0 1 0 dz|
        // |0 0 0 0 0 1  0|
        // |0 0 0 0 0 0  1|
        // T(d)r = r+dr
        // where
        // r = |x |
        //     |x'|
        //     |y |
        //     |y'|
        //     |z |
        //     |z'|
        //     |1 |
        // dr = |dx |
        //      |0  |
        //      |dy |
        //      |0  |
        //      |dz |
        //      |0  |
        //      |0  |
        for (int i = 0; i < 7; i++) {
            T.setElem(i, i, 1);
        }

        T.setElem(0, 6, -delx);
        T.setElem(2, 6, -dely);
        T.setElem(4, 6, -delz);
        PhaseMatrix Phidx = T.inverse().times(matPhi).times(T);

        return Phidx;
    }

    private class FringePMQ {

        private double s1;
        private double s2;
        private double r1;
        private double r2;
        private boolean fringe1;
        private boolean fringe2;
        private double fld;

        public double getK(IProbe probe, double len) {
            setProbe(probe);
            double s = probe.getPosition() + len / 2;
            double q = probe.getSpeciesCharge();

            double sign = 1;
            if (q * fld < 0) {
                sign = -1;
            }
            return sign * KNorm * fringe(s);
        }

        public FringePMQ(IdealPermMagQuad pmq) {
            setPMQ(pmq);
        }

        /**
         * new variable represents for PQEXT parameter in Trace3d which shows
         * the extent of fringe field. This is taken from pqExt of IdealPermQuad
         */
        private static final double pqExt = IdealPermMagQuad.pqExt;

        public double fringe(double s) {
            double z1 = s - s2;
            double z2 = s - s1;
            if (r2 < r1) {
                System.err.println("IdealMagQuad::fringe - outer radius r2 (" + r2 + ") is smaller than inner radius r1 (" + r1 + ")... Aborting");
                return 0;
            }

            //newly added by sako, 26 Sep 06, z1<PQEXT*r1
            if (z1 > 0) {
                if (z1 >= pqExt * r1) {
                    return 0;
                }
            } else if (z2 < 0) {
                if (-z2 >= pqExt * r1) {
                    return 0;
                }
            }

            if ((!fringe1) && (s < smin)) {
                return 0;
            } else if ((!fringe2) && (smax < s)) {
                return 0;
            }

            double v1 = 0;
            double w1 = 0;
            if (r1 != 0) {
                v1 = 1 / Math.sqrt(1 + z1 * z1 / r1 / r1);
                w1 = 1 / Math.sqrt(1 + z2 * z2 / r1 / r1);
            }
            double v2 = 0;
            double w2 = 0;
            if (r2 != 0) {
                v2 = 1 / Math.sqrt(1 + z1 * z1 / r2 / r2);
                w2 = 1 / Math.sqrt(1 + z2 * z2 / r2 / r2);
            }

            double vtemp = (v1 * v1 * v2 * v2 * (v1 * v1 + v2 * v2 + v1 * v2 + 4 + 8 / (v1 * v2))) / (v1 + v2);
            double f1 = 0.5 * (1 - 0.125 * z1 * (1 / r1 + 1 / r2) * vtemp);

            double wtemp = (w1 * w1 * w2 * w2 * (w1 * w1 + w2 * w2 + w1 * w2 + 4 + 8 / (w1 * w2))) / (w1 + w2);
            double f2 = 0.5 * (1 - 0.125 * z2 * (1 / r1 + 1 / r2) * wtemp);

            double f = f1 - f2;

            if (f < 0) {
                LOGGER.log(Level.INFO, "****** WARNING: f = " + f);
                LOGGER.log(Level.INFO, "IdealPermMagQuad s, s1, s2 = " + s + " " + s1 + " " + s2);
                LOGGER.log(Level.INFO, "f = " + f);

                f = 0;
            } else if (f > 1) {
                LOGGER.log(Level.INFO, "****** WARNING: f = " + f);
                LOGGER.log(Level.INFO, "IdealPermMagQuad s, s1, s2 = " + s + " " + s1 + " " + s2);
                LOGGER.log(Level.INFO, "f = " + f);

                f = 1;
            }
            return f;
        }

        private double mbetagamma;

        public void setProbe(IProbe probe) {
            mass = probe.getSpeciesRestEnergy();
            double beta = probe.getBeta();
            double gamma = probe.getGamma();

            mbetagamma = mass * beta * gamma;

            calcKNorm();

        }

        // Get lens parameters
        private double KNorm;
        private double G;
        private double mass;
        private double k0;
        private double smin, smax;

        int bPathFlag;

        public void setPMQ(IdealPermMagQuad pmq) {
            fld = pmq.getMagField();

            s1 = pmq.getSCenter() - pmq.getSLength() / 2;
            s2 = pmq.getSCenter() + pmq.getSLength() / 2;

            r1 = pmq.getRadIn();
            r2 = pmq.getRadOut();

            if (pmq.getFringeLeft() == 1) {
                fringe1 = true;
            } else {
                fringe1 = false;
            }

            if (pmq.getFringeRight() == 1) {
                fringe2 = true;
            } else {
                fringe2 = false;
            }

            smin = pmq.getPosition() - pmq.getLength() / 2;
            smax = pmq.getPosition() + pmq.getLength() / 2;

            G = Math.abs(pmq.getMagField());

            int bPathFlag = (int) pmq.getFieldPathFlag();

            //if bpathflag =1, then use nominal k0 from nominal kine energy
            if (bPathFlag == 1) {

                double w0 = pmq.getNominalKineEnergy();

                double p0 = Math.sqrt(w0 * (w0 + 2 * mass));

                if (p0 == 0) {
                    k0 = 0;
                } else {
                    k0 = Math.sqrt(LIGHT_SPEED * G / p0);
                }
            }

        }

        public void calcKNorm() {

            // Compute focusing constant
            // focusing constant (radians/meter)
            //if bpathflag =1, then use nominal k0 from nominal kine energy
            if (bPathFlag == 0) {
                KNorm = Math.sqrt((LIGHT_SPEED * G) / mbetagamma);
            } else {
                KNorm = k0;
            }

        }
    }
}
