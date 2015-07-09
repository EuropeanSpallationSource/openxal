package edu.stanford.lcls.xal.model;

import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;

public class RunModelConfigurationDesign extends RunModelConfiguration {

	@Override
	public int getRunModelMethod() {
		return 0;
	}

	@Override
	public void initialize(Scenario scenario) throws SynchronizationException {
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		scenario.resync();
	}

}
