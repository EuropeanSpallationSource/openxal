package xal.extension.tracewinimporter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import eu.ess.bled.Subsystem;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public static ESSAccelerator loadAcceleator(URI[] sourceFileNames, String[] sequenceNames, String basePath) throws IOException {
        return loadAcceleator(sourceFileNames, sequenceNames, basePath, JElsElementMapping.getInstance());
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
        // Importing from TraceWin formated file
        TraceWinImporter importer = new TraceWinImporter();
        List<Subsystem> systems;

        String basePath = new File(sourceFileName).getParentFile().toURI().toString();
        systems = importer.importFromTraceWin(sourceFileName, new PrintWriter(System.err), basePath);

        ESSAccelerator acc = exportToOpenxal(systems, modelMapping);

        return acc;
    }

    public static ESSAccelerator loadAcceleator(URI[] sourceFileNames, String[] sequenceNames, String basePath, ElementMapping modelMapping) throws IOException {
        // Importing from TraceWin formated file
        TraceWinImporter importer = new TraceWinImporter();
        List<Subsystem> systems;

        systems = importer.importFromTraceWinSequences(sourceFileNames, sequenceNames, new PrintWriter(System.err), basePath);

        ESSAccelerator acc = exportToOpenxal(systems, modelMapping);

        return acc;
    }

    public static ESSAccelerator exportToOpenxal(List<Subsystem> systems, ElementMapping modelMapping) {
        // Exporting to openxal format
        OpenXalExporter exporter = new OpenXalExporter();
        ESSAccelerator acc = exporter.exportToOpenxal(systems.get(0), systems);

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
     * Usage: TraceWin input outputFile outputName
     * <br>
     * input TraceWin formatted file in which the accelerator is (.dat) or
     * directory with the same structure as ess-lattice repository
     * <br>
     * outputDir directory where export Open XAL files
     * <br>outputName file name of the accelerator files to generate
     *
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {
        // Checking commandline arguments
        if (args.length < 3) {
            System.out.println("Usage: TraceWin input outputDir outputName");
            System.exit(0);
        }
        final String input = args[0];
        final String outputDir = args[1];
        final String outputName = args[2];

        // Starting conversion
        System.out.println("Started parsing.");
        ESSAccelerator accelerator = null;
        File fileInput = new File(input);
        try {
            if (fileInput.isFile()) {
                accelerator = loadAcceleator(fileInput.toURI());
            } else if (fileInput.isDirectory()) {
                File[] inputFiles = fileInput.listFiles();
                List<URI> sourceFileNames = new ArrayList<>();
                List<String> sequenceNames = new ArrayList<>();
                for (File inputFile : inputFiles) {
                    if (inputFile.isDirectory() && inputFile.getName().substring(0, 1).matches("\\d+(\\.\\d+)?")
                            && Integer.parseInt(inputFile.getName().substring(0, 1)) > 2) {
                        File traceWinFile = Paths.get(inputFile.toString(), "Beam_Physics", "lattice.dat").toFile();
                        if (traceWinFile.exists()) {
                            sourceFileNames.add(traceWinFile.toURI());
                            sequenceNames.add(inputFile.getName().substring(4));
                        }
                    }
                }
                accelerator = loadAcceleator(sourceFileNames.toArray(new URI[]{}), sequenceNames.toArray(new String[]{}), fileInput.toURI().toString());
            } else {
                throw new IOException();
            }
        } catch (IOException e1) {
            System.err.println("Error while trying to read input.");
            System.exit(1);
        }
        System.out.println("Parsing finished.");

        AcceleratorExporter accExp = new AcceleratorExporter(accelerator, outputDir, outputName);
        try {
            accExp.export();
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(TraceWin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
;
