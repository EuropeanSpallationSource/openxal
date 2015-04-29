package se.lu.esss.ics.jels.matcher;

import xal.tools.annotation.AProperty.Units;

public class MatcherConfiguration {
	private Class<? extends OnlineModelEvaluator> criteria = MinimiseOscillationsEvaluator.class;
	
	private double timeLimit = 1.;
	
	private InitialBeamParameters initialBeamParameters = new InitialBeamParameters();
	
	private boolean showScore;
	private boolean showSimulation;
	
	public Class<? extends OnlineModelEvaluator> getCriteria() {
		return criteria;
	}
	public void setCriteria(Class<? extends OnlineModelEvaluator> criteria) {
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
}
