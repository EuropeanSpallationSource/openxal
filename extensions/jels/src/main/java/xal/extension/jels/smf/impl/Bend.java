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

import java.util.Arrays;
import xal.ca.ChannelFactory;
import xal.extension.jels.smf.attr.DipoleBucket;
import xal.smf.impl.Magnet;
import xal.smf.impl.qualify.ElementTypeManager;

/**
 * Extends OpenXal Bend class with orientation and FringeField parameters.
 *
 * @author Ivo List
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class Bend extends xal.smf.impl.Bend {

    /**
     * device type
     */
    public static final String TYPE = "D";
    public static final String[] TYPE_DH = {"dh", "horzbend", "hbend"};
    public static final String[] TYPE_DV = {"dv", "vertbend", "vbend"};

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(Bend.class, TYPE);
    }

    /**
     * Creates Bend with horizontal orientation.
     *
     * @param strId node id
     */
    public Bend(String strId) {
        this(strId, HORIZONTAL, null);
    }

    /**
     * Creates Bend with horizontal orientation.
     *
     * @param strId node id
     * @param channelFactory
     */
    public Bend(String strId, ChannelFactory channelFactory) {
        this(strId, HORIZONTAL, channelFactory);
    }

    /**
     * Creates Bend with arbitrary orientation.
     *
     * @param strId node id
     * @param orientation orientation of the magnet, either HORIZONTAL or
     * VERTICAL as defined by MagnetType.
     */
    public Bend(String strId, int orientation) {
        this(strId, orientation, null);
    }

    /**
     * Creates Bend with arbitrary orientation.
     *
     * @param strId node id
     * @param orientation orientation of the magnet, either HORIZONTAL or
     * VERTICAL as defined by MagnetType.
     * @param channelFactory
     */
    public Bend(String strId, int orientation, ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setMagBucket(new DipoleBucket());
        setOrientation(orientation);
    }

    /**
     * Override to provide the correct type signature per instance. This is
     * necessary since the Dipole class can represent more than one official
     * type (DH or DV).
     *
     * @return The official type consistent with the naming convention.
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Sets orientation of the magnet as defined by MagnetType.
     *
     * @param orientation orientation of the magnet, either HORIZONTAL or
     * VERTICAL
     */
    public void setOrientation(int orientation) {
        ((DipoleBucket) getMagBucket()).setOrientation(orientation);
    }

    /**
     * @return total gap of magnet (m)
     */
    public double getGap() {
        return ((DipoleBucket) getMagBucket()).getGap();
    }

    /**
     * @param value total gap of magnet (m)
     */
    public void setGap(double value) {
        ((DipoleBucket) getMagBucket()).setGap(value);
    }

    /**
     * @return Upstream edge face Fringe-field factor (default = 0.45)
     */
    public double getEntrK1() {
        return ((DipoleBucket) getMagBucket()).getEntrFringeFieldFactorK1();
    }

    /**
     * @param value Upstream edge face Fringe-field factor (default = 0.45)
     */
    public void setEntrK1(double value) {
        ((DipoleBucket) getMagBucket()).setEntrFringeFieldFactorK1(value);
    }

    /**
     * @return Upstream edge face Fringe-field factor (default = 2.80)
     */
    public double getEntrK2() {
        return ((DipoleBucket) getMagBucket()).getEntrFringeFieldFactorK2();
    }

    /**
     * @param value Upstream edge face Fringe-field factor (default = 2.80)
     */
    public void setEntrK2(double value) {
        ((DipoleBucket) getMagBucket()).setEntrFringeFieldFactorK2(value);
    }

    /**
     * @return Downstream edge face Fringe-field factor (default = 0.45)
     */
    public double getExitK1() {
        return ((DipoleBucket) getMagBucket()).getExitFringeFieldFactorK1();
    }

    /**
     * @param value Downstream edge face Fringe-field factor (default = 0.45)
     */
    public void setExitK1(double value) {
        ((DipoleBucket) getMagBucket()).setExitFringeFieldFactorK1(value);
    }

    /**
     * @return Downstream edge face Fringe-field factor (default = 2.80)
     */
    public double getExitK2() {
        return ((DipoleBucket) getMagBucket()).getExitFringeFieldFactorK2();
    }

    /**
     * @param value Downstream edge face Fringe-field factor (default = 2.80)
     */
    public void setExitK2(double value) {
        ((DipoleBucket) getMagBucket()).setExitFringeFieldFactorK2(value);
    }

    /**
     * Get the orientation of the magnet as defined by MagnetType. The
     * orientation of the dipole is determined by its type: DH or DV
     *
     * @return One of HORIZONTAL or VERTICAL
     */
    @Override
    public int getOrientation() {
        return ((DipoleBucket) getMagBucket()).getOrientation();
    }

    /**
     * Determine if this node is of the specified type. Override the default
     * method since a dipole could represent either a vertical or horizontal
     * type. Must also handle inheritance checking so we must or the direct type
     * comparison with the inherited type checking.
     *
     * @param type The type against which to compare this quadrupole's type.
     * @return true if the node is a match and false otherwise.
     */
    @Override
    public boolean isKindOf(String type) {
        if (getOrientation() == Magnet.HORIZONTAL) {
            return type.equalsIgnoreCase(TYPE) || Arrays.asList(TYPE_DH).contains(type.toLowerCase()) || super.isKindOf(type);
        } else {
            return type.equalsIgnoreCase(TYPE) || Arrays.asList(TYPE_DV).contains(type.toLowerCase()) || super.isKindOf(type);
        }
    }
}
