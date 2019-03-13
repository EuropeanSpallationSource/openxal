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

import java.util.List;
import xal.extension.jels.smf.impl.FieldMap;
import xal.extension.jels.smf.impl.MagFieldMap;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThickElectromagnet;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * Thick element version for magnetic field map implementation. This class is
 * supposed to be faster than the
 * {@link xal.extension.jels.model.elem.ThickMagFieldMap} for fieldmaps with
 * many data points, since it removes the overhead of creating an element for
 * every point in the fieldmap. The drawback is that it can't be superposed to
 * other ThickElements.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class ThickMagFieldMap extends ThickElectromagnet {

    private FieldMap magFieldmap;

    private double startPosition = 0;
    private double sliceLength = 0;

    private double sliceStartPosition;

    public ThickMagFieldMap() {
        this(null);
    }

    public ThickMagFieldMap(String strId) {
        super("FieldMap", strId);
    }

    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);

        final MagFieldMap fieldmap = (MagFieldMap) element.getHardwareNode();

        sliceStartPosition = element.getStartPosition() - (fieldmap.getPosition() - fieldmap.getLength() / 2.0);

        magFieldmap = fieldmap.getFieldMap();
        sliceLength = fieldmap.getSliceLength();
    }

    /**
     * Method calculates transfer matrix for the field map on the current range
     * (i.e from probe.getPosition, and for dblLength).
     *
     * @return
     * @throws xal.model.ModelException
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen)
            throws ModelException {

        startPosition = getLatticePosition() - getLength() / 2. - sliceStartPosition;

        // Find the field map points included in the current slice.
        List<Double> fieldMapPointPositions = magFieldmap.getFieldMapPointPositions(probe.getPosition() - startPosition, dblLen);

        int numberOfPoints = fieldMapPointPositions.size();

        PhaseMatrix driftMatrix = PhaseMatrix.identity();
        PhaseMatrix transferMatrix = PhaseMatrix.identity();

        // Calculating the length of the first drift from the slice start to the
        // first point of the field map. It could be the end point and only a 
        // drift space is calculated.
        double dz = (numberOfPoints > 0 ? fieldMapPointPositions.get(0) - (probe.getPosition() - startPosition) : 0.0);

        // Add kicks and drifts for each intermediate point (could be none).
        for (int i = 0; i < numberOfPoints; i++) {
            driftMatrix.setElem(0, 1, dz);
            driftMatrix.setElem(2, 3, dz);
            driftMatrix.setElem(4, 5, dz);

            transferMatrix = driftMatrix.times(transferMatrix);

            // Set the length of the following drift spaces.
            dz = sliceLength;

            FieldMapPoint fieldMapPoint = magFieldmap.getFieldAt(fieldMapPointPositions.get(i));

            fieldMapPoint.setAmplitudeFactorB(getMagField());

            // Kick
            transferMatrix = FieldMapIntegrator.transferMap(probe, sliceLength, fieldMapPoint).times(transferMatrix);
        }

        // Last drift space (if any).
        dz = (numberOfPoints > 0 ? probe.getPosition() - startPosition + dblLen - fieldMapPointPositions.get(numberOfPoints - 1) : dblLen);

        driftMatrix.setElem(0, 1, dz);
        driftMatrix.setElem(2, 3, dz);
        driftMatrix.setElem(4, 5, dz);

        transferMatrix = driftMatrix.times(transferMatrix);

        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors        
        transferMatrix = applySliceErrors(transferMatrix, probe, sliceLength);
        
        return new PhaseMap(transferMatrix);
    }

    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return 0.;
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return super.compDriftingTime(probe, dblLen);
    }
}
