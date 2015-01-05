package se.lu.esss.ics.jels.matcher;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import xal.extension.solver.Evaluator;
import xal.extension.solver.Objective;
import xal.extension.solver.TrialPoint;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.Accelerator;
import xal.tools.beam.Twiss;

public abstract class OnlineModelEvaluator implements Evaluator {
	protected InitialBeamParameters initialParameters;
	protected EnvelopeProbe probe = setupOpenXALProbe(); // OpenXAL probe & algorithm
	//EnvelopeProbe probe = setupElsProbe(); // ELS probe & algorithm
					
	// Setup of initial parameters
	protected Scenario scenario; 
	protected List<Objective> objectives = new ArrayList<>();
	
	public OnlineModelEvaluator(Accelerator accelerator, InitialBeamParameters initialParameters) {
		this.initialParameters = initialParameters;
		try {
			scenario = Scenario.newScenarioFor(accelerator);
		} catch (ModelException e1) {
			e1.printStackTrace();
		}	
		
		scenario.setProbe(probe);			
		
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		try {
			scenario.resync();
		} catch (SynchronizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private EnvelopeProbe setupOpenXALProbe() {
		EnvelopeTracker envelopeTracker = new EnvelopeTracker();			
		envelopeTracker.setRfGapPhaseCalculation(true);
		envelopeTracker.setUseSpacecharge(true);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.1);
		envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);
		
		EnvelopeProbe envelopeProbe = new EnvelopeProbe();
		envelopeProbe.setAlgorithm(envelopeTracker);		
		
		return envelopeProbe;
	}
		
	
	
	public void printSolution(String file, TrialPoint trial) {	
		
		initialParameters.setupInitialParameters(probe, trial);
	
		try {
			scenario.run();
		} catch (ModelException e) {
			e.printStackTrace();
		}
		
		//saveLattice(scenario.getLattice(),file);
		
		try {
			Formatter f1  = new Formatter(file+".dat", "UTF8", Locale.ENGLISH);
			Formatter f2  = new Formatter(file+".phi.dat", "UTF8", Locale.ENGLISH);
			
			Trajectory trajectory = probe.getTrajectory();

			Iterator<ProbeState> i = trajectory.stateIterator();
			
			double[] phix = new double[3];
			double pos0 = 0;
			double[] phil = null;
			double posl = 0;
			
			while (i.hasNext()) {
				ProbeState ps = i.next();
				
				Twiss[] t2 = ((EnvelopeProbeState)ps).twissParameters();

				for (int k = 0; k<3; k++) phix[k] += (ps.getPosition() - pos0) / (t2[k].getBeta());
				pos0 = ps.getPosition();
				
			    f1.format("%E %E %E %E %E %E %E %E %E\n", ps.getPosition(), ps.getGamma()-1, 		
							t2[0].getEnvelopeRadius(),
							Math.sqrt(t2[0].getGamma()*t2[0].getEmittance()),
							t2[1].getEnvelopeRadius(),
							Math.sqrt(t2[1].getGamma()*t2[1].getEmittance()),
							t2[2].getEnvelopeRadius()/ps.getGamma(),
							Math.sqrt(t2[2].getGamma()*t2[2].getEmittance())*ps.getGamma(),
							Math.sqrt(t2[2].getGamma()*t2[2].getEmittance())/ps.getGamma());
			    
			    
			    if (ps.getElementId().toUpperCase().startsWith("LATTICE-POINT")) {
			    	if (phil != null)
				    	f2.format("%E %E %E %E\n", ps.getPosition(), 
				    			(phix[0] - phil[0]) / (ps.getPosition() - posl) * 180. / Math.PI,
				    			(phix[1] - phil[1]) / (ps.getPosition() - posl) * 180. / Math.PI,
				    			(phix[2] - phil[2]) / (ps.getPosition() - posl) * 180. / Math.PI);
			    	phil = phix.clone();
			    	posl = ps.getPosition();
				}
			    if (ps.getElementId().toUpperCase().startsWith("LATTICE-END")) phil = null;
			}
			
			f1.close();
			f2.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public List<Objective> getObjectives() {
		return objectives;
	}
}
