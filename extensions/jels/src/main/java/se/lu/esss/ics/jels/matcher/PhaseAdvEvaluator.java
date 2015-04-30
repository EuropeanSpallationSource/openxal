package se.lu.esss.ics.jels.matcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xal.extension.solver.Objective;
import xal.extension.solver.Trial;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.smf.Accelerator;
import xal.tools.beam.Twiss;

public class PhaseAdvEvaluator extends OnlineModelEvaluator {
	public PhaseAdvEvaluator(Accelerator accelerator, EnvelopeProbe probe, InitialBeamParameters initialParameters) {
		super(accelerator, probe, initialParameters);
		objectives.add(new Objective("vcr") {
			@Override
			public double satisfaction(double value) {
				return 1 - value;
			}});	
	}
	
	public static class PhaseAdv {
		private double[] adv = new double[3];
		private double pos;
		
		public PhaseAdv(double pos0, double pos1, double[] phi0, double[] phi1)
		{
			for (int i=0; i<3; i++) {
				adv[i] = (phi1[i] - phi0[i]) / (pos1 - pos0) * 180. / Math.PI;
			}
			pos = pos1;
		}
		
		public double getPos() {
			return pos;
		}
		
		public double getPhaseAdv(int i) {
			return adv[i];
		}
		
	}
	
	public List<PhaseAdv> getPhases(Trajectory trajectory) {
		List<PhaseAdv> phaseAdv = new ArrayList<>();
		
		Iterator<ProbeState> i = trajectory.stateIterator();
		
		double pos = 0;
		double[] phi = new double[3];
		
		double posLp0 = 0;
		double[] phiLp0 = null;
		
		while (i.hasNext()) {
			ProbeState ps = i.next();	
			Twiss[] t = ((EnvelopeProbeState)ps).twissParameters();
	
			for (int k = 0; k<3; k++) phi[k] += (ps.getPosition() - pos) / (t[k].getBeta());
			pos = ps.getPosition();
		    
		    if (ps.getElementId().toUpperCase().startsWith("LATTICE-POINT")) {
		    	if (phiLp0 != null)
		    		phaseAdv.add(new PhaseAdv(posLp0, ps.getPosition(), phiLp0, phi));
		    	phiLp0 = phi.clone();
		    	posLp0 = ps.getPosition();
			}
		    if (ps.getElementId().toUpperCase().startsWith("LATTICE-END")) phiLp0 = null;
		}
		
		return phaseAdv;
	}
	
	
	public double calcVcr(List<PhaseAdv> s) {
		double vcr = 0.;
		PhaseAdv s0 = s.get(0);
		PhaseAdv s1 = s.get(1);
		
		for (int i=2; i<s.size(); i++) {
			PhaseAdv s2 = s.get(i);
			for (int j=0; j<3; j++) {
				vcr += (s0.getPhaseAdv(j) - 2*s1.getPhaseAdv(j) + s2.getPhaseAdv(j)) / s1.getPhaseAdv(j);   
			}
			s0 = s1;
			s1 = s2;
		}
		return vcr / 3. / (s.size()+1);
	}


	@Override
	public void evaluate(Trial trial) {
		initialParameters.setupInitialParameters(probe, trial.getTrialPoint());
		
		try {
			scenario.run();
		} catch (ModelException e) {
			e.printStackTrace();
		}
		
		Trajectory trajectory = probe.getTrajectory();

		trial.setScore(objectives.get(0), calcVcr(getPhases(trajectory)));
	}
	
}
