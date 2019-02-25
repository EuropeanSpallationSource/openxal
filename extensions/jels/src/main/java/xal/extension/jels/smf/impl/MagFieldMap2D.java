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
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class MagFieldMap2D extends FieldMap {

    public MagFieldMap2D(String path, String filename, int numberOfPoints) {
        FieldComponent<double[][]> fieldComponentZ = loadFile2D(path, filename + ".bsz");
        FieldComponent<double[][]> fieldComponentR = loadFile2D(path, filename + ".bsr");

        fieldComponents.put("z", fieldComponentZ);
        fieldComponents.put("r", fieldComponentR);

        // Computing other values used for integration.
        length = fieldComponentZ.getMax()[0];
        if (numberOfPoints == 0) {
            numberOfPoints = fieldComponentZ.getField().length;
        }
        this.numberOfPoints = numberOfPoints;

        sliceLength = length / (numberOfPoints - 1);

        longitudinalPositions = new double[numberOfPoints];
        for (int i = 0; i < numberOfPoints - 1; i++) {
            longitudinalPositions[i] = i * length / (numberOfPoints - 1);
        }
        longitudinalPositions[numberOfPoints - 1] = length;
    }

    @Override
    public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException {
        FieldComponent<double[][]> fieldComponentZ = fieldComponents.get("z");
        FieldComponent<double[][]> fieldComponentR = fieldComponents.get("r");

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
        FieldComponent<double[][]> fieldComponentZ = fieldComponents.get("z");
        FieldComponent<double[][]> fieldComponentR = fieldComponents.get("r");

        if (position < -1e-6 || position > fieldComponentZ.getMax()[0] + 1e-6) {
            return null;
        }

        double[][] fieldZ = fieldComponentZ.getField();
        double[][] fieldR = fieldComponentR.getField();

        double normZ = fieldComponentZ.getNorm();
        double normR = fieldComponentR.getNorm();

        int numberOfPointsZ = fieldZ.length;
        int numberOfPointsR = fieldR[0].length;

        double lengthZ = fieldComponentZ.getMax()[0];
        double lengthR = fieldComponentR.getMax()[1];

        double spacingZ = lengthZ / (numberOfPointsZ - 1);
        double spacingR = lengthR / (numberOfPointsR - 1);

        // Interpolating the field at the given positon.
        int positionIndex = (int) Math.floor(position / spacingZ);

        if (positionIndex < 0) {
            positionIndex = 0;
        } else if (positionIndex >= numberOfPointsZ - 1) {
            positionIndex = numberOfPointsZ - 2;
        }

        double Bz0 = fieldZ[positionIndex][0] + (position - positionIndex * spacingZ)
                * (fieldZ[positionIndex + 1][0] - fieldZ[positionIndex][0]) / spacingZ;
        double dBrdr = fieldR[positionIndex][1] - fieldR[positionIndex][0] + (position - positionIndex * spacingZ)
                * (fieldR[positionIndex + 1][1] - fieldR[positionIndex + 1][0] - (fieldR[positionIndex][1] - fieldR[positionIndex][0])) / spacingZ;
       
        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        fieldMapPoint.setBz(Bz0 / normZ);
        fieldMapPoint.setdBxdx(dBrdr / spacingR / normR);
        fieldMapPoint.setdBydy(dBrdr / spacingR / normR);

        return fieldMapPoint;
    }
}
