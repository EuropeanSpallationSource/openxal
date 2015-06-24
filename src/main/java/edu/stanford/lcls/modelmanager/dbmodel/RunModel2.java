package edu.stanford.lcls.modelmanager.dbmodel;

import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
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
	
	private EnvelopeProbe probe;
	
	public RunModel2() { 
		try {
			accelerator = XMLDataManager.loadDefaultAccelerator();
			
			scenario = Scenario.newScenarioFor(accelerator);//, elementMapping);
		
			//IAlgorithm tracker = AlgorithmFactory.createEnvTrackerAdapt( accelerator );
			IAlgorithm tracker = AlgorithmFactory.createEnvelopeTracker( accelerator );
			
			probe = ProbeFactory.getEnvelopeProbe( accelerator.getSequence("MEBT"), tracker );
		} catch (ModelException | InstantiationException e) {
			e.printStackTrace();
		}	
	}

	/** Beamline selection */
	public void setModelMode(int modelMode) {
		this.modelMode = modelMode;
	}
	
	/** Wirescanner to use for initial parameters (via backpropagation) */
	public void setEmitNode(String refID) {
		this.emitNode = refID;
	}

	/** Use measurement data (true) or design data (false) on that wirescanner */
	public void useDesignRef(boolean refMode) {
		this.refMode = refMode;
	}

	/** scenario synchronization mode */
	public void setRunMode(String syncModeDesign) {
		this.runMode = syncModeDesign;
		
	}

	public void run() throws ModelException {
		scenario.setProbe(probe.copy());
		scenario.setSynchronizationMode(runMode);					
		scenario.resync();
		scenario.run();
	}

	public Accelerator getAccelerator() {
		return accelerator;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public EnvelopeProbe getProbe() {

		return probe;
	}
	
}