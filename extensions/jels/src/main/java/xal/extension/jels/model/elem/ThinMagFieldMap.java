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

import xal.extension.jels.smf.impl.FieldMap;
import xal.extension.jels.smf.impl.MagFieldMap;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThinElectromagnet;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * Magnetic fieldmap 3D implementation.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class ThinMagFieldMap extends ThinElectromagnet {

    private FieldMap magFieldmap;

    private double position;
    private double startPosition;
    private double sliceLength;
    private double centerPosition;

    public ThinMagFieldMap() {
        this(null);
    }

    public ThinMagFieldMap(String strId) {
        super("MagFieldMap3D", strId);
    }

    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);

        position = latticeElement.getStartPosition();

        final MagFieldMap fieldmap = (MagFieldMap) latticeElement.getHardwareNode();
        magFieldmap = fieldmap.getFieldMap();
        sliceLength = fieldmap.getSliceLength();
        startPosition = fieldmap.getPosition() - fieldmap.getLength() / 2.0;
        centerPosition = fieldmap.getPosition();
    }

    /**
     * Method calculates transfer matrix for the field map for a given data
     * point in the field map. Drift spaces are calculated separately.
     *
     * @return
     * @throws xal.model.ModelException
     */
    @Override
    public PhaseMap transferMap(IProbe probe)
            throws ModelException {

        FieldMapPoint fieldMapPoint = magFieldmap.getFieldAt(position - startPosition);

        fieldMapPoint.setAmplitudeFactorB(getMagField());

        double dz = sliceLength;
        // First and last slices of the element get half a kick
        if ((Math.abs(position - startPosition) < 1e-6) || (Math.abs(position - startPosition - magFieldmap.getLength()) < 1e-6)) {
            dz /= 2.;
        }

        FieldMapIntegrator integrator = FieldMapIntegrator.identity();
        integrator.timesKick(probe, dz, fieldMapPoint);

        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors
        double slicepos = centerPosition - position; // distance from the probe position and element center
        PhaseMatrix transferMatrix = applyErrors((PhaseMatrix) integrator, slicepos);

        return new PhaseMap(transferMatrix);
    }

    @Override
    public double energyGain(IProbe probe) {
        return 0.0;
    }

    @Override
    protected double elapsedTime(IProbe probe) {
        return 0.0;
    }
}
