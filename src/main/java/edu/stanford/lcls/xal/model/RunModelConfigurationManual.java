package edu.stanford.lcls.xal.model;

import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.AcceleratorNode;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDevice;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDeviceTableModel;


/**
 * 
 * Run configuration for manual edited parameters. 
 * 
 * @version 1.0 18 Avg 2015
 * 
 * @author unknown
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class RunModelConfigurationManual extends RunModelConfiguration {
	private MachineModelDevice[] machineDevice;
	
	/**
	 * Class constructor.
	 * @param machineDevice array of MachineModelDevices to set for running simulation.
	 */
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
		//setting all parameters
		for (MachineModelDevice dev : machineDevice) {
		    //getting parameter info
		    final String nodeId = (String)dev.getPropertyValue("ELEMENT_NAME"); 
		    final AcceleratorNode node = scenario.getSequence().getNodeWithId(nodeId);
			final Object prop = dev.getPropertyValue("DEVICE_PROPERTY");
			final double val = Double.parseDouble((String) dev.getPropertyValue("DEVICE_VALUE"));
			//checking for parameter type
			if ("B".equals(prop)) {			
				scenario.setModelInput(node, ElectromagnetPropertyAccessor.PROPERTY_FIELD, val);
				
			} else if ("P".equals(prop)) {				
				scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_PHASE, val);
				
			} else if ("A".equals(prop)) {				
				scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, val);
				
			} else if(MachineModelDeviceTableModel.additionalParameters.contains(prop)){//Checking if prop is one of the additional parameters.
                switch(MachineModelDeviceTableModel.additionalParameters.indexOf(prop)){
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
