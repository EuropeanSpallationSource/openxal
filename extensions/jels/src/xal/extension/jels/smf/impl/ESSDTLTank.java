/*
 * Copyright (C) 2017 European Spallation Source ERIC.
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

import xal.smf.impl.qualify.ElementTypeManager;
import xal.ca.ChannelFactory;

/**
 * The implementation of the DTLTank sequence, which derives from the
 * AcceleratorSeq class. This is a container to be used in handling Drift Tube
 * Linacs These devices have RfGaps in them, which are controlled by a single
 * RfCavity. That is, the RfCavity contains the hooks to the klystron signals,
 * which control all of the RfGaps together. As the DTLTank is also a sequence,
 * it is possible for it to contain other types of nodes, such as quads and
 * BPMs.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class ESSDTLTank extends ESSRfCavity {

    /**
     * standard type for instances of this class
     */
    public static final String s_strType = "DTLTank";

    // static initialization
    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(ESSDTLTank.class, s_strType);
    }

    /**
     * Primary Constructor
     */
    public ESSDTLTank(final String strId, final ChannelFactory channelFactory, final int intReserve) {
        super(strId, channelFactory, intReserve);
    }

    /**
     * Constructor
     */
    public ESSDTLTank(final String strId, final ChannelFactory channelFactory) {
        this(strId, channelFactory, 0);
    }

    /**
     * I just added this comment - didn't do any work.
     *
     * @param strId identifier string of the DTL
     *
     * @author Christopher K. Allen
     * @since May 3, 2011
     */
    public ESSDTLTank(final String strId) {
        this(strId, 0);
    }

    /**
     * I just added this comment - didn't do any work.
     *
     * @param strId identifier string of the DTL
     * @param intReserve optional parameter for specifying memory to reserve for
     * the DTL cells if known.
     *
     * @author Christopher K. Allen
     * @since May 3, 2011
     */
    public ESSDTLTank(final String strId, int intReserve) {
        this(strId, null, intReserve);
    }

    /**
     * Support the node type
     *
     * @return
     */
    @Override
    public String getType() {
        return s_strType;
    }
;
}
