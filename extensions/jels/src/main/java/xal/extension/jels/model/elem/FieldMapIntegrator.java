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

import static xal.model.IElement.LIGHT_SPEED;
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
    private boolean firstOrder = false;
    private Operations operations = new CoupledOperations();

    // Reusable scratch buffers for the RK4 integrator, so timesKick does not
    // allocate (or clone) a matrix per field map point.
    private final double[] rkK2 = new double[NDIM * NDIM];
    private final double[] rkK3 = new double[NDIM * NDIM];
    private final double[] rkK4 = new double[NDIM * NDIM];
    private final double[] rkTmp = new double[NDIM * NDIM];

    // Spare accumulator buffer, ping-ponged with the transfer matrix backing
    // array in timesKick so the per-point product does not allocate.
    private double[] accBuf = new double[NDIM * NDIM];

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
            this.firstOrder = true;
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
        // Horizontal plane
        infTransferMatrixArray[1 * NDIM + 0] = k * (fieldMapPoint.getdExdx() - beta * LIGHT_SPEED * fieldMapPoint.getdBydx());
        infTransferMatrixArray[1 * NDIM + 1] = -k * fieldMapPoint.getEz();

        // Vertical plane
        infTransferMatrixArray[3 * NDIM + 2] = k * (fieldMapPoint.getdEydy() + beta * LIGHT_SPEED * fieldMapPoint.getdBxdy());
        infTransferMatrixArray[3 * NDIM + 3] = -k * fieldMapPoint.getEz();

        // Longitudinal plane
        infTransferMatrixArray[5 * NDIM + 4] = k * fieldMapPoint.getdEzdz();
        infTransferMatrixArray[5 * NDIM + 5] = -k * fieldMapPoint.getEz();

        // Coupling terms
        if (coupled) {
            // Horizontal plane
            infTransferMatrixArray[1 * NDIM + 2] = k * (fieldMapPoint.getdExdy() - beta * LIGHT_SPEED * fieldMapPoint.getdBydy());
            infTransferMatrixArray[1 * NDIM + 3] = k * beta * LIGHT_SPEED * fieldMapPoint.getBz();
            infTransferMatrixArray[1 * NDIM + 4] = k * (fieldMapPoint.getdExdz() - beta * LIGHT_SPEED * fieldMapPoint.getdBydz());
            infTransferMatrixArray[1 * NDIM + 5] = -k * (fieldMapPoint.getEx() + beta * LIGHT_SPEED * fieldMapPoint.getBy());

            // Vertical plane
            infTransferMatrixArray[3 * NDIM + 0] = k * (fieldMapPoint.getdEydx() + beta * LIGHT_SPEED * fieldMapPoint.getdBxdx());
            infTransferMatrixArray[3 * NDIM + 1] = -k * beta * LIGHT_SPEED * fieldMapPoint.getBz();
            infTransferMatrixArray[3 * NDIM + 4] = k * (fieldMapPoint.getdEydz() + beta * LIGHT_SPEED * fieldMapPoint.getdBxdz());
            infTransferMatrixArray[3 * NDIM + 5] = -k * (fieldMapPoint.getEy() - beta * LIGHT_SPEED * fieldMapPoint.getBx());

            // Longitudinal plane
            infTransferMatrixArray[5 * NDIM + 0] = k * fieldMapPoint.getdEzdx();
            infTransferMatrixArray[5 * NDIM + 1] = k * fieldMapPoint.getEx();
            infTransferMatrixArray[5 * NDIM + 2] = k * fieldMapPoint.getdEzdy();
            infTransferMatrixArray[5 * NDIM + 3] = k * fieldMapPoint.getEy();
        }

        // Integrating the infitinesimal matrix
        if (firstOrder) {
            firstOrderIntegrator(infTransferMatrixArray, length);
        } else if (coupled) {
            rk4Integrator(infTransferMatrixArray, length);
        } else {
            // For uncoupled maps the generator is block diagonal, so the RK4
            // 4th-order truncated exponential reduces to a closed-form 2x2
            // exponential per plane (see blockRk4IntegratorUncoupled).
            blockRk4IntegratorUncoupled(infTransferMatrixArray, length);
        }

        // Renormalizing coordinates to final energy.
        for (int i = 0; i < 6; i++) {
            infTransferMatrixArray[i * NDIM + 5] *= gammaStart * gammaStart;
            infTransferMatrixArray[5 * NDIM + i] /= gammaEnd * gammaEnd;
        }

        // Dipole strengths
        double dph = length * k * (fieldMapPoint.getEx() - beta * LIGHT_SPEED * fieldMapPoint.getBy());
        infTransferMatrixArray[1 * NDIM + 6] = dph;

        double dpv = length * k * (fieldMapPoint.getEy() + beta * LIGHT_SPEED * fieldMapPoint.getBx());
        infTransferMatrixArray[3 * NDIM + 6] = dpv;

        // Accumulate the kick into the running transfer matrix, ping-ponging
        // between the backing array and the spare buffer to avoid allocating.
        double[] data = getMatrix().data;
        operations.matrixMultiplication(infTransferMatrixArray, data, accBuf);
        getMatrix().data = accBuf;
        accBuf = data;
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
     * Closed-form equivalent of {@link #rk4Integrator} for the uncoupled
     * (block-diagonal) case.
     * <p>
     * When the generator is block diagonal, the RK4 result
     * {@code M = I + A + A^2/2 + A^3/6 + A^4/24} (with {@code A = F*length})
     * decouples into an independent 2x2 truncated exponential per plane. By the
     * Cayley-Hamilton theorem every power of a 2x2 matrix {@code B} is a linear
     * combination of {@code I} and {@code B}, so {@code M_block = alpha*I +
     * beta*B} where the scalar coefficients depend only on the trace and
     * determinant of {@code B}. This replaces the per-point 7x7 matrix RK4 dance
     * with a few scalar operations, and is analytically identical to
     * {@link #rk4Integrator} (differing only by floating-point rounding order).
     *
     * @param m generator {@code F} in place; overwritten with the transfer matrix
     * @param length integration length
     */
    private void blockRk4IntegratorUncoupled(double[] m, double length) {
        for (int plane = 0; plane < 3; plane++) {
            int i0 = 2 * plane;
            int i1 = i0 + 1;

            double a = m[i0 * NDIM + i0] * length;
            double b = m[i0 * NDIM + i1] * length;
            double c = m[i1 * NDIM + i0] * length;
            double d = m[i1 * NDIM + i1] * length;

            double trace = a + d;
            double det = a * d - b * c;

            // Coefficients of the truncated series sum_{n=0}^{4} B^n / n!
            // expressed via B^2 = trace*B - det*I (Cayley-Hamilton).
            double beta = 1.0 + trace / 2.0 + (trace * trace - det) / 6.0
                    + (trace * trace * trace - 2.0 * trace * det) / 24.0;
            double alpha = 1.0 - det / 2.0 - trace * det / 6.0
                    + (det * det - trace * trace * det) / 24.0;

            m[i0 * NDIM + i0] = alpha + beta * a;
            m[i0 * NDIM + i1] = beta * b;
            m[i1 * NDIM + i0] = beta * c;
            m[i1 * NDIM + i1] = alpha + beta * d;
        }
        m[NDIM * NDIM - 1] = 1.0;
    }

    /**
     * Computes the transfer map for a general electromagnetic field using a 4th
     * order Runge-Kutta integrator (non-symplectic).
     *
     * @param infTransferMatrix
     * @param length
     */
    private void rk4Integrator(double[] infTransferMatrixArray, double length) {
        // k1 aliases the input and becomes A = F*L. The RK4 stages are built in
        // the reusable rkK2/rkK3/rkK4 buffers (with rkTmp as scratch), avoiding
        // the per-point clones and result allocations. The operation order is
        // identical to the previous allocating implementation.
        double[] k1 = infTransferMatrixArray;
        operations.matrixDoubleMultiplication(k1, length);

        System.arraycopy(k1, 0, rkTmp, 0, k1.length);
        operations.matrixDoubleMultiplication(rkTmp, 0.5);
        operations.addIdentityInPlace(rkTmp);
        operations.matrixMultiplication(k1, rkTmp, rkK2);

        System.arraycopy(rkK2, 0, rkTmp, 0, rkK2.length);
        operations.matrixDoubleMultiplication(rkTmp, 0.5);
        operations.addIdentityInPlace(rkTmp);
        operations.matrixMultiplication(k1, rkTmp, rkK3);

        System.arraycopy(rkK3, 0, rkTmp, 0, rkK3.length);
        operations.addIdentityInPlace(rkTmp);
        operations.matrixMultiplication(k1, rkTmp, rkK4);

        operations.matrixDoubleMultiplication(k1, 1 / 6.);
        operations.matrixDoubleMultiplication(rkK2, 1 / 3.);
        operations.matrixDoubleMultiplication(rkK3, 1 / 3.);
        operations.matrixDoubleMultiplication(rkK4, 1 / 6.);

        operations.addIdentityInPlace(infTransferMatrixArray);
        operations.matrixSum(infTransferMatrixArray, rkK2);
        operations.matrixSum(infTransferMatrixArray, rkK3);
        operations.matrixSum(infTransferMatrixArray, rkK4);
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
         * Multiply two matrices, writing the product into {@code out} (which
         * must not alias {@code matrix1} or {@code matrix2}). Avoids allocating
         * a result array on the hot path.
         *
         * @param matrix1
         * @param matrix2
         * @param out destination for the product
         */
        abstract void matrixMultiplication(double[] matrix1, double[] matrix2, double[] out);

        /**
         * Multiply two matrices and return the product in a new array.
         *
         * @param matrix1
         * @param matrix2
         */
        public double[] matrixMultiplication(double[] matrix1, double[] matrix2) {
            double[] out = new double[NDIM * NDIM];
            matrixMultiplication(matrix1, matrix2, out);
            return out;
        }

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
        public void matrixMultiplication(double[] matrix1, double[] matrix2, double[] out) {
            for (int i = 0; i < NDIM; i++) {
                for (int j = 0; j < NDIM; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < NDIM; k++) {
                        sum += matrix1[i * NDIM + k] * matrix2[k * NDIM + j];
                    }
                    out[i * NDIM + j] = sum;
                }
            }
            out[NDIM * NDIM - 1] = 1.0;
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
        public void matrixMultiplication(double[] matrix1, double[] matrix2, double[] out) {
            // Only the three 2x2 plane blocks are written; off-block elements of
            // out are left untouched (they are zero in the RK4 scratch buffers,
            // which are only ever written here).
            for (int planes = 0; planes < 3; planes++) {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        double sum = 0.0;
                        for (int k = 0; k < 2; k++) {
                            sum += matrix1[(i + 2 * planes) * NDIM + (k + 2 * planes)] * matrix2[(k + 2 * planes) * NDIM + (j + 2 * planes)];
                        }
                        out[(i + 2 * planes) * NDIM + (j + 2 * planes)] = sum;
                    }
                }
            }
            out[NDIM * NDIM - 1] = 1.0;
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
            // For uncoupled maps only the three 2x2 plane blocks (and the
            // homogeneous [6][6] element) are ever non-zero, so summing the rest
            // is wasted work. Restricting the loop to those elements is
            // bit-identical (the skipped entries are always 0 + 0) and removes
            // the dominant cost of the RK4 integrator.
            for (int planes = 0; planes < 3; planes++) {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        int idx = (i + 2 * planes) * NDIM + (j + 2 * planes);
                        matrix1[idx] += matrix2[idx];
                    }
                }
            }
            matrix1[NDIM * NDIM - 1] += matrix2[NDIM * NDIM - 1];
        }
    }
}
