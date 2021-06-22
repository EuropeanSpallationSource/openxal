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
package xal.extension.jels.smf.impl;

import xal.ca.ChannelFactory;
import xal.extension.jels.smf.attr.RfqDummyBucket;
import xal.smf.AcceleratorNode;
import xal.smf.impl.qualify.ElementTypeManager;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class RfqDummy extends AcceleratorNode {

    public static final String TYPE = "RFQ";

    private RfqDummyBucket bucRfqDummy;

    static {
        registerType();
    }

    public RfqDummy(String strId) {
        this(strId, null);
    }

    public RfqDummy(String strId, ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setRfqDummyBucket(new RfqDummyBucket());
    }

    public final void setRfqDummyBucket(RfqDummyBucket buc) {
        bucRfqDummy = buc;
        super.addBucket(buc);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerTypes(RfqDummy.class, TYPE);
    }

    public double getC11() {
        return bucRfqDummy.getAttC11();
    }

    public double getC12() {
        return bucRfqDummy.getAttC12();
    }

    public double getC22() {
        return bucRfqDummy.getAttC22();
    }

    public double getC33() {
        return bucRfqDummy.getAttC33();
    }

    public double getC34() {
        return bucRfqDummy.getAttC34();
    }

    public double getC44() {
        return bucRfqDummy.getAttC44();
    }

    public double getC55() {
        return bucRfqDummy.getAttC55();
    }

    public double getC56() {
        return bucRfqDummy.getAttC56();
    }

    public double getC66() {
        return bucRfqDummy.getAttC66();
    }

    public double getEnergy() {
        return bucRfqDummy.getAttEnergy();
    }

}
