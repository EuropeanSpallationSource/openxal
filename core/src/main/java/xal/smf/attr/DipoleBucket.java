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
public class DipoleBucket extends MagnetBucket {

    // bend Angle for dipoles (deg) 
    protected Attribute attBendAngle;
    /**
     * path length (m)
     */
    private Attribute attPathLength;
    // dipole rotation angle for entrance pole face (degree)
    protected Attribute attDipoleEntrRotAngle;
    // dipole rotation angle for exit pole face (degree)
    protected Attribute attDipoleExitRotAngle;
    // quadrupole component for bend dipole
    protected Attribute attDipoleQuadComponent;

    public DipoleBucket() {
        super();

        attBendAngle = new Attribute(0.0);
        attPathLength = new Attribute(0.0);
        attDipoleEntrRotAngle = new Attribute(0.0);
        attDipoleExitRotAngle = new Attribute(0.0);
        attDipoleQuadComponent = new Attribute(0.0);

        super.registerAttribute(ARR_NAMES[0], attBendAngle, "Bend angle for dipoles (deg).");
        super.registerAttribute(ARR_NAMES[1], attPathLength, "Path length  (m).");
        super.registerAttribute(ARR_NAMES[2], attDipoleEntrRotAngle, "Dipole rotation angle for entrance pole face (deg).");
        super.registerAttribute(ARR_NAMES[3], attDipoleExitRotAngle, "Dipole rotation angle for exit pole face (deg).");
        super.registerAttribute(ARR_NAMES[4], attDipoleQuadComponent, "Quadrupole component for bend dipole.");
    }

    private static final String TYPE = "dipole";

    protected static final String[] ARR_NAMES = {
        // bend angle
        "bendAngle",
        // path length
        "pathLength",
        // dipole rotation angle for entrance pole face
        "dipoleEntrRotAngle",
        // dipole rotation angle for exit pole face
        "dipoleExitRotAngle",
        // quadrupole component for bend dipole
        "dipoleQuadComponent",};

    /**
     * Override virtual to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * return the dipole bend angle (in degrees)
     */
    public double getBendAngle() {
        return attBendAngle.getDouble();
    }

    /**
     * return the design path length (in m)
     */
    public double getPathLength() {
        return attPathLength.getDouble();
    }

    /**
     * return the dipole rotation angle for entrance pole face (in degrees)
     */
    public double getDipoleEntrRotAngle() {
        return attDipoleEntrRotAngle.getDouble();
    }

    /**
     * return the dipole rotation angle for exit pole face (in degrees)
     */
    public double getDipoleExitRotAngle() {
        return attDipoleExitRotAngle.getDouble();
    }

    /**
     * return the quadrupole component for bend dipole
     */
    public double getDipoleQuadComponent() {
        return attDipoleQuadComponent.getDouble();
    }

    /**
     * set the dipole bend angle (in degrees)
     *
     * @param dblVal dipole bend angle in degrees
     */
    public void setBendAngle(double dblVal) {
        attBendAngle.set(dblVal);
    }

    /**
     * set the dipole path length (in m)
     *
     * @param dblVal path length in meters
     */
    public void setPathLength(double dblVal) {
        attPathLength.set(dblVal);
    }

    public void setDipoleEntrRotAngle(double dblVal) {
        attDipoleEntrRotAngle.set(dblVal);
    }

    /**
     * set the dipole rotation angle for exit pole face (in degrees)
     *
     * @param dblVal dipole rotation angle for exit pole face in degrees
     */
    public void setDipoleExitRotAngle(double dblVal) {
        attDipoleExitRotAngle.set(dblVal);
    }

    /**
     * set the quadrupole component for bend dipole
     *
     * @param dblVal quadrupole component for bend dipole
     */
    public void setDipoleQuadComponent(double dblVal) {
        attDipoleQuadComponent.set(dblVal);
    }
}
