package edu.stanford.lcls.xal.model;

import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDevice;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;

public class RunModelConfigurationManual extends RunModelConfiguration {

	public RunModelConfigurationManual(MachineModelDevice[] machineDevice)
	{
		
	}
	
	@Override
	public int getRunModelMethod() {
		return 3;
	}

	@Override
	public void initialize(Scenario scenario) throws SynchronizationException {
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		scenario.resync();
	}

}
