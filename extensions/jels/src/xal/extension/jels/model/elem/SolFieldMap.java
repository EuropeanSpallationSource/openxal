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

import xal.extension.jels.smf.impl.ESSSolFieldMap;
import xal.extension.jels.smf.impl.FieldProfile2D;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThickElectromagnet;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * Solenoid fieldmap implementation.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class SolFieldMap extends ThickElectromagnet {

    private double frequency;

    private double fieldR[][];
    private double fieldZ[][];
    private double lengthZ;
    private double lengthR;
    private double norm;

    private double startPosition;
    private boolean lastSlice;
    private SolFieldMap firstSliceFieldmap;

    public SolFieldMap() {
        this(null);
    }

    public SolFieldMap(String strId) {
        super("SolFieldMap", strId);
    }

    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);

        if (latticeElement.isFirstSlice()) {
            startPosition = latticeElement.getStartPosition();

            final ESSSolFieldMap fm = (ESSSolFieldMap) latticeElement.getHardwareNode();
            FieldProfile2D fpz = fm.getFieldProfileZ();
            FieldProfile2D fpr = fm.getFieldProfileR();
            fieldZ = fpz.getField();
            lengthZ = fpz.getLengthZ();
            fieldR = fpr.getField();
            lengthR = fpr.getLengthR();
            norm = fpr.getNorm();
        } else {
            try {
                firstSliceFieldmap = (SolFieldMap) latticeElement.getFirstSlice().createModelingElement();
            } catch (ModelException e) {
            }
        }

        if (latticeElement.isLastSlice()) {
            lastSlice = true;
        }
    }

    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return 0.0;
    }

    /**
     * Method calculates transfer matrix for the fieldmap on the current range
     * (i.e from probe.getPosition, and for dblLength). It does so by
     * numerically integrating equations of motion and calculating matrix
     * exponent of them, to get the transfer matrix. The middle point is taken
     * for the integration. It performs a linear interpolation on the fieldmap
     * if the step used for space charge calculation is smaller than the
     * distance between the points from the input file.
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen)
            throws ModelException {
        if (firstSliceFieldmap != null) {
            return firstSliceFieldmap.transferMap(probe, dblLen);
        }
        int Nz = fieldZ.length - 1;
        int Nr = fieldR[0].length - 1;

        double dz;
        double dr = lengthR / Nr;
        double dzFieldMap = lengthZ / Nz;

        double[] Bz0;
        double[] dBr_dr;

        PhaseMatrix T;

        double startStep = probe.getPosition() - startPosition;
        double stopStep = startStep + dblLen;
        
        if (startStep < 0) {
            startStep = 0;
        }
        
        int i0 = (int) Math.floor(startStep / dzFieldMap) + 1;
        int in = (int) Math.floor(stopStep / dzFieldMap);
        if (in >= Nz) {
            in = Nz - 1;
        }

        // First part
        if (in < i0) { // Small step in between two points
            dz = stopStep - startStep;
        } else {
            dz = i0 * dzFieldMap - startStep;
        }

        double z0 = (i0 - 1) * dzFieldMap;

        // Linear interpolation of the magnetic field
        double fieldZStart = fieldZ[i0 - 1][0] + (startStep - z0 + dz / 2) * (fieldZ[i0][0] - fieldZ[i0 - 1][0]) / dzFieldMap;
        double dFieldRStart = fieldR[i0 - 1][1] - fieldR[i0 - 1][0] + (startStep - z0 + dz / 2) * (fieldR[i0][1] - fieldR[i0][0] - (fieldR[i0 - 1][1] - fieldR[i0 - 1][0])) / dzFieldMap;

        Bz0 = new double[]{getMagField() * fieldZStart / norm};
        dBr_dr = new double[]{getMagField() * dFieldRStart / dr / norm};

        T = fieldMapIntegrator(probe, dz, Bz0, dBr_dr);

        // Central part
        if (in > i0) {
            Bz0 = new double[in - i0];
            dBr_dr = new double[in - i0];

            for (int i = 0; i < in - i0; i++) {
                Bz0[i] = getMagField() * 0.5 * (fieldZ[i0 + i][0] + fieldZ[i0 + i + 1][0]) / norm;
                dBr_dr[i] = getMagField() * 0.5 * ((fieldR[i0 + i][1] - fieldR[i0 + i][0]) + (fieldR[i0 + i + 1][1] - fieldR[i0 + i + 1][0])) / dr / norm;
            }

            T = fieldMapIntegrator(probe, dzFieldMap, Bz0, dBr_dr).times(T);
        }

        // Last part
        if (in >= i0 && stopStep > in * dzFieldMap) {
            dz = stopStep - in * dzFieldMap;

            Bz0 = new double[]{getMagField() * 0.5 * (fieldZ[in][0] + fieldZ[in + 1][0]) / norm};
            dBr_dr = new double[]{getMagField() * 0.5 * ((fieldR[in][1] - fieldR[in][0]) + (fieldR[in + 1][1] - fieldR[in + 1][0])) / dr / norm};

            T = fieldMapIntegrator(probe, dz, Bz0, dBr_dr).times(T);
        }
        return new PhaseMap(T);
    }

    // First order integrator
    private PhaseMatrix fieldMapIntegrator(IProbe probe, double dz, double[] Bz0, double[] dBr_dr) {
        double gamma;
        double beta;
        double k;

        double Ek = probe.getKineticEnergy();
        double restEnergy = probe.getSpeciesRestEnergy();

        PhaseMatrix T = PhaseMatrix.identity();
        PhaseMatrix Tstep = PhaseMatrix.identity();

        Tstep.setElem(0, 1, dz);
        Tstep.setElem(2, 3, dz);
        Tstep.setElem(4, 5, dz);

        gamma = Ek / restEnergy + 1.0;
        beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
        k = dz * LightSpeed / (gamma * beta * restEnergy);

        for (int i = 0; i < Bz0.length; i++) {
            Tstep.setElem(1, 2, -k * dBr_dr[i]);
            Tstep.setElem(1, 3, k * Bz0[i]);

            Tstep.setElem(3, 0, k * dBr_dr[i]);
            Tstep.setElem(3, 1, -k * Bz0[i]);

            T = Tstep.times(T);
        }
        return T;
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return super.compDriftingTime(probe, dblLen);
    }
}
