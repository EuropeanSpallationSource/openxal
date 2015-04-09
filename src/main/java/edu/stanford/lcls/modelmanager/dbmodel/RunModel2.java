package edu.stanford.lcls.modelmanager.dbmodel;

import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;

/* TODO OPENXAL Implement/port, version in SLAC library depends on XAL */
public class RunModel2 {
	private int modelMode;
	private String emitNode;
	private boolean refMode;
	private String runMode;
	
	private Accelerator accelerator;
	private Scenario scenario;
	
	public RunModel2(int modelMode) {
		this.modelMode = modelMode;
	}

	public void setEmitNode(String refID) {
		this.emitNode = refID;
	}

	public void useDesignRef(boolean refMode) {
		this.refMode = refMode;
	}

	public void setRunMode(String syncModeDesign) {
		this.runMode = syncModeDesign;
		
	}

	public void run() {
		try {
			accelerator = XMLDataManager.loadDefaultAccelerator();
			scenario = Scenario.newScenarioFor(accelerator);//, elementMapping);	
			IAlgorithm tracker = AlgorithmFactory.createEnvTrackerAdapt( accelerator );
		
			scenario.setProbe(ProbeFactory.getEnvelopeProbe( accelerator.getSequence("MEBT"), tracker ));
							
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
			scenario.resync();
			scenario.run();
		} catch (InstantiationException|ModelException e) {
			e.printStackTrace();
		}
	}

	public Accelerator getAccelerator() {
		return accelerator;
	}

	public Scenario getScenario() {
		return scenario;
	}
	
}