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

    private double[][] fieldR;
    private double[][] fieldZ;
    private double lengthZ;
    private double lengthR;
    private double norm;

    private double startPosition;
    private SolFieldMap firstSliceFieldmap;

    private static final Logger LOGGER = Logger.getLogger(SolFieldMap.class.getName());

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
                LOGGER.log(Level.INFO, "Couldn't load the first slice of the fieldmap{0}", e.getMessage());
            }
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
     *
     * @return The calculated transfer matrix
     * @throws xal.model.ModelException
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen)
            throws ModelException {
        if (firstSliceFieldmap != null) {
            return firstSliceFieldmap.transferMap(probe, dblLen);
        }
        int nPointsZ = fieldZ.length - 1;
        int nPointsR = fieldR[0].length - 1;

        double spacingZ;
        double spacingR = lengthR / nPointsR;
        double dzFieldMap = lengthZ / nPointsZ;

        double[] Bz0;
        double[] dBr_dr;

        PhaseMatrix transferMatrix;

        double startStep = probe.getPosition() - startPosition;
        double stopStep = startStep + dblLen;

        if (startStep < 0) {
            startStep = 0;
        }

        int i0 = (int) Math.floor(startStep / dzFieldMap) + 1;
        int in = (int) Math.floor(stopStep / dzFieldMap);
        if (in >= nPointsZ) {
            in = nPointsZ - 1;
        }

        // First part
        // Small step in between two points
        if (in < i0) {
            spacingZ = stopStep - startStep;
        } else {
            spacingZ = i0 * dzFieldMap - startStep;
        }

        double z0 = (i0 - 1) * dzFieldMap;

        // Linear interpolation of the magnetic field
        double fieldZStart = fieldZ[i0 - 1][0] + (startStep - z0 + spacingZ / 2)
                * (fieldZ[i0][0] - fieldZ[i0 - 1][0]) / dzFieldMap;
        double dFieldRStart = fieldR[i0 - 1][1] - fieldR[i0 - 1][0] + (startStep - z0 + spacingZ / 2)
                * (fieldR[i0][1] - fieldR[i0][0] - (fieldR[i0 - 1][1] - fieldR[i0 - 1][0])) / dzFieldMap;

        Bz0 = new double[]{getMagField() * fieldZStart / norm};
        dBr_dr = new double[]{getMagField() * dFieldRStart / spacingR / norm};

        transferMatrix = fieldMapIntegrator(probe, spacingZ, Bz0, dBr_dr);

        // Central part
        if (in > i0) {
            Bz0 = new double[in - i0];
            dBr_dr = new double[in - i0];

            for (int i = 0; i < in - i0; i++) {
                Bz0[i] = getMagField() * 0.5 * (fieldZ[i0 + i][0] + fieldZ[i0 + i + 1][0]) / norm;
                dBr_dr[i] = getMagField() * 0.5 * ((fieldR[i0 + i][1] - fieldR[i0 + i][0])
                        + (fieldR[i0 + i + 1][1] - fieldR[i0 + i + 1][0])) / spacingR / norm;
            }

            transferMatrix = fieldMapIntegrator(probe, dzFieldMap, Bz0, dBr_dr).times(transferMatrix);
        }

        // Last part
        if (in >= i0 && stopStep > in * dzFieldMap) {
            spacingZ = stopStep - in * dzFieldMap;

            Bz0 = new double[]{getMagField() * 0.5 * (fieldZ[in][0] + fieldZ[in + 1][0]) / norm};
            dBr_dr = new double[]{getMagField() * 0.5
                * ((fieldR[in][1] - fieldR[in][0]) + (fieldR[in + 1][1] - fieldR[in + 1][0]))
                / spacingR / norm};

            transferMatrix = fieldMapIntegrator(probe, spacingZ, Bz0, dBr_dr).times(transferMatrix);
        }
        
        //Add misalignement error
        transferMatrix = applySliceErrors(transferMatrix, probe, dblLen);
        
        return new PhaseMap(transferMatrix);
    }

    // First order integrator
    private PhaseMatrix fieldMapIntegrator(IProbe probe, double dz, double[] Bz0, double[] dBr_dr) {
        double gamma;
        double beta;
        double k;

        double kineticEnergy = probe.getKineticEnergy();
        double restEnergy = probe.getSpeciesRestEnergy();

        PhaseMatrix transferMatrix = PhaseMatrix.identity();
        PhaseMatrix transferMatrixStep = PhaseMatrix.identity();

        transferMatrixStep.setElem(0, 1, dz);
        transferMatrixStep.setElem(2, 3, dz);
        transferMatrixStep.setElem(4, 5, dz);

        gamma = kineticEnergy / restEnergy + 1.0;
        beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
        k = dz * LightSpeed / (gamma * beta * restEnergy);

        for (int i = 0; i < Bz0.length; i++) {
            transferMatrixStep.setElem(1, 2, -k * dBr_dr[i]);
            transferMatrixStep.setElem(1, 3, k * Bz0[i]);

            transferMatrixStep.setElem(3, 0, k * dBr_dr[i]);
            transferMatrixStep.setElem(3, 1, -k * Bz0[i]);

            transferMatrix = transferMatrixStep.times(transferMatrix);
        }
        return transferMatrix;
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return super.compDriftingTime(probe, dblLen);
    }
}
