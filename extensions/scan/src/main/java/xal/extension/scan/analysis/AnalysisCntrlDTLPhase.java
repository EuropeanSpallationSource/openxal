package xal.extension.scan.analysis;

import java.awt.event.*;
import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.data.DataAdaptor;
import xal.extension.application.Application;
import xal.extension.scan.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;

/**
 * This class is a DTL Phase Scan analysis.
 *
 * @author A. Shishlo
 * @version 1.0
 */
public final class AnalysisCntrlDTLPhase extends AnalysisController {

    private static final Logger LOGGER = Logger.getLogger(AnalysisCntrlDTLPhase.class.getName());

    private JPanel dtlPSAnalysisPanel = new JPanel();

    private String theoryWvsADataFileName = "NONE";
    private String theoryKSvsADataFileName = "NONE";

    //common part of the   dtlPS_AnalysisPanel
    private JPanel commonPanel = new JPanel();

    private JLabel designEnrgDevLabel = new JLabel("Energy Dlt [%] = ");
    private DoubleInputTextField designEnrgDevText = new DoubleInputTextField(8);

    private DecimalFormat ampFormat = new DecimalFormat("####.###");
    private DecimalFormat phaseFormat = new DecimalFormat("###.#");

    private JComboBox<String> operationChooser = null;

    //child panels
    private JPanel[] childControlPanels = new JPanel[2];
    private JPanel[] childGraphPanels = new JPanel[2];
    private String[] panelNameList = {"FIND WIDTH FOR 1D SCAN     ",
        "PLOT WIDTH VS. AMPLITUDE   "};

    //--------------------------------------------------
    //EXTERNAL DATA
    //--------------------------------------------------
    //vectors include BasicGraphData instances with
    //"ENERGY_DELTA" as properties key with delta energy in percent parameter
    private String energyDelta = "ENERGY_DELTA";
    private Vector<BasicGraphData> extWidthVsAmpDataV = new Vector<>();
    private Vector<BasicGraphData> extAmpVsWidthDataV = new Vector<>();

    //vectors include BasicGraphData instances with
    //k_shift vs (amp/design_amp) for different "ENERGY_DELTA"
    //as properties key with delta energy in percent parameter
    private Vector<BasicGraphData> extKShiftVsAmpDataV = new Vector<>();
    private Vector<BasicGraphData> extAmpVsKShiftDataV = new Vector<>();

    //--------------------------------------------------
    //PANEL #0  name = "FIND WIDTH FOR 1D SCAN     "
    //--------------------------------------------------
    private JLabel paramPVLabel = new JLabel(" Cavity Ampl.    :");
    private JLabel paramPVRBLabel = new JLabel(" Cavity Ampl. RB :");

    private DoubleInputTextField paramPVValueText = new DoubleInputTextField(8);
    private DoubleInputTextField paramPVRBValueText = new DoubleInputTextField(8);

    private JLabel widthP0Label = new JLabel("Width [dgr] :");
    private JLabel guessAmpP0Label = new JLabel("Guess Ampl  :");
    private JLabel guessPhaseP0Label = new JLabel("Guess Phase :");

    private DoubleInputTextField widthP0Text = new DoubleInputTextField(8);
    private DoubleInputTextField guessAmpP0Text = new DoubleInputTextField(8);
    private DoubleInputTextField guessPhaseP0Text = new DoubleInputTextField(8);

    private JButton findWidthP0Button = new JButton("FIND WIDTH AND GUESS AMPL. & PHASE");
    private JButton setGuessAmpP0Button = new JButton("SET GUESS AMPL. & PHASE TO CAVITY");

    private ActionListener graphChooserListener = null;

    private MouseAdapter graphChooserMouseAdapter = null;

    //--------------------------------------------------
    //PANEL #1  name = "PLOT WIDTH VS. AMPLITUDE   "
    //--------------------------------------------------
    //graph data with functions for certain value of the energy delta
    //from the predefined table:
    //width vs amplitude and
    //amplitude vs width
    private BasicGraphData gdP1WFa = new BasicGraphData();
    private BasicGraphData gdP1AFw = new BasicGraphData();

    //k_shift (ks) coeff vs. normalized amplitude
    //def. of k_shift :  phi_guess = phi_left + k_shift * ( phi_right - phi_left)
    private BasicGraphData gdP1KsFa = new BasicGraphData();
    private BasicGraphData gdP1AFks = new BasicGraphData();

    //graph data width vs amplitude from measured data
    private BasicGraphData gdP1ExpWFa = new BasicGraphData();

    //data with information (max in phase scan) vs normalize amplitude
    private BasicGraphData gdP1MaxVsA = new BasicGraphData();

    //GUI elements for panel #1
    private JLabel enrgDltP1Label = new JLabel("Energy Dlt [%]   :");
    private JLabel guessAmpP1Label = new JLabel("Guess Ampl       :");
    private JLabel guessPhaseP1Label = new JLabel("Guess Phase [dgr]:");

    private DoubleInputTextField enrgDltP1Text = new DoubleInputTextField(8);
    private DoubleInputTextField guessAmpP1Text = new DoubleInputTextField(8);
    private DoubleInputTextField guessPhaseP1Text = new DoubleInputTextField(8);

    private JButton setEnrgDltP1Button = new JButton("MEMORIZE ENERGY DELTA");
    private JButton setGuessAmpP1Button = new JButton("SET GUESS AMPL. & PHASE TO CAVITY");

    //graphs panels that are placed on PANEL #1 graph part
    private FunctionGraphsJPanel widthVsAmpGraph = new FunctionGraphsJPanel();
    private FunctionGraphsJPanel maxValVsAmpGraph = new FunctionGraphsJPanel();

    //vertical line listener
    private ActionListener dragVerLineListener = null;

    /**
     * The constructor.
     *
     * @param mainControllerIn Description of the Parameter
     * @param analysisConf Description of the Parameter
     * @param parentAnalysisPanelIn Description of the Parameter
     * @param customControlPanelIn Description of the Parameter
     * @param customGraphPanelIn Description of the Parameter
     * @param globalButtonsPanelIn Description of the Parameter
     * @param scanVariableParameterIn Description of the Parameter
     * @param scanVariableIn Description of the Parameter
     * @param measuredValuesVIn Description of the Parameter
     * @param graphAnalysisIn Description of the Parameter
     * @param messageTextLocalIn Description of the Parameter
     * @param graphDataLocalIn Description of the Parameter
     */
    public AnalysisCntrlDTLPhase(MainAnalysisController mainControllerIn,
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

        String nameIn = "DTL PHASE SCAN";
        DataAdaptor nameDA = analysisConf.childAdaptor("ANALYSIS_NAME");
        if (nameDA != null) {
            nameIn = nameDA.stringValue("name");
        }
        setName(nameIn);

        DataAdaptor designEnrgDA = analysisConf.childAdaptor("DESIGN_ENERGY_DELTA");
        double designEnrgTmp = 0.0;
        if (designEnrgDA != null) {
            designEnrgTmp = designEnrgDA.doubleValue("value");
        }

        DataAdaptor theoryDataDA = analysisConf.childAdaptor("THEORY_SCAN_DATA");
        if (theoryDataDA != null) {
            DataAdaptor theoryDataDADphiVsAmp = theoryDataDA.childAdaptor("DPHI_VS_AMP");
            DataAdaptor theoryDataDAkShiftVsAmp = theoryDataDA.childAdaptor("KSHIFT_VS_AMP");
            if (theoryDataDADphiVsAmp != null && theoryDataDAkShiftVsAmp != null) {
                readTheoryData(theoryDataDADphiVsAmp.stringValue("file_name"),
                        theoryDataDAkShiftVsAmp.stringValue("file_name"));
            }
        }

        //create main panel
        dtlPSAnalysisPanel.setLayout(new BorderLayout());

        //==================================================
        //create common panel
        //==================================================
        SimpleChartPopupMenu.addPopupMenuTo(widthVsAmpGraph);
        SimpleChartPopupMenu.addPopupMenuTo(maxValVsAmpGraph);

        commonPanel.setLayout(new BorderLayout());

        Border etchedBorder = BorderFactory.createEtchedBorder();
        commonPanel.setBorder(etchedBorder);
        commonPanel.setBackground(commonPanel.getBackground().darker());

        designEnrgDevText.setNormalBackground(Color.white);
        designEnrgDevText.setNumberFormat(ampFormat);
        designEnrgDevText.setHorizontalAlignment(SwingConstants.CENTER);
        designEnrgDevText.setValue(designEnrgTmp);

        JPanel tmp0 = new JPanel();
        tmp0.setLayout(new GridLayout(1, 2, 1, 1));
        tmp0.add(designEnrgDevLabel);
        tmp0.add(designEnrgDevText);

        operationChooser = new JComboBox<>(panelNameList);
        operationChooser.setBackground(Color.cyan);
        operationChooser.addActionListener(e -> {
            int index = operationChooser.getSelectedIndex();
            showPanel(index);
        });

        commonPanel.add(tmp0, BorderLayout.NORTH);
        commonPanel.add(operationChooser, BorderLayout.SOUTH);

        //create PANEL #0 name = "FIND WIDTH FOR 1D SCAN     "
        createPanelFindWidth();

        //create PANEL #1 name = "PLOT WIDTH VS. AMPLITUDE   "
        createPanelWidthVsAmp();

        //create listener for vertical line - phase marker
        dragVerLineListener = e -> {
            int ind = graphAnalysis.getDraggedLineIndex();
            double markerPos = graphAnalysis.getVerticalValue(ind);
            double phaseShift = MainAnalysisController.getPhaseShift(graphAnalysis.getAllGraphData());
            markerPos -= phaseShift;
            if (phaseShift != 0.) {
                markerPos += 180.;
                while (markerPos < 0.) {
                    markerPos += 360.;
                }
                markerPos = markerPos % 360.;
                markerPos -= 180.;
            }
            guessPhaseP0Text.setValueQuietly(markerPos);
            guessPhaseP1Text.setValueQuietly(markerPos);
        };

        guessPhaseP0Text.addActionListener(e -> {
            graphAnalysis.addDraggedVerLinesListener(null);
            double markerPos = guessPhaseP0Text.getValue();
            guessPhaseP1Text.setValueQuietly(markerPos);
            double phaseShift = MainAnalysisController.getPhaseShift(graphAnalysis.getAllGraphData());
            markerPos += phaseShift;
            if (phaseShift != 0.) {
                markerPos += 180.;
                while (markerPos < 0.) {
                    markerPos += 360.;
                }
                markerPos = markerPos % 360.;
                markerPos -= 180.;
            }
            graphAnalysis.setVerticalLineValue(markerPos, 0);
            graphAnalysis.addDraggedVerLinesListener(dragVerLineListener);
        });

        graphAnalysis.addDraggedVerLinesListener(null);
        graphAnalysis.removeVerticalValue(0);

    }

    /**
     * Sets the configurations of the analysis.
     *
     * @param analysisConfig Description of the Parameter
     */
    @Override
    public void dumpAnalysisConfig(DataAdaptor analysisConfig) {
        super.dumpAnalysisConfig(analysisConfig);

        DataAdaptor designEnrgDA = analysisConfig.createChild("DESIGN_ENERGY_DELTA");
        designEnrgDA.setValue("value", designEnrgDevText.getValue());

        DataAdaptor theoryDataDA = analysisConfig.createChild("THEORY_SCAN_DATA");

        DataAdaptor theoryDataDAdphiVsAmp = theoryDataDA.createChild("DPHI_VS_AMP");
        theoryDataDAdphiVsAmp.setValue("file_name", theoryWvsADataFileName);

        DataAdaptor theoryDataDAkShiftVsAmp = theoryDataDA.createChild("KSHIFT_VS_AMP");
        theoryDataDAkShiftVsAmp.setValue("file_name", theoryKSvsADataFileName);

    }

    /**
     * Sets fonts for all GUI elements.
     *
     * @param fnt The new fontsForAll value
     */
    @Override
    public void setFontsForAll(Font fnt) {
        super.setFontsForAll(fnt);

        //common panel
        designEnrgDevLabel.setFont(fnt);
        designEnrgDevText.setFont(fnt);

        operationChooser.setFont(fnt);
        ((JTextField) operationChooser.getEditor().getEditorComponent()).setFont(fnt);
        operationChooser.setPreferredSize(new Dimension(1, fnt.getSize() + 10));

        //panel #0
        paramPVLabel.setFont(fnt);
        paramPVRBLabel.setFont(fnt);
        paramPVValueText.setFont(fnt);
        paramPVRBValueText.setFont(fnt);
        widthP0Label.setFont(fnt);
        guessAmpP0Label.setFont(fnt);
        guessPhaseP0Label.setFont(fnt);
        widthP0Text.setFont(fnt);
        guessAmpP0Text.setFont(fnt);
        guessPhaseP0Text.setFont(fnt);
        findWidthP0Button.setFont(fnt);
        setGuessAmpP0Button.setFont(fnt);

        //panel #1
        enrgDltP1Label.setFont(fnt);
        guessAmpP1Label.setFont(fnt);
        guessPhaseP1Label.setFont(fnt);
        enrgDltP1Text.setFont(fnt);
        guessAmpP1Text.setFont(fnt);
        guessPhaseP1Text.setFont(fnt);
        setEnrgDltP1Button.setFont(fnt);
        setGuessAmpP1Button.setFont(fnt);
    }

    /**
     * Does what necessary for close this analysis window.
     */
    @Override
    public void shutUp() {
        super.shutUp();
        customControlPanel.removeAll();
        customGraphPanel.removeAll();
        graphAnalysis.addDraggedVerLinesListener(null);
        graphAnalysis.removeVerticalValue(0);

        graphAnalysis.addChooseListener(null);
        graphAnalysis.removeMouseListener(graphChooserMouseAdapter);
    }

    /**
     * Does what necessary for open this analysis window.
     */
    @Override
    public void showUp() {
        super.showUp();

        graphAnalysis.addVerticalLine(guessPhaseP0Text.getValue(), Color.red);
        graphAnalysis.addDraggedVerLinesListener(dragVerLineListener);
        graphAnalysis.setDraggedVerLinesMotionListen(true);

        graphAnalysis.addChooseListener(graphChooserListener);
        graphAnalysis.addMouseListener(graphChooserMouseAdapter);

        //for panel #0
        childGraphPanels[0].removeAll();
        childGraphPanels[0].add(graphAnalysis, BorderLayout.CENTER);
        childGraphPanels[0].add(globalButtonsPanel, BorderLayout.SOUTH);

        showPanel(0);
    }

    /**
     * Shows panel with certain index.
     *
     * @param panelIndex Description of the Parameter
     */
    private void showPanel(int panelIndex) {
        customControlPanel.removeAll();
        customGraphPanel.removeAll();
        dtlPSAnalysisPanel.removeAll();

        //clear the message text
        messageTextLocal.setText(null);

        operationChooser.setSelectedIndex(panelIndex);

        if (panelIndex == 0) {
            paramPVValueText.setText(null);
            paramPVValueText.setBackground(Color.white);
            paramPVRBValueText.setText(null);
            paramPVRBValueText.setBackground(Color.white);
        }

        if (panelIndex == 1) {

            double[] params = getBestAmpAndPhase();
            if (params != null) {
                guessAmpP1Text.setValue(params[0]);
                enrgDltP1Text.setValue(params[2]);
                double phaseShift = MainAnalysisController.getPhaseShift(graphAnalysis.getAllGraphData());
                double markerPos = params[3] - phaseShift;
                if (phaseShift != 0.) {
                    markerPos += 180.;
                    while (markerPos < 0.) {
                        markerPos += 360.;
                    }
                    markerPos = markerPos % 360.;
                    markerPos -= 180.;
                }
                guessPhaseP0Text.setValue(markerPos);
                widthVsAmpGraph.refreshGraphJPanel();
                maxValVsAmpGraph.refreshGraphJPanel();
                messageTextLocal.setText("debug amp=" + ampFormat.format(params[0])
                        + " err=" + ampFormat.format(params[1])
                        + " enrgDlt=" + ampFormat.format(params[2])
                        + " phase=" + phaseFormat.format(params[3])
                        + " +- " + phaseFormat.format(params[4])
                        + "  phaseShift=" + phaseFormat.format(phaseShift));
            } else {
                Toolkit.getDefaultToolkit().beep();
                messageTextLocal.setText(null);
                messageTextLocal.setText("Do not have enough data for analysis.");
            }
        }

        dtlPSAnalysisPanel.add(commonPanel, BorderLayout.NORTH);
        dtlPSAnalysisPanel.add(childControlPanels[panelIndex], BorderLayout.CENTER);

        customControlPanel.add(dtlPSAnalysisPanel, BorderLayout.NORTH);
        customGraphPanel.add(childGraphPanels[panelIndex], BorderLayout.CENTER);

        //repaint
        parentAnalysisPanel.validate();
        parentAnalysisPanel.repaint();
    }

    //create PANEL #0 name = "FIND WIDTH FOR 1D SCAN     "
    /**
     * Description of the Method
     */
    private void createPanelFindWidth() {
        childGraphPanels[0] = new JPanel();
        childGraphPanels[0].setLayout(new BorderLayout());

        childControlPanels[0] = new JPanel();
        childControlPanels[0].setLayout(new BorderLayout());

        //GUI elements
        paramPVValueText.setEditable(false);
        paramPVRBValueText.setEditable(false);

        paramPVValueText.setNumberFormat(ampFormat);
        paramPVRBValueText.setNumberFormat(ampFormat);

        paramPVValueText.setHorizontalAlignment(SwingConstants.CENTER);
        paramPVRBValueText.setHorizontalAlignment(SwingConstants.CENTER);

        paramPVValueText.removeInnerFocusListener();
        paramPVRBValueText.removeInnerFocusListener();

        paramPVValueText.setText(null);
        paramPVValueText.setBackground(Color.white);
        paramPVRBValueText.setText(null);
        paramPVRBValueText.setBackground(Color.white);

        widthP0Text.setEditable(false);
        guessAmpP0Text.setEditable(false);
        guessPhaseP0Text.setEditable(false);

        widthP0Text.setNumberFormat(ampFormat);
        guessAmpP0Text.setNumberFormat(ampFormat);
        guessPhaseP0Text.setNumberFormat(phaseFormat);

        widthP0Text.setHorizontalAlignment(SwingConstants.CENTER);
        guessAmpP0Text.setHorizontalAlignment(SwingConstants.CENTER);
        guessPhaseP0Text.setHorizontalAlignment(SwingConstants.CENTER);

        widthP0Text.removeInnerFocusListener();
        guessAmpP0Text.removeInnerFocusListener();
        guessPhaseP0Text.removeInnerFocusListener();

        widthP0Text.setText(null);
        widthP0Text.setBackground(Color.white);
        guessAmpP0Text.setText(null);
        guessAmpP0Text.setBackground(Color.white);
        guessPhaseP0Text.setText(null);
        guessPhaseP0Text.setBackground(Color.white);

        findWidthP0Button.setForeground(Color.blue);
        setGuessAmpP0Button.setForeground(Color.blue);

        graphChooserListener = e -> {
            Integer ind = graphAnalysis.getGraphChosenIndex();
            if (ind != null && ind >= 0) {
                BasicGraphData gd = graphAnalysis.getInstanceOfGraphData(ind);

                Double parD = (Double) gd.getGraphProperty("PARAMETER_VALUE");
                if (parD != null) {
                    paramPVValueText.setValue(parD);
                } else {
                    paramPVValueText.setText(null);
                    paramPVValueText.setBackground(Color.white);
                }
                parD = (Double) gd.getGraphProperty("PARAMETER_VALUERB");
                if (parD != null) {
                    paramPVRBValueText.setValue(parD);
                } else {
                    paramPVRBValueText.setText(null);
                    paramPVRBValueText.setBackground(Color.white);
                }
            } else {
                paramPVValueText.setText(null);
                paramPVValueText.setBackground(Color.white);
                paramPVRBValueText.setText(null);
                paramPVRBValueText.setBackground(Color.white);
            }

            widthP0Text.setText(null);
            widthP0Text.setBackground(Color.white);
            guessAmpP0Text.setText(null);
            guessAmpP0Text.setBackground(Color.white);
            guessPhaseP0Text.setText(null);
            guessPhaseP0Text.setBackground(Color.white);
        };

        graphChooserMouseAdapter
                = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Integer ind = graphAnalysis.getGraphChosenIndex();
                if (ind == null || ind < 0) {
                    paramPVValueText.setText(null);
                    paramPVValueText.setBackground(Color.white);
                    paramPVRBValueText.setText(null);
                    paramPVRBValueText.setBackground(Color.white);
                }
            }
        };

        findWidthP0Button.addActionListener(e -> {
            BasicGraphData gd = mainController.getChoosenDraphData();
            if (gd != null) {
                Double[] resArr = findWidthAndPlot(gd);
                Double widthD = resArr[0];
                Double phaseLD = resArr[1];
                Double phaseRD = resArr[2];

                if (widthD != null && phaseLD != null && phaseRD != null) {
                    double energyDlt = designEnrgDevText.getValue();
                    makeForwardAndBackWardGraphs(energyDlt);
                    //normalized amplitude from ampl_vs_width graph
                    double newAmpNorm = gdP1AFw.getValueY(widthD);
                    double amp = ((Double) gd.getGraphProperty("PARAMETER_VALUE"));
                    amp = amp / newAmpNorm;
                    //get phase_guess
                    double kShift = gdP1KsFa.getValueY(newAmpNorm);
                    double phaseGuess = phaseLD + kShift * (phaseRD - phaseLD);
                    double phaseShift = MainAnalysisController.getPhaseShift(gd);
                    double markerPos = phaseGuess - phaseShift;
                    if (phaseShift != 0.) {
                        markerPos += 180.;
                        while (markerPos < 0.) {
                            markerPos += 360.;
                        }
                        markerPos = markerPos % 360.;
                        markerPos -= 180.;
                    }
                    //DEBUG print ----------------------------------------------------
                    LOGGER.log(Level.INFO, "debug new point  newAmpNorm={0} Edlt={1} w={2} amp/guessA={3} phiL={4} phiR={5} phi={6} k_s={7} phaseShift={8}", new Object[]{ampFormat.format(newAmpNorm), ampFormat.format(energyDlt), ampFormat.format(widthD), ampFormat.format(amp), ampFormat.format(phaseLD), ampFormat.format(phaseRD), ampFormat.format(phaseGuess), ampFormat.format(kShift), ampFormat.format(phaseShift)});
                    //DEBUG print ----------------------------------------------------

                    guessAmpP0Text.setValue(amp);
                    guessPhaseP0Text.setValue(markerPos);
                }
            } else {
                widthP0Text.setText(null);
                widthP0Text.setBackground(Color.white);
                guessAmpP0Text.setText(null);
                guessAmpP0Text.setBackground(Color.white);
                Toolkit.getDefaultToolkit().beep();
                messageTextLocal.setText(null);
                messageTextLocal.setText("Please choose the graph first. Use S-button on the graph panel.");
            }
        });

        setGuessAmpP0Button.addActionListener(e -> {
            double ampVal = guessAmpP0Text.getValue();
            double phaseVal = guessPhaseP0Text.getValue();
            if (scanVariableParameter.getChannel() != null
                    && scanVariable.getChannel() != null) {
                scanVariableParameter.setValue(ampVal);
                scanVariable.setValue(phaseVal);
            } else {
                messageTextLocal.setText(null);
                messageTextLocal.setText("The parameter or Scan Variable PV channel does not exist.");
                Toolkit.getDefaultToolkit().beep();
            }
        });

        JPanel tmp0 = new JPanel();
        tmp0.setLayout(new GridLayout(2, 2, 1, 1));

        Border etchedBorder = BorderFactory.createEtchedBorder();
        tmp0.setBorder(etchedBorder);
        tmp0.setBackground(tmp0.getBackground().darker());

        tmp0.add(paramPVLabel);
        tmp0.add(paramPVValueText);
        tmp0.add(paramPVRBLabel);
        tmp0.add(paramPVRBValueText);

        JPanel tmp1 = new JPanel();
        tmp1.setLayout(new BorderLayout());
        tmp1.setBorder(etchedBorder);
        tmp1.setBackground(tmp0.getBackground().darker());

        JPanel tmp2 = new JPanel();
        tmp2.setLayout(new GridLayout(3, 2, 1, 1));
        tmp2.add(widthP0Label);
        tmp2.add(widthP0Text);
        tmp2.add(guessAmpP0Label);
        tmp2.add(guessAmpP0Text);
        tmp2.add(guessPhaseP0Label);
        tmp2.add(guessPhaseP0Text);

        tmp1.add(findWidthP0Button, BorderLayout.NORTH);
        tmp1.add(tmp2, BorderLayout.CENTER);
        tmp1.add(setGuessAmpP0Button, BorderLayout.SOUTH);

        //add elements to analysis control child - 0
        childControlPanels[0].add(tmp0, BorderLayout.NORTH);
        childControlPanels[0].add(tmp1, BorderLayout.CENTER);

        //the graph panel will be done at the ShowUp() method
    }

    //create PANEL #1 name = "PLOT WIDTH VS. AMPLITUDE   "
    /**
     * Description of the Method
     */
    private void createPanelWidthVsAmp() {
        childGraphPanels[1] = new JPanel();
        childGraphPanels[1].setLayout(new BorderLayout());

        childControlPanels[1] = new JPanel();
        childControlPanels[1].setLayout(new BorderLayout());

        //graph panels
        widthVsAmpGraph.setOffScreenImageDrawing(true);
        widthVsAmpGraph.setName("WIDTH vs. NORMALIZED AMPLITUDE");
        widthVsAmpGraph.setAxisNames("Ampl./Design Ampl", "Phase Width [grd]");
        widthVsAmpGraph.setGraphBackGroundColor(Color.white);
        widthVsAmpGraph.setLegendButtonVisible(true);
        widthVsAmpGraph.setLegendBackground(Color.white);
        widthVsAmpGraph.setLegendVisible(true);

        maxValVsAmpGraph.setOffScreenImageDrawing(true);
        maxValVsAmpGraph.setName("MAX. TRANSMISSION vs. NORMALIZED AMPLITUDE");
        maxValVsAmpGraph.setAxisNames("Ampl./Design Ampl", "Transmission");
        maxValVsAmpGraph.setGraphBackGroundColor(Color.white);
        maxValVsAmpGraph.setLegendButtonVisible(true);
        maxValVsAmpGraph.setLegendBackground(Color.white);
        maxValVsAmpGraph.setLegendVisible(true);

        //graph data properties
        widthVsAmpGraph.addGraphData(gdP1WFa);
        widthVsAmpGraph.addGraphData(gdP1ExpWFa);

        maxValVsAmpGraph.addGraphData(gdP1MaxVsA);

        gdP1WFa.setGraphProperty(graphAnalysis.getLegendKeyString(), "THEORY");
        gdP1ExpWFa.setGraphProperty(graphAnalysis.getLegendKeyString(), "MEASUREMENTS");
        gdP1MaxVsA.setGraphProperty(graphAnalysis.getLegendKeyString(), "MEASUREMENTS");

        gdP1WFa.setImmediateContainerUpdate(false);
        gdP1ExpWFa.setImmediateContainerUpdate(false);
        gdP1MaxVsA.setImmediateContainerUpdate(false);

        gdP1WFa.setDrawLinesOn(true);
        gdP1WFa.setDrawPointsOn(false);
        gdP1WFa.setLineThick(3);
        gdP1WFa.setGraphColor(Color.blue);

        //text elements
        enrgDltP1Text.setEditable(false);
        enrgDltP1Text.setNumberFormat(ampFormat);
        enrgDltP1Text.setHorizontalAlignment(SwingConstants.CENTER);
        enrgDltP1Text.removeInnerFocusListener();
        enrgDltP1Text.setText(null);
        enrgDltP1Text.setBackground(Color.white);

        guessAmpP1Text.setEditable(false);
        guessAmpP1Text.setNumberFormat(ampFormat);
        guessAmpP1Text.setHorizontalAlignment(SwingConstants.CENTER);
        guessAmpP1Text.removeInnerFocusListener();
        guessAmpP1Text.setText(null);
        guessAmpP1Text.setBackground(Color.white);

        guessPhaseP1Text.setEditable(false);
        guessPhaseP1Text.setNumberFormat(phaseFormat);
        guessPhaseP1Text.setHorizontalAlignment(SwingConstants.CENTER);
        guessPhaseP1Text.removeInnerFocusListener();
        guessPhaseP1Text.setText(null);
        guessPhaseP1Text.setBackground(Color.white);

        setEnrgDltP1Button.setForeground(Color.blue);
        setGuessAmpP1Button.setForeground(Color.blue);

        setEnrgDltP1Button.addActionListener(e -> {
            double val = enrgDltP1Text.getValue();
            if (enrgDltP1Text.getText().length() > 0) {
                designEnrgDevText.setValue(val);
            }
        });

        setGuessAmpP1Button.addActionListener(e -> {
            double ampVal = guessAmpP1Text.getValue();
            double phaseVal = guessPhaseP0Text.getValue();
            if (scanVariableParameter.getChannel() != null && scanVariable.getChannel() != null) {
                scanVariableParameter.setValue(ampVal);
                scanVariable.setValue(phaseVal);
            } else {
                messageTextLocal.setText(null);
                messageTextLocal.setText("The parameter PV channel does not exist.");
                Toolkit.getDefaultToolkit().beep();
            }
        });

        JPanel tmp0 = new JPanel();
        tmp0.setLayout(new GridLayout(3, 2, 1, 1));

        Border etchedBorder = BorderFactory.createEtchedBorder();
        tmp0.setBorder(etchedBorder);
        tmp0.add(enrgDltP1Label);
        tmp0.add(enrgDltP1Text);
        tmp0.add(guessAmpP1Label);
        tmp0.add(guessAmpP1Text);
        tmp0.add(guessPhaseP1Label);
        tmp0.add(guessPhaseP1Text);

        JPanel tmp1 = new JPanel();
        tmp1.setLayout(new BorderLayout());
        tmp1.setBorder(etchedBorder);
        tmp1.setBackground(tmp0.getBackground().darker());

        tmp1.add(setEnrgDltP1Button, BorderLayout.NORTH);
        tmp1.add(tmp0, BorderLayout.CENTER);
        tmp1.add(setGuessAmpP1Button, BorderLayout.SOUTH);

        childControlPanels[1].add(tmp1, BorderLayout.NORTH);

        //graph panel
        JPanel tmp10 = new JPanel();
        tmp10.setLayout(new GridLayout(2, 1, 1, 1));
        tmp10.add(widthVsAmpGraph);
        tmp10.add(maxValVsAmpGraph);
        childGraphPanels[1].add(tmp10, BorderLayout.CENTER);
    }

    //find width, left and right phases and plot it
    /**
     * Description of the Method
     *
     * @param gd Description of the Parameter
     * @return Description of the Return Value
     */
    private Double[] findWidthAndPlot(BasicGraphData gd) {

        Double[] resultArr = new Double[3];
        resultArr[0] = null;
        resultArr[1] = null;
        resultArr[2] = null;

        Double widthD;

        graphAnalysis.removeGraphData(graphDataLocal);
        graphDataLocal.removeAllPoints();

        if (gd != null && gd.getNumbOfPoints() > 0) {
            double[] xCross = findWidth(gd);

            if (xCross != null) {
                graphDataLocal.addPoint(gd.getMinX(), gd.getMinY());
                graphDataLocal.addPoint(xCross[0], gd.getMinY());
                graphDataLocal.addPoint(xCross[0] + 0.00000001, gd.getMaxY());
                graphDataLocal.addPoint(xCross[1] - 0.00000001, gd.getMaxY());
                graphDataLocal.addPoint(xCross[1], gd.getMinY());
                graphDataLocal.addPoint(gd.getMaxX(), gd.getMinY());

                widthP0Text.setText(null);
                widthP0Text.setValue(xCross[1] - xCross[0]);
                widthD = xCross[1] - xCross[0];
                resultArr[0] = widthD;
                resultArr[1] = xCross[0];
                resultArr[2] = xCross[1];
                messageTextLocal.setText(null);
            } else {
                widthP0Text.setText(null);
                widthP0Text.setBackground(Color.white);
                guessAmpP0Text.setText(null);
                guessAmpP0Text.setBackground(Color.white);
                guessPhaseP0Text.setText(null);
                guessPhaseP0Text.setBackground(Color.white);
                Toolkit.getDefaultToolkit().beep();
                messageTextLocal.setText(null);
                messageTextLocal.setText("Can not find the width.");
            }
        } else {
            widthP0Text.setText(null);
            widthP0Text.setBackground(Color.white);
            guessAmpP0Text.setText(null);
            guessAmpP0Text.setBackground(Color.white);
            guessPhaseP0Text.setText(null);
            guessPhaseP0Text.setBackground(Color.white);
            Toolkit.getDefaultToolkit().beep();
            messageTextLocal.setText(null);
            messageTextLocal.setText("Can not find the width. Select the curve with N points != 0");
        }

        graphAnalysis.addGraphData(graphDataLocal);
        return resultArr;
    }

    //find width, returns array with left and right points
    /**
     * Description of the Method
     *
     * @param gd Description of the Parameter
     * @return Description of the Return Value
     */
    private double[] findWidth(BasicGraphData gd) {

        double[] wArr = null;

        if (gd != null && gd.getNumbOfPoints() > 0) {
            double yMin = gd.getMinY();
            double yMax = gd.getMaxY();
            double yAvg = yMin + (yMax - yMin) / 2.0;
            int count = 0;
            int[] indexCross = new int[2];
            double y;
            double y1;

            for (int i = 0; i < (gd.getNumbOfPoints() - 1); i++) {
                y = gd.getY(i);
                y1 = gd.getY(i + 1);
                if (yAvg != y1 && (yAvg - y) * (yAvg - y1) <= 0.) {
                    if (count < 2) {
                        indexCross[count] = i;
                    }
                    count++;
                }
            }

            if (count == 2) {
                wArr = new double[2];
                double coef = (yAvg - gd.getY(indexCross[0])) / (gd.getY(indexCross[0] + 1) - gd.getY(indexCross[0]));
                wArr[0] = gd.getX(indexCross[0]) + coef * (gd.getX(indexCross[0] + 1) - gd.getX(indexCross[0]));

                coef = (yAvg - gd.getY(indexCross[1])) / (gd.getY(indexCross[1] + 1) - gd.getY(indexCross[1]));
                wArr[1] = gd.getX(indexCross[1]) + coef * (gd.getX(indexCross[1] + 1) - gd.getX(indexCross[1]));
            }
        }
        return wArr;
    }

    //calculate the best guess about design value of the cavity amplitude and phase
    //At this moment phase calculation is empty
    /**
     * Gets the bestAmpAndPhase attribute of the AnalysisCntrlDTLPhase object
     *
     * @return The bestAmpAndPhase value
     */
    private double[] getBestAmpAndPhase() {

        gdP1WFa.removeAllPoints();
        gdP1AFw.removeAllPoints();
        gdP1ExpWFa.removeAllPoints();
        gdP1MaxVsA.removeAllPoints();

        double[] results = null;
        Vector<BasicGraphData> gdVTmp = graphAnalysis.getAllGraphData();
        BasicGraphData gd;
        Vector<BasicGraphData> gdV = new Vector<>(20);
        double[] xGr = null;
        for (int i = 0; i < gdVTmp.size(); i++) {
            gd = gdVTmp.get(i);
            xGr = findWidth(gd);
            Double ampD = (Double) gd.getGraphProperty("PARAMETER_VALUE");
            if (xGr != null && ampD != null) {
                gdV.add(gd);
                gd.setGraphProperty("PHASE_WIDTH", xGr[1] - xGr[0]);
                gd.setGraphProperty("PHASE_LEFT", xGr[0]);
                gd.setGraphProperty("PHASE_RIGHT", xGr[1]);
            }
        }

        int nMeasurements = gdV.size();

        if (nMeasurements < 2) {
            return results;
        }

        int nEnergies = extWidthVsAmpDataV.size();

        if (nEnergies <= 0) {
            return results;
        }

        double[] guessAmp = new double[nEnergies];
        double[] guessAmp2 = new double[nEnergies];
        for (int i = 0; i < nEnergies; i++) {
            guessAmp[i] = 0.;
            guessAmp2[i] = 0.;
        }

        for (int i = 0; i < nEnergies; i++) {
            double w;
            double amp;
            double ampG;
            double ampGNorm;
            BasicGraphData gdR = extAmpVsWidthDataV.get(i);
            for (int j = 0; j < nMeasurements; j++) {
                gd = gdV.get(j);
                w = ((Double) gd.getGraphProperty("PHASE_WIDTH"));
                amp = ((Double) gd.getGraphProperty("PARAMETER_VALUE"));
                ampGNorm = gdR.getValueY(w);
                ampG = amp / ampGNorm;
                guessAmp[i] += ampG;
                guessAmp2[i] += ampG * ampG;
            }
        }

        for (int i = 0; i < nEnergies; i++) {
            guessAmp[i] /= nMeasurements;
            guessAmp2[i] = Math.sqrt(Math.abs(guessAmp2[i] - nMeasurements * guessAmp[i] * guessAmp[i]));
            guessAmp2[i] *= Math.sqrt(1.0 / (nMeasurements * (nMeasurements - 1)));
        }

        double minErr = guessAmp2[0];
        int minInd = 0;
        for (int i = 0; i < nEnergies; i++) {
            if (minErr > guessAmp2[i]) {
                minErr = guessAmp2[i];
                minInd = i;
            }
        }

        double bestGuessAmp = guessAmp[minInd];
        double bestGuessAmpErr = guessAmp2[minInd];

        gd = extWidthVsAmpDataV.get(minInd);
        double energyDlt = ((Double) gd.getGraphProperty(energyDelta));

        gdP1WFa.removeAllPoints();
        gdP1AFw.removeAllPoints();

        gd = extWidthVsAmpDataV.get(minInd);
        double x;
        double y;
        for (int i = 0; i < gd.getNumbOfPoints(); i++) {
            x = gd.getX(i);
            y = gd.getY(i);
            gdP1WFa.addPoint(x, y);
            gdP1AFw.addPoint(y, x);
        }

        gdP1ExpWFa.removeAllPoints();
        gdP1MaxVsA.removeAllPoints();

        double amp;

        double w;
        for (int j = 0; j < nMeasurements; j++) {
            gd = gdV.get(j);
            amp = ((Double) gd.getGraphProperty("PARAMETER_VALUE"));
            w = ((Double) gd.getGraphProperty("PHASE_WIDTH"));
            gdP1ExpWFa.addPoint(amp / bestGuessAmp, w);
            gdP1MaxVsA.addPoint(amp / bestGuessAmp, gd.getMaxY());
        }

        //calculate guess phase
        double guessPhase = 0.;
        double guessPhase2 = 0.;

        if (extKShiftVsAmpDataV.isEmpty()) {
            return results;
        }

        int graphInd = 0;
        gd = extKShiftVsAmpDataV.get(0);
        double energyDltTmp = ((Double) gd.getGraphProperty(energyDelta));
        double energyDltNearest = Math.abs(energyDlt - energyDltTmp);
        for (int j = 0; j < extKShiftVsAmpDataV.size(); j++) {
            gd = extKShiftVsAmpDataV.get(j);
            energyDltTmp = ((Double) gd.getGraphProperty(energyDelta));
            if (energyDltNearest > Math.abs(energyDlt - energyDltTmp)) {
                energyDltNearest = Math.abs(energyDlt - energyDltTmp);
                graphInd = j;
            }
        }

        double kShift;
        double phiLeft;
        double phiRight;
        double phaseTmp;

        for (int j = 0; j < nMeasurements; j++) {
            gd = gdV.get(j);
            amp = ((Double) gd.getGraphProperty("PARAMETER_VALUE"));
            phiLeft = ((Double) gd.getGraphProperty("PHASE_LEFT"));
            phiRight = ((Double) gd.getGraphProperty("PHASE_RIGHT"));
            double phaseShift = MainAnalysisController.getPhaseShift(gd);
            gd = extKShiftVsAmpDataV.get(graphInd);
            kShift = gd.getValueY(amp / bestGuessAmp);
            phaseTmp = phiLeft + kShift * (phiRight - phiLeft);
            //DEBUG print ----------------------------------------------------
            LOGGER.log(Level.INFO, "debug j={0} amp={1} delta[%]={2} guessAmp={3} amp/guessAmp={4} phi_left={5} phi_right={6} phi={7} k_shift={8} phaseShift={9}", new Object[]{j, ampFormat.format(amp), ampFormat.format(energyDlt), ampFormat.format(bestGuessAmp), ampFormat.format(amp / bestGuessAmp), ampFormat.format(phiLeft), ampFormat.format(phiRight), ampFormat.format(phaseTmp), ampFormat.format(kShift), ampFormat.format(phaseShift)});
            //DEBUG print ----------------------------------------------------

            guessPhase += phaseTmp;
            guessPhase2 += phaseTmp * phaseTmp;
        }

        guessPhase /= nMeasurements;
        guessPhase2 = Math.sqrt(Math.abs(guessPhase2 - nMeasurements * guessPhase * guessPhase));
        guessPhase2 *= Math.sqrt(1.0 / (nMeasurements * (nMeasurements - 1)));

        //set results and return
        results = new double[5];
        results[0] = bestGuessAmp;
        results[1] = bestGuessAmpErr;
        results[2] = energyDlt;
        results[3] = guessPhase;
        results[4] = guessPhase2;

        return results;
    }

    //read theory data from data file
    /**
     * Description of the Method
     *
     * @param fNameWvsA Description of the Parameter
     * @param fNameKSvsA Description of the Parameter
     */
    private void readTheoryData(String fNameWvsA, String fNameKSvsA) {

        theoryWvsADataFileName = fNameWvsA;
        theoryKSvsADataFileName = fNameKSvsA;

        //============READ DPHI Vs. AMPLITUDE =====================================================
        URL dataURL = Application.getAdaptor().getResourceURL("data/" + fNameWvsA);

        try (InputStream inps = dataURL.openStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inps, StandardCharsets.UTF_8))) {

            extWidthVsAmpDataV.clear();
            extAmpVsWidthDataV.clear();

            String lineIn = in.readLine();
            //this array includes n+1 tokens because first element is string with length=0
            String[] dataS = lineIn.split("[,\\s]+");

            for (int i = 0; i < dataS.length; i++) {
                if (dataS[i].length() > 0) {
                    BasicGraphData gd = new BasicGraphData();
                    gd.setGraphProperty(energyDelta, Double.valueOf(dataS[i]));
                    extWidthVsAmpDataV.add(gd);
                    //reverse data
                    BasicGraphData gdR = new BasicGraphData();
                    gdR.setGraphProperty(energyDelta, Double.valueOf(dataS[i]));
                    extAmpVsWidthDataV.add(gdR);

                }
            }

            extWidthVsAmpDataV.remove(0);
            extAmpVsWidthDataV.remove(0);

            int nEnergyPoints = extWidthVsAmpDataV.size();

            lineIn = in.readLine();
            while (lineIn != null) {

                dataS = lineIn.split("[,\\s]+");
                if (dataS.length == (nEnergyPoints + 2)) {
                    double x = Double.parseDouble(dataS[1]);
                    for (int i = 0; i < nEnergyPoints; i++) {
                        double y = Double.parseDouble(dataS[i + 2]);
                        extWidthVsAmpDataV.get(i).addPoint(x, y);
                        extAmpVsWidthDataV.get(i).addPoint(y, x);
                    }
                } else {
                    break;
                }

                lineIn = in.readLine();
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            messageTextLocal.setText(null);
            messageTextLocal.setText("Fatal error. Can not open file =" + fNameWvsA
                    + ". Stop execution all analysis will be wrong");
        }

        //============READ k shift coeff. Vs. AMPLITUDE =====================================================
        dataURL = Application.getAdaptor().getResourceURL("data/" + fNameKSvsA);

        try (
                InputStream inps = dataURL.openStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inps, StandardCharsets.UTF_8))) {

            extKShiftVsAmpDataV.clear();
            extAmpVsKShiftDataV.clear();

            String lineIn = in.readLine();
            //this array includes n+1 tokens because first element is string with length=0
            String[] dataS = lineIn.split("[,\\s]+");

            for (int i = 0; i < dataS.length; i++) {
                if (dataS[i].length() > 0) {
                    BasicGraphData gd = new BasicGraphData();
                    gd.setGraphProperty(energyDelta, Double.valueOf(dataS[i]));
                    extKShiftVsAmpDataV.add(gd);
                    //reverse data
                    BasicGraphData gdR = new BasicGraphData();
                    gdR.setGraphProperty(energyDelta, Double.valueOf(dataS[i]));
                    extAmpVsKShiftDataV.add(gdR);

                }
            }

            extKShiftVsAmpDataV.remove(0);
            extAmpVsKShiftDataV.remove(0);

            int nEnergyPoints = extKShiftVsAmpDataV.size();

            lineIn = in.readLine();
            while (lineIn != null) {

                dataS = lineIn.split("[,\\s]+");
                if (dataS.length == (nEnergyPoints + 2)) {
                    double x = Double.parseDouble(dataS[1]);
                    for (int i = 0; i < nEnergyPoints; i++) {
                        double y = Double.parseDouble(dataS[i + 2]);
                        extKShiftVsAmpDataV.get(i).addPoint(x, y);
                        extAmpVsKShiftDataV.get(i).addPoint(y, x);
                    }
                } else {
                    break;
                }

                lineIn = in.readLine();
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            messageTextLocal.setText(null);
            messageTextLocal.setText("Fatal error. Can not open file =" + fNameKSvsA
                    + ". Stop execution all analysis will be wrong");
        }

    }

    //make predefined forward and backward graphs for width vs. amplitude
    /**
     * Description of the Method
     *
     * @param energyDlt Description of the Parameter
     */
    private void makeForwardAndBackWardGraphs(double energyDlt) {

        //definition of amplitude vs width and backward
        gdP1WFa.removeAllPoints();
        gdP1AFw.removeAllPoints();

        BasicGraphData gd = extWidthVsAmpDataV.get(0);
        double energyDltGrph = ((Double) gd.getGraphProperty(energyDelta));
        double minDev = Math.abs(energyDlt - energyDltGrph);
        int indexGrph = 0;
        for (int i = 0; i < extWidthVsAmpDataV.size(); i++) {
            gd = extWidthVsAmpDataV.get(i);
            energyDltGrph = ((Double) gd.getGraphProperty(energyDelta));
            double dev = Math.abs(energyDlt - energyDltGrph);
            if (minDev > dev) {
                minDev = dev;
                indexGrph = i;
            }
        }

        gd = extWidthVsAmpDataV.get(indexGrph);
        double x;
        double y;
        for (int i = 0; i < gd.getNumbOfPoints(); i++) {
            x = gd.getX(i);
            y = gd.getY(i);
            gdP1WFa.addPoint(x, y);
            gdP1AFw.addPoint(y, x);
        }

        //definition of the k_shift (ks) coeff vs. normalized amplitude and backward
        gdP1KsFa.removeAllPoints();
        gdP1AFks.removeAllPoints();

        gd = extKShiftVsAmpDataV.get(0);
        energyDltGrph = ((Double) gd.getGraphProperty(energyDelta));
        minDev = Math.abs(energyDlt - energyDltGrph);
        indexGrph = 0;
        for (int i = 0; i < extKShiftVsAmpDataV.size(); i++) {
            gd = extKShiftVsAmpDataV.get(i);
            energyDltGrph = ((Double) gd.getGraphProperty(energyDelta));
            double dev = Math.abs(energyDlt - energyDltGrph);
            if (minDev > dev) {
                minDev = dev;
                indexGrph = i;
            }
        }

        gd = extKShiftVsAmpDataV.get(indexGrph);
        for (int i = 0; i < gd.getNumbOfPoints(); i++) {
            x = gd.getX(i);
            y = gd.getY(i);
            gdP1KsFa.addPoint(x, y);
            gdP1AFks.addPoint(y, x);
        }
    }
}
