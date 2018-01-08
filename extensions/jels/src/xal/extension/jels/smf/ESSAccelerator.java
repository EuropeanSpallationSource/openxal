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
package xal.extension.jels.smf;

import xal.ca.ChannelFactory;
import xal.smf.Accelerator;
import xal.tools.data.DataAdaptor;

/**
 * The hierarchical tree of accelerator nodes, elements and sequences of
 * elements. This class extends Accelerator overriding the write() method to
 * export power supplies to the XML file
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class ESSAccelerator extends Accelerator {

    public ESSAccelerator() {
        super();
    }

    /**
     * Primary constructor
     */
    public ESSAccelerator(final String sysId) {
        super(sysId);
    }

    /**
     * Primary constructor
     */
    public ESSAccelerator(final ChannelFactory channelFactory) {
        super(channelFactory);
    }

    /**
     * Primary constructor
     */
    public ESSAccelerator(final String sysId, final ChannelFactory channelFactory) {
        super(sysId, channelFactory);
    }

    /**
     * Instructs the accelerator to write its data to the adaptor for external
     * storage.
     *
     * @param adaptor The adaptor to which the accelerator data is written
     */
    @Override
    public void write(DataAdaptor adaptor) {
        super.write(adaptor);
        // write out power supplies
        DataAdaptor powerSuppliesAdaptor = adaptor.createChild("powersupplies");
        getMagnetMainSupplies().forEach(mps -> mps.write(powerSuppliesAdaptor.createChild("ps")));
    }
}
