package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.io.*;
import javax.swing.border.*;
import java.net.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.prefs.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;
import xal.extension.application.Application;
import xal.tools.apputils.VerticalLayout;
import xal.extension.widgets.swing.*;
import xal.tools.text.ScientificNumberFormat;

/**
 * This creates an ASCII file for dT procedure
 *
 * @author A. Shishlo
 * @version 1.0
 */
public final class AnalysisCntrlTDProcedure extends AnalysisController {

    private static final Logger LOGGER = Logger.getLogger(AnalysisCntrlTDProcedure.class.getName());

    //readiness of the analysis results
    private boolean analysisDone = false;

    private static Border etchedBorder = BorderFactory.createEtchedBorder();

    //local control panel
    private JPanel localCntrlPanel = new JPanel();

    //Buttons
    private JButton exportDataButton = new JButton("EXPORT DATA TO LABVIEW");
    private JButton makeAnalysisButton = new JButton("PERFORM ANALYSIS");

    //vectors with graph data for B and C BPMs
    private Vector<BasicGraphData> gdVB = new Vector<>();
    private Vector<BasicGraphData> gdVC = new Vector<>();

    //Cavity of CCL index
    private int cavIndex = -1;

    //date and time
    private SimpleDateFormat dateFormat = null;
    private JFormattedTextField dateTimeField = null;

    //numbers format
    private DecimalFormat intFormat = new DecimalFormat("###0");
    private DecimalFormat dblFormat = new DecimalFormat("###0.0###");

    //local data file
    private File dataFile = null;

    //default path for ascii file
    private String defaultPath = null;

    //alias in preferences
    private String defaultPathName = "default_export_file_path";

    //left custom control panel
    private JTextField leftTitle = new JTextField("===================SCAN DATA====================");
    private int verticalLeftGraphSize = 13;
    private JLabel verticalLeftGraphLabel1 = new JLabel(" ");
    private JLabel verticalLeftGraphLabel2 = new JLabel(" ");

    //TOP custom panel
    private JLabel moduleNameLabel = new JLabel("Name of the Module", SwingConstants.CENTER);
    private String bpm1NameString = "BPM #1 name";
    private String bpm2NameString = "BPM #2 name";
    private JLabel bpm1NameLabel = new JLabel("  BPM #1 :  ", SwingConstants.CENTER);
    private JLabel bpm2NameLabel = new JLabel("  BPM #2 :  ", SwingConstants.CENTER);
    private JRadioButton aMatrixSwitchButton = new JRadioButton("Use matrix A for Module Amplitude Deviation = 0      ");

    private JLabel rfPhaseLabel = new JLabel("  Recomended RF Phase, deg", SwingConstants.LEFT);
    private JLabel rfAmpLabel = new JLabel("  Recomended RF Amplitude", SwingConstants.LEFT);
    private JButton setToAccelButton = new JButton("  SET VALUES TO RF CAVITY  ");

    private JLabel inputEnergyDevLabel = new JLabel("  Input Energy Deviation, keV", SwingConstants.LEFT);
    private JLabel currentAmpLabel = new JLabel("  Current RF Amplitude", SwingConstants.LEFT);
    private JLabel expSlopeLabel = new JLabel("  Experimental Slope, deg", SwingConstants.LEFT);
    private JLabel energyStepLabel = new JLabel("  Step for Energy Markers, keV", SwingConstants.LEFT);

    private DoubleInputTextField rfPhaseText = new DoubleInputTextField(10);
    private DoubleInputTextField rfAmpText = new DoubleInputTextField(10);
    private DoubleInputTextField inputEnergyDevText = new DoubleInputTextField(10);
    private DoubleInputTextField currentAmpText = new DoubleInputTextField(10);
    private DoubleInputTextField expSlopeText = new DoubleInputTextField(10);
    private DoubleInputTextField energyStepText = new DoubleInputTextField(10);

    private NumberFormat format = new ScientificNumberFormat(5, 10, false);

    //main local analysis panel
    private JPanel localAnalysisPanel = new JPanel();
    private FunctionGraphsJPanel dphi12graphPanel = new FunctionGraphsJPanel();
    private FunctionGraphsJPanel ampDevGraphPanel = new FunctionGraphsJPanel();

    //left graphs with experimental data
    private FunctionGraphsJPanel graphLeftPanel1 = new FunctionGraphsJPanel();
    private FunctionGraphsJPanel graphLeftPanel2 = new FunctionGraphsJPanel();

    //graph data
    private BasicGraphData ampDevVsSlopeThGd = new CubicSplineGraphData();
    private BasicGraphData ampDevVsSlopeExGd = new BasicGraphData();

    private BasicGraphData bpm1ExGd = new BasicGraphData();
    private BasicGraphData bpm1InterpGd = new BasicGraphData();
    private BasicGraphData bpm2ExGd = new BasicGraphData();
    private BasicGraphData bpm2InterpGd = new BasicGraphData();

    private BasicGraphData bpm12ExGd = new BasicGraphData();
    private BasicGraphData bpm12InterpGd = new BasicGraphData();

    private BasicGraphData energyLineGd = new BasicGraphData();
    private BasicGraphData energyPosGd = new BasicGraphData();
    private BasicGraphData energyNegGd = new BasicGraphData();

    //raw data from scan
    private BasicGraphData gdBpmBOn = null;
    private BasicGraphData gdBpmCOn = null;
    private BasicGraphData gdBpmBOff = null;
    private BasicGraphData gdBpmCOff = null;

    //the name of data file with A-matrices
    private String fileNameTheoryData = "";

    private DeltaTdata theoryData = new DeltaTdata();

    /**
     * The constructor.
     *
     * @param mainControllerIn The MainAnalysisController reference
     * @param analysisConf The DataAdaptor instance with configuration data
     * @param parentAnalysisPanelIn The parent panel for analysis
     * @param customControlPanelIn The control panel for GUI elements specific
     * for this analysis
     * @param customGraphPanelIn The graph panel for graphs specific for this
     * analysis
     * @param globalButtonsPanelIn The global buttons panel
     * @param scanVariableParameterIn The ScanParameter reference
     * @param scanVariableIn The scan variable reference
     * @param measuredValuesVIn The vector with measured values references
     * @param graphAnalysisIn The graphAnalysis panel
     * @param messageTextLocalIn The message text field
     * @param graphDataLocalIn The external graph data for temporary graph
     */
    public AnalysisCntrlTDProcedure(MainAnalysisController mainControllerIn,
            DataAdaptor analysisConf,
            JPanel parentAnalysisPanelIn,
            JPanel customControlPanelIn,
            JPanel customGraphPanelIn,
            JPanel globalButtonsPanelIn,
            ScanVariable scanVariableParameterIn,
            ScanVariable scanVariableIn,
            Vector<MeasuredValue> measuredValuesVIn,
            FunctionGraphsJPanel graphAnalysisIn,
            JTextField messageTextLocalIn,
            BasicGraphData graphDataLocalIn) {

        //call the superclass constructor
        super(mainControllerIn,
                analysisConf,
                parentAnalysisPanelIn,
                customControlPanelIn,
                customGraphPanelIn,
                globalButtonsPanelIn,
                scanVariableParameterIn,
                scanVariableIn,
                measuredValuesVIn,
                graphAnalysisIn,
                messageTextLocalIn,
                graphDataLocalIn);

        //reading data from data adaptor
        String nameIn = "DELTA-T PROCEDURE ANALYSIS";
        DataAdaptor nameDA = analysisConf.childAdaptor("ANALYSIS_NAME");
        if (nameDA != null) {
            nameIn = nameDA.stringValue("name");
        }
        setName(nameIn);

        DataAdaptor cavInfoDA = analysisConf.childAdaptor("CAVITY_INFO");
        if (cavInfoDA != null) {
            cavIndex = cavInfoDA.intValue("index");
            moduleNameLabel.setText(cavInfoDA.stringValue("cavity_name"));
            bpm1NameString = cavInfoDA.stringValue("bpm1_name");
            bpm2NameString = cavInfoDA.stringValue("bpm2_name");
            bpm1NameLabel.setText(bpm1NameLabel.getText() + bpm1NameString + "  ");
            bpm2NameLabel.setText(bpm2NameLabel.getText() + bpm2NameString + "  ");
            energyStepText.setValue(cavInfoDA.doubleValue("energy_step_kev"));
        }

        DataAdaptor thFileDA = analysisConf.childAdaptor("THEORETICAL_FILE");
        if (thFileDA != null) {
            String fileName = thFileDA.stringValue("name");
            readTheoryData(fileName);
        }

        //get the preference
        Preferences pref = xal.tools.apputils.Preferences.nodeForPackage(this.getClass());
        defaultPath = pref.get(defaultPathName, null);

        //set action listener to button
        defineButtonActions();

        //date and time format definition
        dateFormat = new SimpleDateFormat(" MM/dd/yyyy HH:mm ");
        dateTimeField = new JFormattedTextField(dateFormat);

        //specify the left local panel
        JPanel tmpL = new JPanel();
        tmpL.setLayout(new FlowLayout(FlowLayout.CENTER));
        tmpL.add(makeAnalysisButton);
        makeAnalysisButton.setForeground(Color.red);

        //specify the left local panel
        JPanel tmp3 = new JPanel();
        tmp3.setLayout(new FlowLayout(FlowLayout.CENTER));
        tmp3.add(exportDataButton);
        exportDataButton.setForeground(Color.red);

        //left graph panels
        JPanel tmp2 = new JPanel();
        tmp2.setLayout(new GridLayout(2, 1));

        JPanel tmp21 = new JPanel();
        tmp21.setLayout(new BorderLayout());

        JPanel tmp21L = new JPanel();
        tmp21L.setLayout(new GridLayout(verticalLeftGraphSize, 1));
        tmp21L.add(verticalLeftGraphLabel1);
        tmp21.add(tmp21L, BorderLayout.WEST);
        tmp21.add(graphLeftPanel1, BorderLayout.CENTER);

        JPanel tmp22 = new JPanel();
        tmp22.setLayout(new BorderLayout());

        JPanel tmp22L = new JPanel();
        tmp22L.setLayout(new GridLayout(verticalLeftGraphSize, 1));
        tmp22L.add(verticalLeftGraphLabel2);
        tmp22.add(tmp22L, BorderLayout.WEST);
        tmp22.add(graphLeftPanel2, BorderLayout.CENTER);

        tmp2.add(tmp21);
        tmp2.add(tmp22);

        tmp21.setBorder(etchedBorder);
        tmp22.setBorder(etchedBorder);

        localCntrlPanel.setLayout(new VerticalLayout());
        localCntrlPanel.add(leftTitle);
        localCntrlPanel.add(tmpL);
        localCntrlPanel.add(tmp2);
        localCntrlPanel.add(tmp3);

        graphLeftPanel1.setGraphBackGroundColor(Color.BLACK);
        graphLeftPanel2.setGraphBackGroundColor(Color.BLACK);
        graphLeftPanel1.setGridLineColor(Color.gray);
        graphLeftPanel2.setGridLineColor(Color.gray);
        graphLeftPanel1.setOffScreenImageDrawing(true);
        graphLeftPanel2.setOffScreenImageDrawing(true);

        graphLeftPanel1.setAxisNames("Module Phase, deg", "D-Phi BPM #1,deg");
        graphLeftPanel2.setAxisNames("Module Phase, deg", "D-Phi BPM #2,deg");

        //make local analysis panel
        makeLocalAnalysisPanel();

        //make graph data
        int nP = theoryData.getNumbPoints();
        for (int i = 0; i < nP; i++) {
            ampDevVsSlopeThGd.addPoint(theoryData.getSlope(i), 100 * (theoryData.getAmplitude(i) - 1.0));
        }
        ampDevVsSlopeThGd.setGraphProperty(ampDevGraphPanel.getLegendKeyString(), " Theory Data ");
        ampDevVsSlopeExGd.setGraphProperty(ampDevGraphPanel.getLegendKeyString(), " This Scan ");

        ampDevVsSlopeThGd.setGraphColor(Color.green);
        ampDevVsSlopeExGd.setGraphColor(Color.red);
        ampDevVsSlopeExGd.setGraphPointSize(8);

        ampDevGraphPanel.addGraphData(ampDevVsSlopeThGd);
        ampDevGraphPanel.addGraphData(ampDevVsSlopeExGd);

        //graph data properties
        bpm1ExGd.setGraphColor(Color.green);
        bpm1InterpGd.setGraphColor(Color.green);
        bpm1InterpGd.setDrawPointsOn(false);
        bpm2ExGd.setGraphColor(Color.green);
        bpm2InterpGd.setGraphColor(Color.green);
        bpm2InterpGd.setDrawPointsOn(false);

        bpm12ExGd.setGraphColor(Color.red);
        bpm12ExGd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Scan Data ");

        bpm12InterpGd.setGraphColor(Color.blue);
        bpm12InterpGd.setDrawPointsOn(false);
        bpm12InterpGd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Fit of Scan Data ");

        energyLineGd.setGraphColor(Color.green);
        energyLineGd.setDrawPointsOn(false);
        energyLineGd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Variable Energy Line ");

        Rectangle shape = new Rectangle(-4, -4, 8, 8);
        energyPosGd.setGraphPointShape(shape);
        energyNegGd.setGraphPointShape(shape);
        energyPosGd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Markers Delta-E > 0 ");
        energyNegGd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Markers Delta-E < 0 ");

        energyPosGd.setGraphColor(Color.red);
        energyPosGd.setDrawLinesOn(false);

        energyNegGd.setGraphColor(Color.cyan);
        energyNegGd.setDrawLinesOn(false);

        graphLeftPanel1.addGraphData(bpm1ExGd);
        graphLeftPanel1.addGraphData(bpm1InterpGd);
        graphLeftPanel2.addGraphData(bpm2ExGd);
        graphLeftPanel2.addGraphData(bpm2InterpGd);

        dphi12graphPanel.addGraphData(bpm12ExGd);
        dphi12graphPanel.addGraphData(bpm12InterpGd);
        dphi12graphPanel.addGraphData(energyLineGd);
        dphi12graphPanel.addGraphData(energyPosGd);
        dphi12graphPanel.addGraphData(energyNegGd);

        //set analysis into initial state
        clearResultsOfAnalysis();

    }

    /**
     * Sets the configurations of the analysis.
     *
     * @param analysisConfig The DataAdaptor instance with configuration data
     */
    @Override
    public void dumpAnalysisConfig(DataAdaptor analysisConfig) {
        super.dumpAnalysisConfig(analysisConfig);

        DataAdaptor cavInfoDA = analysisConfig.createChild("CAVITY_INFO");
        cavInfoDA.setValue("index", cavIndex);
        cavInfoDA.setValue("cavity_name", moduleNameLabel.getText());
        cavInfoDA.setValue("bpm1_name", bpm1NameString);
        cavInfoDA.setValue("bpm2_name", bpm2NameString);
        cavInfoDA.setValue("energy_step_kev", energyStepText.getValue());

        DataAdaptor thFileDA = analysisConfig.createChild("THEORETICAL_FILE");
        thFileDA.setValue("name", fileNameTheoryData);
    }

    /**
     * Sets fonts for all GUI elements.
     *
     * @param fnt The new font
     */
    @Override
    public void setFontsForAll(Font fnt) {
        super.setFontsForAll(fnt);
        exportDataButton.setFont(fnt);
        makeAnalysisButton.setFont(fnt);

        leftTitle.setFont(fnt);

        moduleNameLabel.setFont(fnt);
        bpm1NameLabel.setFont(fnt);
        bpm2NameLabel.setFont(fnt);

        aMatrixSwitchButton.setFont(fnt);

        rfPhaseLabel.setFont(fnt);
        rfAmpLabel.setFont(fnt);

        setToAccelButton.setFont(fnt);

        inputEnergyDevLabel.setFont(fnt);
        currentAmpLabel.setFont(fnt);
        expSlopeLabel.setFont(fnt);
        energyStepLabel.setFont(fnt);

        rfPhaseText.setFont(fnt);
        rfAmpText.setFont(fnt);
        inputEnergyDevText.setFont(fnt);
        currentAmpText.setFont(fnt);
        expSlopeText.setFont(fnt);
        energyStepText.setFont(fnt);

    }

    /**
     * Does what necessary to close this analysis window.
     */
    @Override
    public void shutUp() {
        super.shutUp();
        customControlPanel.removeAll();
        customGraphPanel.removeAll();
    }

    /**
     * Does what necessary to open this analysis window. This method could be
     * overridden, because it is empty here.
     */
    @Override
    public void showUp() {
        super.showUp();

        customControlPanel.add(localCntrlPanel, BorderLayout.NORTH);
        customGraphPanel.add(localAnalysisPanel, BorderLayout.CENTER);

        clearResultsOfAnalysis();

        //repaint
        parentAnalysisPanel.validate();
        parentAnalysisPanel.repaint();

    }

    /**
     * Updates data on the analysis graph panel.
     */
    @Override
    public void updateDataSetOnGraphPanel() {
        super.updateDataSetOnGraphPanel();
        if (analysisDone) {
            clearResultsOfAnalysis();
        }
    }

    /**
     * Clears all data on the analysis panel
     */
    private void clearResultsOfAnalysis() {
        analysisDone = false;

        rfPhaseText.setText(null);
        rfAmpText.setText(null);
        inputEnergyDevText.setText(null);
        currentAmpText.setText(null);
        expSlopeText.setText(null);
        rfPhaseText.setText(null);

        rfPhaseText.setBackground(Color.white);
        rfAmpText.setBackground(Color.white);
        inputEnergyDevText.setBackground(Color.white);
        currentAmpText.setBackground(Color.white);
        expSlopeText.setBackground(Color.white);
        rfPhaseText.setBackground(Color.white);

        ampDevVsSlopeExGd.removeAllPoints();

        bpm1ExGd.removeAllPoints();
        bpm1InterpGd.removeAllPoints();
        bpm2ExGd.removeAllPoints();
        bpm2InterpGd.removeAllPoints();

        bpm12ExGd.removeAllPoints();
        bpm12InterpGd.removeAllPoints();
        energyLineGd.removeAllPoints();
        energyPosGd.removeAllPoints();
        energyNegGd.removeAllPoints();
    }

    /**
     * Defines if the scan data have right form
     *
     * @return True if the scan data can be used as input for delta-t procedure
     */
    private boolean canSaveASDTtable() {

        if (cavIndex <= 0) {
            return false;
        }

        int nMeasuredValues = measuredValuesV.size();
        if (nMeasuredValues < 2) {
            return false;
        }

        MeasuredValue bpmBMv = measuredValuesV.get(0);
        MeasuredValue bpmCMv = measuredValuesV.get(1);

        gdVB = bpmBMv.getDataContainers();
        gdVC = bpmCMv.getDataContainers();

        if (gdVB.size() != 2 || gdVC.size() != 2) {
            return false;
        }

        gdBpmBOn = gdVB.get(0);
        gdBpmCOn = gdVC.get(0);

        gdBpmBOff = gdVB.get(1);
        gdBpmCOff = gdVC.get(1);

        if (gdBpmBOn.getNumbOfPoints() != gdBpmCOn.getNumbOfPoints()
                || gdBpmBOff.getNumbOfPoints() != gdBpmCOff.getNumbOfPoints()) {
            return false;
        }

        int nP = gdBpmBOn.getNumbOfPoints();
        for (int i = 0; i < nP; i++) {
            if (Math.abs(gdBpmBOn.getX(i) - gdBpmCOn.getX(i)) > 0.0001) {
                return false;
            }
        }

        nP = gdBpmBOff.getNumbOfPoints();
        for (int i = 0; i < nP; i++) {
            if (Math.abs(gdBpmBOff.getX(i) - gdBpmCOff.getX(i)) > 0.0001) {
                return false;
            }
        }

        return true;
    }

    /**
     * Description of the Method
     */
    private void defineButtonActions() {

        //make analysis button
        makeAnalysisButton.addActionListener(e -> makeDeltaTimeAnalysis());

        //set data to EPICS
        setToAccelButton.addActionListener(e -> setAmplitudeAndPhaseToEPICS());

        //"EXPORT ASCII"
        exportDataButton.addActionListener(e -> {
            if (canSaveASDTtable()) {
                //save data as table
                JFileChooser ch = new JFileChooser();
                ch.setDialogTitle("Export data into dT procedure file");
                if (dataFile != null) {
                    ch.setSelectedFile(dataFile);
                } else {
                    if (defaultPath != null) {
                        File path = new File(defaultPath);
                        if (path != null && path.exists()) {
                            ch.setSelectedFile(path);
                        }
                    }
                }
                int returnVal = ch.showSaveDialog(parentAnalysisPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    dataFile = ch.getSelectedFile();
                    defaultPath = dataFile.getAbsolutePath();
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(dataFile))) {
                        Preferences pref = xal.tools.apputils.Preferences.nodeForPackage(this.getClass());

                        defaultPath = dataFile.getAbsolutePath();
                        pref.put(defaultPathName, defaultPath);
                        pref.flush();

                        //place to write data into acsii file
                        gdBpmBOn = gdVB.get(0);
                        gdBpmCOn = gdVC.get(0);

                        gdBpmBOff = gdVB.get(1);
                        gdBpmCOff = gdVC.get(1);

                        double amplitude = ((Double) gdBpmBOn.getGraphProperty("PARAMETER_VALUE"));

                        //1-st line - set date and time
                        dateTimeField.setValue(new Date());

                        String line = "Dt module ";
                        StringBuilder builder = new StringBuilder();
                        builder.append(line);
                        builder.append(cavIndex).append(dateTimeField.getText()).append(" ");

                        out.write(builder.toString());
                        out.newLine();

                        //2-nd line (nPoints for On) (Amplitude) (nPoints for Off)
                        builder = new StringBuilder();
                        builder.append(" ");
                        builder.append(intFormat.format(gdBpmBOn.getNumbOfPoints())).append(" ");
                        builder.append(dblFormat.format(amplitude)).append(" ");
                        builder.append(intFormat.format(gdBpmBOff.getNumbOfPoints())).append(" ");

                        out.write(builder.toString());
                        out.newLine();

                        //3-rd line - x-array (phase values)
                        builder = new StringBuilder();
                        builder.append(" ");
                        int nP = gdBpmBOn.getNumbOfPoints();
                        for (int i = 0; i < nP; i++) {
                            builder.append(dblFormat.format(gdBpmBOn.getX(i))).append(" ");
                        }
                        builder.append(" ");
                        out.write(builder.toString());
                        out.newLine();

                        //4-th line B-bpm phases for On
                        builder = new StringBuilder();
                        builder.append(" ");
                        for (int i = 0; i < nP; i++) {
                            builder.append(dblFormat.format(gdBpmBOn.getY(i))).append(" ");
                        }
                        builder.append(" ");
                        out.write(builder.toString());
                        out.newLine();

                        //5-th line C-bpm phases for On
                        builder = new StringBuilder();
                        builder.append(" ");
                        for (int i = 0; i < nP; i++) {
                            builder.append(dblFormat.format(gdBpmCOn.getY(i))).append(" ");
                        }
                        builder.append(" ");
                        out.write(builder.toString());
                        out.newLine();

                        //6-th line B-bpm phases for Off
                        nP = gdBpmBOff.getNumbOfPoints();
                        builder = new StringBuilder();
                        builder.append(" ");
                        for (int i = 0; i < nP; i++) {
                            builder.append(dblFormat.format(gdBpmBOff.getY(i))).append(" ");
                        }
                        builder.append(" ");
                        out.write(builder.toString());
                        out.newLine();

                        //7-th line C-bpm phases for Off
                        builder = new StringBuilder();
                        builder.append(" ");
                        for (int i = 0; i < nP; i++) {
                            builder.append(dblFormat.format(gdBpmCOff.getY(i))).append(" ");
                        }
                        builder.append(" ");
                        out.write(builder.toString());
                        out.newLine();

                        out.flush();
                    } catch (IOException exp) {
                        Toolkit.getDefaultToolkit().beep();
                        LOGGER.log(Level.WARNING, null, exp);
                    } catch (BackingStoreException exp) {
                        LOGGER.log(Level.WARNING, null, exp);
                    }
                }
                messageTextLocal.setText(null);
            } else {
                Toolkit.getDefaultToolkit().beep();
                messageTextLocal.setText(null);
                messageTextLocal.setText("Cannot save data as an ASCII file for dT procedure."
                        + " Clean and measure again!");
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }

    /**
     * Creates local analysis panel
     */
    private void makeLocalAnalysisPanel() {
        setToAccelButton.setForeground(Color.red);

        rfPhaseText.setNumberFormat(format);
        rfAmpText.setNumberFormat(format);
        inputEnergyDevText.setNumberFormat(format);
        currentAmpText.setNumberFormat(format);
        expSlopeText.setNumberFormat(format);
        energyStepText.setNumberFormat(format);

        rfPhaseText.setHorizontalAlignment(SwingConstants.CENTER);
        rfAmpText.setHorizontalAlignment(SwingConstants.CENTER);
        inputEnergyDevText.setHorizontalAlignment(SwingConstants.CENTER);
        currentAmpText.setHorizontalAlignment(SwingConstants.CENTER);
        expSlopeText.setHorizontalAlignment(SwingConstants.CENTER);
        rfPhaseText.setHorizontalAlignment(SwingConstants.CENTER);
        energyStepText.setHorizontalAlignment(SwingConstants.CENTER);

        rfPhaseText.setEditable(false);
        rfAmpText.setEditable(false);
        inputEnergyDevText.setEditable(false);
        currentAmpText.setEditable(false);
        expSlopeText.setEditable(false);
        rfPhaseText.setEditable(false);
        energyStepText.setEditable(false);

        rfPhaseText.setBackground(Color.white);
        rfAmpText.setBackground(Color.white);
        inputEnergyDevText.setBackground(Color.white);
        currentAmpText.setBackground(Color.white);
        expSlopeText.setBackground(Color.white);
        rfPhaseText.setBackground(Color.white);
        energyStepText.setBackground(Color.white);

        dphi12graphPanel.setAxisNameX("Delta Phi BPM #1, deg");
        ampDevGraphPanel.setAxisNameX("Slope of Phase Scan Line, deg");
        dphi12graphPanel.setAxisNameY("Delta Phi BPM #2, deg");
        ampDevGraphPanel.setAxisNameY("RF Ampl. Deviation, %");
        dphi12graphPanel.setOffScreenImageDrawing(true);
        ampDevGraphPanel.setOffScreenImageDrawing(true);

        ampDevGraphPanel.setGraphBackGroundColor(Color.BLACK);
        ampDevGraphPanel.setGridLineColor(Color.gray);

        dphi12graphPanel.setGraphBackGroundColor(Color.white);
        dphi12graphPanel.setGridLineColor(Color.gray);

        dphi12graphPanel.setLegendButtonVisible(true);
        ampDevGraphPanel.setLegendButtonVisible(true);

        //compose panels
        JPanel tmpName = new JPanel(new GridLayout(3, 1));

        tmpName.add(moduleNameLabel);
        tmpName.add(bpm1NameLabel);
        tmpName.add(bpm2NameLabel);
        tmpName.setBorder(etchedBorder);

        JPanel tmp0 = new JPanel(new GridLayout(2, 1));
        tmp0.add(rfPhaseText);
        tmp0.add(rfAmpText);

        JPanel tmp1 = new JPanel(new GridLayout(2, 1));
        tmp1.add(rfPhaseLabel);
        tmp1.add(rfAmpLabel);

        JPanel tmp2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tmp2.add(setToAccelButton);

        JPanel tmp3 = new JPanel(new BorderLayout());
        tmp3.add(tmp0, BorderLayout.WEST);
        tmp3.add(tmp1, BorderLayout.CENTER);
        tmp3.add(tmp2, BorderLayout.SOUTH);

        JPanel tmpRes = new JPanel(new BorderLayout());
        tmpRes.add(tmp3, BorderLayout.WEST);
        tmpRes.setBorder(etchedBorder);

        JPanel tmpTop = new JPanel(new BorderLayout());
        tmpTop.add(tmpName, BorderLayout.WEST);
        tmpTop.add(tmpRes, BorderLayout.CENTER);

        JPanel tmp10 = new JPanel(new GridLayout(4, 1));
        tmp10.add(inputEnergyDevLabel);
        tmp10.add(currentAmpLabel);
        tmp10.add(expSlopeLabel);
        tmp10.add(energyStepLabel);

        JPanel tmp11 = new JPanel(new GridLayout(4, 1));
        tmp11.add(inputEnergyDevText);
        tmp11.add(currentAmpText);
        tmp11.add(expSlopeText);
        tmp11.add(energyStepText);

        JPanel tmpLeftTop = new JPanel(new BorderLayout());
        tmpLeftTop.setBorder(etchedBorder);
        tmpLeftTop.add(aMatrixSwitchButton, BorderLayout.NORTH);
        tmpLeftTop.add(tmp10, BorderLayout.CENTER);
        tmpLeftTop.add(tmp11, BorderLayout.WEST);

        JPanel tmp20 = new JPanel(new BorderLayout());
        tmp20.setBorder(etchedBorder);
        tmp20.add(ampDevGraphPanel, BorderLayout.CENTER);

        JPanel tmpLeft = new JPanel(new BorderLayout());
        tmpLeft.add(tmpLeftTop, BorderLayout.NORTH);
        tmpLeft.add(tmp20, BorderLayout.CENTER);

        JPanel tmp30 = new JPanel(new BorderLayout());
        tmp30.setBorder(etchedBorder);
        tmp30.add(dphi12graphPanel, BorderLayout.CENTER);

        JPanel tmpCenter = new JPanel(new BorderLayout());
        tmpCenter.add(tmp30, BorderLayout.CENTER);
        tmpCenter.add(tmpLeft, BorderLayout.EAST);

        localAnalysisPanel.setLayout(new BorderLayout());

        localAnalysisPanel.add(tmpTop, BorderLayout.NORTH);
        localAnalysisPanel.add(tmpCenter, BorderLayout.CENTER);
    }

    /**
     * Reads theory data from data file
     *
     * @param fileNameTheoryDataIn Description of the Parameter
     */
    private void readTheoryData(String fileNameTheoryDataIn) {
        theoryData.clean();
        fileNameTheoryData = fileNameTheoryDataIn;

        URL dataURL = Application.getAdaptor().getResourceURL("data/delta_t/" + fileNameTheoryData);

        try (InputStream inps = dataURL.openStream()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(inps));
            String lineIn = in.readLine();
            String[] dataS;

            while (lineIn != null) {
                if (!lineIn.startsWith("%")) {
                    dataS = lineIn.split("\\s+");
                    theoryData.setDeltaPhiIni(Double.parseDouble(dataS[1]),
                            Double.parseDouble(dataS[2]));
                    break;
                } else {
                    lineIn = in.readLine();
                }
            }

            double[] a = new double[6];
            lineIn = in.readLine();
            while (lineIn != null) {
                dataS = lineIn.split("\\s+");
                if (dataS.length > 0
                        && !lineIn.startsWith("%")
                        && dataS.length == 7) {
                    for (int i = 0; i < 6; i++) {
                        a[i] = Double.parseDouble(dataS[i + 1]);
                    }
                    theoryData.addData(a);
                }
                lineIn = in.readLine();
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            Toolkit.getDefaultToolkit().beep();
            messageTextLocal.setText(null);
            messageTextLocal.setText("Fatal error. Can not read file ="
                    + fileNameTheoryData
                    + ". Stop execution. Call the developer.");
        }

    }

    /**
     * Performs analysis
     */
    private void makeDeltaTimeAnalysis() {

        if (!canSaveASDTtable()) {
            Toolkit.getDefaultToolkit().beep();
            messageTextLocal.setText(null);
            messageTextLocal.setText("Insufficient or wrong data."
                    + " Clean and measure again!");
            clearResultsOfAnalysis();
            return;
        }

        //set all graph data as non-active in updating graphs panel
        //It will speed up the method
        //at the end of this method this property should be restored
        bpm1ExGd.setImmediateContainerUpdate(false);
        bpm1InterpGd.setImmediateContainerUpdate(false);
        bpm2ExGd.setImmediateContainerUpdate(false);
        bpm2InterpGd.setImmediateContainerUpdate(false);

        bpm12ExGd.setImmediateContainerUpdate(false);
        bpm12InterpGd.setImmediateContainerUpdate(false);
        energyLineGd.setImmediateContainerUpdate(false);
        energyPosGd.setImmediateContainerUpdate(false);
        energyNegGd.setImmediateContainerUpdate(false);

        boolean analysisExists = false;
        if (bpm12ExGd.getNumbOfPoints() > 1) {
            analysisExists = true;
        }

        double y1;
        double y2;
        double x1;
        double x2;

        //interpolation coefficients of experimental data for bpm1 and bpm2
        double[][] coeff1;
        double[][] coeff2;

        if (!analysisExists) {
            //we have to calculate experimental data first
            bpm1ExGd.removeAllPoints();
            bpm2ExGd.removeAllPoints();
            bpm12ExGd.removeAllPoints();

            //calculate average phase for "off" state
            double phi1AvgOff = 0.;
            double phi2AvgOff = 0.;
            for (int i = 0; i < gdBpmBOff.getNumbOfPoints(); i++) {
                phi1AvgOff += gdBpmBOff.getY(i) / gdBpmBOff.getNumbOfPoints();
                phi2AvgOff += gdBpmCOff.getY(i) / gdBpmCOff.getNumbOfPoints();
            }

            for (int i = 0; i < gdBpmBOn.getNumbOfPoints(); i++) {
                y1 = -(gdBpmBOn.getY(i) - phi1AvgOff) - theoryData.getDeltaPhiIni1();
                y2 = -(gdBpmCOn.getY(i) - phi2AvgOff) - theoryData.getDeltaPhiIni2();

                y1 += 180.;
                while (y1 < 0.) {
                    y1 += 360.;
                }
                y1 = y1 % 360.;
                y1 -= 180.;

                y2 += 180.;
                while (y2 < 0.) {
                    y2 += 360.;
                }
                y2 = y2 % 360.;
                y2 -= 180.;

                bpm1ExGd.addPoint(gdBpmBOn.getX(i), y1, gdBpmBOn.getErr(i));
                bpm2ExGd.addPoint(gdBpmCOn.getX(i), y2, gdBpmCOn.getErr(i));
            }

            GraphDataOperations.unwrapData(bpm1ExGd);
            GraphDataOperations.unwrapData(bpm2ExGd);
            for (int i = 0; i < bpm1ExGd.getNumbOfPoints(); i++) {
                bpm12ExGd.addPoint(bpm1ExGd.getY(i), bpm2ExGd.getY(i));
            }

            //define interpolation from the beginning
            coeff1 = GraphDataOperations.polynomialFit(bpm1ExGd,
                    bpm1ExGd.getMinX(),
                    bpm1ExGd.getMaxX(),
                    1);

            coeff2 = GraphDataOperations.polynomialFit(bpm2ExGd,
                    bpm2ExGd.getMinX(),
                    bpm2ExGd.getMaxX(),
                    1);

        } else {
            //experimental data already exist, but we need to
            //recalculate coefficients within the new limits
            coeff1 = GraphDataOperations.polynomialFit(bpm1ExGd,
                    graphLeftPanel1.getCurrentMinX(),
                    graphLeftPanel1.getCurrentMaxX(),
                    1);

            coeff2 = GraphDataOperations.polynomialFit(bpm2ExGd,
                    graphLeftPanel2.getCurrentMinX(),
                    graphLeftPanel2.getCurrentMaxX(),
                    1);
        }

        if (coeff1 == null || coeff2 == null) {
            Toolkit.getDefaultToolkit().beep();
            messageTextLocal.setText(null);
            messageTextLocal.setText("Insufficient or wrong data."
                    + " Clean and measure again!");
            clearResultsOfAnalysis();
            return;
        }

        //set all coefficients
        double coeff1K = coeff1[0][1];
        double coeff1B = coeff1[0][0];

        double coeff2K = coeff2[0][1];
        double coeff2B = coeff2[0][0];

        double coeffK = coeff2K / coeff1K;
        double coeffB = (coeff2B * coeff1K - coeff1B * coeff2K) / coeff1K;

        double[] coeff = new double[2];
        coeff[0] = coeffB;
        coeff[1] = coeffK;

        //define linear approximations
        bpm1InterpGd.removeAllPoints();
        bpm2InterpGd.removeAllPoints();
        bpm12InterpGd.removeAllPoints();
        int nApp = 10;
        for (int i = 0; i < nApp; i++) {
            //bpm1 data approximation
            x1 = bpm1ExGd.getMinX() + i * (bpm1ExGd.getMaxX() - bpm1ExGd.getMinX()) / (nApp - 1);
            y1 = GraphDataOperations.polynom(x1, coeff1[0]);
            bpm1InterpGd.addPoint(x1, y1);

            //bpm1 data approximation
            x1 = bpm2ExGd.getMinX() + i * (bpm2ExGd.getMaxX() - bpm2ExGd.getMinX()) / (nApp - 1);
            y1 = GraphDataOperations.polynom(x1, coeff2[0]);
            bpm2InterpGd.addPoint(x1, y1);

            //bpm1-2 data approximation
            x1 = bpm12ExGd.getMinX() + i * (bpm12ExGd.getMaxX() - bpm12ExGd.getMinX()) / (nApp - 1);
            y1 = GraphDataOperations.polynom(x1, coeff);
            bpm12InterpGd.addPoint(x1, y1);
        }

        //ampl_dev in [%] of deviation from nominal
        double slope = Math.atan(coeffK) * 180. / Math.PI;
        double ampDev = ampDevVsSlopeThGd.getValueY(slope);
        ampDevVsSlopeExGd.removeAllPoints();
        ampDevVsSlopeExGd.addPoint(slope, ampDev);

        //real amplitude - what we have at this moment
        double amplCurrent = ((Double) gdBpmBOn.getGraphProperty("PARAMETER_VALUE"));
        double amplRecommended = amplCurrent / (1.0 + 0.01 * ampDev);
        currentAmpText.setValue(amplCurrent);
        expSlopeText.setValue(slope);
        rfAmpText.setValue(amplRecommended);

        //make energy graphs
        double ampDevTmp = 1.0;
        if (!aMatrixSwitchButton.isSelected()) {
            ampDevTmp = ampDev * 0.01 + 1.0;
        }

        //data a21 and a22 in eV/rad -> eV/grad
        double a11 = theoryData.getA(1, 1, ampDevTmp);
        double a12 = theoryData.getA(1, 2, ampDevTmp);
        double a21 = theoryData.getA(2, 1, ampDevTmp) * Math.PI / 180.;
        double a22 = theoryData.getA(2, 2, ampDevTmp) * Math.PI / 180.;

        x1 = bpm12ExGd.getMinX();
        x2 = bpm12ExGd.getMaxX();
        y1 = -(a11 / a12) * x1;
        y2 = -(a11 / a12) * x2;
        energyLineGd.removeAllPoints();
        energyLineGd.addPoint(x1, y1);
        energyLineGd.addPoint(x2, y2);

        double energyMin = Math.min(a21 * x1 + a22 * y1, a21 * x2 + a22 * y2);
        double energyMax = Math.max(a21 * x1 + a22 * y1, a21 * x2 + a22 * y2);

        //step from [keV] to [eV]
        double energyStep = energyStepText.getValue() * 1000.;

        int nEpMin = ((int) (energyMin / energyStep)) - 1;
        int nEpMax = ((int) (energyMax / energyStep)) + 1;

        double enrg;
        energyPosGd.removeAllPoints();
        energyNegGd.removeAllPoints();
        if (Math.abs(nEpMax - nEpMin) < 100) {
            for (int i = nEpMin; i <= nEpMax; i++) {
                enrg = i * energyStep;
                if (i != 0) {
                    x1 = enrg / (a21 - a22 * a11 / a12);
                    y1 = -(a11 / a12) * x1;
                    if (y1 <= energyLineGd.getMaxY()
                            && y1 >= energyLineGd.getMinY()) {
                        if (enrg > 0.) {
                            energyPosGd.addPoint(x1, y1);
                        } else {
                            energyNegGd.addPoint(x1, y1);
                        }
                    }
                }
            }
        }

        //calculate recommendet phase for the RF
        double phaseRecom = -(coeff1B * a11 + coeff2B * a12)
                / (coeff1K * a11 + coeff2K * a12);

        double phaseShift = MainAnalysisController.getPhaseShift(gdBpmBOn);
        phaseRecom -= phaseShift;

        phaseRecom += 180.;
        while (phaseRecom < 0.) {
            phaseRecom += 360.;
        }
        phaseRecom = phaseRecom % 360.;
        phaseRecom -= 180.;
        rfPhaseText.setValue(phaseRecom);

        //calculation of the energy
        double energyDelta = ((coeff2B * coeff1K - coeff1B * coeff2K)
                / (coeff1K * a11 + coeff2K * a12))
                * (a11 * a22 - a12 * a21);
        energyDelta = 0.001 * energyDelta;
        inputEnergyDevText.setValue(energyDelta);

        //update graphs
        bpm1ExGd.setImmediateContainerUpdate(true);
        bpm2ExGd.setImmediateContainerUpdate(true);

        bpm12ExGd.setImmediateContainerUpdate(true);

        dphi12graphPanel.clearZoomStack();
        ampDevGraphPanel.clearZoomStack();

        graphLeftPanel1.clearZoomStack();
        graphLeftPanel2.clearZoomStack();

        analysisDone = true;
    }

    /**
     * Sets the amplitudeAndPhaseToEPICS attribute of the
     * AnalysisCntrlTDProcedure object
     */
    private void setAmplitudeAndPhaseToEPICS() {
        if (analysisDone) {
            double ampVal = rfAmpText.getValue();
            double phaseVal = rfPhaseText.getValue();
            if (scanVariableParameter.getChannel() != null
                    && scanVariable.getChannel() != null) {
                scanVariableParameter.setValue(ampVal);
                scanVariable.setValue(phaseVal);
            } else {
                messageTextLocal.setText(null);
                messageTextLocal.setText("The parameter PV channel does not exist.");
                Toolkit.getDefaultToolkit().beep();
            }

        } else {
            Toolkit.getDefaultToolkit().beep();
            messageTextLocal.setText(null);
            messageTextLocal.setText("Perform analysis first!");
            clearResultsOfAnalysis();
        }
    }

    /**
     * The data container for theoretical data for delta-t procedure
     *
     * @author shishlo
     */
    static class DeltaTdata {

        private CubicSplineGraphData slopeData = new CubicSplineGraphData();
        private CubicSplineGraphData a11Data = new CubicSplineGraphData();
        private CubicSplineGraphData a12Data = new CubicSplineGraphData();
        private CubicSplineGraphData a21Data = new CubicSplineGraphData();
        private CubicSplineGraphData a22Data = new CubicSplineGraphData();

        private double deltaPhiIniBpm1 = 0.0;
        private double deltaPhiIniBpm2 = 0.0;

        /**
         * Constructor for the DeltaTdata object
         */
        DeltaTdata() {
        }

        /**
         * Sets the phases for default amplitude
         *
         * @param deltaPhiIniBpm1 The new phi value for bmp1
         * @param deltaPhiIniBpm2 The new phi value for bpm2
         */
        void setDeltaPhiIni(double deltaPhiIniBpm1, double deltaPhiIniBpm2) {
            this.deltaPhiIniBpm1 = deltaPhiIniBpm1;
            this.deltaPhiIniBpm2 = deltaPhiIniBpm2;
        }

        /**
         * Adds slope and matrix elements of A to the DeltaTdata object
         *
         * @param a The data array [amplitude,slope,a11,12,a21,a22]
         */
        void addData(double[] a) {
            if (a.length == 6) {
                slopeData.addPoint(a[0], a[1]);
                a11Data.addPoint(a[0], a[2]);
                a12Data.addPoint(a[0], a[3]);
                a21Data.addPoint(a[0], a[4]);
                a22Data.addPoint(a[0], a[5]);
            }
        }

        /**
         * Gets the deltaPhiIni1 attribute of the DeltaTdata object
         *
         * @return The deltaPhiIni1 value
         */
        double getDeltaPhiIni1() {
            return deltaPhiIniBpm1;
        }

        /**
         * Gets the deltaPhiIni2 attribute of the DeltaTdata object
         *
         * @return The deltaPhiIni2 value
         */
        double getDeltaPhiIni2() {
            return deltaPhiIniBpm2;
        }

        /**
         * Gets the numbPoints attribute of the DeltaTdata object
         *
         * @return The numbPoints value
         */
        int getNumbPoints() {
            return slopeData.getNumbOfPoints();
        }

        /**
         * Gets the amplitude attribute of the DeltaTdata object
         *
         * @param index Description of the Parameter
         * @return The amplitude value
         */
        double getAmplitude(int index) {
            return slopeData.getX(index);
        }

        /**
         * Gets the slope attribute of the DeltaTdata object
         *
         * @param index Description of the Parameter
         * @return The slope value
         */
        double getSlope(int index) {
            return slopeData.getY(index);
        }

        /**
         * Gets the slope attribute of the DeltaTdata object
         *
         * @param amp Description of the Parameter
         * @return The slope value
         */
        double getSlope(double amp) {
            return slopeData.getValueY(amp);
        }

        /**
         * Gets the a attribute of the DeltaTdata object
         *
         * @param i Description of the Parameter
         * @param j Description of the Parameter
         * @param index Description of the Parameter
         * @return The a value
         */
        double getA(int i, int j, int index) {
            if (i == 1 && j == 1) {
                return a11Data.getY(index);
            }
            if (i == 1 && j == 2) {
                return a12Data.getY(index);
            }
            if (i == 2 && j == 1) {
                return a21Data.getY(index);
            }
            if (i == 2 && j == 2) {
                return a22Data.getY(index);
            }
            return 0.;
        }

        /**
         * Gets the a attribute of the DeltaTdata object
         *
         * @param i Description of the Parameter
         * @param j Description of the Parameter
         * @param amp Description of the Parameter
         * @return The a value
         */
        double getA(int i, int j, double amp) {
            if (i == 1 && j == 1) {
                return a11Data.getValueY(amp);
            }
            if (i == 1 && j == 2) {
                return a12Data.getValueY(amp);
            }
            if (i == 2 && j == 1) {
                return a21Data.getValueY(amp);
            }
            if (i == 2 && j == 2) {
                return a22Data.getValueY(amp);
            }
            return 0.;
        }

        /**
         * Remove all data
         */
        void clean() {
            slopeData.removeAllPoints();
            a11Data.removeAllPoints();
            a12Data.removeAllPoints();
            a21Data.removeAllPoints();
            a22Data.removeAllPoints();

            deltaPhiIniBpm1 = 0.0;
            deltaPhiIniBpm2 = 0.0;
        }
    }
}
