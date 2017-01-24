package se.lu.esss.ics.jels;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.LatticeXmlWriter;
import xal.model.xml.ParsingException;
import xal.model.xml.ProbeXmlParser;
import xal.model.xml.ProbeXmlWriter;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.xml.XmlDataAdaptor;


public class JElsDemo {


	public static void main(String[] args) throws InstantiationException, ModelException {
		if (args.length > 0 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
			System.out.println("Usage: [-a] [combo sequence] [accelerator file main.xal] [probe file]");
			System.out.println("	-a	use adaptive tracker");
			System.out.println("	If no combo sequence is given, the first one is choosen");
			return;
		}
		
		Accelerator accelerator;
		AcceleratorSeq sequence;
		EnvelopeProbe probe;
		String comboSequence = null;
		
		boolean adaptiveTracker = false;
		
		int argspos = 0;
		
		// Option -a
		if (args.length > argspos && args[argspos].equals("-a")) {
			adaptiveTracker = true;
			argspos++;
		}
		
		// combo sequence
		if (args.length > argspos) {
			comboSequence = args[argspos++];
		}

		// path to accelerator
		if (args.length > argspos) {
			accelerator = loadAccelerator(args[argspos++]);
		} else {
			accelerator = loadAccelerator();
		} 
		
		
		// now load the sequence
		if (comboSequence != null) {
			sequence = accelerator.getComboSequence(comboSequence);
			if (sequence == null) {
				sequence = accelerator.getSequence(comboSequence);
			}
			if (sequence == null) {
				System.err.println("No appropriate sequence with name: "+comboSequence);
				return;
			}
		} else {			
			sequence = accelerator.getComboSequences().get(0);
			System.out.println("Selecting combo sequence "+sequence.getId());
		}		
		
		// path to probe
		if (args.length > argspos) {
			probe = loadProbeFromXML(args[argspos++]);
		} else {
			Tracker tracker;
			if (adaptiveTracker)
				tracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);
			else
				tracker = AlgorithmFactory.createEnvelopeTracker(sequence);
			probe = ProbeFactory.getEnvelopeProbe(sequence, tracker);
		}
		
		run(sequence, probe);
	}

	/*public static EnvelopeProbe defaultProbe() {
		EnvelopeProbe probe = setupOpenXALProbe(); // OpenXAL probe & algorithm
		//EnvelopeProbe probe = setupElsProbe(); // ELS probe & algorithm
						
		// Setup of initial parameters
		setupInitialParameters(probe);
        //loadInitialParameters(probe, "mebt-initial-state.xml");		
		
		return probe;
	}*/	
		
	public static void run(AcceleratorSeq sequence, EnvelopeProbe probe) throws ModelException {
		// Setup (pick combination  of elements and algorithm)
		//ElementMapping elementMapping = JElsElementMapping.getInstance(); // JELS element mapping - transfer matrices in OpenXal reference frame
		//ElementMapping elementMapping = ElsElementMapping.getInstance(); // ELS element mapping - transfer matrices in TraceWin reference frame
		//ElementMapping elementMapping = DefaultElementMapping.getInstance(); // OpenXAL element mapping - transfer matrices in OpenXal reference frame
				
		
		// Generates lattice from SMF accelerator
		Scenario scenario = Scenario.newScenarioFor(sequence);//, elementMapping);		
		scenario.setProbe(probe);			
						
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
		
		// Manually changing magnet field
		/*AcceleratorNode mag = sequence.getNodeWithId("MEBT-PBO_QV-1");
		scenario.setModelInput(mag, ElectromagnetPropertyAccessor.PROPERTY_FIELD, 15);	
		scenario.resync();*/
				
		// Manually changing rfcavity parameters
		/*AcceleratorNode rfcavity = sequence.getNodeWithId("DTL-TANK-1-CELL-1");
		scenario.setModelInput(rfcavity, 
		    RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, 17145); //was 17145.76
		scenario.setModelInput(rfcavity, 
		    RfCavityPropertyAccessor.PROPERTY_PHASE, -34); // was -35
		scenario.resync();  // all rfgaps are updated*/		
		
		// Outputting lattice elements
		//saveLattice(scenario.getLattice(), "lattice.xml");
						
		// Running simulation
		//scenario.setStartElementId("BEGIN_mebt");
		scenario.run();
		
		
		// Getting results
		Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();
		
		EnvelopeProbeState ps = trajectory.stateAtPosition(0); // I was forced to cast from ProbeState to EnvelopeProbeState		
		Iterator<EnvelopeProbeState> iterState= trajectory.stateIterator();
     
		/*ProbeState ps1 = trajectory.stateForElement("MEBT-PBI_BPM-2a");
		ProbeState ps2 = trajectory.stateAtPosition(101.2);*/
		
		int ns= trajectory.numStates();
		
		double [] s = new double[ns];
		double [] bx = new double[ns];
		double [] sx = new double[ns];
		double [] by = new double[ns];
		double [] bz = new double[ns];
		double [] w = new double[ns];
        //BasicGraphData myDataX = new BasicGraphData();
		int i = 0;
    	while (iterState.hasNext())
     	{
    		ps = iterState.next();
		        
		    s[i]= ps.getPosition() ;
		    String elem=ps.getElementId() ;
		    Twiss[] twiss;	
		   
			twiss = ps.twissParameters();			
			
		    bx[i] = twiss[0].getBeta();
		    sx[i] = twiss[0].getEnvelopeRadius();
		    by[i] = twiss[1].getBeta();
		    bz[i] = twiss[2].getBeta();
		    w[i] = ps.getKineticEnergy()/1.e6;
		    PhaseVector mean = ps.phaseMean();
			
		    System.out.printf("%E %E %E %E %E %E %E %E %E %E %E %E %E %E %E %E %E %E %s\n", ps.getPosition(), ps.getGamma()-1, 		
					twiss[0].getEnvelopeRadius(),
					Math.sqrt(twiss[0].getGamma()*twiss[0].getEmittance()),
					twiss[1].getEnvelopeRadius(),
					Math.sqrt(twiss[1].getGamma()*twiss[1].getEmittance()),
					twiss[2].getEnvelopeRadius()/ps.getGamma(),
					Math.sqrt(twiss[2].getGamma()*twiss[2].getEmittance())*ps.getGamma(),
					Math.sqrt(twiss[2].getGamma()*twiss[2].getEmittance())/ps.getGamma(),
				
					mean.getx(),
					mean.getxp(),
					mean.gety(),
					mean.getyp(),
					mean.getz(),
					mean.getzp(),
				
					twiss[0].getBeta(),
					twiss[1].getBeta(),
					ps.getTime(),
		    		ps.getElementId());
		
		    /*if (ps.getElementId().startsWith("BEGIN")) {
		    	String sec = ps.getElementId().substring(6);
		    	AcceleratorNode node = sequence.getNodeWithId(sec);
		    	if (node instanceof AcceleratorSeq && ((AcceleratorSeq)node).getParent() instanceof Accelerator) {	
			    	char[] axis = new char[]{'x','y','z'};
			    	for (int j=0; j<3; j++) {
			    		System.out.printf("<record name=\"%s\" coordinate=\"%c\" alpha=\"%E\" beta=\"%E\" emittance=\"%E\"/>\n", 
			    				sec, axis[j], twiss[j].getAlpha(), twiss[j].getBeta(), twiss[j].getEmittance());
			    	}
			    	System.out.printf("<record name=\"%s\" species=\"PROTON\" W=\"%E\"/>\n", 
		    				sec, ps.getKineticEnergy());
		    	}
		    }*/
		    i=i+1;
		}
    	
    	
    	/*final JFrame frame = new JFrame();
    	FunctionGraphsJPanel plot = new FunctionGraphsJPanel();
     	plot.setVisible(true);
  	    myDataX.addPoint(s, sx);
     	plot.addGraphData(myDataX);
     	plot.setAxisNames("position", "sigma_x");
     	plot.refreshGraphJPanel();
     	frame.setSize(500,500);
        frame.addMouseListener(new MouseAdapter(){
             public void mouseClicked(MouseEvent e) {
                 System.out.println(e.getPoint().getX());
                 System.out.println(e.getPoint().getY());
             }
        });
        frame.add(plot);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);*/      	
	}

	/*private static EnvelopeProbe setupOpenXALProbe() {
		EnvelopeTracker envelopeTracker = new EnvelopeTracker();			
		envelopeTracker.setRfGapPhaseCalculation(true);
		envelopeTracker.setUseSpacecharge(true);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.01);
		envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_ALWAYS);
		
		EnvelopeProbe envelopeProbe = new EnvelopeProbe();
		envelopeProbe.setAlgorithm(envelopeTracker);		
		
		return envelopeProbe;
	}
	
	public static ElsProbe setupElsProbe() {
		// Envelope probe and tracker
		ElsTracker elsTracker = new ElsTracker();			
		elsTracker.setRfGapPhaseCalculation(false);
		//envelopeTracker.setUseSpacecharge(false);
		//envelopeTracker.setEmittanceGrowth(false);
		//envelopeTracker.setStepSize(0.004);
		elsTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);
		
		ElsProbe elsProbe = new ElsProbe();
		elsProbe.setAlgorithm(elsTracker);
		
		return elsProbe;
	}


	public static void setupInitialParameters(EnvelopeProbe probe) {
		probe.setSpeciesCharge(1);
		probe.setSpeciesRestEnergy(9.3827202900E8);
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
	}*/

	public static void loadInitialParameters(EnvelopeProbe probe, String file) {
		XmlDataAdaptor document = XmlDataAdaptor.adaptorForUrl( JElsDemo.class.getResource(file).toString(), false);
        EnvelopeProbeState state = new EnvelopeProbeState();
        state.load(document.childAdaptor("state"));
        probe.applyState(state);     
	}
	
	public static EnvelopeProbe loadProbeFromXML(String file) {
		try {			
			EnvelopeProbe probe = (EnvelopeProbe)ProbeXmlParser.parse(file);
			return probe;
		} catch (ParsingException e1) {
			e1.printStackTrace();
		}		
		return null;
	}

	static void saveProbe(EnvelopeProbe probe, String file) {		
		try {
			//probe.setSaveTwissFlag(true);
			ProbeXmlWriter.writeXml(probe, file);			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	static void saveLattice(Lattice lattice, String file) {		
		lattice.setAuthor(System.getProperty("user.name", "ESS"));		
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}

	private static Accelerator loadAccelerator() {
		System.out.println("Loading accelerator from: "+XMLDataManager.defaultPath());
		/* Loading SMF model */				
		Accelerator accelerator = XMLDataManager.loadDefaultAccelerator();
				
		
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 			
		return accelerator;
		/* Selects a section */
		/*List<AcceleratorSeq> seqs  = accelerator.getAllSeqs();
		for (AcceleratorSeq seq : seqs)
			if ("mebt".equals(seq.getId())) return seq;
		return null;*/
		/*
		AcceleratorSeq sequence = accelerator.findSequence( "mebt" );
		//AcceleratorSeq sequence = accelerator.findSequence( "HEBT2" );				
		return sequence;
*/		
		/* We can instead build lattice for the whole accelerator */
		//return accelerator;
	}
	
	private static Accelerator loadAccelerator(String path) {
		/* Loading SMF model */
		System.out.println("Loading accelerator from: "+path);
		Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(new File(path).toURI().toString());
		
		
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the accelerator.");
		} 			
		return accelerator;
	}
}
