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

    private Attribute attC11;
    private Attribute attC12;
    private Attribute attC22;
    private Attribute attC33;
    private Attribute attC34;
    private Attribute attC44;
    private Attribute attC55;
    private Attribute attC56;
    private Attribute attC66;
    private Attribute attEnergy;

    private static final long serialVersionUID = 1;

    public static final String c_strType = "rfq";

    static final String[] ARR_NAMES = {
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

        attC11 = new Attribute(0.);
        attC12 = new Attribute(0.);
        attC22 = new Attribute(0.);
        attC33 = new Attribute(0.);
        attC34 = new Attribute(0.);
        attC44 = new Attribute(0.);
        attC55 = new Attribute(0.);
        attC56 = new Attribute(0.);
        attC66 = new Attribute(0.);
        attEnergy = new Attribute(0.);

        super.registerAttribute(ARR_NAMES[0], attC11);
        super.registerAttribute(ARR_NAMES[1], attC12);
        super.registerAttribute(ARR_NAMES[2], attC22);
        super.registerAttribute(ARR_NAMES[3], attC33);
        super.registerAttribute(ARR_NAMES[4], attC34);
        super.registerAttribute(ARR_NAMES[5], attC44);
        super.registerAttribute(ARR_NAMES[6], attC55);
        super.registerAttribute(ARR_NAMES[7], attC56);
        super.registerAttribute(ARR_NAMES[8], attC66);
        super.registerAttribute(ARR_NAMES[9], attEnergy);
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

    public double getAttC11() {
        return attC11.getDouble();
    }

    public void setAttC11(double attC11) {
        this.attC11.set(attC11);
    }

    public double getAttC12() {
        return attC12.getDouble();
    }

    public void setAttC12(double attC12) {
        this.attC12.set(attC12);
    }

    public double getAttC22() {
        return attC22.getDouble();
    }

    public void setAttC22(double attC22) {
        this.attC22.set(attC22);
    }

    public double getAttC33() {
        return attC33.getDouble();
    }

    public void setAttC33(double attC33) {
        this.attC33.set(attC33);
    }

    public double getAttC34() {
        return attC34.getDouble();
    }

    public void setAttC34(double attC34) {
        this.attC34.set(attC34);
    }

    public double getAttC44() {
        return attC44.getDouble();
    }

    public void setAttC44(double attC44) {
        this.attC44.set(attC44);
    }

    public double getAttC55() {
        return attC55.getDouble();
    }

    public void setAttC55(double attC55) {
        this.attC55.set(attC55);
    }

    public double getAttC56() {
        return attC56.getDouble();
    }

    public void setAttC56(double attC56) {
        this.attC56.set(attC56);
    }

    public double getAttC66() {
        return attC66.getDouble();
    }

    public void setAttC66(double attC66) {
        this.attC66.set(attC66);
    }

    public double getAttEnergy() {
        return attEnergy.getDouble();
    }

    public void setAttEnergy(double attEnergy) {
        this.attEnergy.set(attEnergy);
    }

}
