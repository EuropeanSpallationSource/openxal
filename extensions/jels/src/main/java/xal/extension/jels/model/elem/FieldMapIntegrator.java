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
public class FieldMapIntegrator {

    private static PhaseMatrix identity = PhaseMatrix.identity();
    private static PhaseMatrix transferMatrix = PhaseMatrix.identity();
    private static PhaseMatrix infTransferMatrix = PhaseMatrix.zero();

    private FieldMapIntegrator() {
    }

    public static PhaseMatrix transferMap(IProbe probe, double length, FieldMapPoint fieldMapPoint) {
        return transferMap(probe, length, fieldMapPoint, 0.);
    }

    public static PhaseMatrix transferMap(IProbe probe, double length, FieldMapPoint fieldMapPoint, double energyGain) {
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
        // Horizontal plane
        infTransferMatrix.setElem(1, 0, k * (fieldMapPoint.getdExdx() - beta * LightSpeed * fieldMapPoint.getdBydx()));
        infTransferMatrix.setElem(1, 1, -k * fieldMapPoint.getEz());
        infTransferMatrix.setElem(1, 2, k * (fieldMapPoint.getdExdy() - beta * LightSpeed * fieldMapPoint.getdBydy()));
        infTransferMatrix.setElem(1, 3, k * beta * LightSpeed * fieldMapPoint.getBz());
        infTransferMatrix.setElem(1, 4, k * (fieldMapPoint.getdExdz() - beta * LightSpeed * fieldMapPoint.getdBydz()));
        infTransferMatrix.setElem(1, 5, -k * (fieldMapPoint.getEx() + beta * LightSpeed * fieldMapPoint.getBy()));

        // Vertical plane
        infTransferMatrix.setElem(3, 0, k * (fieldMapPoint.getdEydx() + beta * LightSpeed * fieldMapPoint.getdBxdx()));
        infTransferMatrix.setElem(3, 1, -k * beta * LightSpeed * fieldMapPoint.getBz());
        infTransferMatrix.setElem(3, 2, k * (fieldMapPoint.getdEydy() + beta * LightSpeed * fieldMapPoint.getdBxdy()));
        infTransferMatrix.setElem(3, 3, -k * fieldMapPoint.getEz());
        infTransferMatrix.setElem(3, 4, k * (fieldMapPoint.getdEydz() + beta * LightSpeed * fieldMapPoint.getdBxdz()));
        infTransferMatrix.setElem(3, 5, -k * (fieldMapPoint.getEy() - beta * LightSpeed * fieldMapPoint.getBx()));

        // Longitudinal plane
        infTransferMatrix.setElem(5, 0, k * fieldMapPoint.getdEzdx());
        infTransferMatrix.setElem(5, 1, k * fieldMapPoint.getEx());
        infTransferMatrix.setElem(5, 2, k * fieldMapPoint.getdEzdy());
        infTransferMatrix.setElem(5, 3, k * fieldMapPoint.getEy());
        infTransferMatrix.setElem(5, 4, k * fieldMapPoint.getdEzdz());
        infTransferMatrix.setElem(5, 5, -k * fieldMapPoint.getEz());

        // TODO: make integrator a choice that can be set in configuration.
        transferMatrix = FieldMapIntegrator.RK4Integrator(infTransferMatrix, length);

        // Renormalizing coordinates to final energy.
          for (int i = 0; i < 6; i++) {
            transferMatrix.setElem(i, 5, transferMatrix.getElem(i, 5) * gammaStart * gammaStart);
            transferMatrix.setElem(5, i, transferMatrix.getElem(5, i) / gammaEnd / gammaEnd);
        }

        // Dipole strengths
        double dph = length * k * (fieldMapPoint.getEx() - beta * LightSpeed * fieldMapPoint.getBy());
        transferMatrix.setElem(1, 6, dph);

        double dpv = length * k * (fieldMapPoint.getEy() + beta * LightSpeed * fieldMapPoint.getBx());
        transferMatrix.setElem(3, 6, dpv);

        return transferMatrix;
    }

    /**
     * First order electromagnetic fieldmap integrator.
     */
    private static PhaseMatrix firtOrderIntegrator(PhaseMatrix infTransferMatrix, double length) {
        transferMatrix = identity.plus(infTransferMatrix.times(length));
        return transferMatrix;
    }

    /**
     * Computes the transfer map for a general electromagnetic field using a 4th
     * order Runge-Kutta integrator (non-symplectic).
     *
     * @param infTransferMatrix
     * @param length
     * @return
     */
    private static PhaseMatrix RK4Integrator(PhaseMatrix infTransferMatrix, double length) {
        PhaseMatrix k1 = infTransferMatrix.times(length);        
        PhaseMatrix k2 = k1.times(0.5);
        k2.plusEquals(identity);
        k2 = k2.times(k1);
        PhaseMatrix k3 = k2.times(0.5);
        k3.plusEquals(identity);
        k3 = k3.times(k1);
        PhaseMatrix k4 = k3.plus(identity);
        k4 = k4.times(k1);

        k1.timesEquals(1/6.);
        k2.timesEquals(1/3.);
        k3.timesEquals(1/3.);
        k4.timesEquals(1/6.);

        transferMatrix = identity.plus(k1);
        transferMatrix.plusEquals(k2);
        transferMatrix.plusEquals(k3);
        transferMatrix.plusEquals(k4);

        return transferMatrix;
    }
}
