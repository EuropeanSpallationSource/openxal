package se.lu.esss.ics.jels.matcher;

import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.tools.annotation.AProperty.Units;

public class MatcherConfiguration {
	private ModelEvaluatorEnum criteria = ModelEvaluatorEnum.MinimiseOscillations;
	
	private double timeLimit = 1.;
	
	private InitialBeamParameters initialBeamParameters = new InitialBeamParameters();
	
	private boolean showScore;
	private boolean showSimulation;
	
	private Probe<?> probe;
	
	public MatcherConfiguration(Probe<?> probe) {
		this.probe = probe;
	}

	public ModelEvaluatorEnum getCriteria() {
		return criteria;
	}
	
	public void setCriteria(ModelEvaluatorEnum criteria) {
		this.criteria = criteria;
	}	
	
	@Units("min")
	public double getTimeLimit() {
		return timeLimit;
	}
	
	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}
	
	public InitialBeamParameters getInitialBeamParameters() {
		return initialBeamParameters;
	}
	
	public boolean isShowScore() {
		return showScore;
	}
	
	public void setShowScore(boolean showScore) {
		this.showScore = showScore;
	}
	
	public boolean isShowSimulation() {
		return showSimulation;
	}
	
	public void setShowSimulation(boolean showSimulation) {
		this.showSimulation = showSimulation;
	}

	public Probe<?> getProbe() {
		return probe;
	}
}

enum ModelEvaluatorEnum 
{
	MinimiseOscillations(MinimiseOscillationsEvaluator.class), 
	PhaseAdvance(PhaseAdvEvaluator.class);
	
	private  Class<? extends OnlineModelEvaluator> c;
	
	ModelEvaluatorEnum(Class<? extends OnlineModelEvaluator> c) {
		this.c = c;
	}
}
