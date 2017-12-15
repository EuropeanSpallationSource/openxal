/*
 * Copyright (C) 2017 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.jels.model.elem;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.jels.smf.impl.ESSFieldMap;
import xal.extension.jels.smf.impl.FieldProfile;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThickElement;
import xal.model.elem.sync.IRfCavityCell;
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.RfCavity;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.GenericMatrix;

/**
 * This is direct fieldmap implementation, matching TraceWin implementation.
 *
 * @author Ivo List <ivo.list@cosylab.com>
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class FieldMapExpInt extends ThickElement implements IRfGap, IRfCavityCell {

    private static final Logger LOGGER = Logger.getLogger(FieldMapExpInt.class.getName());

    private double frequency;

    private double[] field;
    private double totalLength;
    private double ETL;
    private boolean inverted;

    private double phi0;
    private double phipos;
    private double[] phase;

    private double startPosition;
    private boolean lastSlice;
    private FieldMapExpInt firstSliceFieldmap;

    private int indCell;
    private double dblCavModeConst = 0.;

    public FieldMapExpInt() {
        this(null);
    }

    public FieldMapExpInt(String strId) {
        super("FieldMap", strId);
    }

    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);

        if (latticeElement.isFirstSlice()) {
            startPosition = latticeElement.getStartPosition();

            final ESSFieldMap fm = (ESSFieldMap) latticeElement.getHardwareNode();
            FieldProfile fp = fm.getFieldProfile();
            field = fp.getField();
            totalLength = fp.getLength();

            phipos = fm.getPhasePosition();
            //WORKAROUND difference between ESS and SNS lattice
            inverted = fm.getParent().getClass().equals(RfCavity.class) && fp.isFirstInverted();
        } else {
            try {
                firstSliceFieldmap = (FieldMapExpInt) latticeElement.getFirstSlice().createModelingElement();
            } catch (ModelException e) {
                LOGGER.log(Level.INFO, "Couldn't load the first slice element.", e);
            }
        }

        if (latticeElement.isLastSlice()) {
            lastSlice = true;
        }
    }

    /**
     * This method precalculates the energy and phase on the whole current
     * element, so that we don't have any problems when a probe visits of
     * smaller parts of the element.
     *
     * @param beta energy at the start of the element
     * @param initialEnergy energy at the start of the element
     * @param restMass particles rest energy
     */
    private void initPhase(double initialEnergy, double restMass, double phi0) {
        phase = new double[field.length];

        double phi = phi0;
        double dz = totalLength / field.length;
        double energyGain = 0.;

        for (int i = 0; i < field.length; i++) {
            phase[i] = phi;

            energyGain += ETL * field[i] * Math.cos(phi) * dz;
            double gamma = (initialEnergy + energyGain) / restMass + 1.0;
            double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
            phi += 2 * Math.PI * frequency * dz / (beta * LightSpeed);
        }
    }

    @Override
    public double energyGain(IProbe probe, double dblLen) {
        if (firstSliceFieldmap != null) {
            return firstSliceFieldmap.energyGain(probe, dblLen);
        }

        double p0 = probe.getPosition() - startPosition;

        int i0 = (int) Math.round(p0 / totalLength * field.length);
        int in = (int) Math.round((p0 + dblLen) / totalLength * field.length);

        double energyGain = 0;
        double dz = totalLength / field.length;

        for (int i = i0; i < Math.min(in, field.length - 1); i++) {
            energyGain += ETL * field[i] * Math.cos(phase[i]) * dz;
        }

        return energyGain;
    }

    /**
     * Method calculates transfer matrix for the fieldmap on the current range
     * (i.e from probe.getPosition, and for dblLength). It does so by
     * numerically integrating equations of motion and calculating matrix
     * exponent of them, to get the transfer matrix.
     *
     * @return
     * @throws xal.model.ModelException
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen)
            throws ModelException {
        if (firstSliceFieldmap != null) {
            return firstSliceFieldmap.transferMap(probe, dblLen);
        }

        double p0 = probe.getPosition() - startPosition;
        int i0 = (int) Math.round(p0 / totalLength * field.length);
        int in = (int) Math.round((p0 + dblLen) / totalLength * field.length);
        if (in >= field.length) {
            in = field.length;
        }

        double dz = totalLength / field.length;
        double Ek = probe.getKineticEnergy();
        double restEnergy = probe.getSpeciesRestEnergy();

        GenericMatrix transversalTransferMatrix = new GenericMatrix(2, 2);
        transversalTransferMatrix.assignIdentity();

        GenericMatrix longitudinalTransferMatrix = new GenericMatrix(2, 2);
        longitudinalTransferMatrix.assignIdentity();

        double gammaStart;

        for (int i = i0; i < in; i++) {
            double Edz = field[i] * dz;
            double phi = phase[i];
            double dE = (i == 0 ? field[i + 1] : (i == field.length - 1 ? field[i - 1] : field[i + 1] - field[i - 1])) / 2.;

            gammaStart = Ek / restEnergy + 1.0;
            double betaStart = Math.sqrt(1.0 - 1.0 / (gammaStart * gammaStart));
            double energyKick = ETL * Edz * Math.cos(phi);

            double pEz_pzdz = ETL * dE * Math.cos(phi);

            double pEx_pxdz = -0.5 * ETL * dE * Math.cos(phi);
            double pBx_pydz = 2 * Math.PI * frequency / (2. * LightSpeed * LightSpeed) * ETL * Edz * Math.sin(phi);
            //double pBy_px = -pBx_py;

            double k = 1. / (gammaStart * Math.pow(betaStart, 2) * restEnergy);

            double gammaEnd = (Ek + energyKick) / restEnergy + 1.0;

            double[][] Ay = new double[][]{{0, dz}, {k * (pEx_pxdz + betaStart * LightSpeed * pBx_pydz), -k * energyKick}};
            double[][] Az = new double[][]{{0, dz}, {k * pEz_pzdz / (gammaEnd * gammaEnd), gammaStart * energyKick * (-k * gammaStart - 2 / restEnergy) / (gammaEnd * gammaEnd)}};

            // Following line fixes the determinant of longitudinal transfer matrix
            //Az[0][0] = ((betaStart*gammaStart)/(betaEnd*gammaEnd) + Az[0][1]*Az[1][0]) / Az[1][1];
            GenericMatrix Atr = new GenericMatrix(matrix22Exp(Ay));
            transversalTransferMatrix = Atr.times(transversalTransferMatrix);

            Az = matrix22Exp(Az);
            longitudinalTransferMatrix = new GenericMatrix(Az).times(longitudinalTransferMatrix);

            Ek += energyKick;
        }

        PhaseMatrix T = PhaseMatrix.identity();
        T.setSubMatrix(0, 1, 0, 1, transversalTransferMatrix.getArrayCopy());
        T.setSubMatrix(2, 3, 2, 3, transversalTransferMatrix.getArrayCopy());
        T.setSubMatrix(4, 5, 4, 5, longitudinalTransferMatrix.getArrayCopy());

        return new PhaseMap(T);
    }

    // Exponential of a 2x2 matrix
    public double[][] matrix22Exp(double[][] A) {
        double a = A[0][0], b = A[0][1], c = A[1][0], d = A[1][1];
        double D = Math.pow((a - d), 2) + 4 * b * c;

        double cosh, sinhD;
        if (D > 0.) {
            D = Math.sqrt(D);
            cosh = Math.cosh(D / 2.);
            sinhD = Math.sinh(D / 2.) / D;
        } else if (D < 0.) {
            D = Math.sqrt(-D);
            cosh = Math.cos(D / 2.);
            sinhD = Math.sin(D / 2.) / D;
        } else {
            cosh = 1;
            sinhD = 0.5;
        }

        double exp = Math.exp((a + d) / 2.);

        double m11 = exp * (cosh + (a - d) * sinhD);
        double m12 = 2 * b * exp * sinhD;
        double m21 = 2 * c * exp * sinhD;
        double m22 = exp * (cosh + (d - a) * sinhD);

        A[0][0] = m11;
        A[0][1] = m12;
        A[1][0] = m21;
        A[1][1] = m22;

        return A;
    }

    public static void ROpenXal2TW(double gamma_start, double gamma_end, PhaseMap pm) {
        PhaseMatrix r = pm.getFirstOrder();

        for (int i = 0; i < 6; i++) {
            r.setElem(i, 5, r.getElem(i, 5) / gamma_start / gamma_start);
            r.setElem(5, i, r.getElem(5, i) * gamma_end * gamma_end);
        }
        pm.setLinearPart(r);
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        if (firstSliceFieldmap != null) {
            return firstSliceFieldmap.elapsedTime(probe, dblLen);
        }

        double p0 = probe.getPosition() - startPosition;

        int i0 = (int) Math.round(p0 / totalLength * field.length);
        int in = (int) Math.round((p0 + dblLen) / totalLength * field.length);

        in = Math.min(in, field.length - 1);

        return (phase[in] - phase[i0]) / (2 * Math.PI * frequency);
    }

    /**
     * Since it is currently hard to track phase on the probe, this way we
     * initialize the phase and deinitialize it when the probe passes.
     * @throws xal.model.ModelException
     */
    @Override
    public void propagate(IProbe probe) throws ModelException {
        if (firstSliceFieldmap == null || this == firstSliceFieldmap) {
            startPosition = probe.getPosition();

            double phi00;

            if (indCell == 0) {
                double phim = phipos / (probe.getBeta() * IElement.LightSpeed / (2 * Math.PI * frequency));
                //phim = ti.getSyncPhase(probe.getBeta());

                phi00 = Math.IEEEremainder(phi0 - phim - (inverted ? Math.PI : 0.), 2 * Math.PI);
            } else {
                phi00 = probe.getLongitinalPhase() + dblCavModeConst * Math.PI * indCell;
            }

            initPhase(probe.getKineticEnergy(), probe.getSpeciesRestEnergy(), phi00);
        }
        super.propagate(probe);
        if (lastSlice) {
            phase = null;
        }
    }

    @Override
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen) {
        if (firstSliceFieldmap != null) {
            return firstSliceFieldmap.longitudinalPhaseAdvance(probe, dblLen);
        }

        double p0 = probe.getPosition() - startPosition;
        int in = (int) Math.round((p0 + dblLen) / totalLength * field.length);
        if (in >= field.length) {
            in = field.length;
        }
        return -probe.getLongitinalPhase() + phase[in - 1] - dblCavModeConst * Math.PI * indCell;
    }

    @Override
    public void setETL(double dblETL) {
        ETL = dblETL;
    }

    @Override
    public void setE0(double E) {
        // We ignore this value. gap amplitude
    }

    @Override
    public void setPhase(double dblPhase) {
        phi0 = dblPhase;
    }

    @Override
    public void setFrequency(double dblFreq) {
        frequency = dblFreq;

    }

    @Override
    public double getETL() {
        return ETL;
    }

    @Override
    public double getPhase() {
        return phi0;
    }

    @Override
    public double getFrequency() {
        return frequency;
    }

    @Override
    public double getE0() {
        // We ignore cavity amplitude
        return 1;
    }

    @Override
    public boolean isFirstGap() {
        return indCell == 0;
    }

    @Override
    public void setCavityCellIndex(int indCell) {
        this.indCell = indCell;
    }

    @Override
    public void setCavityModeConstant(double dblCavModeConst) {
        this.dblCavModeConst = dblCavModeConst;
    }

    @Override
    public int getCavityCellIndex() {
        return indCell;
    }

    @Override
    public double getCavityModeConstant() {
        return dblCavModeConst;
    }

    @Override
    public boolean isEndCell() {
        // this is ignored
        return false;
    }

    @Override
    public boolean isFirstCell() {
        return indCell == 0;
    }
}
