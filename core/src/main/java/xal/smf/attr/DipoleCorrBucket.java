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
    private Attribute m_attSlices;
    // effective length of each slice
    private Attribute m_attSlicesEffLength;

    public DipoleCorrBucket() {
        super();

        m_attSlices = new Attribute(1);
        m_attSlicesEffLength = new Attribute(new double[]{1.0});

        super.registerAttribute(c_arrNames[0], m_attSlices, "Number of slices in which thin correctors are split.");
        super.registerAttribute(c_arrNames[1], m_attSlicesEffLength, "Effective length of each slice (m).");
    }

    private final static String c_strType = "steerer";

    private final static String[] c_arrNames = {
        "slices", // number of slices in which thin correctors are split
        "slicesEffLength" // effecttive length of each slice
    };

    /**
     * Override virtual to provide type signature
     */
    public String getType() {
        return c_strType;
    }

    /**
     * return the number of slices in which thin correctors are split
     */
    public int getSlices() {
        return m_attSlices.getInteger();
    }

    /**
     * return array with each slice weight
     */
    public double[] getSlicesEffLength() {
        return m_attSlicesEffLength.getArrDbl();
    }

    /**
     * set the number of slices in which thin correctors are split
     *
     * @param intVal number of slices
     */
    public void setSlices(int intVal) {
        m_attSlices.set(intVal);
    }

    /**
     * set the number of slices in which thin correctors are split
     *
     * @param arrVal array with weight of slices
     */
    public void setSlicesEffLength(double[] arrVal) {
        m_attSlicesEffLength.set(arrVal);
    }
}
