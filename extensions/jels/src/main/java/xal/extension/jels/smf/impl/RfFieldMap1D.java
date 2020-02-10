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
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class RfFieldMap1D extends FieldMap {

    private double fieldIntegral;

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

        fieldComponents.put("z", fieldComponent);

        // Compute other values.
        length = fieldComponent.getMax()[0];
        if (numberOfPoints == 0) {
            numberOfPoints = field.length;
        }
        this.numberOfPoints = numberOfPoints;

        recalculateSliceLength();

        setCoupled(false);
    }

    public double getFieldIntegral() {
        return fieldIntegral;
    }

    @Override
    public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException {
        FieldComponent<double[]> fieldComponentZ = fieldComponents.get("z");

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
        FieldComponent<double[]> fieldComponent = fieldComponents.get("z");

        if (position < -1e-6 || position > fieldComponent.getMax()[0] + 1e-6) {
            return null;
        }

        double[] field = fieldComponent.getField();

        int numberOfPointsZ = field.length;
        double spacingZ = length / (numberOfPointsZ - 1);

        // Interpolating the field at the given positon.
        int positionIndex = (int) Math.floor(position / spacingZ);

        if (positionIndex < 0) {
            positionIndex = 0;
        } else if (positionIndex >= numberOfPointsZ - 1) {
            positionIndex = numberOfPointsZ - 2;
        }

        double Ez0 = field[positionIndex] + (position - positionIndex * spacingZ)
                * (field[positionIndex + 1] - field[positionIndex]) / spacingZ;
        double dEzds;
        positionIndex = (int) Math.round(position / spacingZ);
        if (positionIndex == 0) {
            dEzds = field[positionIndex + 1] / spacingZ;
        } else if (positionIndex == field.length - 1) {
            dEzds = field[positionIndex - 1] / spacingZ;
        } else {
            dEzds = (field[positionIndex + 1] - field[positionIndex - 1]) / (2.0 * spacingZ);
        }

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        fieldMapPoint.setEz(Ez0);
        fieldMapPoint.setdExdx(-0.5 * dEzds);
        fieldMapPoint.setdEydy(-0.5 * dEzds);
        fieldMapPoint.setdEzdz(dEzds);
        fieldMapPoint.setdBxdy(0.5 * Ez0);
        fieldMapPoint.setdBydx(-0.5 * Ez0);
        fieldMapPoint.setEz(Ez0);

        return fieldMapPoint;
    }
}
