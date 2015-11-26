package se.lu.esss.ics.jels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.tools.beam.Twiss;
import xal.tools.data.EditContext;

public class ImporterHelpers {
	public static AcceleratorSeqCombo addDefaultComboSeq(Accelerator acc)
	{		
		List<AcceleratorSeq> seqs = acc.getSequences();
		String name;
		if (seqs.size() >= 2) 
			name = seqs.get(0).getId() + "-" + seqs.get(seqs.size()-1).getId();
		else 
			name = "ALL";	
		AcceleratorSeqCombo comboSeq = new AcceleratorSeqCombo(name, seqs);
		acc.addComboSequence(comboSeq);
		return comboSeq;
	}
	
	
	// TODO remove hardcoded probe initialisation
	public static EnvelopeProbe defaultProbe() {
		EnvelopeProbe probe = setupOpenXALProbe(); // OpenXAL probe & algorithm
		//EnvelopeProbe probe = setupElsProbe(); // ELS probe & algorithm
						
		// Setup of initial parameters
		//setupInitialParameters(probe);
        //loadInitialParameters(probe, "mebt-initial-state.xml");		
		
		return probe;
	}
	
	private static EnvelopeProbe setupOpenXALProbe() {
		EnvelopeTracker envelopeTracker = new EnvelopeTracker();			
		envelopeTracker.setRfGapPhaseCalculation(true);
		envelopeTracker.setUseSpacecharge(true);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.1);
		envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_ALWAYS);
		
		EnvelopeProbe envelopeProbe = new EnvelopeProbe();
		envelopeProbe.setAlgorithm(envelopeTracker);		
		
		return envelopeProbe;
	}
	
	public static void setupInitialParameters(EnvelopeProbe probe) {	
		probe.setSpeciesCharge(1);
		probe.setSpeciesRestEnergy(9.3827202900E8);
		probe.setSpeciesName("PROTON");
		//elsProbe.setSpeciesRestEnergy(9.38272013e8);	
		probe.setKineticEnergy(3.6218151e6);//energy
		probe.setPosition(0.0);
		probe.setTime(0.0);		
				
		double beta_gamma = probe.getBeta() * probe.getGamma();
	
		
		probe.initFromTwiss(new Twiss[]{new Twiss(-0.051952048,0.20962859,0.2529362*1e-6 / beta_gamma),
										  new Twiss(-0.31155119,0.37226081,0.2510271*1e-6 / beta_gamma),
										  new Twiss(-0.48513031,0.92578192,0.3599253*1e-6 / beta_gamma)});
		probe.setBeamCurrent(62.5e-3);
		probe.setBunchFrequency(352.21e6); 	
	}
	
	
	public static List<EnvelopeProbeState> simulateInitialValues(EnvelopeProbe probe, AcceleratorSeqCombo seqCombo) throws ModelException
	{
		Scenario scenario = Scenario.newScenarioFor(seqCombo);//, elementMapping);		
		scenario.setProbe(probe);			
						
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
	
		scenario.run();
		
		Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();
		
		List<EnvelopeProbeState> initialValues = new ArrayList<>();
		
		for (AcceleratorSeq seq : seqCombo.getConstituents()) {
		
			EnvelopeProbeState state = trajectory.stateNearestPosition(seqCombo.getPosition(seq)).copy();
			state.setElementId(seq.getId());
			
			initialValues.add(state);		    
		}
		
		probe.reset();
		System.gc();
		
		return initialValues;
	}


	public static void addHardcodedInitialParameters(Accelerator accelerator) {
		if (accelerator != null) {
			AcceleratorSeqCombo comboSeq = addDefaultComboSeq(accelerator);

	        EditContext editContext = new EditContext();
	        
	        EnvelopeProbe probe = defaultProbe();
	        setupInitialParameters(probe);
	        ProbeFactory.createSchema(editContext, probe);
	        accelerator.setEditContext(editContext);
	        try {
	        	List<EnvelopeProbeState> states = simulateInitialValues(probe, comboSeq);
	        	ProbeFactory.storeInitialValues(editContext, states);			
			} catch (ModelException e) {
				System.err.println("Unable to simulate initial states on sequences. Only setting the first sequence.");
				List<EnvelopeProbeState> states = Arrays.asList(probe.cloneCurrentProbeState());
				states.get(0).setElementId(comboSeq.getConstituents().get(0).getId());
				ProbeFactory.storeInitialValues(editContext, states);
			}
		}
		
	}

	/**
	 * Cleans up XML OpenXal produces
	 * @param parent node to clean
	 */
	public static void xmlCleanup(Node parent) {
		NodeList children = parent.getChildNodes();
		NamedNodeMap attrs = parent.getAttributes();
		if (attrs != null) {
			// unneeded attributes 
			if (attrs.getNamedItem("s") != null) attrs.removeNamedItem("s");
			if (attrs.getNamedItem("pid") != null) attrs.removeNamedItem("pid");
			if (attrs.getNamedItem("status") != null) attrs.removeNamedItem("status");
			if (attrs.getNamedItem("eid") != null) attrs.removeNamedItem("eid");

			// remove type="sequence" on sequences - import doesn't work otherwise
			if ("sequence".equals(parent.getNodeName()) && attrs.getNamedItem("type") != null && "sequence".equals(attrs.getNamedItem("type").getNodeValue())) 
				attrs.removeNamedItem("type");

			if ("xdxf".equals(parent.getNodeName())) {
				attrs.removeNamedItem("id");
				attrs.removeNamedItem("len");
				attrs.removeNamedItem("pos");
				attrs.removeNamedItem("type");
			}
		}

		for (int i = 0; i<children.getLength(); )
		{
			Node child = children.item(i);
			attrs = child.getAttributes();

			if ("align".equals(child.getNodeName()) || "twiss".equals(child.getNodeName()))
				// remove twiss and align - not needed
				parent.removeChild(child);
			else if ("channelsuite".equals(child.getNodeName()) && !child.hasChildNodes()) {
				parent.removeChild(child);
			}
			else if ("aperture".equals(child.getNodeName()) && "0.0".equals(attrs.getNamedItem("x").getNodeValue()))
				// remove empty apertures
				parent.removeChild(child);
			else {
				xmlCleanup(child);
				// remove empty attributes
				if ("attributes".equals(child.getNodeName()) && child.getChildNodes().getLength()==0)
				{
					parent.removeChild(child);
				} else
					i++;
			}
		}
	}
}
