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

    private final AxisInterpolator3D fieldX;
    private final AxisInterpolator3D fieldY;
    private final AxisInterpolator3D fieldZ;

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

        fieldX = new AxisInterpolator3D(fieldComponentX);
        fieldY = new AxisInterpolator3D(fieldComponentY);
        fieldZ = new AxisInterpolator3D(fieldComponentZ);

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
        if (isOutside(position)) {
            return null;
        }

        AxisSample bx = fieldX.interpolate(position);
        AxisSample by = fieldY.interpolate(position);
        AxisSample bz = fieldZ.interpolate(position);

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

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
