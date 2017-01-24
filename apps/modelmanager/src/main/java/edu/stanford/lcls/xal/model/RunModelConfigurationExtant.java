package edu.stanford.lcls.xal.model;

import edu.stanford.slac.Message.Message;
import xal.model.ModelException;
import xal.sim.scenario.Scenario;

public class RunModelConfigurationExtant extends RunModelConfiguration {
	public RunModelConfigurationExtant() {
		
	}
	
	@Override
	public int getRunModelMethod() {
		return 1;
	}

	@Override
	public void initialize(Scenario scenario) throws ModelException {
		//TODO Connect all channels for model use.  This may have to be done earlier, not here.
		
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
		scenario.resync();
	}

}
