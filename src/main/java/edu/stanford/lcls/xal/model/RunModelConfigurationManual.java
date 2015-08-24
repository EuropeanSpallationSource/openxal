package edu.stanford.lcls.xal.model;

import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.AcceleratorNode;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDevice;


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
			switch((String) prop){
			case "B":          
                scenario.setModelInput(node, ElectromagnetPropertyAccessor.PROPERTY_FIELD, val);
                break;
			case "P":              
                scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_PHASE, val);
                break;
			case "A":              
                scenario.setModelInput(node, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, val);
                break;
			case "APRX":
			    node.getAper().setAperX(val);
			    break;
			case "MISX":
			    node.getAlign().setX(val);
			    break;
			case "MISY":
			    node.getAlign().setY(val);
			    break;
			case "MISZ":
			    node.getAlign().setZ(val);
			    break;
			case "ROTX":
			    node.getAlign().setPitch(val);
			    break;
			case "ROTY":
			    node.getAlign().setYaw(val);
			    break;
			case "ROTZ":
			    node.getAlign().setRoll(val);
			    break;
			case "ENBL":
			    node.setStatus((val == 1));
			    break;
                }
			}
		scenario.resync();
	}

}
