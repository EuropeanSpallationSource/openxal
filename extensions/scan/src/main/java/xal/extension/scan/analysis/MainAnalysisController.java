/*
 *  AnalysisController.java
 *
 *  Created on September 26, 2003, 10:25 AM
 */
package xal.extension.scan.analysis;

import javax.swing.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.*;

import xal.extension.scan.*;
import xal.extension.widgets.plot.*;
import xal.tools.data.DataAdaptor;

/**
 * AnalysisController is a class to handle analysis of the scan data.
 *
 * @author shishlo
 */
public class MainAnalysisController {

    private static final Logger LOGGER = Logger.getLogger(MainAnalysisController.class.getName());

    //variables from upper level application
    private Object mainScanDocument = null;

    private boolean scanPVShowState = false;
    private boolean scanPVRBShowState = false;

    private JPanel parentAnalysisPanel = null;
    private JPanel analysisControlPanel = null;
    private JPanel customGraphPanel = null;

    private ScanVariable scanVariableParameter = null;
    private ScanVariable scanVariable = null;
    private Vector<MeasuredValue> measuredValuesV = null;
    private Vector<Boolean> measuredValuesShowStateV = null;
    private FunctionGraphsJPanel graphScan = null;
    private FunctionGraphsJPanel graphAnalysis = null;
    private JTextField messageTextLocal = new JTextField(40);

    //place for custom control elements
    private JPanel customControlPanel = new JPanel();

    //panel for buttons
    private JPanel globalButtonsPanel = new JPanel();

    //general global buttons
    private JButton hideGlobalButton = new JButton("HIDE");
    private JButton showAllGlobalButton = new JButton("SHOW ALL");
    private JButton removePointGlobalButton = new JButton("REMOVE POINT");
    private JButton removeGraphGlobalButton = new JButton("REMOVE GRAPH");
    private JButton removeAllGlobalButton = new JButton("REMOVE ALL");
    private JButton wrapGraphGlobalButton = new JButton("WRAP DATA");
    private JButton exportGraphGlobalButton = new JButton("EXPORT ASCII");
    private JButton exportGraphToCSVGlobalButton = new JButton("EXPORT CSV");
    private JButton incrColorGlobalButton = new JButton("INCR. COLOR");
    private JButton dataColorGlobalButton = new JButton("DATA COLOR");
    private JButton removeTmpGlobalButton = new JButton("REMOVE TEMP.");
    private JButton phaseShiftGlobalButton = new JButton("SHIFT x-PHASE");
    private JButton removePhaseShiftGlobalButton = new JButton("UN-SHIFT");

    //data reader as part of main element panel
    /**
     * Description of the Field
     */
    protected JPanel dataReaderPanel = null;

    private JLabel xValPanelMainLabel = new JLabel(" X value : ");
    private JLabel yValPanelMainLabel = new JLabel(" Y value : ");
    private JTextField xValPanelMainText = null;
    private JTextField yValPanelMainText = null;

    private static DecimalFormat xyPanelMainFormat = new DecimalFormat("0.00000E0");

    private static TitledBorder dataReaderBorder = null;

    private static String phaseShiftGraphPropertyKey = "X_PHASE_SHIFT";

    //------------------------------------------------
    //CuSTOM ANALYSIS PANELS
    //------------------------------------------------
    //main element
    private String[] operationList = new String[0];
    private AnalysisController[] analysisControllers = new AnalysisController[0];

    private JComboBox<String> operationChooser = null;

    private int indexOfPanelNew = -1;
    private int indexOfPanelOld = -1;

    //local data file
    private File dataFile = null;

    // data file to hold comma separated values
    private File csvFile = null;

    //local font
    private Font font;

    //------------------------------------------------
    //AUXILIARY MEMBERS
    //------------------------------------------------
    private JLabel chooserLabel = new JLabel("-------    ANALYSIS CONTROL    ---------");

    //local temporary draph data
    private BasicGraphData graphDataLocal = new BasicGraphData();

    /**
     * Constructor. The first parameter is the parent panel. It is used to
     * refresh the whole analysis panel. The second is used to place control
     * elements. The third is used to place custom graph panels and general
     * buttons panel. The 4-th is the scan variable. The 5-th is the vector with
     * MeasuredValues instances. The 6-th includes masks specifying should be
     * shown graphs from MeasuredValues or not. The 7-th is the graph panel to
     * show graph data.
     *
     * @param mainScanDocumentIn Description of the Parameter
     * @param parentAnalysisPanelIn Description of the Parameter
     * @param analysisControlPanelIn Description of the Parameter
     * @param customGraphPanelIn Description of the Parameter
     * @param scanVariableParameterIn Description of the Parameter
     * @param scanVariableIn Description of the Parameter
     * @param measuredValuesVIn Description of the Parameter
     * @param measuredValuesShowStateVIn Description of the Parameter
     * @param graphScanIn Description of the Parameter
     * @param graphAnalysisIn Description of the Parameter
     */
    public MainAnalysisController(Object mainScanDocumentIn,
            JPanel parentAnalysisPanelIn,
            JPanel analysisControlPanelIn,
            JPanel customGraphPanelIn,
            ScanVariable scanVariableParameterIn,
            ScanVariable scanVariableIn,
            Vector<MeasuredValue> measuredValuesVIn,
            Vector<Boolean> measuredValuesShowStateVIn,
            FunctionGraphsJPanel graphScanIn,
            FunctionGraphsJPanel graphAnalysisIn) {

        mainScanDocument = mainScanDocumentIn;
        parentAnalysisPanel = parentAnalysisPanelIn;
        analysisControlPanel = analysisControlPanelIn;
        customGraphPanel = customGraphPanelIn;
        scanVariableParameter = scanVariableParameterIn;
        scanVariable = scanVariableIn;
        measuredValuesV = measuredValuesVIn;
        measuredValuesShowStateV = measuredValuesShowStateVIn;
        graphScan = graphScanIn;
        graphAnalysis = graphAnalysisIn;

        //set analysis graph panel proterty
        graphAnalysis.setLegendButtonVisible(true);
        graphAnalysis.setChooseModeButtonVisible(true);
        graphAnalysis.setVerLinesButtonVisible(true);
        graphAnalysis.setLegendBackground(Color.white);

        //result of analysis graph properties
        graphDataLocal.setImmediateContainerUpdate(false);
        graphDataLocal.setDrawLinesOn(true);
        graphDataLocal.setDrawPointsOn(false);
        graphDataLocal.setGraphColor(Color.blue);
        graphDataLocal.setLineThick(3);
        graphDataLocal.setGraphProperty(graphAnalysis.getLegendKeyString(), "FITTING");

        //define global buttons panel
        defineGlobalButtons();

        //create data reader panel
        dataReaderPanel = new JPanel();
        xValPanelMainText = graphAnalysis.getClickedPointObject().getxValueText();
        yValPanelMainText = graphAnalysis.getClickedPointObject().getyValueText();
        graphAnalysis.getClickedPointObject().setxValueFormat(xyPanelMainFormat);
        graphAnalysis.getClickedPointObject().setyValueFormat(xyPanelMainFormat);
        Border etchedBorder = BorderFactory.createEtchedBorder();
        dataReaderBorder = BorderFactory.createTitledBorder(etchedBorder, "GRAPH DATA READER");
        dataReaderPanel.setBackground(dataReaderPanel.getBackground().darker());
        dataReaderPanel.setBorder(dataReaderBorder);
        dataReaderPanel.setLayout(new GridLayout(2, 2, 1, 1));
        dataReaderPanel.add(xValPanelMainLabel);
        dataReaderPanel.add(xValPanelMainText);
        dataReaderPanel.add(yValPanelMainLabel);
        dataReaderPanel.add(yValPanelMainText);

        //define main chooser
        operationChooser = new JComboBox<>(operationList);
        operationChooser.setBackground(Color.cyan);
        operationChooser.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = operationChooser.getSelectedIndex();
                setAnalysisControlPanel(index);
            }
        });

        customControlPanel.setLayout(new BorderLayout());
        customControlPanel.removeAll();
        customGraphPanel.removeAll();

        analysisControlPanel.setBackground(customControlPanel.getBackground().darker());
        analysisControlPanel.setBorder(etchedBorder);

        analysisControlPanel.setLayout(new BorderLayout());
        analysisControlPanel.add(chooserLabel, BorderLayout.NORTH);
        analysisControlPanel.add(operationChooser, BorderLayout.CENTER);
        analysisControlPanel.add(customControlPanel, BorderLayout.SOUTH);

        setAnalysisControlPanel(-1);

    }

    /**
     * Returns the key string for graph properties specifying the x-coordinate
     * phase shift.
     *
     * @return The phase shift key string
     */
    public static String getPhaseShiftKey() {
        return phaseShiftGraphPropertyKey;
    }

    /**
     * Sets fonts for all GUI elements.
     *
     * @param fnt The new fontsForAll value
     */
    public void setFontsForAll(Font fnt) {
        font = fnt;
        hideGlobalButton.setFont(fnt);
        showAllGlobalButton.setFont(fnt);
        removePointGlobalButton.setFont(fnt);
        removeGraphGlobalButton.setFont(fnt);
        removeAllGlobalButton.setFont(fnt);
        wrapGraphGlobalButton.setFont(fnt);
        exportGraphGlobalButton.setFont(fnt);
        exportGraphToCSVGlobalButton.setFont(fnt);
        incrColorGlobalButton.setFont(fnt);
        dataColorGlobalButton.setFont(fnt);
        removeTmpGlobalButton.setFont(fnt);
        phaseShiftGlobalButton.setFont(fnt);
        removePhaseShiftGlobalButton.setFont(fnt);

        operationChooser.setFont(fnt);
        ((JTextField) operationChooser.getEditor().getEditorComponent()).setFont(fnt);
        operationChooser.setPreferredSize(new Dimension(1, fnt.getSize() + 10));

        for (int i = 0; i < analysisControllers.length; i++) {
            analysisControllers[i].setFontsForAll(fnt);
        }

        chooserLabel.setFont(fnt);

        //data reader panel
        xValPanelMainLabel.setFont(fnt);
        yValPanelMainLabel.setFont(fnt);
        xValPanelMainText.setFont(fnt);
        yValPanelMainText.setFont(fnt);
        dataReaderBorder.setTitleFont(fnt);

        //repaint
        parentAnalysisPanel.validate();
        parentAnalysisPanel.repaint();
    }

    /**
     * Sets local message text field.
     *
     * @param messageTextLocal The new messageTextField value
     */
    public void setMessageTextField(JTextField messageTextLocal) {
        this.messageTextLocal = messageTextLocal;
        for (int i = 0; i < analysisControllers.length; i++) {
            analysisControllers[i].setMessageTextField(messageTextLocal);
        }
    }

    /**
     * Returns the main scan document reference as Object instance. It is used
     * to define the resource location.
     *
     * @return The mainScanDocument value
     */
    public Object getMainScanDocument() {
        return mainScanDocument;
    }

    /**
     * Creates all custom analysis.
     *
     * @return The dataReaderPanel value
     */
    public JPanel getDataReaderPanel() {
        return dataReaderPanel;
    }

    /**
     * Creates all custom analysis.
     *
     * @param analysisConfig Description of the Parameter
     */
    public void createChildAnalysis(DataAdaptor analysisConfig) {

        Vector<AnalysisController> analysisContrV = new Vector<>();

        analysisControlPanel.removeAll();
        customControlPanel.removeAll();
        customGraphPanel.removeAll();

        for (final DataAdaptor analysisConf : analysisConfig.childAdaptors()) {
            AnalysisController ac = AnalysisControllerFactory.getAC(this,
                    analysisConf,
                    parentAnalysisPanel,
                    customControlPanel,
                    customGraphPanel,
                    globalButtonsPanel,
                    scanVariableParameter,
                    scanVariable,
                    measuredValuesV,
                    graphAnalysis,
                    messageTextLocal,
                    graphDataLocal);

            analysisContrV.add(ac);
        }
        analysisControllers = new AnalysisController[analysisContrV.size()];
        operationList = new String[analysisContrV.size()];
        for (int i = 0; i < analysisContrV.size(); i++) {
            analysisControllers[i] = analysisContrV.get(i);
            operationList[i] = analysisControllers[i].getName();
            analysisControllers[i].setFontsForAll(font);
        }

        //define main chooser
        operationChooser = new JComboBox<>(operationList);
        operationChooser.setBackground(Color.cyan);
        operationChooser.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = operationChooser.getSelectedIndex();
                setAnalysisControlPanel(index);
            }
        });

        font = operationChooser.getFont();
        setFontsForAll(font);

        analysisControlPanel.add(chooserLabel, BorderLayout.NORTH);
        analysisControlPanel.add(operationChooser, BorderLayout.CENTER);
        analysisControlPanel.add(customControlPanel, BorderLayout.SOUTH);

        indexOfPanelOld = -1;
        indexOfPanelNew = -1;

        graphAnalysis.removeVerticalValues();

        setAnalysisControlPanel(0);
    }

    /**
     * Sets the configurations of the analysis by passing config file to the
     * children.
     *
     * @param analysisConfig Description of the Parameter
     */
    public void dumpChildAnalysisConfig(DataAdaptor analysisConfig) {
        for (int i = 0; i < analysisControllers.length; i++) {
            DataAdaptor aConf = analysisConfig.createChild(analysisControllers[i].getTypeName());
            analysisControllers[i].dumpAnalysisConfig(aConf);
        }
    }

    /**
     * Sets mask specifying if the data for scan PV scan read back PV should be
     * shown.
     *
     * @param scanPVShowState The new scanPVandScanPV_RB_State value
     * @param scanPVRBShowState The new scanPVandScanPV_RB_State value
     */
    public void setScanPVandScanPV_RB_State(boolean scanPVShowState, boolean scanPVRBShowState) {
        this.scanPVShowState = scanPVShowState;
        this.scanPVRBShowState = scanPVRBShowState;
        for (int i = 0; i < analysisControllers.length; i++) {
            analysisControllers[i].setScanPVandScanPV_RB_State(scanPVShowState, scanPVRBShowState);
        }
    }

    /**
     * Updates data on the analysis graph panel. This method should be called
     * outside of the analysis controller.
     */
    public void updateDataSetOnGraphPanel() {
        graphAnalysis.removeAllGraphData();
        graphAnalysis.clearZoomStack();
        for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
            if (measuredValuesShowStateV.get(i)) {
                MeasuredValue mv_tmp = measuredValuesV.get(i);
                if (scanPVShowState || scanVariable.getChannel() == null) {
                    graphAnalysis.addGraphData(mv_tmp.getDataContainers());
                }
                if (scanPVRBShowState) {
                    graphAnalysis.addGraphData(mv_tmp.getDataContainersRB());
                }
            }
        }

        graphAnalysis.addGraphData(graphDataLocal);

        if (indexOfPanelNew < analysisControllers.length) {
            analysisControllers[indexOfPanelNew].updateDataSetOnGraphPanel();
        }
    }

    /**
     * Will show the specific analysis with index indexOfPanelNewIn.
     *
     * @param indexOfPanelNewIn The new analysisControlPanel value
     */
    private void setAnalysisControlPanel(int indexOfPanelNewIn) {

        if (indexOfPanelNewIn >= 0 && indexOfPanelNewIn < analysisControllers.length) {

            indexOfPanelNew = indexOfPanelNewIn;

            messageTextLocal.setText(null);
            if (indexOfPanelOld >= 0
                    && indexOfPanelOld < analysisControllers.length) {
                analysisControllers[indexOfPanelOld].ShutUp();
            }
            if (indexOfPanelNew >= 0
                    && indexOfPanelNew < analysisControllers.length) {
                analysisControllers[indexOfPanelNew].ShowUp();
            }

            indexOfPanelOld = indexOfPanelNew;
        }
        parentAnalysisPanel.validate();
        parentAnalysisPanel.repaint();
    }

    /**
     * This method executed when analysis panel showing up.
     */
    public void isGoingShowUp() {
    }

    /**
     * This method executed when analysis panel is about to be shut up.
     */
    public void isGoingShutUp() {
        restoreDataColoring();
    }

    /**
     * Sets global buttons and actions.
     */
    private void defineGlobalButtons() {

        //define global buttons panel
        globalButtonsPanel.setLayout(new GridLayout(3, 5, 1, 1));

        globalButtonsPanel.add(hideGlobalButton);
        globalButtonsPanel.add(showAllGlobalButton);
        globalButtonsPanel.add(removePointGlobalButton);
        globalButtonsPanel.add(removeGraphGlobalButton);
        globalButtonsPanel.add(removeAllGlobalButton);
        globalButtonsPanel.add(wrapGraphGlobalButton);
        globalButtonsPanel.add(exportGraphGlobalButton);
        globalButtonsPanel.add(exportGraphToCSVGlobalButton);
        globalButtonsPanel.add(incrColorGlobalButton);
        globalButtonsPanel.add(dataColorGlobalButton);
        globalButtonsPanel.add(removeTmpGlobalButton);
        globalButtonsPanel.add(phaseShiftGlobalButton);
        globalButtonsPanel.add(removePhaseShiftGlobalButton);
        globalButtonsPanel.add(new JPanel());
        globalButtonsPanel.add(new JPanel());
        globalButtonsPanel.add(new JPanel());

        hideGlobalButton.setToolTipText("Hides a selected graph");
        showAllGlobalButton.setToolTipText("Shows all previously hidden graphs");
        removePointGlobalButton.setToolTipText("Removes a selected point");
        removeGraphGlobalButton.setToolTipText("Removes a selected graph");
        removeAllGlobalButton.setToolTipText("Removes all visible graphs");
        wrapGraphGlobalButton.setToolTipText("Wrap phase scan data using -180 +180 interval");
        exportGraphGlobalButton.setToolTipText("Writes data of a selected graph into file");
        exportGraphToCSVGlobalButton.setToolTipText("Export all displayed data to a file in CSV format.");
        incrColorGlobalButton.setToolTipText("Applay incremental coloring to visible graphs");
        dataColorGlobalButton.setToolTipText("Apply data color to the graph (colors by default)");
        removeTmpGlobalButton.setToolTipText("Remove a temporary graph created during analysis");
        phaseShiftGlobalButton.setToolTipText("Shift x-coordinates (phase) by +25 degrees");
        removePhaseShiftGlobalButton.setToolTipText("Un-shift x-coordinates (phase)");

        //"HIDE" button
        hideGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BasicGraphData gd = getChoosenDraphData();
                if (gd != null) {
                    graphAnalysis.removeGraphData(gd);
                    messageTextLocal.setText(null);
                } else {
                    messageTextLocal.setText(null);
                    messageTextLocal.setText("Please choose graph first. Use S-button on the graph panel.");
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        //"SHOW ALL" button
        showAllGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDataSetOnGraphPanel();
                messageTextLocal.setText(null);
            }
        });

        //"REMOVE POINT" button
        removePointGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] choosenObjArr = getChoosenDraphDataAndPoint();
                BasicGraphData gd = (BasicGraphData) choosenObjArr[0];
                Integer IndP = (Integer) choosenObjArr[1];
                if (gd != null && IndP != null) {
                    gd.removePoint(IndP.intValue());
                    graphAnalysis.refreshGraphJPanel();
                } else {
                    messageTextLocal.setText(null);
                    messageTextLocal.setText("Please choose graph and point first. Use S-button on the graph panel.");
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        //"REMOVE GRAPH" button
        removeGraphGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BasicGraphData gd = getChoosenDraphData();
                Integer Ind = graphAnalysis.getGraphChosenIndex();
                if (gd != null) {
                    if (gd != graphDataLocal) {
                        graphAnalysis.removeGraphData(gd);
                        for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
                            MeasuredValue mv_tmp = measuredValuesV.get(i);
                            mv_tmp.removeDataContainer(gd);
                        }
                    } else {
                        graphDataLocal.removeAllPoints();
                    }
                    graphAnalysis.refreshGraphJPanel();
                    messageTextLocal.setText(null);
                } else {
                    messageTextLocal.setText(null);
                    messageTextLocal.setText("Please choose graph and point first. Use S-button on the graph panel.");
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        //"REMOVE ALL" button
        removeAllGlobalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
                    if (measuredValuesShowStateV.get(i)) {
                        MeasuredValue mv_tmp = measuredValuesV.get(i);
                        if (scanPVShowState || scanVariable.getChannel() == null) {
                            mv_tmp.removeAllDataContainersNonRB();
                        }
                        if (scanPVRBShowState) {
                            mv_tmp.removeAllDataContainersRB();
                        }
                    }
                }
                updateDataSetOnGraphPanel();
                messageTextLocal.setText(null);
            }
        });

        //"WRAP DATA" button
        wrapGraphGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BasicGraphData gd = getChoosenDraphData();
                if (gd != null) {
                    GraphDataOperations.unwrapData(gd);
                    graphAnalysis.refreshGraphJPanel();
                    messageTextLocal.setText(null);
                } else {
                    messageTextLocal.setText(null);
                    messageTextLocal.setText("Please choose graph first. Use S-button on the graph panel.");
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        //"EXPORT ASCII"
        exportGraphGlobalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BasicGraphData gd = getChoosenDraphData();
                if (gd != null) {
                    JFileChooser ch = new JFileChooser();
                    ch.setDialogTitle("Export to ASCII");
                    if (dataFile != null) {
                        ch.setSelectedFile(dataFile);
                    }
                    int returnVal = ch.showSaveDialog(parentAnalysisPanel);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            dataFile = ch.getSelectedFile();
                            BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));
                            int nP = gd.getNumbOfPoints();
                            for (int i = 0; i < nP; i++) {
                                out.write(" " + gd.getX(i) + " " + gd.getY(i) + " " + gd.getErr(i));
                                out.newLine();
                            }
                            out.flush();
                            out.close();
                        } catch (IOException exp) {
                            Toolkit.getDefaultToolkit().beep();
                            LOGGER.log(Level.INFO, exp.toString());
                        }
                    }
                    messageTextLocal.setText(null);
                } else {
                    if (canSaveAsTable()) {
                        //save data as table
                        JFileChooser ch = new JFileChooser();
                        ch.setDialogTitle("Export to ASCII as a Table");
                        if (dataFile != null) {
                            ch.setSelectedFile(dataFile);
                        }
                        int returnVal = ch.showSaveDialog(parentAnalysisPanel);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            try {
                                dataFile = ch.getSelectedFile();
                                BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));
                                Vector<BasicGraphData> gdV_tmp = graphAnalysis.getAllGraphData();

                                Vector<BasicGraphData> gdV = new Vector<>();
                                for (int i = 0; i < gdV_tmp.size(); i++) {
                                    gd = gdV_tmp.get(i);
                                    if (gd.getNumbOfPoints() > 0) {
                                        gdV.add(gd);
                                    }
                                }

                                gd = gdV.get(0);
                                int nP = gd.getNumbOfPoints();

                                for (int i = 0; i < gdV.size(); i++) {
                                    gd = gdV.get(i);
                                    out.write("% data # " + i + "  Legend = "
                                            + gd.getGraphProperty(graphAnalysis.getLegendKeyString()));
                                    out.newLine();
                                }

                                out.write("% x/data # ");
                                for (int i = 0; i < gdV.size(); i++) {
                                    out.write("       " + i + "    ");

                                }
                                out.newLine();

                                for (int j = 0; j < nP; j++) {
                                    gd = gdV.get(0);
                                    out.write(" " + xyPanelMainFormat.format(gd.getX(j)));

                                    for (int i = 0; i < gdV.size(); i++) {
                                        gd = gdV.get(i);
                                        out.write(" " + xyPanelMainFormat.format(gd.getY(j)));

                                    }

                                    out.write(" ");
                                    out.newLine();
                                }

                                out.flush();
                                out.close();
                            } catch (IOException exp) {
                                Toolkit.getDefaultToolkit().beep();
                                LOGGER.log(Level.INFO, exp.toString());
                            }
                        }
                        messageTextLocal.setText(null);
                    } else {
                        messageTextLocal.setText(null);
                        messageTextLocal.setText("Please choose graph first. Use S-button on the graph panel.");
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        });

        exportGraphToCSVGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (canSaveAsTable()) {
                    exportDisplayedGraphToCSV();
                } else {
                    messageTextLocal.setText("The X data is not common to all graphs. Please select only one of either the Setpoint or Readback under Scan PV.");
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
        );

        //"INCR. COLOR" button
        incrColorGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vector<BasicGraphData> gdV = graphAnalysis.getAllGraphData();
                int count = 0;
                for (int i = 0, n = gdV.size(); i < n; i++) {
                    BasicGraphData gd = gdV.get(i);
                    if (gd != graphDataLocal) {
                        gd.setGraphColor(IncrementalColor.getColor(count));
                        count++;
                    }
                }
                graphAnalysis.refreshGraphJPanel();
                messageTextLocal.setText(null);
            }
        });

        //"DATA COLOR" button
        dataColorGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restoreDataColoring();
                messageTextLocal.setText(null);
            }
        });

        //"REMOVE TEMP." button
        removeTmpGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphDataLocal.removeAllPoints();
                graphAnalysis.refreshGraphJPanel();
                messageTextLocal.setText(null);
            }
        });

        //SHIFT x-Phase button
        phaseShiftGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vector<BasicGraphData> gdV = graphAnalysis.getAllGraphData();
                for (int i = 0, n = gdV.size(); i < n; i++) {
                    BasicGraphData gd = gdV.get(i);
                    if (gd != graphDataLocal) {
                        shiftGraphData(gd, 25.);
                    }
                }
                graphAnalysis.refreshGraphJPanel();
                messageTextLocal.setText(null);
            }
        });

        //UN-SHIFT button
        removePhaseShiftGlobalButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vector<BasicGraphData> gdV = graphAnalysis.getAllGraphData();
                for (int i = 0, n = gdV.size(); i < n; i++) {
                    BasicGraphData gd = gdV.get(i);
                    if (gd != graphDataLocal) {
                        unShiftGraphData(gd);
                    }
                }
                graphAnalysis.refreshGraphJPanel();
                messageTextLocal.setText(null);
            }
        });
    }

    /**
     * Export displayed graph data to a file in comma separated value (CSV)
     * format
     */
    private void exportDisplayedGraphToCSV() {
        //save data as table
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export to CSV as a Table");
        if (csvFile != null) {
            chooser.setSelectedFile(csvFile);
        } else {
            csvFile = new File("Data.csv");
            chooser.setSelectedFile(csvFile);
        }
        int returnVal = chooser.showSaveDialog(parentAnalysisPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                final Vector<BasicGraphData> allGraphData = graphAnalysis.getAllGraphData();

                final Vector<BasicGraphData> validGraphData = new Vector<>();
                for (int i = 0; i < allGraphData.size(); i++) {
                    final BasicGraphData graphData = allGraphData.get(i);
                    if (graphData.getNumbOfPoints() > 0) {
                        validGraphData.add(graphData);
                    }
                }

                final int numPoints = validGraphData.get(0).getNumbOfPoints();

                if (validGraphData.size() > 0) {
                    csvFile = chooser.getSelectedFile();
                    final BufferedWriter out = new BufferedWriter(new FileWriter(csvFile));

                    out.write("" + validGraphData.get(0).getGraphProperty("xLabel"));
                    for (int column = 0; column < validGraphData.size(); column++) {
                        final BasicGraphData graphData = validGraphData.get(column);
                        out.write(", " + graphData.getGraphProperty("yLabel"));
                    }
                    out.newLine();

                    for (int row = 0; row < numPoints; row++) {
                        out.write(" " + xyPanelMainFormat.format(validGraphData.get(0).getX(row)));

                        for (int column = 0; column < validGraphData.size(); column++) {
                            final BasicGraphData graphData = validGraphData.get(column);
                            out.write(", " + xyPanelMainFormat.format(graphData.getY(row)));

                        }
                        out.newLine();
                    }

                    out.flush();
                    out.close();
                }
            } catch (IOException exception) {
                Toolkit.getDefaultToolkit().beep();
                LOGGER.log(Level.SEVERE, null, exception);
            }
        }
    }

    /**
     * Description of the Method
     */
    private void restoreDataColoring() {
        for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
            MeasuredValue mv_tmp = measuredValuesV.get(i);
            mv_tmp.setColor(IncrementalColor.getColor(i));
        }
        graphAnalysis.refreshGraphJPanel();
        graphScan.refreshGraphJPanel();
    }

    /**
     * Shifts all data along x-axis by the shift value
     *
     * @param gd The graph data container
     * @param shiftVal The shift value
     */
    private static void shiftGraphData(BasicGraphData gd, double shiftVal) {
        String key = getPhaseShiftKey();
        if (gd.getGraphProperty(key) == null) {
            gd.setGraphProperty(key, 0.);
        }
        double shiftOld = ((Double) gd.getGraphProperty(key));
        double shiftTotal = shiftOld + shiftVal;
        shiftTotal += 180.;
        while (shiftTotal < 0.) {
            shiftTotal += 360.;
        }
        shiftTotal = shiftTotal % 360.;
        shiftTotal -= 180.;
        gd.setGraphProperty(key, shiftTotal);
        int nP = gd.getNumbOfPoints();
        double[] x = new double[nP];
        double[] y = new double[nP];
        double[] err = new double[nP];
        for (int i = 0; i < nP; i++) {
            x[i] = gd.getX(i);
            y[i] = gd.getY(i);
            err[i] = gd.getErr(i);
            x[i] += shiftVal;
            x[i] = x[i] + 180.0;
            while (x[i] < 0.) {
                x[i] += 360.;
            }
            x[i] = x[i] % 360;
            x[i] = x[i] - 180.;
        }
        gd.updateValues(x, y, err);
    }

    /**
     * Unshifts all data along x-axis
     *
     * @param gd The graph data container
     */
    private static void unShiftGraphData(BasicGraphData gd) {
        String key = getPhaseShiftKey();
        if (gd.getGraphProperty(key) != null) {
            double shift = ((Double) gd.getGraphProperty(key));
            shift = shift % 360;
            int nP = gd.getNumbOfPoints();
            double[] x = new double[nP];
            double[] y = new double[nP];
            double[] err = new double[nP];
            for (int i = 0; i < nP; i++) {
                x[i] = gd.getX(i);
                y[i] = gd.getY(i);
                err[i] = gd.getErr(i);
                x[i] -= shift;
                x[i] = x[i] + 180.0;
                while (x[i] < 0.) {
                    x[i] += 360.;
                }
                x[i] = x[i] % 360;
                x[i] = x[i] - 180.;
            }
            gd.updateValues(x, y, err);
            gd.setGraphProperty(key, null);
        }
    }

    /**
     * Returns the average phase shift value for graphs
     *
     * @param gdV The vector with graph data objects
     * @return The phase shift value
     */
    public static double getPhaseShift(Vector<BasicGraphData> gdV) {
        String key = getPhaseShiftKey();
        double shift = 0.;
        int nCount = 0;
        for (int i = 0, n = gdV.size(); i < n; i++) {
            BasicGraphData gd = gdV.get(i);
            int nP = gd.getNumbOfPoints();
            if (nP > 0) {
                if (gd.getGraphProperty(key) != null) {
                    shift += ((Double) gd.getGraphProperty(key));
                    nCount++;
                }
            }
        }
        if (nCount > 0) {
            shift /= nCount;
        }
        return shift;
    }

    /**
     * Returns the phase shift value for graph
     *
     * @param gd The graph data objects
     * @return The phase shift value
     */
    public static double getPhaseShift(BasicGraphData gd) {
        String key = getPhaseShiftKey();
        double shift = 0.;
        int nP = gd.getNumbOfPoints();
        if (nP > 0) {
            if (gd.getGraphProperty(key) != null) {
                shift = ((Double) gd.getGraphProperty(key));
            }
        }
        return shift;
    }

    /**
     * Gets the choosenDraphData attribute of the MainAnalysisController object
     *
     * @return The choosenDraphData value
     */
    protected BasicGraphData getChoosenDraphData() {
        BasicGraphData gd = null;
        Integer ind = graphAnalysis.getGraphChosenIndex();
        if (ind != null && ind >= 0) {
            gd = graphAnalysis.getInstanceOfGraphData(ind);
            if (gd == graphDataLocal) {
                return null;
            }
            return gd;
        } else {
            Vector<BasicGraphData> gdV = graphAnalysis.getAllGraphData();
            gdV.remove(graphDataLocal);
            if (gdV.size() == 1) {
                gd = gdV.get(0);
                return gd;
            } else {
                if (gdV.size() == 0) {
                    return null;
                }
                Vector<BasicGraphData> gdInsideV
                        = GraphDataOperations.getDataInsideRectangle(gdV,
                                graphAnalysis.getCurrentMinX(),
                                graphAnalysis.getCurrentMaxX(),
                                graphAnalysis.getCurrentMinY(),
                                graphAnalysis.getCurrentMaxY());

                if (gdInsideV.size() == 1) {
                    return gdInsideV.get(0);
                }
            }
        }
        return null;
    }

    /**
     * Gets the choosenDraphDataAndPoint attribute of the MainAnalysisController
     * object
     *
     * @return The choosenDraphDataAndPoint value
     */
    protected Object[] getChoosenDraphDataAndPoint() {
        Object[] objArr = new Object[2];
        objArr[0] = null;
        objArr[1] = null;
        Integer ind = graphAnalysis.getGraphChosenIndex();
        Integer indP = graphAnalysis.getPointChosenIndex();
        if (ind != null
                && indP != null
                && ind >= 0
                && indP >= 0
                && graphAnalysis.getInstanceOfGraphData(ind) != graphDataLocal) {
            objArr[0] = graphAnalysis.getInstanceOfGraphData(ind);
            objArr[1] = indP;
        } else {
            BasicGraphData gd = getChoosenDraphData();
            Vector<BasicGraphData> oneV = new Vector<>();
            oneV.add(gd);
            objArr
                    = GraphDataOperations.getGraphDataAndPointIndexInside(
                            oneV,
                            graphAnalysis.getCurrentMinX(),
                            graphAnalysis.getCurrentMaxX(),
                            graphAnalysis.getCurrentMinY(),
                            graphAnalysis.getCurrentMaxY());
        }
        return objArr;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    private boolean canSaveAsTable() {
        Vector<BasicGraphData> gdV_tmp = graphAnalysis.getAllGraphData();
        Vector<BasicGraphData> gdV = new Vector<>();
        for (int i = 0; i < gdV_tmp.size(); i++) {
            BasicGraphData gd = gdV_tmp.get(i);
            if (gd.getNumbOfPoints() > 0) {
                gdV.add(gd);
            }
        }
        if (gdV.size() <= 0) {
            return false;
        }
        BasicGraphData gd_0 = gdV.get(0);
        int nP_0 = gd_0.getNumbOfPoints();
        for (int i = 1; i < gdV.size(); i++) {
            BasicGraphData gd = gdV.get(i);
            int nP = gd.getNumbOfPoints();
            if (nP_0 != nP) {
                return false;
            }
            for (int j = 0; j < nP; j++) {
                double diff = Math.abs(gd_0.getX(j) - gd.getX(j));
                double val = Math.abs(gd_0.getX(j));
                if (val > 0.) {
                    if (diff / val > 0.0001) {
                        return false;
                    }
                } else {
                    if (diff > 0.0001) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
