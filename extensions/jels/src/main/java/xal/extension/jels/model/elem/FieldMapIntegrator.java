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
 * General first order electromagnetic fieldmap integrator. Only the energy kick
 * part, with longitudinal and transverse focusing plus bending, but not the
 * drift space.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class FieldMapIntegrator {

    private static PhaseMatrix transferMatrix = PhaseMatrix.identity();

    private FieldMapIntegrator() {
    }

    public static PhaseMatrix transferMap(IProbe probe, double length, FieldMapPoint fieldMapPoint) {
        return transferMap(probe, length, fieldMapPoint, 0.);
    }

    /**
     * Computes the transfer map for a general electromagnetic field.
     *
     * @param probe
     * @param length
     * @param fieldMapPoint
     * @param energyGain Accumulated energy gain not added to the probe kinetic
     * energy property yet. To be used when the probe energy is not up-to-date,
     * i.e., when calculating an RF cavity in several steps.
     * @return
     */
    public static PhaseMatrix transferMap(IProbe probe, double length, FieldMapPoint fieldMapPoint, double energyGain) {
        // Get probe parameters
        double q = probe.getSpeciesCharge();
        double kineticEnergy = probe.getKineticEnergy() + energyGain;
        double restEnergy = probe.getSpeciesRestEnergy();
        double gammaStart = kineticEnergy / restEnergy + 1.0;
        double beta = Math.sqrt(1.0 - 1.0 / (gammaStart * gammaStart));

        kineticEnergy += fieldMapPoint.getEz() * length;

        double gammaEnd = kineticEnergy / probe.getSpeciesRestEnergy() + 1.0;

        double k = q * length / (gammaStart * beta * beta * restEnergy);

        // Horizontal plane
        transferMatrix.setElem(1, 0, k * (fieldMapPoint.getdExdx() - beta * LightSpeed * fieldMapPoint.getdBydx()));
        transferMatrix.setElem(1, 1, 1 - k * fieldMapPoint.getEz());
        transferMatrix.setElem(1, 2, k * (fieldMapPoint.getdExdy() - beta * LightSpeed * fieldMapPoint.getdBydy()));
        transferMatrix.setElem(1, 3, k * beta * LightSpeed * fieldMapPoint.getBz());
        transferMatrix.setElem(1, 4, k * (fieldMapPoint.getdExdz() - beta * LightSpeed * fieldMapPoint.getdBydz()));
        transferMatrix.setElem(1, 5, -k * gammaStart * gammaStart * (fieldMapPoint.getEx() + beta * LightSpeed * fieldMapPoint.getBy()));

        // Vertical plane
        transferMatrix.setElem(3, 0, k * (fieldMapPoint.getdEydx() + beta * LightSpeed * fieldMapPoint.getdBxdx()));
        transferMatrix.setElem(3, 1, -k * beta * LightSpeed * fieldMapPoint.getBz());
        transferMatrix.setElem(3, 2, k * (fieldMapPoint.getdEydy() + beta * LightSpeed * fieldMapPoint.getdBxdy()));
        transferMatrix.setElem(3, 3, 1 - k * fieldMapPoint.getEz());
        transferMatrix.setElem(3, 4, k * (fieldMapPoint.getdEydz() + beta * LightSpeed * fieldMapPoint.getdBxdz()));
        transferMatrix.setElem(3, 5, -k * gammaStart * gammaStart * (fieldMapPoint.getEy() - beta * LightSpeed * fieldMapPoint.getBx()));

        // Longitudinal plane
        transferMatrix.setElem(5, 0, k * fieldMapPoint.getdEzdx() / (gammaEnd * gammaEnd));
        transferMatrix.setElem(5, 1, k * fieldMapPoint.getEx() / (gammaEnd * gammaEnd));
        transferMatrix.setElem(5, 2, k * fieldMapPoint.getdEzdy() / (gammaEnd * gammaEnd));
        transferMatrix.setElem(5, 3, k * fieldMapPoint.getEy() / (gammaEnd * gammaEnd));
        transferMatrix.setElem(5, 4, k * fieldMapPoint.getdEzdz() / (gammaEnd * gammaEnd));
        transferMatrix.setElem(5, 5, (1 - k * fieldMapPoint.getEz()) * (gammaStart * gammaStart) / (gammaEnd * gammaEnd));

        // Dipole strengths
        double dph = k * (fieldMapPoint.getEx() - beta * LightSpeed * fieldMapPoint.getBy());
        transferMatrix.setElem(1, 6, -dph);

        double dpv = k * (fieldMapPoint.getEy() + beta * LightSpeed * fieldMapPoint.getBx());
        transferMatrix.setElem(3, 6, -dpv);

        return transferMatrix;
    }
}
