package edu.stanford.lcls.xal.model;

import edu.stanford.slac.Message.Message;
import xal.model.ModelException;
import xal.sim.scenario.Scenario;

public class RunModelConfigurationExtant extends RunModelConfiguration {
	private String emitNode;
	private boolean refMode;

	/** Wirescanner to use for initial parameters (via backpropagation) */

	/** Use measurement data (true) or design data (false) on that wirescanner */
	public RunModelConfigurationExtant(String refID, boolean useDesignRefInd) {
		this.refMode = useDesignRefInd;
		this.emitNode = refID;
	}
	
	@Override
	public int getRunModelMethod() {
		return 1;
	}

	@Override
	public void initialize(Scenario scenario) throws ModelException {
		//TODO Connect all channels for model use.  This may have to be done earlier, not here.
		
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
		scenario.resync();
		// System.out.println("Use reference pt.: " + emitNode +
		// " for Twiss.");
		Message.info("Use reference pt.: " + emitNode +
				 " for Twiss back propagate.");
		// do Twiss back propagate
	}

}
