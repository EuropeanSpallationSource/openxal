/*
 * Copyright (C) 2020 European Spallation Source ERIC
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
package xal.smf.attr;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class DipoleCorrBucket extends DipoleBucket {

    // number of slices in which thin correctors are split
    private Attribute attSlices;
    // effective length of each slice
    private Attribute attSlicesEffLength;

    public DipoleCorrBucket() {
        super();

        attSlices = new Attribute(1);
        attSlicesEffLength = new Attribute(new double[]{1.0});

        super.registerAttribute(ARR_NAMES[0], attSlices, "Number of slices in which thin correctors are split.");
        super.registerAttribute(ARR_NAMES[1], attSlicesEffLength, "Effective length of each slice (m).");
    }

    private static final String TYPE = "steerer";

    private static final String[] ARR_NAMES = {
        // number of slices in which thin correctors are split
        "slices",
        // effecttive length of each slice
        "slicesEffLength"
    };

    /**
     * Override virtual to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * return the number of slices in which thin correctors are split
     */
    public int getSlices() {
        return attSlices.getInteger();
    }

    /**
     * return array with each slice weight
     */
    public double[] getSlicesEffLength() {
        return attSlicesEffLength.getArrDbl();
    }

    /**
     * set the number of slices in which thin correctors are split
     *
     * @param intVal number of slices
     */
    public void setSlices(int intVal) {
        attSlices.set(intVal);
    }

    /**
     * set the number of slices in which thin correctors are split
     *
     * @param arrVal array with weight of slices
     */
    public void setSlicesEffLength(double[] arrVal) {
        attSlicesEffLength.set(arrVal);
    }
}
