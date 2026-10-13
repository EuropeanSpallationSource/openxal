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
 * 1D FieldMap element for RF cavities, assuming a radial dependence similar to
 * pillbox cavity.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class RfFieldMap1D extends FieldMap {

    // Interpolation invariants resolved once at construction (constant for the
    // loaded map) to keep them out of the getFieldAt hot path.
    private final double[] fieldZ;
    private final double spacingZ;

    public RfFieldMap1D(String path, String filename, int numberOfPoints) {
        FieldComponent<double[]> fieldComponent = loadFile1D(path, filename + ".edz");

        // Normalizing the field map.
        double[] field = fieldComponent.getField();
        fieldIntegral = 0;
        for (int i = 0; i < field.length; i++) {
            fieldIntegral += Math.abs(field[i]);
        }
        fieldIntegral *= fieldComponent.getMax()[0] / field.length;
        for (int i = 0; i < field.length; i++) {
            field[i] /= fieldIntegral;
        }
        fieldComponent.setField(field);

        electricField.put("z", fieldComponent);

        // Compute other values.
        length = fieldComponent.getMax()[0];
        if (numberOfPoints == 0) {
            numberOfPoints = field.length;
        }
        this.numberOfPoints = numberOfPoints;

        this.fieldZ = field;
        this.spacingZ = length / (field.length - 1);

        recalculateSliceLength();

        setCoupled(false);
    }

    @Override
    public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException {
        FieldComponent<double[]> fieldComponentZ = (FieldComponent<double[]>) electricField.get("z");

        saveFile1D(path, filename + ".edz", fieldComponentZ);
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

        double[] field = fieldZ;

        // Interpolating the field at the given positon.
        int positionIndex = (int) Math.floor(position / spacingZ);

        if (positionIndex < 0) {
            positionIndex = 0;
        } else if (positionIndex >= field.length - 1) {
            positionIndex = field.length - 2;
        }

        double t = position / spacingZ - positionIndex;
        double ez0 = field[positionIndex] + t * (field[positionIndex + 1] - field[positionIndex]);
        double dEzds = nodeDerivative(positionIndex)
                + t * (nodeDerivative(positionIndex + 1) - nodeDerivative(positionIndex));

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        fieldMapPoint.setEz(ez0);
        fieldMapPoint.setdExdx(-0.5 * dEzds);
        fieldMapPoint.setdEydy(-0.5 * dEzds);
        fieldMapPoint.setdEzdz(dEzds);
        fieldMapPoint.setdBxdy(0.5 * ez0);
        fieldMapPoint.setdBydx(-0.5 * ez0);

        return fieldMapPoint;
    }

    /**
     * Derivative of the field at grid point i: central difference inside the
     * map, one-sided at its ends.
     */
    private double nodeDerivative(int i) {
        if (i == 0) {
            return (fieldZ[1] - fieldZ[0]) / spacingZ;
        }
        if (i == fieldZ.length - 1) {
            return (fieldZ[i] - fieldZ[i - 1]) / spacingZ;
        }
        return (fieldZ[i + 1] - fieldZ[i - 1]) / (2.0 * spacingZ);
    }
}
