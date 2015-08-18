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

public class RunModel {	
	private Accelerator accelerator;
	private Scenario scenario;
	protected String modelMode;
	private EnvelopeProbe probe;
	private List<String> beamlines;
	private String defaultBeamline;
	
	public static final String DEFAULT_BEAMLINE_TEXT = "DEFAULT";

	
	public RunModel(Accelerator acc) { 
		accelerator = acc;
			
		initBeamlines();
		resetProbe();				
	}

	public void run(RunModelConfiguration config) throws ModelException {
	    try {
            scenario = Scenario.newScenarioFor(getSequence()); //, elementMapping);
        } catch (ModelException e) {
            e.printStackTrace();
        }
	    
		config.initialize(scenario);
		
		scenario.setProbe(probe.copy());
		
		scenario.run();
	}

	public AcceleratorSeq getSequence()
	{
		AcceleratorSeq seq = accelerator.getSequence(modelMode);
		if (seq == null) {
			seq = accelerator.getComboSequence(modelMode);
		}
		return seq;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public EnvelopeProbe getProbe() {
		return probe;
	}

	/** beamline selection */
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