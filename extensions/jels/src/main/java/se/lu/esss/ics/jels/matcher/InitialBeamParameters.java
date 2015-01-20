package se.lu.esss.ics.jels.matcher;

import java.util.Arrays;
import java.util.List;

import xal.extension.solver.TrialPoint;
import xal.extension.solver.Variable;
import xal.model.probe.EnvelopeProbe;
import xal.tools.beam.Twiss;

public class InitialBeamParameters {
	//0.2011591303 0.3583795833 0.9771896612
	//-0.1083747827 -0.3119539455 -0.0341951600
	
	final Variable 
		/*ax = new Variable("ax", -0.051805615, -0.15, -0.04),  // initial conditions by Royichi
		bx = new Variable("bx", 0.20954703, 0.1, 0.4),
//		ex = new Variable("ex", 0.25288, 0.1, 0.4),
		ay = new Variable("ay", -0.30984478, -0.4, -0.2),
		by = new Variable("by", 0.37074849, 0.2, 0.4),
//		ey = new Variable("ey",  0.251694, 0.2, 0.3),
		az = new Variable("az", -0.48130325, -0.6,-0.4),
		bz = new Variable("bz", 0.92564505, 0.8, 1);*/
//		ez = new Variable("ez", 0.3615731, 0.3, 0.5),
		//E = new Variable("E", 3.6217853e6, 3.5e6, 3.7e6);
	
			ax = new Variable("ax", -0.051952048, -0.15, -0.04),         // initial conditions by Mamad
			bx = new Variable("bx",0.20962859, 0.1, 0.4),
			//ex = new Variable("ex", 0.25288, 0.1, 0.4),
			ay = new Variable("ay", -0.31155119, -0.4, -0.2),
			by = new Variable("by", 0.37226081, 0.2, 0.4),
			//ey = new Variable("ey",  0.251694, 0.2, 0.3),
			az = new Variable("az", -0.48513031, -0.6,-0.4),
			bz = new Variable("bz", 0.92578192, 0.8, 1);
			//ez = new Variable("ez", 0.3615731, 0.3, 0.5),
			//E = new Variable("E", 3.6217853e6, 3.5e6, 3.7e6);
	
	/*ax = new Variable("ax", -0.1, -0.15, -0.04),         // initial off conditions
	bx = new Variable("bx",0.2, 0.1, 0.4),
	//ex = new Variable("ex", 0.25288, 0.1, 0.4),
	ay = new Variable("ay", -0.3, -0.4, -0.2),
	by = new Variable("by", 0.3, 0.2, 0.4),
	//ey = new Variable("ey",  0.251694, 0.2, 0.3),
	az = new Variable("az", -0.5, -0.6,-0.4),
	bz = new Variable("bz", 0.9, 0.8, 1);
	//ez = new Variable("ez", 0.3615731, 0.3, 0.5),
	//E = new Variable("E", 3.6217853e6, 3.5e6, 3.7e6);*/
	
	final double Ex = 0.2529362, Ey = 0.2510271, Ez = 0.3599253, e0 =  3.6218151e6;
	//final List<Variable> variables = Arrays.asList(ax,bx,ex,ay,by,ey,az,bz,ez,E);
	 
	
	
	final List<Variable> variables = Arrays.asList(ax,bx,ay,by,az,bz);
	
	
	public void setupInitialParameters(EnvelopeProbe probe,
			TrialPoint trialPoint) {
		probe.reset();
		
		double Ax = trialPoint.getValue(ax),
				Bx = trialPoint.getValue(bx),
		//		Ex = trialPoint.getValue(ex),
				Ay = trialPoint.getValue(ay),
				By = trialPoint.getValue(by),
		//		Ey = trialPoint.getValue(ey),
				Az = trialPoint.getValue(az),
				Bz = trialPoint.getValue(bz);
		//		Ez = trialPoint.getValue(ez),
		//		e0 = trialPoint.getValue(E);
		
		probe.setSpeciesCharge(1);
		probe.setSpeciesRestEnergy(9.3827202900E8);
		//elsProbe.setSpeciesRestEnergy(9.38272013e8);	
		probe.setKineticEnergy(e0);//energy
		probe.setPosition(0.0);
		probe.setTime(0.0);		
				
		double beta_gamma = probe.getBeta() * probe.getGamma();
	
		
		probe.initFromTwiss(new Twiss[]{new Twiss(Ax,Bx,Ex*1e-6 / beta_gamma),
										  new Twiss(Ay,By,Ey*1e-6 / beta_gamma),
										  new Twiss(Az,Bz,Ez*1e-6 / beta_gamma)});
		probe.setBeamCurrent(62.5e-3);
		probe.setBunchFrequency(352.21e6); 	
		
	}


	public List<Variable> getVariables() {
		return variables;
	}
}
