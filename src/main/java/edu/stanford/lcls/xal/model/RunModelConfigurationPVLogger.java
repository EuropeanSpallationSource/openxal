package edu.stanford.lcls.xal.model;

import xal.model.ModelException;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.sim.scenario.Scenario;

public class RunModelConfigurationPVLogger extends RunModelConfiguration {
	private long pvLoggerId;
	
	public RunModelConfigurationPVLogger(long pvLoggerId) {
		this.pvLoggerId = pvLoggerId;
	}

	@Override
	public int getRunModelMethod() {
		return 2;
	}

	@Override
	public void initialize(Scenario scenario) throws ModelException {
		PVLoggerDataSource plds = new PVLoggerDataSource(pvLoggerId);
		plds.setModelSource(scenario.getSequence(), scenario);
		scenario.resync();
	}

}
