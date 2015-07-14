package edu.stanford.lcls.xal.model;

import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDevice;
import edu.stanford.slac.Message.Message;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.RfCavity;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;

public class RunModelConfigurationManual extends RunModelConfiguration {
	private MachineModelDevice[] machineDevice;
	
	public RunModelConfigurationManual(MachineModelDevice[] machineDevice)
	{
		this.machineDevice = machineDevice;
	}
	
	@Override
	public int getRunModelMethod() {
		return 3;
	}

	@Override
	public void initialize(Scenario scenario) throws SynchronizationException {
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		
		//scenario.setModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD, field );
		for (MachineModelDevice dev : machineDevice) {
			Object prop = dev.getPropertyValue("DEVICE_PROPERTY");
			if ("B".equals(prop)) {
				String nodeId = (String)dev.getPropertyValue("ELEMENT_NAME");
				AcceleratorNode node = scenario.getSequence().getNodeWithId(nodeId);
				double val = Double.parseDouble((String)dev.getPropertyValue("DEVICE_VALUE"));				
				scenario.setModelInput(node, ElectromagnetPropertyAccessor.PROPERTY_FIELD, val);
			} else if ("P".equals(prop)) {
				String nodeId = (String)dev.getPropertyValue("ELEMENT_NAME");
				AcceleratorNode node = scenario.getSequence().getNodeWithId(nodeId);
				double val = Double.parseDouble((String)dev.getPropertyValue("DEVICE_VALUE"));				
				scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_PHASE, val);
			} else if ("A".equals(prop)) {				
				String nodeId = (String)dev.getPropertyValue("ELEMENT_NAME");
				AcceleratorNode node = scenario.getSequence().getNodeWithId(nodeId);
				double val = Double.parseDouble((String)dev.getPropertyValue("DEVICE_VALUE"));				
				scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, val);
			}
		}
		scenario.resync();
	}

}
