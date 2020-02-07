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
package xal.extension.jels.smf.attr;

import xal.smf.attr.Attribute;
import xal.smf.attr.AttributeBucket;

/**
 * A set of dummy RFQ attributes.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class RfqDummyBucket extends AttributeBucket {

    private Attribute m_attC11;
    private Attribute m_attC12;
    private Attribute m_attC22;
    private Attribute m_attC33;
    private Attribute m_attC34;
    private Attribute m_attC44;
    private Attribute m_attC55;
    private Attribute m_attC56;
    private Attribute m_attC66;
    private Attribute m_attEnergy;

    private static final long serialVersionUID = 1;

    public static final String c_strType = "rfq";

    static final String[] c_arrNames = {
        "c11",
        "c12",
        "c22",
        "c33",
        "c34",
        "c44",
        "c55",
        "c56",
        "c66",
        "energy"
    };

    public RfqDummyBucket() {
        super();

        m_attC11 = new Attribute(0.);
        m_attC12 = new Attribute(0.);
        m_attC22 = new Attribute(0.);
        m_attC33 = new Attribute(0.);
        m_attC34 = new Attribute(0.);
        m_attC44 = new Attribute(0.);
        m_attC55 = new Attribute(0.);
        m_attC56 = new Attribute(0.);
        m_attC66 = new Attribute(0.);
        m_attEnergy = new Attribute(0.);

        super.registerAttribute(c_arrNames[0], m_attC11);
        super.registerAttribute(c_arrNames[1], m_attC12);
        super.registerAttribute(c_arrNames[2], m_attC22);
        super.registerAttribute(c_arrNames[3], m_attC33);
        super.registerAttribute(c_arrNames[4], m_attC34);
        super.registerAttribute(c_arrNames[5], m_attC44);
        super.registerAttribute(c_arrNames[6], m_attC55);
        super.registerAttribute(c_arrNames[7], m_attC56);
        super.registerAttribute(c_arrNames[8], m_attC66);
        super.registerAttribute(c_arrNames[9], m_attEnergy);
    }


    /*
     *  User Interface
     */
    /**
     * @return Override virtual to provide type signature
     */
    @Override
    public String getType() {
        return c_strType;
    }

    @Override
    public String[] getAttrNames() {
        return c_arrNames;
    }

    public double getAttC11() {
        return m_attC11.getDouble();
    }

    public void setAttC11(double m_attC11) {
        this.m_attC11.set(m_attC11);
    }

    public double getAttC12() {
        return m_attC12.getDouble();
    }

    public void setAttC12(double m_attC12) {
        this.m_attC12.set(m_attC12);
    }

    public double getAttC22() {
        return m_attC22.getDouble();
    }

    public void setAttC22(double m_attC22) {
        this.m_attC22.set(m_attC22);
    }

    public double getAttC33() {
        return m_attC33.getDouble();
    }

    public void setAttC33(double m_attC33) {
        this.m_attC33.set(m_attC33);
    }

    public double getAttC34() {
        return m_attC34.getDouble();
    }

    public void setAttC34(double m_attC34) {
        this.m_attC34.set(m_attC34);
    }

    public double getAttC44() {
        return m_attC44.getDouble();
    }

    public void setAttC44(double m_attC44) {
        this.m_attC44.set(m_attC44);
    }

    public double getAttC55() {
        return m_attC55.getDouble();
    }

    public void setAttC55(double m_attC55) {
        this.m_attC55.set(m_attC55);
    }

    public double getAttC56() {
        return m_attC56.getDouble();
    }

    public void setAttC56(double m_attC56) {
        this.m_attC56.set(m_attC56);
    }

    public double getAttC66() {
        return m_attC66.getDouble();
    }

    public void setAttC66(double m_attC66) {
        this.m_attC66.set(m_attC66);
    }

    public double getAttEnergy() {
        return m_attEnergy.getDouble();
    }

    public void setAttEnergy(double m_attEnergy) {
        this.m_attEnergy.set(m_attEnergy);
    }

}
