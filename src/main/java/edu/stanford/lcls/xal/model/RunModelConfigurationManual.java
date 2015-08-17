package edu.stanford.lcls.xal.model;

import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.AcceleratorNode;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDevice;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDeviceTableModel;

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
		    final String nodeId = (String)dev.getPropertyValue("ELEMENT_NAME"); 
		    final AcceleratorNode node = scenario.getSequence().getNodeWithId(nodeId);
			final Object prop = dev.getPropertyValue("DEVICE_PROPERTY");
			final String val = (String) dev.getPropertyValue("DEVICE_VALUE");
			
			if ("B".equals(prop)) {			
				scenario.setModelInput(node, ElectromagnetPropertyAccessor.PROPERTY_FIELD, Double.parseDouble(val));
				
			} else if ("P".equals(prop)) {				
				scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_PHASE, Double.parseDouble(val));
				
			} else if ("A".equals(prop)) {				
				scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, Double.parseDouble(val));
				
			} else if(MachineModelDeviceTableModel.additionalParameters.contains(prop)){
                switch(MachineModelDeviceTableModel.additionalParameters.indexOf(prop)){//CHECK maybe do this nicer ?
                case 0:
                    node.getAper().setAperX(Double.parseDouble(val));
                    break;
                case 1:
                    node.getAlign().setX(Double.parseDouble(val));
                    break;
                case 2:
                    node.getAlign().setY(Double.parseDouble(val));
                    break;
                case 3:
                    node.getAlign().setZ(Double.parseDouble(val));
                    break;
                case 4:
                    node.getAlign().setPitch(Double.parseDouble(val));
                    break;
                case 5:
                    node.getAlign().setYaw(Double.parseDouble(val));
                    break;

                case 6:
                    node.getAlign().setRoll(Double.parseDouble(val));
                    break;
                case 7:
                    node.setStatus(Boolean.valueOf(val));
                    break;
                }
			}
		}
		scenario.resync();
	}

}
