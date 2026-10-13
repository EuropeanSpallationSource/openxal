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

    private final AxisInterpolator3D eX;
    private final AxisInterpolator3D eY;
    private final AxisInterpolator3D eZ;
    private final AxisInterpolator3D bX;
    private final AxisInterpolator3D bY;
    private final AxisInterpolator3D bZ;

    public RfFieldMap3D(String path, String filename, int numberOfPoints) {
        FieldComponent<double[][][]> electricFieldX = loadFile3D(path, filename + ".edx");
        FieldComponent<double[][][]> electricFieldY = loadFile3D(path, filename + ".edy");
        FieldComponent<double[][][]> electricFieldZ = loadFile3D(path, filename + ".edz");
        FieldComponent<double[][][]> magneticFieldX = loadFile3D(path, filename + ".bdx");
        FieldComponent<double[][][]> magneticFieldY = loadFile3D(path, filename + ".bdy");
        FieldComponent<double[][][]> magneticFieldZ = loadFile3D(path, filename + ".bdz");

        int numberOfPointsZ = electricFieldZ.getField().length;
        int numberOfPointsY = electricFieldZ.getField()[0].length;
        int numberOfPointsX = electricFieldZ.getField()[0][0].length;

        eX = new AxisInterpolator3D(electricFieldX);
        eY = new AxisInterpolator3D(electricFieldY);
        eZ = new AxisInterpolator3D(electricFieldZ);
        bX = new AxisInterpolator3D(magneticFieldX);
        bY = new AxisInterpolator3D(magneticFieldY);
        bZ = new AxisInterpolator3D(magneticFieldZ);

        double[][][] eFieldX = electricFieldX.getField();
        double[][][] eFieldY = electricFieldY.getField();
        double[][][] eFieldZ = electricFieldZ.getField();
        double[][][] bFieldX = magneticFieldX.getField();
        double[][][] bFieldY = magneticFieldY.getField();
        double[][][] bFieldZ = magneticFieldZ.getField();

        // Normalizing the field map.
        fieldIntegral = 0;
        for (int i = 0; i < numberOfPointsZ; i++) {
            fieldIntegral += Math.abs(eZ.rawValueOnAxis(i));
        }

        fieldIntegral *= electricFieldZ.getMax()[0] / numberOfPointsZ;

        for (int i = 0; i < numberOfPointsZ; i++) {
            for (int j = 0; j < numberOfPointsY; j++) {
                for (int k = 0; k < numberOfPointsX; k++) {
                    eFieldX[i][j][k] /= fieldIntegral;
                    eFieldY[i][j][k] /= fieldIntegral;
                    eFieldZ[i][j][k] /= fieldIntegral;
                    bFieldX[i][j][k] /= fieldIntegral;
                    bFieldY[i][j][k] /= fieldIntegral;
                    bFieldZ[i][j][k] /= fieldIntegral;
                }
            }
        }

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

    @Override
    public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException {
        FieldComponent<double[][][]> electricFieldX = (FieldComponent<double[][][]>) electricField.get("x");
        FieldComponent<double[][][]> electricFieldY = (FieldComponent<double[][][]>) electricField.get("y");
        FieldComponent<double[][][]> electricFieldZ = (FieldComponent<double[][][]>) electricField.get("z");

        FieldComponent<double[][][]> magneticFieldX = (FieldComponent<double[][][]>) magneticField.get("x");
        FieldComponent<double[][][]> magneticFieldY = (FieldComponent<double[][][]>) magneticField.get("y");
        FieldComponent<double[][][]> magneticFieldZ = (FieldComponent<double[][][]>) magneticField.get("z");

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
        if (isOutside(position)) {
            return null;
        }

        AxisSample ex = eX.interpolate(position);
        AxisSample ey = eY.interpolate(position);
        AxisSample ez = eZ.interpolate(position);
        AxisSample bx = bX.interpolate(position);
        AxisSample by = bY.interpolate(position);
        AxisSample bz = bZ.interpolate(position);

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        fieldMapPoint.setEx(ex.value());
        fieldMapPoint.setEy(ey.value());
        fieldMapPoint.setEz(ez.value());

        fieldMapPoint.setdExdx(ex.ddx());
        fieldMapPoint.setdExdy(ex.ddy());
        fieldMapPoint.setdExdz(ex.ddz());

        fieldMapPoint.setdEydx(ey.ddx());
        fieldMapPoint.setdEydy(ey.ddy());
        fieldMapPoint.setdEydz(ey.ddz());

        fieldMapPoint.setdEzdx(ez.ddx());
        fieldMapPoint.setdEzdy(ez.ddy());
        fieldMapPoint.setdEzdz(ez.ddz());

        fieldMapPoint.setBx(bx.value());
        fieldMapPoint.setBy(by.value());
        fieldMapPoint.setBz(bz.value());

        fieldMapPoint.setdBxdx(bx.ddx());
        fieldMapPoint.setdBxdy(bx.ddy());
        fieldMapPoint.setdBxdz(bx.ddz());

        fieldMapPoint.setdBydx(by.ddx());
        fieldMapPoint.setdBydy(by.ddy());
        fieldMapPoint.setdBydz(by.ddz());

        return fieldMapPoint;
    }
}
