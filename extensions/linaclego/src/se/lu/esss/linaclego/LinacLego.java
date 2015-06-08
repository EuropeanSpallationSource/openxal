package se.lu.esss.linaclego;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.openepics.discs.exporters.OpenXALExporter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import se.lu.esss.ics.jels.JElsDemo;
import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import se.lu.esss.linaclego.elements.Drift;
import se.lu.esss.linaclego.elements.Quad;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.ElementMapping;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.Twiss;

public class LinacLego {
	public static Linac load(String sourceFileName) throws JAXBException, SAXException, ParserConfigurationException, MalformedURLException 
	{
		JAXBContext context = JAXBContext.newInstance(Linac.class, Drift.class, Quad.class, FieldProfile.class);
		Unmarshaller um = context.createUnmarshaller();
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setXIncludeAware(true);
		spf.setNamespaceAware(true);
		spf.setValidating(true);
	
		XMLReader xr = spf.newSAXParser().getXMLReader();
		SAXSource source = new SAXSource(xr, new InputSource(sourceFileName));
		Linac ll = um.unmarshal(source, Linac.class).getValue();
		ll.setSource(new URL(sourceFileName)); // source is set so that field profile inside fieldmaps can be loaded
		
		return ll;
	}
	
	public static Accelerator loadAcceleator(String sourceFileName) {
		return loadAcceleator(sourceFileName, JElsElementMapping.getInstance());
	}
	
	public static Accelerator loadAcceleator(String sourceFileName, ElementMapping modelMapping) {
		try {
			Accelerator acc = OpenXALExporter.convert(load(sourceFileName));
			acc.setElementMapping(modelMapping);
			return acc;
		} catch (MalformedURLException | JAXBException | SAXException
				| ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws ModelException
	{
		if (args.length == 0) {
			System.out.println("Usage: <path/url to linacLego.xml> [probe file]");
			return;
		}
		
		AcceleratorSeq sequence = loadAcceleator(args[0]);
		EnvelopeProbe probe;
		if (args.length > 1) {
			probe = JElsDemo.loadProbeFromXML(args[1]);
		} else {
			probe = defaultProbe();
		}
		
		JElsDemo.run(sequence, probe);
	}
	
	
	// TODO remove hardcoded probe initialisation
	public static EnvelopeProbe defaultProbe() {
		EnvelopeProbe probe = setupOpenXALProbe(); // OpenXAL probe & algorithm
		//EnvelopeProbe probe = setupElsProbe(); // ELS probe & algorithm
						
		// Setup of initial parameters
		setupInitialParameters(probe);
        //loadInitialParameters(probe, "mebt-initial-state.xml");		
		
		return probe;
	}
	
	private static EnvelopeProbe setupOpenXALProbe() {
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
	}
}
