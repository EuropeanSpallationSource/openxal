package se.lu.esss.ics.jels.smf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import xal.ca.ChannelFactory;
import xal.sim.scenario.ElementMapping;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNodeFactory;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.TimingCenter;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetTrimSupply;
import xal.tools.data.DataAdaptor;
import xal.tools.data.EditContext;

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
        getMagnetMainSupplies().forEach((mps) -> {
            mps.write(powerSuppliesAdaptor.createChild("ps"));
        });
    }
}
