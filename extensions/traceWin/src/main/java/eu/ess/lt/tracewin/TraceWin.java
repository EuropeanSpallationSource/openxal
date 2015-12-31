package eu.ess.lt.tracewin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.ess.bled.Subsystem;
import eu.ess.lt.parser.openxal.OpenXalExporter;
import eu.ess.lt.parser.tracewin.TraceWinImporter;
import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import xal.sim.scenario.ElementMapping;
import xal.smf.Accelerator;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlWriter;

/**
 * Loader for accelerators in TraceWin formatted files.
 * 
 * <p>
 * Uses Bled importer to impoer accelerator from traceIn to Bled format and then
 * exports it to openxalFormat.
 * </p>
 *
 * @version 0.1 4 Sep 2015
 * @author Blaz Kranjc
 */
public class TraceWin {

	/**
	 * Calls {{@link #loadAcceleator(String, ElementMapping)} with default
	 * (JEls) element mapping.
	 * 
	 * @see #loadAcceleator(String, ElementMapping)
	 * @param sourceFilenName
	 *            traceWin formatted file in which the accelerator is.
	 * @return accelerator
	 * @throws IOException
	 *             if there was a problem reading from file
	 */
	public static Accelerator loadAcceleator(URI sourceFileName) throws IOException {
		return loadAcceleator(sourceFileName, JElsElementMapping.getInstance());
	}

	/**
	 * Loads accelerator from given file
	 * 
	 * @param sourceFileName
	 *            traceWin formatted file in which the accelerator is.
	 * @param modelMapping
	 *            smf mapping for accelerator nodes to be set to accelerator
	 *            after conversion.
	 * @return accelerator
	 * @throws IOException
	 *             if there was a problem reading from file
	 */
	public static Accelerator loadAcceleator(URI fileName, ElementMapping modelMapping) throws IOException {
		Accelerator acc = null;
		// Importing from TraceWin formated file
		eu.ess.lt.parser.tracewin.TraceWinImporter importer = new TraceWinImporter();
		BufferedReader br = new BufferedReader(new InputStreamReader(fileName.toURL().openStream()));
		List<Subsystem> systems = importer.importFromTraceWin(br, new PrintWriter(System.err), new File(fileName).getParentFile().toURI().toString());
		br.close();

		// Exporting to openxal format
		OpenXalExporter exporter = new OpenXalExporter();
		acc = exporter.exportToOpenxal(systems.get(0), systems);

		// Setting element mapping
		acc.setElementMapping(modelMapping);

		return acc;

	}

	/**
	 * Converts accelerator in TraceWin formatted file into openxal format and
	 * writes it to destination file or standard output.
	 * <p>
	 * Usage: TraceWin inputFile [outputFile] If no output file is given,
	 * accelerator will be printed to standard output in openxal format.
	 * inputFile TraceWin formatted file in which the accelerator is. (.dat)
	 * outputFile file to write accelerator in (.xdxf)
	 * 
	 * </p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Checking commandline arguments
		if (args.length < 1) {
			System.out.println("Usage: TraceWin inputFile [outputFile]");
			System.out.println("	if no output file is given, accelerator will be printed to standard output.");
			System.exit(0);
		}
		final String input = args[0];
		String output = null;
		if (args.length > 1) {
			output = args[1];
		}
		// Starting conversion
		System.out.println("Started parsing.");
		Accelerator acc = null;
		try {
			acc = loadAcceleator(new File(input).toURI());
		} catch (IOException e1) {
			System.err.println("Error while trying to read file.");
			System.exit(1);
		}
		System.out.println("Parsing finished.");

		// Choosing output destination
		try {
			OutputStream out;
			if (output == null) {
				System.out.println("Parsed accelerator: ");
				out = System.out;
			} else {
				out = new FileOutputStream(output);
				System.out.println("Writing accelerator to file.");
			}
			// Writing accelerator
			OutputStreamWriter writer;
			writer = new OutputStreamWriter(out);
			XmlDataAdaptor da = XmlDataAdaptor.newDocumentAdaptor(acc, "xdxf.dtd");
			Document document = da.document();
			cleanup(document);
			XmlWriter.writeToWriter(document, writer);
			writer.flush();
			writer.close();

			System.out.println("Finished");
		} catch (FileNotFoundException e) {
			System.err.println("Error while trying to write to file.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error while flushing and closing file.");
			System.exit(1);
		}
	}

	/**
	 * Cleans up XML OpenXal produces
	 * 
	 * @param parent
	 *            node to clean
	 */
	private static void cleanup(Node parent) {
		NodeList children = parent.getChildNodes();
		NamedNodeMap attrs = parent.getAttributes();
		if (attrs != null) {
			// unneeded attributes
			if (attrs.getNamedItem("s") != null)
				attrs.removeNamedItem("s");
			if (attrs.getNamedItem("pid") != null)
				attrs.removeNamedItem("pid");
			if (attrs.getNamedItem("status") != null)
				attrs.removeNamedItem("status");
			if (attrs.getNamedItem("eid") != null)
				attrs.removeNamedItem("eid");

			// remove type="sequence" on sequences - import doesn't work
			// otherwise
			if ("sequence".equals(parent.getNodeName()) && attrs.getNamedItem("type") != null
					&& "sequence".equals(attrs.getNamedItem("type").getNodeValue()))
				attrs.removeNamedItem("type");
		}

		for (int i = 0; i < children.getLength();) {
			Node child = children.item(i);
			attrs = child.getAttributes();

			if ("align".equals(child.getNodeName()) || "twiss".equals(child.getNodeName()))
				// remove twiss and align - not needed
				parent.removeChild(child);
			else if ("channelsuite".equals(child.getNodeName()) && !child.hasChildNodes()) {
				parent.removeChild(child);
			} else if ("aperture".equals(child.getNodeName()) && "0.0".equals(attrs.getNamedItem("x").getNodeValue()))
				// remove empty apertures
				parent.removeChild(child);
			else {
				cleanup(child);
				// remove empty attributes
				if ("attributes".equals(child.getNodeName()) && child.getChildNodes().getLength() == 0) {
					parent.removeChild(child);
				} else
					i++;
			}
		}

	}

}
