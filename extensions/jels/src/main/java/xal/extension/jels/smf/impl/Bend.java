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

import xal.extension.jels.smf.attr.MagnetBucket;
import xal.ca.ChannelFactory;
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
    public static final String s_strType = "D";

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(Bend.class, s_strType);
    }

    private final MagnetBucket m_bucESSMagnet = new MagnetBucket();

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
        setOrientation(orientation);
        setMagBucket(m_bucESSMagnet);
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
        return s_strType;
    }

    /**
     * Sets orientation of the magnet as defined by MagnetType.
     *
     * @param orientation orientation of the magnet, either HORIZONTAL or
     * VERTICAL
     */
    public void setOrientation(int orientation) {
        m_bucESSMagnet.setOrientation(orientation);
    }

    /**
     * @return total gap of magnet (m)
     */
    public double getGap() {
        return m_bucESSMagnet.getGap();
    }

    /**
     * @param value total gap of magnet (m)
     */
    public void setGap(double value) {
        m_bucESSMagnet.setGap(value);
    }

    /**
     * @return Upstream edge face Fringe-field factor (default = 0.45)
     */
    public double getEntrK1() {
        return m_bucESSMagnet.getEntrFringeFieldFactorK1();
    }

    /**
     * @param value Upstream edge face Fringe-field factor (default = 0.45)
     */
    public void setEntrK1(double value) {
        m_bucESSMagnet.setEntrFringeFieldFactorK1(value);
    }

    /**
     * @return Upstream edge face Fringe-field factor (default = 2.80)
     */
    public double getEntrK2() {
        return m_bucESSMagnet.getEntrFringeFieldFactorK2();
    }

    /**
     * @param value Upstream edge face Fringe-field factor (default = 2.80)
     */
    public void setEntrK2(double value) {
        m_bucESSMagnet.setEntrFringeFieldFactorK2(value);
    }

    /**
     * @return Downstream edge face Fringe-field factor (default = 0.45)
     */
    public double getExitK1() {
        return m_bucESSMagnet.getExitFringeFieldFactorK1();
    }

    /**
     * @param value Downstream edge face Fringe-field factor (default = 0.45)
     */
    public void setExitK1(double value) {
        m_bucESSMagnet.setExitFringeFieldFactorK1(value);
    }

    /**
     * @return Downstream edge face Fringe-field factor (default = 2.80)
     */
    public double getExitK2() {
        return m_bucESSMagnet.getExitFringeFieldFactorK2();
    }

    /**
     * @param value Downstream edge face Fringe-field factor (default = 2.80)
     */
    public void setExitK2(double value) {
        m_bucESSMagnet.setExitFringeFieldFactorK2(value);
    }

    /**
     * Get the orientation of the magnet as defined by MagnetType. The
     * orientation of the dipole is determined by its type: DH or DV
     *
     * @return One of HORIZONTAL or VERTICAL
     */
    @Override
    public int getOrientation() {
        return m_bucESSMagnet.getOrientation();
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
    public boolean isKindOf(final String type) {
        return type.equalsIgnoreCase(s_strType) || super.isKindOf(type);
    }
}
