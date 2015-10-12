package se.lu.esss.linaclego;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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

import se.lu.esss.ics.jels.ImporterHelpers;
import se.lu.esss.ics.jels.JElsDemo;
import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import se.lu.esss.linaclego.elements.Drift;
import se.lu.esss.linaclego.elements.Quad;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ElementMapping;
import xal.sim.scenario.ProbeFactory;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;

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
	
	
	public static Accelerator loadAcceleatorWithInitialConditions(String sourceFileName, ElementMapping modelMapping) {
		try {
			OpenXALExporter exporter = OpenXALExporter.convertFull(LinacLego.load(sourceFileName));
			Accelerator accelerator = exporter.getAccelerator();
			accelerator.setElementMapping(modelMapping);
			
			accelerator.setElementMapping(JElsElementMapping.getInstance());
			// add initial parameters
			AcceleratorSeqCombo comboSeq = ImporterHelpers.addDefaultComboSeq(accelerator);

			EnvelopeProbe probe = ImporterHelpers.defaultProbe();
			exporter.readInitialParameters(probe);
			probe.initialize();		        
	        ProbeFactory.createSchema(accelerator.editContext(), probe);
		    
		    List<EnvelopeProbeState> states = ImporterHelpers.simulateInitialValues(probe, comboSeq);
		    ProbeFactory.storeInitialValues(accelerator.editContext(), states);			
			
			return accelerator;						
		} catch (MalformedURLException | JAXBException | SAXException
				| ParserConfigurationException|ModelException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void main(String[] args) throws ModelException, InstantiationException
	{
		if (args.length == 0) {
			System.out.println("Usage: <path/url to linacLego.xml> [probe file]");
			return;
		}
		
		AcceleratorSeq sequence = loadAcceleatorWithInitialConditions(args[0], JElsElementMapping.getInstance());
		EnvelopeProbe probe;
		if (args.length > 1) {
			probe = JElsDemo.loadProbeFromXML(args[1]);
		} else {
			probe = ProbeFactory.getEnvelopeProbe(sequence, AlgorithmFactory.createEnvelopeTracker(sequence));
		}
		
		JElsDemo.run(sequence, probe);
	}
}
