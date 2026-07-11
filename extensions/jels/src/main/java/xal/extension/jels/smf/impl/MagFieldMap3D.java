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
 * 3D FieldMap element for static magnets.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class MagFieldMap3D extends FieldMap {

    // Interpolation invariants resolved once at construction (constant for the
    // loaded map) to keep them out of the getFieldAt hot path.
    private final double[][][] fieldX;
    private final double[][][] fieldY;
    private final double[][][] fieldZ;
    private final double normX;
    private final double normY;
    private final double normZ;
    private final double spacingX;
    private final double spacingY;
    private final double spacingZ;
    private final int midPointX;
    private final int midPointY;

    public MagFieldMap3D(String path, String filename, int numberOfPoints) {
        FieldComponent<double[][][]> fieldComponentX = loadFile3D(path, filename + ".bsx");
        FieldComponent<double[][][]> fieldComponentY = loadFile3D(path, filename + ".bsy");
        FieldComponent<double[][][]> fieldComponentZ = loadFile3D(path, filename + ".bsz");

        magneticField.put("x", fieldComponentX);
        magneticField.put("y", fieldComponentY);
        magneticField.put("z", fieldComponentZ);

        // Compute other values.
        length = fieldComponentZ.getMax()[0];
        if (numberOfPoints == 0) {
            numberOfPoints = fieldComponentZ.getField().length;
        }
        this.numberOfPoints = numberOfPoints;

        fieldX = fieldComponentX.getField();
        fieldY = fieldComponentY.getField();
        fieldZ = fieldComponentZ.getField();
        normX = fieldComponentX.getNorm();
        normY = fieldComponentY.getNorm();
        normZ = fieldComponentZ.getNorm();

        double minX = fieldComponentX.getMin()[1];
        double minY = fieldComponentY.getMin()[2];
        spacingX = (fieldComponentX.getMax()[1] - minX) / (fieldX[0][0].length - 1);
        spacingY = (fieldComponentY.getMax()[2] - minY) / (fieldY[0].length - 1);
        spacingZ = fieldComponentZ.getMax()[0] / (fieldZ.length - 1);
        midPointX = (int) (-minX / spacingX);
        midPointY = (int) (-minY / spacingY);

        recalculateSliceLength();
    }

    @Override
    public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException {
        FieldComponent<double[][][]> fieldComponentX = (FieldComponent<double[][][]>) magneticField.get("x");
        FieldComponent<double[][][]> fieldComponentY = (FieldComponent<double[][][]>) magneticField.get("y");
        FieldComponent<double[][][]> fieldComponentZ = (FieldComponent<double[][][]>) magneticField.get("z");

        saveFile3D(path, filename + ".bsx", fieldComponentX);
        saveFile3D(path, filename + ".bsy", fieldComponentY);
        saveFile3D(path, filename + ".bsz", fieldComponentZ);
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
        if (position < 0.0 || position > length) {
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

        double bx0 = fieldX[positionIndex][midPointY][midPointX] + interpolationFactor
                * (fieldX[positionIndex + 1][midPointY][midPointX] - fieldX[positionIndex][midPointY][midPointX]);
        double by0 = fieldY[positionIndex][midPointY][midPointX] + interpolationFactor
                * (fieldY[positionIndex + 1][midPointY][midPointX] - fieldY[positionIndex][midPointY][midPointX]);
        double bz0 = fieldZ[positionIndex][midPointY][midPointX] + interpolationFactor
                * (fieldZ[positionIndex + 1][midPointY][midPointX] - fieldZ[positionIndex][midPointY][midPointX]);

        double dBxdx = (fieldX[positionIndex][midPointY][midPointX + 1] - fieldX[positionIndex][midPointY][midPointX]) + interpolationFactor
                * (fieldX[positionIndex + 1][midPointY][midPointX + 1] - fieldX[positionIndex + 1][midPointY][midPointX] - (fieldX[positionIndex][midPointY][midPointX + 1] - fieldX[positionIndex][midPointY][midPointX]));
        double dBxdy = (fieldX[positionIndex][midPointY + 1][midPointX] - fieldX[positionIndex][midPointY][midPointX]) + interpolationFactor
                * (fieldX[positionIndex + 1][midPointY + 1][midPointX] - fieldX[positionIndex + 1][midPointY][midPointX] - (fieldX[positionIndex][midPointY + 1][midPointX] - fieldX[positionIndex][midPointY][midPointX]));
        double dBydx = (fieldY[positionIndex][midPointY][midPointX + 1] - fieldY[positionIndex][midPointY][midPointX]) + interpolationFactor
                * (fieldY[positionIndex + 1][midPointY][midPointX + 1] - fieldY[positionIndex + 1][midPointY][midPointX] - (fieldY[positionIndex][midPointY][midPointX + 1] - fieldY[positionIndex][midPointY][midPointX]));
        double dBydy = (fieldY[positionIndex][midPointY + 1][midPointX] - fieldY[positionIndex][midPointY][midPointX]) + interpolationFactor
                * (fieldY[positionIndex + 1][midPointY + 1][midPointX] - fieldY[positionIndex + 1][midPointY][midPointX] - (fieldY[positionIndex][midPointY + 1][midPointX] - fieldY[positionIndex][midPointY][midPointX]));

        double dBxdz;
        double dBydz;

        if (positionIndex == fieldZ.length - 1) {
            dBxdz = (fieldX[positionIndex][midPointY][midPointX] - fieldX[positionIndex - 1][midPointY][midPointX]);
            dBydz = (fieldY[positionIndex][midPointY][midPointX] - fieldY[positionIndex - 1][midPointY][midPointX]);
        } else {
            dBxdz = (fieldX[positionIndex + 1][midPointY][midPointX] - fieldX[positionIndex][midPointY][midPointX]);
            dBydz = (fieldY[positionIndex + 1][midPointY][midPointX] - fieldY[positionIndex][midPointY][midPointX]);
        }

        // Denormalising
        bx0 /= normX;
        by0 /= normY;
        bz0 /= normZ;
        dBxdx /= spacingX * normX;
        dBxdy /= spacingY * normX;
        dBydx /= spacingX * normY;
        dBydy /= spacingY * normY;
        dBxdz /= spacingZ * normX;
        dBydz /= spacingZ * normY;

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        fieldMapPoint.setBx(bx0);
        fieldMapPoint.setBy(by0);
        fieldMapPoint.setBz(bz0);

        fieldMapPoint.setdBxdx(dBxdx);
        fieldMapPoint.setdBxdy(dBxdy);
        fieldMapPoint.setdBxdz(dBxdz);

        fieldMapPoint.setdBydx(dBydx);
        fieldMapPoint.setdBydy(dBydy);
        fieldMapPoint.setdBydz(dBydz);

        return fieldMapPoint;
    }
}
