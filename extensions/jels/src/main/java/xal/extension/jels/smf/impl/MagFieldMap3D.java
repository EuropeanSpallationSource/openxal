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
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class MagFieldMap3D extends FieldMap {

    public MagFieldMap3D(String path, String filename, int numberOfPoints) {
        FieldComponent<double[][][]> fieldComponentX = loadFile3D(path, filename + ".bsx");
        FieldComponent<double[][][]> fieldComponentY = loadFile3D(path, filename + ".bsy");
        FieldComponent<double[][][]> fieldComponentZ = loadFile3D(path, filename + ".bsz");

        fieldComponents.put("x", fieldComponentX);
        fieldComponents.put("y", fieldComponentY);
        fieldComponents.put("z", fieldComponentZ);

        // Compute other values.
        length = fieldComponentZ.getMax()[0];
        if (numberOfPoints == 0) {
            numberOfPoints = fieldComponentZ.getField().length;
        }
        this.numberOfPoints = numberOfPoints;

        recalculateSliceLength();
    }

    @Override
    public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException {
        FieldComponent<double[][][]> fieldComponentX = fieldComponents.get("x");
        FieldComponent<double[][][]> fieldComponentY = fieldComponents.get("y");
        FieldComponent<double[][][]> fieldComponentZ = fieldComponents.get("z");

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
        FieldComponent<double[][][]> fieldComponentX = fieldComponents.get("x");
        FieldComponent<double[][][]> fieldComponentY = fieldComponents.get("y");
        FieldComponent<double[][][]> fieldComponentZ = fieldComponents.get("z");

        if (position < 0.0 || position > fieldComponentZ.getMax()[0]) {
            return null;
        }

        double[][][] fieldX = fieldComponentX.getField();
        double[][][] fieldY = fieldComponentY.getField();
        double[][][] fieldZ = fieldComponentZ.getField();

        int numberOfPointsZ = fieldZ.length;
        int numberOfPointsY = fieldY[0].length;
        int numberOfPointsX = fieldX[0][0].length;

        double normX = fieldComponentX.getNorm();
        double normY = fieldComponentY.getNorm();
        double normZ = fieldComponentZ.getNorm();

        double lengthZ = fieldComponentZ.getMax()[0];
        double minX = fieldComponentX.getMin()[1];
        double minY = fieldComponentY.getMin()[2];
        double maxX = fieldComponentX.getMax()[1];
        double maxY = fieldComponentY.getMax()[2];

        double spacingX = (maxX - minX) / (numberOfPointsX - 1);
        double spacingY = (maxY - minY) / (numberOfPointsY - 1);
        double spacingZ = lengthZ / (numberOfPointsZ - 1);

        // Interpolating the field at the given positon.
        int positionIndex = (int) Math.floor(position / spacingZ);

        if (positionIndex < 0) {
            positionIndex = 0;
        } else if (positionIndex >= numberOfPointsZ - 1) {
            positionIndex = numberOfPointsZ - 2;
        }

        double interpolation_factor = position / spacingZ - positionIndex;

        // To get the (0,0) point in the XY plane.
        int midPointX = (int) (-minX / spacingX);
        int midPointY = (int) (-minY / spacingY);

        double Bx0 = fieldX[positionIndex][midPointY][midPointX] + interpolation_factor
                * (fieldX[positionIndex + 1][midPointY][midPointX] - fieldX[positionIndex][midPointY][midPointX]);
        double By0 = fieldY[positionIndex][midPointY][midPointX] + interpolation_factor
                * (fieldY[positionIndex + 1][midPointY][midPointX] - fieldY[positionIndex][midPointY][midPointX]);
        double Bz0 = fieldZ[positionIndex][midPointY][midPointX] + interpolation_factor
                * (fieldZ[positionIndex + 1][midPointY][midPointX] - fieldZ[positionIndex][midPointY][midPointX]);

        double dBxdx = (fieldX[positionIndex][midPointY][midPointX + 1] - fieldX[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldX[positionIndex + 1][midPointY][midPointX + 1] - fieldX[positionIndex + 1][midPointY][midPointX] - (fieldX[positionIndex][midPointY][midPointX + 1] - fieldX[positionIndex][midPointY][midPointX]));
        double dBxdy = (fieldX[positionIndex][midPointY + 1][midPointX] - fieldX[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldX[positionIndex + 1][midPointY + 1][midPointX] - fieldX[positionIndex + 1][midPointY][midPointX] - (fieldX[positionIndex][midPointY + 1][midPointX] - fieldX[positionIndex][midPointY][midPointX]));
        double dBydx = (fieldY[positionIndex][midPointY][midPointX + 1] - fieldY[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldY[positionIndex + 1][midPointY][midPointX + 1] - fieldY[positionIndex + 1][midPointY][midPointX] - (fieldY[positionIndex][midPointY][midPointX + 1] - fieldY[positionIndex][midPointY][midPointX]));
        double dBydy = (fieldY[positionIndex][midPointY + 1][midPointX] - fieldY[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldY[positionIndex + 1][midPointY + 1][midPointX] - fieldY[positionIndex + 1][midPointY][midPointX] - (fieldY[positionIndex][midPointY + 1][midPointX] - fieldY[positionIndex][midPointY][midPointX]));

        double dBxdz;
        double dBydz;

        if (positionIndex == 0) {
            dBxdz = (fieldX[positionIndex + 1][midPointY][midPointX] - fieldX[positionIndex][midPointY][midPointX]);
            dBydz = (fieldY[positionIndex + 1][midPointY][midPointX] - fieldY[positionIndex][midPointY][midPointX]);
        } else if (positionIndex == fieldZ.length - 1) {
            dBxdz = (fieldX[positionIndex][midPointY][midPointX] - fieldX[positionIndex - 1][midPointY][midPointX]);
            dBydz = (fieldY[positionIndex][midPointY][midPointX] - fieldY[positionIndex - 1][midPointY][midPointX]);
        } else {
            dBxdz = (fieldX[positionIndex + 1][midPointY][midPointX] - fieldX[positionIndex][midPointY][midPointX]);
            dBydz = (fieldY[positionIndex + 1][midPointY][midPointX] - fieldY[positionIndex][midPointY][midPointX]);
        }

        // Denormalising
        Bx0 /= normX;
        By0 /= normY;
        Bz0 /= normZ;
        dBxdx /= spacingX * normX;
        dBxdy /= spacingY * normX;
        dBydx /= spacingX * normY;
        dBydy /= spacingY * normY;
        dBxdz /= spacingZ * normX;
        dBydz /= spacingZ * normY;

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        fieldMapPoint.setBx(Bx0);
        fieldMapPoint.setBy(By0);
        fieldMapPoint.setBz(Bz0);

        fieldMapPoint.setdBxdx(dBxdx);
        fieldMapPoint.setdBxdy(dBxdy);
        fieldMapPoint.setdBxdz(dBxdz);

        fieldMapPoint.setdBydx(dBydx);
        fieldMapPoint.setdBydy(dBydy);
        fieldMapPoint.setdBydz(dBydz);

        return fieldMapPoint;
    }
}
