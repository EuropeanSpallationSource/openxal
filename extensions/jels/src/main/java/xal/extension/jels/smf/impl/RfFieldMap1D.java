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

import xal.extension.jels.model.elem.FieldMapPoint;

/**
 * 1D FieldMap element for RF cavities, assuming a radial dependence similar to
 * pillbox cavity.
 *
 * @author juanfestebanmuller
 */
public class RfFieldMap1D extends FieldMap {

    public RfFieldMap1D(String path, String filename) {
        FieldComponent<double[]> fieldComponent = loadFile1D(path, filename+".edz");

        // Normalizing the field map.
        double[] field = fieldComponent.getField();
        double fieldIntegral = 0;
        for (int i = 0; i < field.length; i++) {
            fieldIntegral += Math.abs(field[i]);
        }
        fieldIntegral *= fieldComponent.getMax() / field.length;
        for (int i = 0; i < field.length; i++) {
            field[i] /= fieldIntegral;
        }
        fieldComponent.setField(field);

        fieldComponents.put("z", fieldComponent);
    }

    /**
     * This method returns the amplitude of the field at the closest point. It
     * does not perform any interpolation!
     *
     * @param position
     * @return
     */
    @Override
    public FieldMapPoint getFieldAt(double position) {
        FieldComponent<double[]> fieldComponent = fieldComponents.get("z");

        if (position < 0.0 || position > fieldComponent.getMax()) {
            return null;
        }

        double[] field = fieldComponent.getField();

        int numberOfPoints = field.length;
        int positionIndex = (int) (position / fieldComponent.getMax() * (numberOfPoints - 1));

        double Ez0 = field[positionIndex];
        double ds = fieldComponent.getMax() / (numberOfPoints - 1);
        double dEz0ds;
        if (positionIndex == 0) {
            dEz0ds = field[positionIndex + 1] / ds;
        } else if (positionIndex == field.length - 1) {
            dEz0ds = field[positionIndex - 1] / ds;
        } else {
            dEz0ds = (field[positionIndex + 1] - field[positionIndex - 1]) / (2 * ds);
        }

        FieldMapPoint fieldMapPoint = new FieldMapPoint();

        fieldMapPoint.setEz(Ez0);
        fieldMapPoint.setdExdx(-0.5 * dEz0ds);
        fieldMapPoint.setdEydy(-0.5 * dEz0ds);
        fieldMapPoint.setdEzdz(dEz0ds);
        fieldMapPoint.setdBxdy(0.5 * Ez0);
        fieldMapPoint.setdBydx(-0.5 * Ez0);
        fieldMapPoint.setEz(Ez0);

        return fieldMapPoint;
    }

    @Override
    public double[] getLongitudinalPositions() {
        FieldComponent<double[]> fieldComponent = fieldComponents.get("z");

        double[] field = fieldComponent.getField();

        int numberOfPoints = field.length;
        double dz = fieldComponent.getMax() / (numberOfPoints - 1);

        double[] longitudinalPositions = new double[numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            longitudinalPositions[i] = i * dz;
        }
        return longitudinalPositions;
    }

    @Override
    public double getLength() {
        FieldComponent<double[]> fieldComponent = fieldComponents.get("z");
        
        return fieldComponent.getMax();
    }

    @Override
    public double getSliceLength() {
        FieldComponent<double[]> fieldComponent = fieldComponents.get("z");
        double[] field = fieldComponent.getField();
        
        int numberOfPoints = field.length;
        
        return fieldComponent.getMax() / (numberOfPoints - 1);
    }

}
