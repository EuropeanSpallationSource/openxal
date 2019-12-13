/*
 * Copyright (C) 2019 European Spallation Source ERIC.
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

import static xal.model.IElement.LightSpeed;
import xal.model.IProbe;
import xal.tools.beam.PhaseMatrix;

/**
 * General electromagnetic field map integrator. Only the energy kick part, with
 * longitudinal and transverse focusing plus bending, but not the drift space.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class FieldMapIntegrator extends PhaseMatrix {

    private static final int NDIM = 7;
    private boolean coupled = true;
    private Integrator integrator = this::RK4Integrator;
    private Operations operations = new CoupledOperations();

    public void setCoupled(boolean coupled) {
        this.coupled = coupled;
        if (coupled) {
            operations = new CoupledOperations();
        } else {
            operations = new UncoupledOperations();
        }
    }

    public FieldMapIntegrator(PhaseMatrix matrix) {
        super(matrix);
    }

    public FieldMapIntegrator(PhaseMatrix matrix, String integrator) {
        super(matrix);
        if (integrator.equals("FirstOrder")) {
            this.integrator = this::firstOrderIntegrator;
        }
    }

    public static FieldMapIntegrator identity() {
        return new FieldMapIntegrator(PhaseMatrix.identity());
    }

    public void timesKick(IProbe probe, double length, FieldMapPoint fieldMapPoint) {
        timesKick(probe, length, fieldMapPoint, 0.);
    }

    public void timesKick(IProbe probe, double length, FieldMapPoint fieldMapPoint, double energyGain) {
        // Get probe parameters
        double q = probe.getSpeciesCharge();
        double kineticEnergy = probe.getKineticEnergy() + energyGain;
        double restEnergy = probe.getSpeciesRestEnergy();
        double gammaStart = kineticEnergy / restEnergy + 1.0;
        double beta = Math.sqrt(1.0 - 1.0 / (gammaStart * gammaStart));

        kineticEnergy += fieldMapPoint.getEz() * length;

        double gammaEnd = kineticEnergy / probe.getSpeciesRestEnergy() + 1.0;

        double k = q / (gammaStart * beta * beta * restEnergy);

        // Building the infinitesimal transfer matrix (X' = F*X)
        double[] infTransferMatrixArray = new double[NDIM * NDIM];
        // Horizontal plane;
        infTransferMatrixArray[1 * NDIM + 0] = k * (fieldMapPoint.getdExdx() - beta * LightSpeed * fieldMapPoint.getdBydx());
        infTransferMatrixArray[1 * NDIM + 1] = -k * fieldMapPoint.getEz();

        // Vertical plane
        infTransferMatrixArray[3 * NDIM + 2] = k * (fieldMapPoint.getdEydy() + beta * LightSpeed * fieldMapPoint.getdBxdy());
        infTransferMatrixArray[3 * NDIM + 3] = -k * fieldMapPoint.getEz();

        // Longitudinal plane
        infTransferMatrixArray[5 * NDIM + 4] = k * fieldMapPoint.getdEzdz();
        infTransferMatrixArray[5 * NDIM + 5] = -k * fieldMapPoint.getEz();

        // Coupling terms
        if (coupled) {
            // Horizontal plane;
            infTransferMatrixArray[1 * NDIM + 2] = k * (fieldMapPoint.getdExdy() - beta * LightSpeed * fieldMapPoint.getdBydy());
            infTransferMatrixArray[1 * NDIM + 3] = k * beta * LightSpeed * fieldMapPoint.getBz();
            infTransferMatrixArray[1 * NDIM + 4] = k * (fieldMapPoint.getdExdz() - beta * LightSpeed * fieldMapPoint.getdBydz());
            infTransferMatrixArray[1 * NDIM + 5] = -k * (fieldMapPoint.getEx() + beta * LightSpeed * fieldMapPoint.getBy());

            // Vertical plane
            infTransferMatrixArray[3 * NDIM + 0] = k * (fieldMapPoint.getdEydx() + beta * LightSpeed * fieldMapPoint.getdBxdx());
            infTransferMatrixArray[3 * NDIM + 1] = -k * beta * LightSpeed * fieldMapPoint.getBz();
            infTransferMatrixArray[3 * NDIM + 4] = k * (fieldMapPoint.getdEydz() + beta * LightSpeed * fieldMapPoint.getdBxdz());
            infTransferMatrixArray[3 * NDIM + 5] = -k * (fieldMapPoint.getEy() - beta * LightSpeed * fieldMapPoint.getBx());

            // Longitudinal plane
            infTransferMatrixArray[5 * NDIM + 0] = k * fieldMapPoint.getdEzdx();
            infTransferMatrixArray[5 * NDIM + 1] = k * fieldMapPoint.getEx();
            infTransferMatrixArray[5 * NDIM + 2] = k * fieldMapPoint.getdEzdy();
            infTransferMatrixArray[5 * NDIM + 3] = k * fieldMapPoint.getEy();
        }

        // Integrating the infitinesimal matrix
        integrator.integrate(infTransferMatrixArray, length);

        // Renormalizing coordinates to final energy.
        for (int i = 0; i < 6; i++) {
            infTransferMatrixArray[i * NDIM + 5] *= gammaStart * gammaStart;
            infTransferMatrixArray[5 * NDIM + i] /= gammaEnd * gammaEnd;
        }

        // Dipole strengths
        double dph = length * k * (fieldMapPoint.getEx() - beta * LightSpeed * fieldMapPoint.getBy());
        infTransferMatrixArray[1 * NDIM + 6] = dph;

        double dpv = length * k * (fieldMapPoint.getEy() + beta * LightSpeed * fieldMapPoint.getBx());
        infTransferMatrixArray[3 * NDIM + 6] = dpv;

        getMatrix().data = operations.matrixMultiplication(infTransferMatrixArray, getMatrix().data);
    }

    public void timesDriftLeft(double length) {
        operations.timesDriftLeft(length, getMatrix().data);
    }

    /**
     * First order electromagnetic fieldmap integrator.
     */
    private void firstOrderIntegrator(double[] infTransferMatrixArray, double length) {
        operations.matrixDoubleMultiplication(infTransferMatrixArray, length);
        operations.addIdentityInPlace(infTransferMatrixArray);
    }

    /**
     * Computes the transfer map for a general electromagnetic field using a 4th
     * order Runge-Kutta integrator (non-symplectic).
     *
     * @param infTransferMatrix
     * @param length
     */
    private void RK4Integrator(double[] infTransferMatrixArray, double length) {
        double[] k1 = infTransferMatrixArray;
        operations.matrixDoubleMultiplication(k1, length);
        double[] k2 = k1.clone();
        operations.matrixDoubleMultiplication(k2, 0.5);
        operations.addIdentityInPlace(k2);
        k2 = operations.matrixMultiplication(k1, k2);
        double[] k3 = k2.clone();
        operations.matrixDoubleMultiplication(k3, 0.5);
        operations.addIdentityInPlace(k3);
        k3 = operations.matrixMultiplication(k1, k3);
        double[] k4 = k3.clone();
        operations.addIdentityInPlace(k4);
        k4 = operations.matrixMultiplication(k1, k4);

        operations.matrixDoubleMultiplication(k1, 1 / 6.);
        operations.matrixDoubleMultiplication(k2, 1 / 3.);
        operations.matrixDoubleMultiplication(k3, 1 / 3.);
        operations.matrixDoubleMultiplication(k4, 1 / 6.);

        operations.addIdentityInPlace(infTransferMatrixArray);
        operations.matrixSum(infTransferMatrixArray, k2);
        operations.matrixSum(infTransferMatrixArray, k3);
        operations.matrixSum(infTransferMatrixArray, k4);
    }

    /**
     * Interface to select an integrator by reference.
     */
    public interface Integrator {

        void integrate(double[] infTransferMatrixArray, double length);
    }

    /**
     * Abstract class for implementing optimized functions for matrix
     * operations.
     */
    public abstract class Operations {

        /**
         * Multiply in place a matrix by a scalar value.
         *
         * @param matrix
         * @param value
         */
        abstract void matrixDoubleMultiplication(double[] matrix, double value);

        /**
         * Multiply two matrices and return the result in the first matrix.
         *
         * @param matrix1
         * @param matrix2
         */
        abstract double[] matrixMultiplication(double[] matrix1, double[] matrix2);

        /**
         * Sum two matrices and return the result in the first matrix.
         *
         * @param matrix1
         * @param matrix2
         */
        abstract void matrixSum(double[] matrix1, double[] matrix2);

        /**
         * Apply a drift transfer matrix to the matrix from the left.
         */
        abstract void timesDriftLeft(double length, double[] matrix);

        /**
         * Add the identity matrix to the given matrix.
         *
         * @param matrix
         */
        public void addIdentityInPlace(double[] matrix) {
            for (int i = 0; i < NDIM; i++) {
                matrix[i * NDIM + i] += 1.0;
            }
        }
    }

    /**
     * Implementation for fieldmaps that can couple different planes.
     */
    private class CoupledOperations extends Operations {

        @Override
        public double[] matrixMultiplication(double[] matrix1, double[] matrix2) {
            double[] auxMatrix = new double[NDIM * NDIM];

            for (int i = 0; i < NDIM; i++) {
                for (int j = 0; j < NDIM; j++) {
                    for (int k = 0; k < NDIM; k++) {
                        auxMatrix[i * NDIM + j] += matrix1[i * NDIM + k] * matrix2[k * NDIM + j];
                    }
                }
            }
            auxMatrix[NDIM * NDIM - 1] = 1.0;

            return auxMatrix;
        }

        @Override
        public void matrixDoubleMultiplication(double[] matrix, double value) {
            for (int i = 0; i < NDIM; i++) {
                for (int j = 0; j < NDIM; j++) {
                    matrix[i * NDIM + j] *= value;
                }
            }
        }

        @Override
        public void timesDriftLeft(double length, double[] matrix) {
            for (int i = 0; i < NDIM - 1; i += 2) {
                for (int j = 0; j < NDIM; j++) {
                    matrix[i * NDIM + j] += length * matrix[(i + 1) * NDIM + j];
                }
            }
        }

        @Override
        public void matrixSum(double[] matrix1, double[] matrix2) {
            for (int i = 0; i < NDIM; i++) {
                for (int j = 0; j < NDIM; j++) {
                    matrix1[i * NDIM + j] += matrix2[i * NDIM + j];
                }
            }
        }
    }

    /**
     * Implementation for fieldmaps where different planes are decoupled.
     */
    private class UncoupledOperations extends Operations {

        @Override
        public double[] matrixMultiplication(double[] matrix1, double[] matrix2) {
            double[] auxMatrix = new double[NDIM * NDIM];

            for (int planes = 0; planes < 3; planes++) {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        for (int k = 0; k < 2; k++) {
                            auxMatrix[(i + 2 * planes) * NDIM + (j + 2 * planes)] += matrix1[(i + 2 * planes) * NDIM + (k + 2 * planes)] * matrix2[(k + 2 * planes) * NDIM + (j + 2 * planes)];
                        }
                    }
                }
            }
            auxMatrix[NDIM * NDIM - 1] = 1.0;

            return auxMatrix;
        }

        @Override
        public void matrixDoubleMultiplication(double[] matrix, double value) {
            for (int planes = 0; planes < 3; planes++) {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        matrix[(i + 2 * planes) * NDIM + (j + 2 * planes)] *= value;
                    }
                }
            }
        }

        @Override
        public void timesDriftLeft(double length, double[] matrix) {
            for (int i = 0; i < NDIM - 1; i += 2) {
                for (int j = 0; j < 2; j++) {
                    matrix[i * NDIM + j + i] += length * matrix[(i + 1) * NDIM + j + i];
                }
            }
        }

        @Override
        public void matrixSum(double[] matrix1, double[] matrix2) {
            for (int i = 0; i < NDIM; i++) {
                for (int j = 0; j < NDIM; j++) {
                    matrix1[i * NDIM + j] += matrix2[i * NDIM + j];
                }
            }
        }
    }
}
