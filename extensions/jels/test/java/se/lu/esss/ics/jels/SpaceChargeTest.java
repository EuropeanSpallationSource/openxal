package se.lu.esss.ics.jels;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;

public class SpaceChargeTest {
	
	private static final String RESULTS_DIR = "temp";
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		new File(RESULTS_DIR).mkdir();
	}
	
	//@Test
	public void driftTest() throws ModelException, IOException
	{
		final double frequency = 4.025e8;
		EnvelopeProbe probe = TestCommon.setupOpenXALProbe2( 3e6, frequency, 30e-3); 
		((EnvelopeTracker)probe.getAlgorithm()).setProbeUpdatePolicy(Tracker.UPDATE_ALWAYS);
		AcceleratorSeq sequence = DriftTest.drift(500, 0., 0.);
		Accelerator acc = new Accelerator();
		acc.addNode(sequence);
		acc.setElementMapping(JElsElementMapping.getInstance());
		double dataOX[][] = GeneralTest.run(probe, sequence);
		GeneralTest.saveResults(RESULTS_DIR + "/openxal.drift500.txt", dataOX);
	}
	
	//@Test
	public void meterTest() throws ModelException, IOException
	{
		for (int i : new int[]{10,20,50,100,1000}) {
			final double frequency = 4.025e8;
			EnvelopeProbe probe = TestCommon.setupOpenXALProbe2( 3e6, frequency, 30e-3); 
			((EnvelopeTracker)probe.getAlgorithm()).setProbeUpdatePolicy(Tracker.UPDATE_ALWAYS);
			((EnvelopeTracker)probe.getAlgorithm()).setStepSize(1./i);
			double dataOX[][] = GeneralTest.run(probe, XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("test/main.xal").toString()));
			GeneralTest.saveResults(RESULTS_DIR + "/openxal.s"+i+".txt", dataOX);
		}
	}
	
	
	@Test
	public void dgdTest() throws ModelException, IOException
	{
		final double frequency = 4.025e8;
		EnvelopeProbe probe = TestCommon.setupOpenXALProbe2( 3e6, frequency, 30e-3); 
		((EnvelopeTracker)probe.getAlgorithm()).setProbeUpdatePolicy(Tracker.UPDATE_ALWAYS);
		((EnvelopeTracker)probe.getAlgorithm()).setStepSize(1./100.);
		
		//MEBT-DRFT-9: DRIFT 63 15
		//MEBT-RF_Cav-1: GAP 124000 -90 14.5 0
		//MEBT-DRFT-10: DRIFT 63 15
		
		Accelerator acc = new Accelerator();
		AcceleratorSeq seq = GapTest.gap(352.21e6, 124000, -90, 14.5, 0, 0, 0, 0, 0, 0, 0);
		seq.setPosition(0.063);
		acc.addNode(seq);
		acc.setLength(0.126);
		acc.setElementMapping(JElsElementMapping.getInstance());
		double dataOX[][] = GeneralTest.run(probe, acc);
		GeneralTest.saveResults(RESULTS_DIR + "/openxal.dgd6.txt", dataOX);
	}
}
