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

import xal.tools.data.DataAdaptor;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class DipoleBucket extends MagnetBucket {

           
    // bend Angle for dipoles (deg) 
    protected Attribute       m_attBendAngle;
    /** path length  (m) */
    private Attribute       m_attPathLength;         
    // dipole rotation angle for entrance pole face (degree)
    protected Attribute       m_attDipoleEntrRotAngle;
    // dipole rotation angle for exit pole face (degree)
    protected Attribute       m_attDipoleExitRotAngle;
    // quadrupole component for bend dipole
    protected Attribute       m_attDipoleQuadComponent;
    
    public DipoleBucket() {
        super();
        
        m_attBendAngle = new Attribute(0.0);
        m_attPathLength = new Attribute(0.0 );
        m_attDipoleEntrRotAngle = new Attribute(0.0);
        m_attDipoleExitRotAngle = new Attribute(0.0);
        m_attDipoleQuadComponent = new Attribute(0.0);
        
        super.registerAttribute(c_arrNames[0], m_attBendAngle);
        super.registerAttribute(c_arrNames[1], m_attPathLength);
        super.registerAttribute(c_arrNames[2], m_attDipoleEntrRotAngle);
        super.registerAttribute(c_arrNames[3], m_attDipoleExitRotAngle);
	super.registerAttribute(c_arrNames[4], m_attDipoleQuadComponent);
    }

    private final static String c_strType = "dipole";

    protected final static String[] c_arrNames = {
        "bendAngle", // bend angle
        "pathLength",   // path length
        "dipoleEntrRotAngle", // dipole rotation angle for entrance pole face
        "dipoleExitRotAngle", // dipole rotation angle for exit pole face
        "dipoleQuadComponent", // quadrupole component for bend dipole
    };

    /**
     * Override virtual to provide type signature
     */
    public String getType() {
        return c_strType;
    }
    
    /** return the dipole bend angle (in degrees) */
    public double   getBendAngle()   { return m_attBendAngle.getDouble(); }
    /** return the design path length (in m) */
    public double   getPathLength() { return m_attPathLength.getDouble(); };
    
    /** return the dipole rotation angle for entrance pole face (in degrees) */
    public double   getDipoleEntrRotAngle() { return m_attDipoleEntrRotAngle.getDouble(); }
    /** return the dipole rotation angle for exit pole face (in degrees) */
    public double   getDipoleExitRotAngle() { return m_attDipoleExitRotAngle.getDouble(); }
    /** return the quadrupole component for bend dipole */
    public double   getDipoleQuadComponent() { return m_attDipoleQuadComponent.getDouble(); }
   
    
    /** set the dipole bend angle (in degrees)
     * @param dblVal dipole bend angle in degrees
     */
    public void setBendAngle(double dblVal)      { m_attBendAngle.set(dblVal); }
    /** set the dipole path length (in m) 
     * @param dblVal path length in meters
     */
    public void setPathLength(double dblVal)    { m_attPathLength.set(dblVal); };
    public void setDipoleEntrRotAngle(double dblVal)    { m_attDipoleEntrRotAngle.set(dblVal); }
    /** set the dipole rotation angle for exit pole face (in degrees) 
     * @param dblVal dipole rotation angle for exit pole face in degrees
     */
    public void setDipoleExitRotAngle(double dblVal)    { m_attDipoleExitRotAngle.set(dblVal); }
    /** set the quadrupole component for bend dipole
     * @param dblVal quadrupole component for bend dipole
     */
    public void setDipoleQuadComponent(double dblVal)    { m_attDipoleQuadComponent.set(dblVal); }
    

    @Override
    public void update(DataAdaptor adaptor) throws NumberFormatException {
        super.update(adaptor);
    }
    
}
