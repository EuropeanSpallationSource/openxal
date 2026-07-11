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
package xal.extension.jels.smf.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import xal.extension.jels.model.elem.FieldMapPoint;

/**
 * 2D FieldMap element for static magnets.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class MagFieldMap2D extends FieldMap {

    // Interpolation invariants resolved once at construction (constant for the
    // loaded map) to keep them out of the getFieldAt hot path.
    private final double[][] fieldZ;
    private final double[][] fieldR;
    private final double normZ;
    private final double normR;
    private final double spacingZ;
    private final double spacingR;

    public MagFieldMap2D(String path, String filename, int numberOfPoints) {
        FieldComponent<double[][]> fieldComponentZ = loadFile2D(path, filename + ".bsz");
        FieldComponent<double[][]> fieldComponentR = loadFile2D(path, filename + ".bsr");

        magneticField.put("z", fieldComponentZ);
        magneticField.put("r", fieldComponentR);

        // Computing other values used for integration.
        length = fieldComponentZ.getMax()[0];
        if (numberOfPoints == 0) {
            numberOfPoints = fieldComponentZ.getField().length;
        }
        this.numberOfPoints = numberOfPoints;

        fieldZ = fieldComponentZ.getField();
        fieldR = fieldComponentR.getField();
        normZ = fieldComponentZ.getNorm();
        normR = fieldComponentR.getNorm();
        spacingZ = fieldComponentZ.getMax()[0] / (fieldZ.length - 1);
        spacingR = fieldComponentR.getMax()[1] / (fieldR[0].length - 1);

        recalculateSliceLength();
    }

    @Override
    public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException {
        FieldComponent<double[][]> fieldComponentZ = (FieldComponent<double[][]>) magneticField.get("z");
        FieldComponent<double[][]> fieldComponentR = (FieldComponent<double[][]>) magneticField.get("r");

        saveFile2D(path, filename + ".bsz", fieldComponentZ);
        saveFile2D(path, filename + ".bsr", fieldComponentR);
    }

    /**
     * This method returns the amplitude of the field at the given point. Linear
     * interpolation is used.
     *
     * @param position
     * @return
     */
    @Override
    public FieldMapPoint getFieldAt(double position) {
        if (position < -1e-6 || position > length + 1e-6) {
            return null;
        }

        // Interpolating the field at the given positon.
        int positionIndex = (int) Math.floor(position / spacingZ);

        if (positionIndex < 0) {
            positionIndex = 0;
        } else if (positionIndex >= fieldZ.length - 1) {
            positionIndex = fieldZ.length - 2;
        }

        double interpolationFactor = position / spacingZ - positionIndex;

        double bz0 = fieldZ[positionIndex][0] + interpolationFactor
                * (fieldZ[positionIndex + 1][0] - fieldZ[positionIndex][0]);

        // First derivative - 1th order accuracy (forward)
        double dBrdr = -(fieldR[positionIndex][0] + interpolationFactor * (fieldR[positionIndex + 1][0] - fieldR[positionIndex][0]))
                + (fieldR[positionIndex][1] + interpolationFactor * (fieldR[positionIndex + 1][1] - fieldR[positionIndex][1]));

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        fieldMapPoint.setBz(bz0 / normZ);
        fieldMapPoint.setdBxdx(dBrdr / spacingR / normR);
        fieldMapPoint.setdBydy(dBrdr / spacingR / normR);

        return fieldMapPoint;
    }
}
