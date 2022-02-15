package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;

/**
 * This class is a analysis class to find min and max.
 *
 * @author A. Shishlo
 * @version 1.0
 */
public final class AnalysisCntrlFindMinMax extends AnalysisController {

    //DEFINITION  "FIND MIN/MAX" PANEL
    private JPanel findMinMaxPanel = new JPanel();
    private JLabel markerPosLabel = new JLabel(" Marker Position :");
    private JLabel pvSetLabel = new JLabel(" Scan PV Set:");
    private JLabel pvRBLabel = new JLabel(" Scan PV RB:");

    private JButton findButton = new JButton("FIND MAX/MIN");
    private JButton setValButton = new JButton("SET FOUND VALUE TO EPICS");
    private JButton readValButton = new JButton("READ CURRENT VALUES");

    private DoubleInputTextField markerPosText = new DoubleInputTextField(10);
    private DoubleInputTextField pvSetValText = new DoubleInputTextField(10);
    private DoubleInputTextField pvRBValText = new DoubleInputTextField(10);

    private DecimalFormat valFormat = new DecimalFormat("####.####");

    private ActionListener dragVerLineListener = null;
    private double markerPos = 0.;
    private double phaseShift = 0.;

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
    public AnalysisCntrlFindMinMax(MainAnalysisController mainControllerIn,
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

        String nameIn = "FIND MIN/MAX";
        DataAdaptor nameDA = analysisConf.childAdaptor("ANALYSIS_NAME");
        if (nameDA != null) {
            nameIn = nameDA.stringValue("name");
        }
        setName(nameIn);

        graphAnalysis.addDraggedVerLinesListener(null);
        graphAnalysis.removeVerticalValue(0);

        makeFindMinMaxPanel();
    }

    /**
     * Sets fonts for all GUI elements.
     *
     * @param fnt The new fontsForAll value
     */
    @Override
    public void setFontsForAll(Font fnt) {
        super.setFontsForAll(fnt);

        markerPosLabel.setFont(fnt);
        pvSetLabel.setFont(fnt);
        pvRBLabel.setFont(fnt);
        findButton.setFont(fnt);
        setValButton.setFont(fnt);
        readValButton.setFont(fnt);
        markerPosText.setFont(fnt);
        pvSetValText.setFont(fnt);
        pvRBValText.setFont(fnt);
    }

    /**
     * Does what necessary for close this analysis window.
     */
    @Override
    public void shutUp() {
        super.shutUp();
        customControlPanel.removeAll();
        graphAnalysis.addDraggedVerLinesListener(null);
        graphAnalysis.removeVerticalValue(0);
    }

    /**
     * Does what necessary for open this analysis window. This method could be
     * overridden, because it is empty here.
     */
    @Override
    public void showUp() {
        super.showUp();

        graphAnalysis.addVerticalLine(markerPos, Color.red);
        graphAnalysis.addDraggedVerLinesListener(dragVerLineListener);
        graphAnalysis.setDraggedVerLinesMotionListen(true);

        customControlPanel.add(dataReaderPanel, BorderLayout.NORTH);
        customControlPanel.add(findMinMaxPanel, BorderLayout.CENTER);
        customGraphPanel.add(graphAnalysis, BorderLayout.CENTER);
        customGraphPanel.add(globalButtonsPanel, BorderLayout.SOUTH);
    }

    //-----------------------------------------------------
    //PANEL DEFINITION
    //-----------------------------------------------------
    /**
     * Description of the Method
     */
    private void makeFindMinMaxPanel() {
        markerPosText.setEditable(true);
        pvSetValText.setEditable(false);
        pvRBValText.setEditable(false);

        markerPosText.setNumberFormat(valFormat);
        pvSetValText.setNumberFormat(valFormat);
        pvRBValText.setNumberFormat(valFormat);

        markerPosText.setHorizontalAlignment(SwingConstants.CENTER);
        pvSetValText.setHorizontalAlignment(SwingConstants.CENTER);
        pvRBValText.setHorizontalAlignment(SwingConstants.CENTER);

        markerPosText.removeInnerFocusListener();
        pvSetValText.removeInnerFocusListener();
        pvRBValText.removeInnerFocusListener();

        findMinMaxPanel.setLayout(new BorderLayout());
        Border etchedBorder = BorderFactory.createEtchedBorder();
        findMinMaxPanel.setBorder(etchedBorder);

        JPanel temp0 = new JPanel();
        temp0.setLayout(new GridLayout(1, 2, 1, 1));
        temp0.add(markerPosLabel);
        temp0.add(markerPosText);

        JPanel temp1 = new JPanel();
        temp1.setLayout(new BorderLayout());
        temp1.add(findButton, BorderLayout.NORTH);
        temp1.add(temp0, BorderLayout.CENTER);
        temp1.add(setValButton, BorderLayout.SOUTH);

        JPanel temp2 = new JPanel();
        temp2.setLayout(new GridLayout(2, 2, 1, 1));
        temp2.add(pvSetLabel);
        temp2.add(pvSetValText);
        temp2.add(pvRBLabel);
        temp2.add(pvRBValText);

        JPanel temp3 = new JPanel();
        temp3.setLayout(new BorderLayout());
        temp3.add(temp1, BorderLayout.NORTH);
        temp3.add(temp2, BorderLayout.CENTER);
        temp3.add(readValButton, BorderLayout.SOUTH);

        findMinMaxPanel.add(temp3, BorderLayout.NORTH);

        dragVerLineListener = e -> {
            int ind = graphAnalysis.getDraggedLineIndex();
            markerPos = graphAnalysis.getVerticalValue(ind);
            markerPos -= phaseShift;
            if (phaseShift != 0.) {
                markerPos += 180.;
                while (markerPos < 0.) {
                    markerPos += 360.;
                }
                markerPos = markerPos % 360.;
                markerPos -= 180.;
            }
            markerPosText.setValueQuietly(markerPos);
        };

        markerPosText.addActionListener(e -> {
            graphAnalysis.addDraggedVerLinesListener(null);
            markerPos = markerPosText.getValue();
            double phase = markerPos + phaseShift;
            if (phaseShift != 0.) {
                phase += 180.;
                while (phase < 0.) {
                    phase += 360.;
                }
                phase = phase % 360.;
                phase -= 180.;
            }
            graphAnalysis.setVerticalLineValue(phase, 0);
            graphAnalysis.addDraggedVerLinesListener(dragVerLineListener);
        });

        ActionListener findMaxMinListener = e -> {
            BasicGraphData gd = mainController.getChoosenDraphData();
            if (gd != null) {
                graphAnalysis.removeGraphData(graphDataLocal);
                graphDataLocal.removeAllPoints();
                if (gd.getNumbOfPoints() > 0) {
                    GraphDataOperations.polynomialFit(gd, graphDataLocal,
                            graphAnalysis.getCurrentMinX(),
                            graphAnalysis.getCurrentMaxX(), 2, 10);
                    double dMaxPos = GraphDataOperations.getExtremumPosition(graphDataLocal,
                            graphAnalysis.getCurrentMinX(),
                            graphAnalysis.getCurrentMaxX());
                    if (dMaxPos > graphAnalysis.getCurrentMinX() && dMaxPos < graphAnalysis.getCurrentMaxX()) {
                        phaseShift = MainAnalysisController.getPhaseShift(gd);
                        graphAnalysis.addDraggedVerLinesListener(null);
                        dMaxPos -= phaseShift;
                        if (phaseShift != 0.) {
                            dMaxPos += 180.;
                            while (dMaxPos < 0.) {
                                dMaxPos += 360.;
                            }
                            dMaxPos = dMaxPos % 360.;
                            dMaxPos -= 180.;
                        }
                        markerPosText.setValue(dMaxPos);
                        graphAnalysis.addDraggedVerLinesListener(dragVerLineListener);
                        messageTextLocal.setText(null);
                        messageTextLocal.setText("Extremum has been found. The phase_shift value =" + valFormat.format(phaseShift));
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                        messageTextLocal.setText(null);
                        messageTextLocal.setText("Cannot find extremum in the specified region.");
                        graphDataLocal.removeAllPoints();
                        graphAnalysis.refreshGraphJPanel();
                    }

                } else {
                    Toolkit.getDefaultToolkit().beep();
                    messageTextLocal.setText(null);
                    messageTextLocal.setText("The graph does not have data points.");
                }
                graphAnalysis.addGraphData(graphDataLocal);
            } else {
                messageTextLocal.setText(null);
                messageTextLocal.setText("Please choose graph and point first. Use S-button on the graph panel.");
                Toolkit.getDefaultToolkit().beep();
            }
        };

        setValButton.addActionListener(e -> {
            double val = markerPosText.getValue();
            if (scanVariable.getChannel() != null) {
                scanVariable.setValue(val);
            } else {
                messageTextLocal.setText(null);
                messageTextLocal.setText("The scan PV channel does not exist.");
                Toolkit.getDefaultToolkit().beep();
            }
        });

        readValButton.addActionListener(e -> {
            if (scanVariable.getChannel() != null) {
                pvSetValText.setValue(scanVariable.getValue());
            } else {
                pvSetValText.setText(null);
                pvSetValText.setBackground(Color.white);
            }
            if (scanVariable.getChannelRB() != null) {
                pvRBValText.setValue(scanVariable.getValueRB());
            } else {
                pvRBValText.setText(null);
                pvRBValText.setBackground(Color.white);
            }
        });

        findButton.addActionListener(findMaxMinListener);

        findButton.setForeground(Color.blue);
        setValButton.setForeground(Color.blue);
        readValButton.setForeground(Color.blue);
    }
}
