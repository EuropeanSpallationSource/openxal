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
			final double val = Double.parseDouble((String) dev.getPropertyValue("DEVICE_VALUE"));
			
			if ("B".equals(prop)) {			
				scenario.setModelInput(node, ElectromagnetPropertyAccessor.PROPERTY_FIELD, val);
				
			} else if ("P".equals(prop)) {				
				scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_PHASE, val);
				
			} else if ("A".equals(prop)) {				
				scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, val);
				
			} else if(MachineModelDeviceTableModel.additionalParameters.contains(prop)){
                switch(MachineModelDeviceTableModel.additionalParameters.indexOf(prop)){//CHECK maybe do this nicer ?
                case 0:
                    node.getAper().setAperX(val);
                    break;
                case 1:
                    node.getAlign().setX(val);
                    break;
                case 2:
                    node.getAlign().setY(val);
                    break;
                case 3:
                    node.getAlign().setZ(val);
                    break;
                case 4:
                    node.getAlign().setPitch(val);
                    break;
                case 5:
                    node.getAlign().setYaw(val);
                    break;
                case 6:
                    node.getAlign().setRoll(val);
                    break;
                case 7:
                    node.setStatus((val == 1));
                    break;
                }
			}
		}
		scenario.resync();
	}

}
