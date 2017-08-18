package xal.extension.tracewinimporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.ess.bled.Subsystem;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.jels.ImporterHelpers;

import xal.extension.tracewinimporter.openxalexporter.OpenXalExporter;
import xal.extension.tracewinimporter.parser.TraceWinImporter;
import xal.extension.jels.model.elem.jels.JElsElementMapping;
import xal.extension.jels.smf.ESSAccelerator;
import xal.extension.tracewinimporter.openxalexporter.AcceleratorExporter;

import xal.sim.scenario.ElementMapping;

/**
 * Loader for accelerators in TraceWin formatted files.
 *
 * <p>
 * Import accelerator from TraceWin to OpenXAL format SMF.
 * </p>
 *
 * @version 0.1 4 Sep 2015
 * @author Blaz Kranjc
 *
 * @version 0.2 11 Jul 2017
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class TraceWin {
    /**
     * Calls {{@link #loadAcceleator(String, ElementMapping)} with default
     * (JEls) element mapping.
     *
     * @see #loadAcceleator(String, ElementMapping)
     * @param sourceFileName traceWin formatted file in which the accelerator
     * is.
     * @return accelerator
     * @throws IOException if there was a problem reading from file
     */
    public static ESSAccelerator loadAcceleator(URI sourceFileName) throws IOException {
        return loadAcceleator(sourceFileName, JElsElementMapping.getInstance());
    }

    /**
     * Loads accelerator from given file
     *
     * @param sourceFileName traceWin formatted file in which the accelerator
     * is.
     * @param modelMapping smf mapping for accelerator nodes to be set to
     * accelerator after conversion.
     * @return accelerator
     * @throws IOException if there was a problem reading from file
     */
    public static ESSAccelerator loadAcceleator(URI sourceFileName, ElementMapping modelMapping) throws IOException {
        ESSAccelerator acc;
        // Importing from TraceWin formated file
        TraceWinImporter importer = new TraceWinImporter();
        BufferedReader br = new BufferedReader(new InputStreamReader(sourceFileName.toURL().openStream()));
        List<Subsystem> systems = importer.importFromTraceWin(br, new PrintWriter(System.err), new File(sourceFileName).getParentFile().toURI().toString());
        br.close();

        // Exporting to openxal format
        OpenXalExporter exporter = new OpenXalExporter();
        acc = exporter.exportToOpenxal(systems.get(0), systems);

        // Setting element mapping
        acc.setElementMapping(modelMapping);

        // Loading hardcoded initial paramaters. 
        // TODO: load from Tracewin init files
        ImporterHelpers.addHardcodedInitialParameters(acc);

        return acc;

    }

    /**
     * Converts accelerator in TraceWin formatted file to the OpenXAL format
     * with all all required files.
     * <p>
     * Usage: TraceWin inputFile outputFile inputFile TraceWin formatted file in
     * which the accelerator is. (.dat) outputDir directory where export Open
     * XAL files outputName file name of the accelerator files to generate
     *
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {
        // Checking commandline arguments
        if (args.length < 3) {
            System.out.println("Usage: TraceWin inputFile outputDir outputName");
            System.exit(0);
        }
        final String input = args[0];
        final String outputDir = args[1];
        final String outputName = args[2];

        // Starting conversion
        System.out.println("Started parsing.");
        ESSAccelerator accelerator = null;
        try {
            accelerator = loadAcceleator(new File(input).toURI());
        } catch (IOException e1) {
            System.err.println("Error while trying to read file.");
            System.exit(1);
        }
        System.out.println("Parsing finished.");

        AcceleratorExporter accExp = new AcceleratorExporter(accelerator, outputDir, outputName);
        try {
            accExp.export();
        } catch (IOException ex) {
            Logger.getLogger(TraceWin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(TraceWin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Cleans up XML OpenXal produces
     *
     * @param parent node to clean
     */
    public static void cleanup(Node parent) {
        NodeList children = parent.getChildNodes();
        NamedNodeMap attrs = parent.getAttributes();
        if (attrs != null) {
            // unneeded attributes
            if (attrs.getNamedItem("s") != null) {
                attrs.removeNamedItem("s");
            }
            if (attrs.getNamedItem("pid") != null) {
                attrs.removeNamedItem("pid");
            }
            if (attrs.getNamedItem("status") != null) {
                attrs.removeNamedItem("status");
            }
            if (attrs.getNamedItem("eid") != null) {
                attrs.removeNamedItem("eid");
            }

            // remove type="sequence" on sequences - import doesn't work
            // otherwise
            if ("sequence".equals(parent.getNodeName()) && attrs.getNamedItem("type") != null
                    && "sequence".equals(attrs.getNamedItem("type").getNodeValue())) {
                attrs.removeNamedItem("type");
            }
        }

        for (int i = 0; i < children.getLength();) {
            Node child = children.item(i);
            attrs = child.getAttributes();

            if ("align".equals(child.getNodeName()) || "twiss".equals(child.getNodeName())) // remove twiss and align - not needed
            {
                parent.removeChild(child);
            } else if ("channelsuite".equals(child.getNodeName()) && !child.hasChildNodes()) {
                parent.removeChild(child);
            } else if ("aperture".equals(child.getNodeName()) && "0.0".equals(attrs.getNamedItem("x").getNodeValue())) // remove empty apertures
            {
                parent.removeChild(child);
            } else {
                cleanup(child);
                // remove empty attributes
                if ("attributes".equals(child.getNodeName()) && child.getChildNodes().getLength() == 0) {
                    parent.removeChild(child);
                } else {
                    i++;
                }
            }
        }

    }

}
