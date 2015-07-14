package edu.stanford.lcls.xal.model;

import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;

public class RunModel {	
	private AcceleratorSeq accelerator;
	private Scenario scenario;
	protected int modelMode = 5;
	private EnvelopeProbe probe;
	
	public RunModel(AcceleratorSeq acc) { 
		try {
			accelerator = acc;
			
			scenario = Scenario.newScenarioFor(accelerator);//, elementMapping);
		
			//IAlgorithm tracker = AlgorithmFactory.createEnvTrackerAdapt( accelerator );
			IAlgorithm tracker = AlgorithmFactory.createEnvelopeTracker( accelerator );
			
			probe = ProbeFactory.getEnvelopeProbe( accelerator.getSequence("MEBT"), tracker );
		} catch (ModelException | InstantiationException e) {
			e.printStackTrace();
		}	
	}



	public void run(RunModelConfiguration config) throws ModelException {
		config.initialize(scenario);
		
		scenario.setProbe(probe.copy());
		
		scenario.run();
	}

	public AcceleratorSeq getAccelerator() {
		return accelerator;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public EnvelopeProbe getProbe() {
		return probe;
	}

	/** beamline selection */
	public void setModelMode(int _modelMode) {
		modelMode = _modelMode;
	}



	public void resetProbe() {
		IAlgorithm tracker;
		try {
			tracker = AlgorithmFactory.createEnvelopeTracker( accelerator );
			probe = ProbeFactory.getEnvelopeProbe( accelerator.getSequence("MEBT"), tracker );
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}
	
}