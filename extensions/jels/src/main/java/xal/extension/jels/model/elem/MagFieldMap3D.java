/*
 * Copyright (C) 2018 European Spallation Source ERIC.
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
import xal.extension.jels.smf.impl.ESSMagFieldMap3D;
import xal.extension.jels.smf.impl.MagFieldProfile3D;
import static xal.model.IElement.LightSpeed;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThickElectromagnet;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * Magnetic fieldmap 3D implementation.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class MagFieldMap3D extends ThickElectromagnet {

    private double[][][] fieldX;
    private double[][][] fieldY;
    private double[][][] fieldZ;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double lengthZ;
    private double norm;

    private double startPosition;
    private MagFieldMap3D firstSliceFieldmap;

    private static final Logger LOGGER = Logger.getLogger(MagFieldMap3D.class.getName());

    public MagFieldMap3D() {
        this(null);
    }

    public MagFieldMap3D(String strId) {
        super("MagFieldMap3D", strId);
    }

    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);

        if (latticeElement.isFirstSlice()) {
            startPosition = latticeElement.getStartPosition();

            final ESSMagFieldMap3D fm = (ESSMagFieldMap3D) latticeElement.getHardwareNode();
            MagFieldProfile3D fpx = fm.getMagFieldProfileX();
            MagFieldProfile3D fpy = fm.getMagFieldProfileY();
            MagFieldProfile3D fpz = fm.getMagFieldProfileZ();
            fieldX = fpx.getField();
            minX = fpx.getMinX();
            maxX = fpx.getMaxX();
            fieldY = fpy.getField();
            minY = fpx.getMinY();
            maxY = fpx.getMaxY();
            fieldZ = fpz.getField();
            lengthZ = fpz.getLengthZ();
            norm = fpx.getNorm();
        } else {
            try {
                firstSliceFieldmap = (MagFieldMap3D) latticeElement.getFirstSlice().createModelingElement();
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
        int nPointsX = fieldX[0][0].length - 1;
        int nPointsY = fieldY[0].length - 1;
        int nPointsZ = fieldZ.length - 1;

        // To get the (0,0) point in the XY plane.
        int midPointX = (int) ((nPointsX + 1) * minX / (minX - maxX));
        int midPointY = (int) ((nPointsY + 1) * minY / (minY - maxY));

        double spacingZ;
        double dxFieldMap = (maxX - minX) / nPointsX;
        double dyFieldMap = (maxY - minY) / nPointsY;
        double dzFieldMap = lengthZ / nPointsZ;

        double[] Bx0;
        double[] By0;
        double[] Bz0;
        double[] dBx_dx;
        double[] dBx_dy;
        double[] dBx_dz;
        double[] dBy_dx;
        double[] dBy_dy;
        double[] dBy_dz;

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
        double Bx0Start = fieldX[i0 - 1][midPointY][midPointX] + (startStep - z0 + spacingZ / 2)
                * (fieldX[i0][midPointY][midPointX] - fieldX[i0 - 1][midPointY][midPointX]) / dzFieldMap;
        double By0Start = fieldY[i0 - 1][midPointY][midPointX] + (startStep - z0 + spacingZ / 2)
                * (fieldY[i0][midPointY][midPointX] - fieldY[i0 - 1][midPointY][midPointX]) / dzFieldMap;
        double Bz0Start = fieldZ[i0 - 1][midPointY][midPointX] + (startStep - z0 + spacingZ / 2)
                * (fieldZ[i0][midPointY][midPointX] - fieldZ[i0 - 1][midPointY][midPointX]) / dzFieldMap;
        // Linear interpolation of the derivatives of the magnetic field  
        double dBx_dxStart = (fieldX[i0 - 1][midPointY][midPointX + 1] - fieldX[i0 - 1][midPointY][midPointX]
                + (fieldX[i0][midPointY][midPointX + 1] - fieldX[i0][midPointY][midPointX]
                - (fieldX[i0 - 1][midPointY][midPointX + 1] - fieldX[i0 - 1][midPointY][midPointX]))
                * (startStep - z0 + spacingZ / 2) / dzFieldMap) / dxFieldMap;
        double dBx_dyStart = (fieldX[i0 - 1][midPointY + 1][midPointX] - fieldX[i0 - 1][midPointY][midPointX]
                + (fieldX[i0][midPointY + 1][midPointX] - fieldX[i0][midPointY][midPointX]
                - (fieldX[i0 - 1][midPointY + 1][midPointX] - fieldX[i0 - 1][midPointY][midPointX]))
                * (startStep - z0 + spacingZ / 2) / dzFieldMap) / dyFieldMap;
        double dBx_dzStart = (fieldX[i0][midPointY][midPointX] - fieldX[i0 - 1][midPointY][midPointX]) / dzFieldMap;

        double dBy_dxStart = (fieldY[i0 - 1][midPointY][midPointX + 1] - fieldY[i0 - 1][midPointY][midPointX]
                + (fieldY[i0][midPointY][midPointX + 1] - fieldY[i0][midPointY][midPointX]
                - (fieldY[i0 - 1][midPointY][midPointX + 1] - fieldY[i0 - 1][midPointY][midPointX]))
                * (startStep - z0 + spacingZ / 2) / dzFieldMap) / dxFieldMap;
        double dBy_dyStart = (fieldY[i0 - 1][midPointY + 1][midPointX] - fieldY[i0 - 1][midPointY][midPointX]
                + (fieldY[i0][midPointY + 1][midPointX] - fieldY[i0][midPointY][midPointX]
                - (fieldY[i0 - 1][midPointY + 1][midPointX] - fieldY[i0 - 1][midPointY][midPointX]))
                * (startStep - z0 + spacingZ / 2) / dzFieldMap) / dyFieldMap;
        double dBy_dzStart = (fieldY[i0][midPointY][midPointX] - fieldY[i0 - 1][midPointY][midPointX]) / dzFieldMap;

        Bx0 = new double[]{getMagField() * Bx0Start / norm};
        By0 = new double[]{getMagField() * By0Start / norm};
        Bz0 = new double[]{getMagField() * Bz0Start / norm};
        dBx_dx = new double[]{getMagField() * dBx_dxStart / norm};
        dBx_dy = new double[]{getMagField() * dBx_dyStart / norm};
        dBx_dz = new double[]{getMagField() * dBx_dzStart / norm};
        dBy_dx = new double[]{getMagField() * dBy_dxStart / norm};
        dBy_dy = new double[]{getMagField() * dBy_dyStart / norm};
        dBy_dz = new double[]{getMagField() * dBy_dzStart / norm};

        transferMatrix = fieldMapIntegrator(probe, spacingZ, Bx0, By0, Bz0, dBx_dx, dBx_dy, dBx_dz, dBy_dx, dBy_dy, dBy_dz);

        // Central part
        if (in > i0) {
            Bx0 = new double[in - i0];
            By0 = new double[in - i0];
            Bz0 = new double[in - i0];
            dBx_dx = new double[in - i0];
            dBx_dy = new double[in - i0];
            dBx_dz = new double[in - i0];
            dBy_dx = new double[in - i0];
            dBy_dy = new double[in - i0];
            dBy_dz = new double[in - i0];

            for (int i = 0; i < in - i0; i++) {
                Bx0[i] = getMagField() * 0.5 * (fieldX[i0 + i][midPointY][midPointX] + fieldX[i0 + i + 1][midPointY][midPointX]) / norm;
                By0[i] = getMagField() * 0.5 * (fieldY[i0 + i][midPointY][midPointX] + fieldY[i0 + i + 1][midPointY][midPointX]) / norm;
                Bz0[i] = getMagField() * 0.5 * (fieldZ[i0 + i][midPointY][midPointX] + fieldZ[i0 + i + 1][midPointY][midPointX]) / norm;
                dBx_dx[i] = getMagField() * 0.5 * ((fieldX[i0 + i][midPointY][midPointX + 1] - fieldX[i0 + i][midPointY][midPointX])
                        + (fieldX[i0 + i + 1][midPointY][midPointX + 1] - fieldX[i0 + i + 1][midPointY][midPointX])) / dxFieldMap / norm;
                dBx_dy[i] = getMagField() * 0.5 * ((fieldX[i0 + i][midPointY + 1][midPointX] - fieldX[i0 + i][midPointY][midPointX])
                        + (fieldX[i0 + i + 1][midPointY + 1][midPointX] - fieldX[i0 + i + 1][midPointY][midPointX])) / dyFieldMap / norm;
                dBx_dz[i] = getMagField() * (fieldX[i0 + i + 1][midPointY][midPointX] - fieldX[i0 + i][midPointY][midPointX]) / dzFieldMap / norm;
                dBy_dx[i] = getMagField() * 0.5 * ((fieldY[i0 + i][midPointY][midPointX + 1] - fieldY[i0 + i][midPointY][midPointX])
                        + (fieldY[i0 + i + 1][midPointY][midPointX + 1] - fieldY[i0 + i + 1][midPointY][midPointX])) / dxFieldMap / norm;
                dBy_dy[i] = getMagField() * 0.5 * ((fieldY[i0 + i][midPointY + 1][midPointX] - fieldY[i0 + i][midPointY][midPointX])
                        + (fieldY[i0 + i + 1][midPointY + 1][midPointX] - fieldY[i0 + i + 1][midPointY][midPointX])) / dyFieldMap / norm;
                dBy_dz[i] = getMagField() * (fieldY[i0 + i + 1][midPointY][midPointX] - fieldY[i0 + i][midPointY][midPointX]) / dzFieldMap / norm;
            }

            transferMatrix = fieldMapIntegrator(probe, spacingZ, Bx0, By0, Bz0, dBx_dx, dBx_dy, dBx_dz, dBy_dx, dBy_dy, dBy_dz).times(transferMatrix);
        }

        // Last part
        if (in >= i0 && stopStep > in * dzFieldMap) {
            spacingZ = stopStep - in * dzFieldMap;

            Bx0 = new double[]{getMagField() * 0.5 * (fieldX[in][midPointY][midPointX] + fieldX[in + 1][midPointY][midPointX]) / norm};
            By0 = new double[]{getMagField() * 0.5 * (fieldY[in][midPointY][midPointX] + fieldY[in + 1][midPointY][midPointX]) / norm};
            Bz0 = new double[]{getMagField() * 0.5 * (fieldZ[in][midPointY][midPointX] + fieldZ[in + 1][midPointY][midPointX]) / norm};
            dBx_dx = new double[]{getMagField() * 0.5 * ((fieldX[in][midPointY][midPointX + 1] - fieldX[in][midPointY][midPointX])
                + (fieldX[in + 1][midPointY][midPointX + 1] - fieldX[in + 1][midPointY][midPointX])) / dxFieldMap / norm};
            dBx_dy = new double[]{getMagField() * 0.5 * ((fieldX[in][midPointY + 1][midPointX] - fieldX[in][midPointY][midPointX])
                + (fieldX[in + 1][midPointY + 1][midPointX] - fieldX[in + 1][midPointY][midPointX])) / dyFieldMap / norm};
            dBx_dz = new double[]{getMagField() * (fieldX[in + 1][midPointY][midPointX] - fieldX[in][midPointY][midPointX]) / dzFieldMap / norm};
            dBy_dx = new double[]{getMagField() * 0.5 * ((fieldY[in][midPointY][midPointX + 1] - fieldY[in][midPointY][midPointX])
                + (fieldY[in + 1][midPointY][midPointX + 1] - fieldY[in + 1][midPointY][midPointX])) / dxFieldMap / norm};
            dBy_dy = new double[]{getMagField() * 0.5 * ((fieldY[in][midPointY + 1][midPointX] - fieldY[in][midPointY][midPointX])
                + (fieldY[in + 1][midPointY + 1][midPointX] - fieldY[in + 1][midPointY][midPointX])) / dyFieldMap / norm};
            dBy_dz = new double[]{getMagField() * (fieldY[in + 1][midPointY][midPointX] - fieldY[in][midPointY][midPointX]) / dzFieldMap / norm};

            transferMatrix = fieldMapIntegrator(probe, spacingZ, Bx0, By0, Bz0, dBx_dx, dBx_dy, dBx_dz, dBy_dx, dBy_dy, dBy_dz).times(transferMatrix);            
        }
        
        //Add misalignement error
        transferMatrix = applySliceErrors(transferMatrix, probe, dblLen);
        
        return new PhaseMap(transferMatrix);
    }

    // First order integrator
    private PhaseMatrix fieldMapIntegrator(IProbe probe, double dz, double[] Bx0,
            double[] By0, double[] Bz0, double[] dBx_dx, double[] dBx_dy,
            double[] dBx_dz, double[] dBy_dx, double[] dBy_dy, double[] dBy_dz) {

        double gamma;
        double beta;
        double k;

        double dph, dpv;    // Dipole strengths

        // Get probe parameters
        double q = probe.getSpeciesCharge();
        double kineticEnergy = probe.getKineticEnergy();
        double restEnergy = probe.getSpeciesRestEnergy();

        PhaseMatrix transferMatrix = PhaseMatrix.identity();
        PhaseMatrix transferMatrixStep = PhaseMatrix.identity();

        transferMatrixStep.setElem(0, 1, dz);
        transferMatrixStep.setElem(2, 3, dz);
        transferMatrixStep.setElem(4, 5, dz);

        gamma = kineticEnergy / restEnergy + 1.0;
        beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
        k = q * dz * LightSpeed / (gamma * beta * restEnergy);

        for (int i = 0; i < Bz0.length; i++) {
            transferMatrixStep.setElem(1, 0, -k * dBy_dx[i]);
            transferMatrixStep.setElem(1, 2, -k * dBy_dy[i]);
            transferMatrixStep.setElem(1, 3, k * Bz0[i]);
            transferMatrixStep.setElem(1, 4, -k * dBy_dz[i]);
            transferMatrixStep.setElem(1, 5, -k * (gamma * gamma) * By0[i]);

            transferMatrixStep.setElem(3, 0, k * dBx_dx[i]);
            transferMatrixStep.setElem(3, 1, -k * Bz0[i]);
            transferMatrixStep.setElem(3, 2, k * dBx_dy[i]);
            transferMatrixStep.setElem(3, 4, k * dBx_dz[i]);
            transferMatrixStep.setElem(3, 5, k * (gamma * gamma) * Bx0[i]);

            dph = k * -By0[i];
            transferMatrixStep.setElem(1, 6, -dph);

            dpv = k * Bx0[i];
            transferMatrixStep.setElem(3, 6, -dpv);

            transferMatrix = transferMatrixStep.times(transferMatrix);
        }
        return transferMatrix;
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return super.compDriftingTime(probe, dblLen);
    }
}
