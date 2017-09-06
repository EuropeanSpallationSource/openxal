/*
 * Copyright (C) 2017 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
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
import xal.smf.Accelerator;
import xal.tools.beam.Twiss;

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

    private File inputFile;
    private File inputDir;
    private String inputGit;

    double bunchFrequency;

    public double getBunchFrequency() {
        return bunchFrequency;
    }

    public void setBunchFrequency(double bunchFrequency) {
        this.bunchFrequency = bunchFrequency;
    }

    public double getBeamCurrent() {
        return beamCurrent;
    }

    public void setBeamCurrent(double beamCurrent) {
        this.beamCurrent = beamCurrent;
    }

    public double getKineticEnergy() {
        return kineticEnergy;
    }

    public void setKineticEnergy(double kineticEnergy) {
        this.kineticEnergy = kineticEnergy;
    }

    public Twiss[] getInitialTwiss() {
        return initialTwiss;
    }

    public void setInitialTwiss(Twiss[] initialTwiss) {
        this.initialTwiss = initialTwiss;
    }
    double beamCurrent;
    double kineticEnergy;
    Twiss[] initialTwiss;
    int initialParametersMode = 0;

    private ImportLogger logger;

    private String outputDir;
    private String outputName;

    public TraceWin() {
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getInputDir() {
        return inputDir;
    }

    public void setInputDir(File inputDir) {
        this.inputDir = inputDir;
    }

    public String getInputGit() {
        return inputGit;
    }

    public void setInputGit(String inputGit) {
        this.inputGit = inputGit;
    }

    public int getInitialParametersMode() {
        return initialParametersMode;
    }

    public void setLogger(ImportLogger logger) {
        this.logger = logger;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    /**
     *
     * @param initialParametersMode to select origin of input parameters:
     * <br> 0: default. Hardcoded for single input file and from MEBT.ini for
     * dir and git
     * <br> 1: Hardcoded.
     * <br> 2: From MEBT.ini and simulated for other sequences
     * <br> 3: From .ini files for all sequences
     */
    public void setInitialParametersMode(int initialParametersMode) {
        this.initialParametersMode = initialParametersMode;
    }

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
    public ESSAccelerator loadAcceleator(URI sourceFileName) throws IOException {
        return loadAcceleator(sourceFileName, JElsElementMapping.getInstance());
    }

    private ESSAccelerator loadAcceleator(URI[] sourceFileNames, String[] sequenceNames, String basePath) throws IOException {
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
    private ESSAccelerator loadAcceleator(URI sourceFileName, ElementMapping modelMapping) throws IOException {
        // Importing from TraceWin formated file
        TraceWinImporter importer = new TraceWinImporter();
        List<Subsystem> systems;

        String basePath = new File(sourceFileName).getParentFile().toURI().toString();
        systems = importer.importFromTraceWin(sourceFileName, new PrintWriter(System.err), basePath);

        ESSAccelerator accelerator = exportToOpenxal(systems, modelMapping);

        return accelerator;
    }

    private ESSAccelerator loadAcceleator(URI[] sourceFileNames, String[] sequenceNames, String basePath, ElementMapping modelMapping) throws IOException {
        // Importing from TraceWin formated file
        TraceWinImporter importer = new TraceWinImporter();
        List<Subsystem> systems;

        systems = importer.importFromTraceWinSequences(sourceFileNames, sequenceNames, new PrintWriter(System.err), basePath);

        ESSAccelerator accelerator = exportToOpenxal(systems, modelMapping);

        return accelerator;
    }

    private ESSAccelerator exportToOpenxal(List<Subsystem> systems, ElementMapping modelMapping) {
        // Exporting to openxal format
        OpenXalExporter exporter = new OpenXalExporter();
        ESSAccelerator accelerator = exporter.exportToOpenxal(systems.get(0), systems);

        // Setting element mapping
        accelerator.setElementMapping(modelMapping);

        return accelerator;
    }

    /**
     * Converts accelerator in TraceWin formatted file to the OpenXAL format
     * with all all required files.
     * <p>
     * Usage: TraceWin input outputFile outputName
     * <br>
     * input TraceWin formatted file in which the accelerator is (.dat), a
     * directory with the same structure as ess-lattice repository, or the
     * address (REST) of the bitbucket repository
     * <br>
     * outputDir directory where export Open XAL files
     * <br>outputName file name of the accelerator files to generate
     * <br>initialParametersMode optional paramaterer to set initial parameters:
     * mode 1 is hardcoded parameters mode 0 is default (hardcoded for input
     * file, MEBT from takes hardcoded parameters
     *
     * </p>
     *
     * @param args
     */
    public void main(String[] args) {
        setLogger(new ImportLogger());

        // Checking commandline arguments
        if (args.length < 3) {
            System.out.println("Usage: TraceWin input outputDir outputName [initialParametersMode]");
            System.exit(0);
        }
        final String input = args[0];
        setOutputDir(args[1]);
        setOutputName(args[2]);

        if (args.length == 4) {
            setInitialParametersMode(Integer.parseInt(args[3]));
        }

        File fileInput = new File(input);
        try {
            if (fileInput.isFile()) {
                setInputFile(fileInput);

                // Hardcoded initial parameters if using a TraceWin file
                bunchFrequency = 352.21;
                beamCurrent = 62.5e-3;
                kineticEnergy = 3.6217853e6;

                initialTwiss = new Twiss[]{new Twiss(-0.051805615, 0.20954703, 0.25288 * 1e-6),
                    new Twiss(-0.30984478, 0.37074849, 0.251694 * 1e-6),
                    new Twiss(-0.48130325, 0.92564505, 0.3615731 * 1e-6)};
            } else if (fileInput.isDirectory()) {
                setInputDir(fileInput);
            } else if (input.startsWith("http")) {
                setInputGit(input);
            } else {
                throw new IOException();
            }
        } catch (IOException e1) {
            System.err.println("Error while trying to read input.");
            System.exit(1);
        }

        importTW();
    }

    public void importTW() {
        // Starting conversion
        logger.log("Started parsing.");
        ESSAccelerator accelerator = null;
        IniFileParser iniFileParser = null;

        if ((getInitialParametersMode() >= 2) || ((getInitialParametersMode() == 0) && (getInputFile() == null))) {
            iniFileParser = new IniFileParser();
        }

        URI[] sourceFileNamesArray;
        String[] sequenceNamesArray = null;
        String basePath = null;

        List<Double> bunchFrequencyList = new ArrayList<>();
        List<Double> beamCurrentList = new ArrayList<>();
        List<Double> kineticEnergyList = new ArrayList<>();
        List<Twiss[]> initialTwissList = new ArrayList<>();

        try {
            if (getInputFile() != null) {
                accelerator = loadAcceleator(getInputFile().toURI());
                if (getInitialParametersMode() > 1) {
                    logger.log("Initial parameters mode incomatible. Changing to hardcoded values for MEBT.");
                }
                setInitialParametersMode(1);
            } else if (getInputDir() != null) {
                File[] inputFiles = getInputDir().listFiles();
                List<URI> sourceFileNames = new ArrayList<>();
                List<String> sequenceNames = new ArrayList<>();
                basePath = getInputDir().toURI().toString();
                for (File inputFilei : inputFiles) {
                    if (inputFilei.isDirectory() && inputFilei.getName().substring(0, 1).matches("\\d+(\\.\\d+)?")
                            && Integer.parseInt(inputFilei.getName().substring(0, 1)) > 2 // Load from MEBT
                            && Integer.parseInt(inputFilei.getName().substring(2, 3)) == 0) { // Remove Dump
                        File traceWinFile = Paths.get(inputFilei.toString(), "Beam_Physics", "lattice.dat").toFile();
                        if (traceWinFile.exists()) {
                            sourceFileNames.add(traceWinFile.toURI());
                            sequenceNames.add(inputFilei.getName().substring(4));
                        }
                    }
                }
                sourceFileNamesArray = sourceFileNames.toArray(new URI[]{});
                sequenceNamesArray = sequenceNames.toArray(new String[]{});
                accelerator = loadAcceleator(sourceFileNamesArray, sequenceNamesArray, basePath);
                if (getInitialParametersMode() == 1) {
                    logger.log("Initial parameters mode incomatible. Changing to 'From MEBT.ini and simulated for other sequences.'");
                    setInitialParametersMode(2);
                }
            } else if (getInputGit() != null) {
                GitParser gitParser = new GitParser();
                gitParser.URL2Json(getInputGit());
                sourceFileNamesArray = gitParser.getSourceFileNames();
                sequenceNamesArray = gitParser.getSequenceNames();
                basePath = gitParser.getBasePath();
                accelerator = loadAcceleator(sourceFileNamesArray, sequenceNamesArray, basePath);
                if (getInitialParametersMode() == 1) {
                    logger.log("Initial parameters mode incomatible. Changing to 'From MEBT.ini and simulated for other sequences.'");
                    setInitialParametersMode(2);
                }
            } else {
                throw new IOException();
            }
        } catch (IOException e1) {
            logger.log("Error while trying to read input.");
            System.exit(1);
        }
        logger.log("Parsing finished.");

        // Loading initial paramaters
        if (getInitialParametersMode() == 3) {
            for (String seq : sequenceNamesArray) {
                iniFileParser.loadTwissFromIni(basePath + "/ProjectFiles/" + seq + ".ini");
                bunchFrequencyList.add(iniFileParser.getBunchFrequency());
                beamCurrentList.add(iniFileParser.getBeamCurrent());
                kineticEnergyList.add(iniFileParser.getKineticEnergy());
                initialTwissList.add(iniFileParser.getInitialTwiss());
            }

            ImporterHelpers.addAllInitialParameters(accelerator, bunchFrequencyList, beamCurrentList, kineticEnergyList, initialTwissList);
        } else {
            if (getInitialParametersMode() != 1) { //For mode 2 or 0 if not single TW file
                // Taking initial parameters from MEBT. For the other sequences are simulated.
                iniFileParser.loadTwissFromIni(basePath + "/ProjectFiles/MEBT.ini");
                bunchFrequency = iniFileParser.getBunchFrequency();
                beamCurrent = iniFileParser.getBeamCurrent();
                kineticEnergy = iniFileParser.getKineticEnergy();
                initialTwiss = iniFileParser.getInitialTwiss();
            }
            try {
                ImporterHelpers.addInitialParameters(accelerator, bunchFrequency, beamCurrent, kineticEnergy, initialTwiss);
            } catch (Exception ex) {
                logger.log("Problem with input parameters. Output probably corrupted (*-model.params).");
            }

        }

        AcceleratorExporter accExp = new AcceleratorExporter(accelerator, getOutputDir(), getOutputName());
        try {
            accExp.export();
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(TraceWin.class.getName()).log(Level.SEVERE, null, ex);
            logger.log("Error exporting.");
        }
        logger.log("Finished exporting.");

        report(accelerator);

        logger.close();
    }

    /**
     * This methods prints the names of the sequences imported
     *
     * @param accelerator
     */
    private void report(Accelerator accelerator) {
        logger.log("--------------------------");
        logger.log("The following sequences were exported to Open XAL:");

        accelerator.getSequences().forEach((seq) -> {
            logger.log(seq.getId());
        });
    }
};
