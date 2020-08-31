/*
 * Copyright (C) 2020 European Spallation Source ERIC.
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
 * 3D FieldMap element for RF cavities
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class RfFieldMap3D extends FieldMap {

    private double fieldIntegral;

    public RfFieldMap3D(String path, String filename, int numberOfPoints) {
        FieldComponent<double[][][]> electricFieldX = loadFile3D(path, filename + ".edx");
        FieldComponent<double[][][]> electricFieldY = loadFile3D(path, filename + ".edy");
        FieldComponent<double[][][]> electricFieldZ = loadFile3D(path, filename + ".edz");
        FieldComponent<double[][][]> magneticFieldX = loadFile3D(path, filename + ".bdx");
        FieldComponent<double[][][]> magneticFieldY = loadFile3D(path, filename + ".bdy");
        FieldComponent<double[][][]> magneticFieldZ = loadFile3D(path, filename + ".bdz");

        // Normalizing the field map.
        int numberOfPointsZ = electricFieldZ.getField().length;
        int numberOfPointsY = electricFieldZ.getField()[0].length;
        int numberOfPointsX = electricFieldZ.getField()[0][0].length;

        double minX = electricFieldX.getMin()[1];
        double minY = electricFieldY.getMin()[2];
        double maxX = electricFieldX.getMax()[1];
        double maxY = electricFieldY.getMax()[2];

        double spacingX = (maxX - minX) / (numberOfPointsX - 1);
        double spacingY = (maxY - minY) / (numberOfPointsY - 1);

        int midPointX = (int) (-minX / spacingX);
        int midPointY = (int) (-minY / spacingY);

        double[][][] fieldX = electricFieldX.getField();
        double[][][] fieldY = electricFieldY.getField();
        double[][][] fieldZ = electricFieldZ.getField();

        fieldIntegral = 0;
        for (int i = 0; i < numberOfPointsZ; i++) {
            fieldIntegral += Math.abs(fieldZ[i][midPointY][midPointX]);
        }

        fieldIntegral *= electricFieldZ.getMax()[0] / numberOfPointsZ;

        for (int i = 0; i < numberOfPointsZ; i++) {
            for (int j = 0; j < numberOfPointsY; j++) {
                for (int k = 0; k < numberOfPointsX; k++) {
                    fieldX[i][j][k] /= fieldIntegral;
                    fieldY[i][j][k] /= fieldIntegral;
                    fieldZ[i][j][k] /= fieldIntegral;
                }
            }
        }
//        electricFieldX.setField(fieldX);
//        electricFieldY.setField(fieldY);
//        electricFieldZ.setField(fieldZ);

        electricField.put("x", electricFieldX);
        electricField.put("y", electricFieldY);
        electricField.put("z", electricFieldZ);
        magneticField.put("x", magneticFieldX);
        magneticField.put("y", magneticFieldY);
        magneticField.put("z", magneticFieldZ);

        // Compute other values.
        length = electricFieldZ.getMax()[0];
        if (numberOfPoints == 0) {
            numberOfPoints = electricFieldZ.getField().length;
        }
        this.numberOfPoints = numberOfPoints;

        recalculateSliceLength();
    }

    public double getFieldIntegral() {
        return fieldIntegral;
    }

    @Override
    public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException {
        FieldComponent<double[][][]> electricFieldX = electricField.get("x");
        FieldComponent<double[][][]> electricFieldY = electricField.get("y");
        FieldComponent<double[][][]> electricFieldZ = electricField.get("z");

        FieldComponent<double[][][]> magneticFieldX = magneticField.get("x");
        FieldComponent<double[][][]> magneticFieldY = magneticField.get("y");
        FieldComponent<double[][][]> magneticFieldZ = magneticField.get("z");

        saveFile3D(path, filename + ".edx", electricFieldX);
        saveFile3D(path, filename + ".edy", electricFieldY);
        saveFile3D(path, filename + ".edz", electricFieldZ);

        saveFile3D(path, filename + ".bdx", magneticFieldX);
        saveFile3D(path, filename + ".bdy", magneticFieldY);
        saveFile3D(path, filename + ".bdz", magneticFieldZ);
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
        FieldComponent<double[][][]> electricFieldX = electricField.get("x");
        FieldComponent<double[][][]> electricFieldY = electricField.get("y");
        FieldComponent<double[][][]> electricFieldZ = electricField.get("z");

        FieldComponent<double[][][]> magneticFieldX = magneticField.get("x");
        FieldComponent<double[][][]> magneticFieldY = magneticField.get("y");
        FieldComponent<double[][][]> magneticFieldZ = magneticField.get("z");

        if (position < 0.0 || position > electricFieldZ.getMax()[0] || position > magneticFieldZ.getMax()[0]) {
            return null;
        }

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        interpolateField(position, electricFieldX, electricFieldY, electricFieldZ,
                fieldMapPoint, true);

        interpolateField(position, magneticFieldX, magneticFieldY, magneticFieldZ,
                fieldMapPoint, false);

        return fieldMapPoint;
    }

    private void interpolateField(double position,
            FieldComponent<double[][][]> fieldComponentX,
            FieldComponent<double[][][]> fieldComponentY,
            FieldComponent<double[][][]> fieldComponentZ,
            FieldMapPoint fieldMapPoint, boolean electricField) {

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

        double Fx0 = fieldX[positionIndex][midPointY][midPointX] + interpolation_factor
                * (fieldX[positionIndex + 1][midPointY][midPointX] - fieldX[positionIndex][midPointY][midPointX]);
        double Fy0 = fieldY[positionIndex][midPointY][midPointX] + interpolation_factor
                * (fieldY[positionIndex + 1][midPointY][midPointX] - fieldY[positionIndex][midPointY][midPointX]);
        double Fz0 = fieldZ[positionIndex][midPointY][midPointX] + interpolation_factor
                * (fieldZ[positionIndex + 1][midPointY][midPointX] - fieldZ[positionIndex][midPointY][midPointX]);

        double dFxdx = (fieldX[positionIndex][midPointY][midPointX + 1] - fieldX[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldX[positionIndex + 1][midPointY][midPointX + 1] - fieldX[positionIndex + 1][midPointY][midPointX] - (fieldX[positionIndex][midPointY][midPointX + 1] - fieldX[positionIndex][midPointY][midPointX]));
        double dFxdy = (fieldX[positionIndex][midPointY + 1][midPointX] - fieldX[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldX[positionIndex + 1][midPointY + 1][midPointX] - fieldX[positionIndex + 1][midPointY][midPointX] - (fieldX[positionIndex][midPointY + 1][midPointX] - fieldX[positionIndex][midPointY][midPointX]));
        double dFydx = (fieldY[positionIndex][midPointY][midPointX + 1] - fieldY[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldY[positionIndex + 1][midPointY][midPointX + 1] - fieldY[positionIndex + 1][midPointY][midPointX] - (fieldY[positionIndex][midPointY][midPointX + 1] - fieldY[positionIndex][midPointY][midPointX]));
        double dFydy = (fieldY[positionIndex][midPointY + 1][midPointX] - fieldY[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldY[positionIndex + 1][midPointY + 1][midPointX] - fieldY[positionIndex + 1][midPointY][midPointX] - (fieldY[positionIndex][midPointY + 1][midPointX] - fieldY[positionIndex][midPointY][midPointX]));
        double dFzdx = (fieldZ[positionIndex][midPointY][midPointX + 1] - fieldZ[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldZ[positionIndex + 1][midPointY][midPointX + 1] - fieldZ[positionIndex + 1][midPointY][midPointX] - (fieldZ[positionIndex][midPointY][midPointX + 1] - fieldZ[positionIndex][midPointY][midPointX]));
        double dFzdy = (fieldZ[positionIndex][midPointY + 1][midPointX] - fieldZ[positionIndex][midPointY][midPointX]) + interpolation_factor
                * (fieldZ[positionIndex + 1][midPointY + 1][midPointX] - fieldZ[positionIndex + 1][midPointY][midPointX] - (fieldZ[positionIndex][midPointY + 1][midPointX] - fieldZ[positionIndex][midPointY][midPointX]));

        double dFxdz;
        double dFydz;
        double dFzdz;

        if (positionIndex == 0) {
            dFxdz = (fieldX[positionIndex + 1][midPointY][midPointX] - fieldX[positionIndex][midPointY][midPointX]);
            dFydz = (fieldY[positionIndex + 1][midPointY][midPointX] - fieldY[positionIndex][midPointY][midPointX]);
            dFzdz = (fieldZ[positionIndex + 1][midPointY][midPointX] - fieldZ[positionIndex][midPointY][midPointX]);
        } else if (positionIndex == fieldZ.length - 1) {
            dFxdz = (fieldX[positionIndex][midPointY][midPointX] - fieldX[positionIndex - 1][midPointY][midPointX]);
            dFydz = (fieldY[positionIndex][midPointY][midPointX] - fieldY[positionIndex - 1][midPointY][midPointX]);
            dFzdz = (fieldZ[positionIndex][midPointY][midPointX] - fieldZ[positionIndex - 1][midPointY][midPointX]);
        } else {
            dFxdz = (fieldX[positionIndex + 1][midPointY][midPointX] - fieldX[positionIndex][midPointY][midPointX]);
            dFydz = (fieldY[positionIndex + 1][midPointY][midPointX] - fieldY[positionIndex][midPointY][midPointX]);
            dFzdz = (fieldZ[positionIndex + 1][midPointY][midPointX] - fieldZ[positionIndex][midPointY][midPointX]);
        }

        // Denormalising
        Fx0 /= normX;
        Fy0 /= normY;
        Fz0 /= normZ;
        dFxdx /= spacingX * normX;
        dFxdy /= spacingY * normX;
        dFydx /= spacingX * normY;
        dFydy /= spacingY * normY;
        dFxdz /= spacingZ * normX;
        dFydz /= spacingZ * normY;
        dFzdx /= spacingX * normZ;
        dFzdy /= spacingY * normZ;
        dFzdz /= spacingZ * normZ;

        
        if (electricField) {
            fieldMapPoint.setEx(Fx0);
            fieldMapPoint.setEy(Fy0);
            fieldMapPoint.setEz(Fz0);

            fieldMapPoint.setdExdx(dFxdx);
            fieldMapPoint.setdExdy(dFxdy);
            fieldMapPoint.setdExdz(dFxdz);

            fieldMapPoint.setdEydx(dFydx);
            fieldMapPoint.setdEydy(dFydy);
            fieldMapPoint.setdEydz(dFydz);

            fieldMapPoint.setdEzdx(dFzdx);
            fieldMapPoint.setdEzdy(dFzdy);
            fieldMapPoint.setdEzdz(dFzdz);
        } else {
            fieldMapPoint.setBx(Fx0);
            fieldMapPoint.setBy(Fy0);
            fieldMapPoint.setBz(Fz0);

            fieldMapPoint.setdBxdx(dFxdx);
            fieldMapPoint.setdBxdy(dFxdy);
            fieldMapPoint.setdBxdz(dFxdz);

            fieldMapPoint.setdBydx(dFydx);
            fieldMapPoint.setdBydy(dFydy);
            fieldMapPoint.setdBydz(dFydz);
        }
    }
}
