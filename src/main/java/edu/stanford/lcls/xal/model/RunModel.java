package edu.stanford.lcls.xal.model;

import java.util.ArrayList;
import java.util.List;

import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;

/**
 * 
 * Run model for running simulations.
 * 
 * @version 1.0 18 Avg 2015
 * 
 * @author unknown
 * @author Blaž Kranjc 
 */
public class RunModel {	
	private Accelerator accelerator;
	private Scenario scenario;
	protected String modelMode;
	private EnvelopeProbe probe;
	private List<String> beamlines;
	private String defaultBeamline;
	
	public static final String DEFAULT_BEAMLINE_TEXT = "DEFAULT";

	/**
	 * Constructor
	 * @param acc on which the simulation is to be run.
	 */
	public RunModel(Accelerator acc) { 
		accelerator = acc;
			
		initBeamlines();
		resetProbe();				
	}

	/**
	 * Runs the scenario simulation.
	 * @param config configuration to set up all parameters.
	 * @throws ModelException
	 */
	public void run(RunModelConfiguration config) throws ModelException {
	    try {
            scenario = Scenario.newScenarioFor(getSequence()); 
        } catch (ModelException e) {
            e.printStackTrace();
        }
	    
		config.initialize(scenario);
		
		scenario.setProbe(probe.copy());
		
		scenario.run();
	}

	/**
	 * Get used sequence.
	 * @return AcceleratorSequence used by run model.
	 */
	public AcceleratorSeq getSequence()
	{
		AcceleratorSeq seq = accelerator.getSequence(modelMode);
		if (seq == null) {
			seq = accelerator.getComboSequence(modelMode);
		}
		return seq;
	}

	/**
	 * Get used scenario.
	 * @return scenario used by run model.
	 */
	public Scenario getScenario() {
		return scenario;
	}
	/**
     * Get used probe.
     * @return probe used by run model.
     */
	public EnvelopeProbe getProbe() {
		return probe;
	}

	/** 
	 * Beamline selection 
	 * @param _modelMode mode of obtaining parameters.
	 */
	public void setModelMode(String _modelMode) {
		if (DEFAULT_BEAMLINE_TEXT.equals(_modelMode)) {
			modelMode = defaultBeamline;
		} else {
			modelMode = _modelMode;
		}
		resetProbe();
		try {
			scenario = Scenario.newScenarioFor(getSequence()); //, elementMapping);
		} catch (ModelException e) {
			e.printStackTrace();
		}
	}
	/**Resets probe and sets it position */
	public void resetProbe() {
		IAlgorithm tracker;
		try {
			tracker = AlgorithmFactory.createEnvelopeTracker( accelerator );
			AcceleratorSeq seq = getSequence();
			probe = ProbeFactory.getEnvelopeProbe(seq, tracker);
			if (seq instanceof AcceleratorSeqCombo)
				seq = ((AcceleratorSeqCombo)seq).getConstituents().get(0);
			probe.setPosition(accelerator.getPosition(seq));
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}
	/**Initializes beamlines and sets model mode.*/
	private void initBeamlines()
	{
		beamlines = new ArrayList<>();
		beamlines.add(DEFAULT_BEAMLINE_TEXT);
		for (AcceleratorSeq seq : accelerator.getComboSequences()) {
			beamlines.add(seq.getId());
		}
		for (AcceleratorSeq seq : accelerator.getSequences()) {
			beamlines.add(seq.getId());
		}
		defaultBeamline = beamlines.get(1);
		setModelMode(defaultBeamline);
	}
	
	public List<String> getBeamlines() {	
		return beamlines;
	}

	public String getModelMode() {
		return modelMode;
	}
	
}