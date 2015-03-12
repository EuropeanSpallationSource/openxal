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
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.ElementMapping;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;

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
			probe = JElsDemo.defaultProbe();
		}
		
		JElsDemo.run(sequence, probe);
	}
	
}
