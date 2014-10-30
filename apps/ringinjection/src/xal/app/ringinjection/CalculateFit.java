/*
 * CalculateFit.java
 *
 * Created on April 1, 2004 
 */

package xal.app.ringinjection;
import java.util.List;

import xal.ca.Channel;
import xal.extension.fit.DampedSinusoidFit;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeqCombo;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.calc.CalculationsOnRings;
import xal.tools.math.r3.R3;

/**
 * Performs the fit for the turn-by-turn signal on a BPM.  Records the 
 * fit parameters in the BPMAgent class.
 * @author  cp3
 */

public class CalculateFit{
	
    protected Channel channel;
    protected BpmAgent localagent;
    
    private Accelerator accl = new Accelerator();
    private AcceleratorSeqCombo seq;
    private TransferMapProbe probe;
    private Scenario scenario;
    private Trajectory<TransferMapState> traj;
    private CalculationsOnRings simRingCalcEngine;
    private String syncstate;
	
    double xtune;
    double xphase;
    double xslope;
    double xamp;
    double xoffset;
    
    double ytune;
    double yphase;
    double yslope;
    double yamp; 
    double yoffset;
    
    double xamp_err;
    double yamp_err;
    double xphase_err;
    double yphase_err;
    
    
    GenDocument doc;
    /** Creates new CalculateFit*/
    public CalculateFit(GenDocument aDocument, String latticestate) {
		doc = aDocument;
		syncstate = latticestate;
		setupModel();
    }
    
    public void bpmXFit(BpmAgent bpmagent, int points) {
		int i;
		
		double[] iarr;
		double[] xarr;
		
		localagent = bpmagent;
		
		iarr = new double[points];
		xarr = new double[points];
		if(localagent.isConnected()){
			System.arraycopy(localagent.getXAvgTBTArray(), 0, xarr, 0, points);
		}
		else{
			System.out.println("BPM " + localagent + " is not connected.");
		}
		
		for(i=0; i<points; i++){
			iarr[i]=(new Integer(i)).doubleValue();
		}
		
		
		final DampedSinusoidFit sineFit = new DampedSinusoidFit( xarr, points );
		sineFit.solveWithNoiseMaxEvaluations( 0.0, 1000 );	// solve using the default noise estimation

		xtune = sineFit.getFrequency();
		xphase = sineFit.getCosineLikePhase();
		xslope = - sineFit.getGrowthRate();
		xamp = sineFit.getAmplitude();
		xoffset = sineFit.getOffset();

		final double xtune_err = Math.sqrt( sineFit.getInitialFrequencyVariance() );
		final double xslope_err = Math.sqrt( sineFit.getInitialGrowthRateVariance() );
		final double xoffset_err = Math.sqrt( sineFit.getInitialOffsetVariance() );

		System.out.println("\nHorizontal fit results for BPM " + localagent.name());
		System.out.println("Tune = " + xtune + " +- " + xtune_err );
		System.out.println("Phase = " + xphase + " +- ?" );
		System.out.println("Slope = " + xslope + " +- " + xslope_err );
		System.out.println("Amp = " + xamp + " +- ?" );
		System.out.println("Offset = " + xoffset + " +- " + xoffset_err + "\n");
		

		//Store the fit data and results in the bpm agent class.
		
		double[] xfitparams = new double[5];
		xfitparams[0] = xtune;
		xfitparams[1] = xphase;
		xfitparams[2] = xslope;
		xfitparams[3] = xamp;
		xfitparams[4] = xoffset;
		
		localagent.setDataSize(points);
		localagent.saveXTBTData(xarr);  
		localagent.setXTBTFitParams(xfitparams);
		
	}
	
	
	public void bpmYFit(BpmAgent bpmagent, int points) {
		int i;
		
		double[] iarr;
		double[] yarr;
		
		localagent = bpmagent;
		
		iarr = new double[points];
		yarr = new double[points];
		System.arraycopy(localagent.getYAvgTBTArray(), 0, yarr, 0, points);
		
		for(i=0; i<points; i++){
			iarr[i]=(new Integer(i)).doubleValue();
			//yarr_err[i] = 0.001;
			//System.out.println(iarr[i] + " " + yarr[i]);
		}
		
		final DampedSinusoidFit sineFit = new DampedSinusoidFit( yarr, points );
		sineFit.solveWithNoiseMaxEvaluations( 0.0, 1000 );	// solve using the default noise estimation

		ytune = sineFit.getFrequency();
		yphase = sineFit.getCosineLikePhase();
		yslope = - sineFit.getGrowthRate();
		yamp = sineFit.getAmplitude();
		yoffset = sineFit.getOffset();

		final double ytune_err = Math.sqrt( sineFit.getInitialFrequencyVariance() );
		final double yslope_err = Math.sqrt( sineFit.getInitialGrowthRateVariance() );
		final double yoffset_err = Math.sqrt( sineFit.getInitialOffsetVariance() );

		System.out.println("\nVertical fit results for BPM " + localagent.name());
		System.out.println("Tune = " + ytune + " +- " + ytune_err );
		System.out.println("Phase = " + yphase + " +- ?" );
		System.out.println("Slope = " + yslope + " +- " + yslope_err );
		System.out.println("Amp = " + yamp + " +- ?" );
		System.out.println("Offset = " + yoffset + " +- " + yoffset_err + "\n");

		//Store the fit data and results in the bpm agent class.
		
		double[] yfitparams = new double[5];
		yfitparams[0] = ytune;
		yfitparams[1] = yphase;
		yfitparams[2] = yslope;
		yfitparams[3] = yamp;
		yfitparams[4] = yoffset;
		
		localagent.setDataSize(points);
		localagent.saveYTBTData(yarr);  
		localagent.setYTBTFitParams(yfitparams);
		
		getFoilParams();
		
	}
	
	
    public void setupModel(){	    
		accl = doc.getAccelerator();
		seq = accl.getComboSequence("Ring");
		
		AcceleratorNode smfFoil = seq.getNodeWithId("Ring_Inj:Foil");  // TODO CKA - never used
		
		try{
		    // CKA 10/29/2014 - Open XAL must use AlgorithmFactory
//			TransferMapTracker tracker = new TransferMapTracker();
		    TransferMapTracker tracker = AlgorithmFactory.createTransferMapTracker(seq);
			System.out.println("TransferMapTracker is " + tracker + " " + seq.getId());
			
			probe = ProbeFactory.getTransferMapProbe(seq, tracker);
			probe.initialize();      // CKA 10/29/2014
			
			scenario = Scenario.newScenarioFor(seq);
			scenario.setProbe(probe);
			
			if(syncstate == "Live"){
				scenario.setSynchronizationMode( Scenario.SYNC_MODE_RF_DESIGN );
			}
			else{
				scenario.setSynchronizationMode( Scenario.SYNC_MODE_DESIGN );
			}
			
			//scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
			
			// CKA 10/30/2014 - Must simulate the entire ring then choose location
			//   of ring parameter calculations.  Cannot start within the ring.
//			scenario.setStartElementId("Ring_Inj:Foil");
			
			scenario.resync();
			scenario.run();
			traj = probe.getTrajectory();
			simRingCalcEngine = new CalculationsOnRings( traj );
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
    }
    
    
    void getFoilParams(){
		
     	//ProbeState injstate = traj.stateForElement("Ring_Inj:Foil");
		double position = scenario.getPositionRelativeToStart(0.0);               // TODO CKA - never used
		//TransferMapState injstate = (TransferMapState)traj.statesInPositionRange(position - 0.00001, position + 0.00001)[0];
		TransferMapState injstate = traj.stateForElement("Ring_Inj:Foil");
		Twiss[] injtwiss = simRingCalcEngine.computeTwissParameters( injstate );
		R3 injphase = simRingCalcEngine.computeBetatronPhase( injstate );         // TODO CKA - never used
		PhaseVector injorbit = simRingCalcEngine.computeFixedOrbit( injstate );   // TODO CKA - never used
		double beta_x_i = injtwiss[0].getBeta();
		double alpha_x_i = injtwiss[0].getAlpha();
		double beta_y_i = injtwiss[1].getBeta(); 
		double alpha_y_i = injtwiss[1].getAlpha();
		
		//System.out.println("At position " + position + ", beta x, alpha_x, beta y, and alphay are "+ beta_x_i + " " + alpha_x_i + " " + beta_y_i + " " + alpha_y_i + "\n");

		// CKA - let's make this readable for debugging
//        TransferMapState localstate = traj.statesForElement(localagent.getNode().getId()).get(0);
		String    strBpmId = this.localagent.getNode().getId();
		List<TransferMapState> lstStates = this.traj.statesForElement(strBpmId);
		TransferMapState localstate = lstStates.get(0);
		
		Twiss[] localtwiss = simRingCalcEngine.computeTwissParameters( localstate );
		R3 localphase = simRingCalcEngine.computeBetatronPhase( localstate );
		PhaseVector localorbit = simRingCalcEngine.computeFixedOrbit( localstate );  // TODO CKA - never used
		double beta_x = localtwiss[0].getBeta();
		double alpha_x = localtwiss[0].getAlpha();    // TODO CKA - never used
		double beta_y = localtwiss[1].getBeta(); 
		double alpha_y = localtwiss[1].getAlpha();    // TODO CKA - never used
		double phase_x = localphase.getx();
		double phase_y = localphase.gety();
		
		//Calculate the position and angle at the foil.
		
      	double[] xfinalParams = new double[4];
		double[] yfinalParams = new double[4];
		double xarg = phase_x - xphase;
		double yarg = phase_y - yphase;
		
       	xfinalParams[0]=xamp*Math.sqrt(beta_x_i/beta_x)*Math.cos(xarg);
		yfinalParams[0]=yamp*Math.sqrt(beta_y_i/beta_y)*Math.cos(yarg);
		
       	xfinalParams[1]=Math.abs((Math.sqrt(beta_x_i/beta_x)*Math.cos(xarg) * xamp_err + 
								  xamp*Math.sqrt(beta_x_i/beta_x)*Math.sin(xarg) * xphase_err));
		yfinalParams[1]=Math.abs((Math.sqrt(beta_y_i/beta_y)*Math.cos(yarg) * yamp_err + 
								  yamp*Math.sqrt(beta_y_i/beta_y)*Math.sin(yarg) * yphase_err));		
		
       	xfinalParams[2]=xamp/Math.sqrt(beta_x_i*beta_x)*(Math.sin(xarg) - alpha_x_i*Math.cos(xarg));
       	yfinalParams[2]=yamp/Math.sqrt(beta_y_i*beta_y)*(Math.sin(yarg) - alpha_y_i*Math.cos(yarg));
		
		
		double term1 = (Math.sin(xarg) - alpha_x_i*Math.cos(xarg)) * xamp_err;
		double term2 = xamp*(Math.cos(xarg) + alpha_x_i*Math.sin(xarg)) * xphase_err;
		
		xfinalParams[3]=Math.abs(1/(Math.sqrt(beta_x_i*beta_x)) * (term1 - term2));
		
		term1 = (Math.sin(yarg) - alpha_y_i*Math.cos(yarg)) * yamp_err;
		term2 = yamp*(Math.cos(yarg) + alpha_y_i*Math.sin(yarg)) * yphase_err;
		
		yfinalParams[3]=Math.abs(1/(Math.sqrt(beta_y_i*beta_y)) * (term1 - term2));
		
       	localagent.setXFoilResults(xfinalParams);
		localagent.setYFoilResults(yfinalParams);
		
	}
	
	public void changeSyncState(String latticestate){
		syncstate = latticestate;
		setupModel();
		System.out.println("Here1");
	}
	
}




