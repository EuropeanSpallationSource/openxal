package edu.stanford.lcls.xal.model;

import xal.model.ModelException;
import xal.sim.scenario.Scenario;

public abstract class RunModelConfiguration {
	public abstract int getRunModelMethod();
	public abstract void initialize(Scenario scenario) throws ModelException;
}
