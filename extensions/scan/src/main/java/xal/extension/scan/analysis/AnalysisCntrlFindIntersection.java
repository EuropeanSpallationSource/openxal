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
 * This class is a analysis class to find an intersection point.
 *
 * @author A. Shishlo
 * @version 1.0
 */
public final class AnalysisCntrlFindIntersection extends AnalysisController {

    //DEFINITION  "FIND INTERSECTION" PANEL
    private JPanel findIntersectionPanel = new JPanel();
    private JLabel markerPosLabel = new JLabel(" Marker Position :");
    private JLabel pvSetLabel = new JLabel(" Scan PV Set:");
    private JLabel pvRBLabel = new JLabel(" Scan PV RB:");

    private ActionListener findIntersectionListener = null;

    private JButton findButton = new JButton("FIND INTERSECTION");
    private JButton setValButton = new JButton("SET FOUND VALUE TO EPICS");
    private JButton readValButton = new JButton("READ CURRENT VALUES");

    private DoubleInputTextField markerPosText = new DoubleInputTextField(10);
    private DoubleInputTextField pvSetValText = new DoubleInputTextField(10);
    private DoubleInputTextField pvRBValText = new DoubleInputTextField(10);

    private DecimalFormat valFormat = new DecimalFormat("####.####");

    private ActionListener dragVerLineListener = null;
    private double markerPos = 0.;

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
    public AnalysisCntrlFindIntersection(MainAnalysisController mainControllerIn,
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

        String nameIn = "FIND INTERSECTION";
        DataAdaptor nameDA = analysisConf.childAdaptor("ANALYSIS_NAME");
        if (nameDA != null) {
            nameIn = nameDA.stringValue("name");
        }
        setName(nameIn);

        graphAnalysis.addDraggedVerLinesListener(null);
        graphAnalysis.removeVerticalValue(0);

        makeIntersectionFindingPanel();

    }

    /**
     * Sets the configurations of the analysis.
     *
     * @param analysisConfig Description of the Parameter
     */
    @Override
    public void dumpAnalysisConfig(DataAdaptor analysisConfig) {
        super.dumpAnalysisConfig(analysisConfig);
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
    public void ShutUp() {
        super.ShutUp();
        customControlPanel.removeAll();
        graphAnalysis.addDraggedVerLinesListener(null);
        graphAnalysis.removeVerticalValue(0);
    }

    /**
     * Does what necessary for open this analysis window. This method could be
     * overridden, because it is empty here.
     */
    @Override
    public void ShowUp() {
        super.ShowUp();

        graphAnalysis.addVerticalLine(markerPos, Color.red);
        graphAnalysis.addDraggedVerLinesListener(dragVerLineListener);
        graphAnalysis.setDraggedVerLinesMotionListen(true);

        customControlPanel.add(dataReaderPanel, BorderLayout.NORTH);
        customControlPanel.add(findIntersectionPanel, BorderLayout.CENTER);
        customGraphPanel.add(graphAnalysis, BorderLayout.CENTER);
        customGraphPanel.add(globalButtonsPanel, BorderLayout.SOUTH);
    }

    /**
     * Updates data on the analysis graph panel.
     */
    @Override
    public void updateDataSetOnGraphPanel() {
        super.updateDataSetOnGraphPanel();
    }

    //-----------------------------------------------------
    //PANEL DEFINITION
    //-----------------------------------------------------
    /**
     * Description of the Method
     */
    private void makeIntersectionFindingPanel() {
        markerPosText.setEditable(true);
        pvSetValText.setEditable(false);
        pvRBValText.setEditable(false);

        markerPosText.setNumberFormat(valFormat);
        pvSetValText.setNumberFormat(valFormat);
        pvRBValText.setNumberFormat(valFormat);

        markerPosText.setHorizontalAlignment(JTextField.CENTER);
        pvSetValText.setHorizontalAlignment(JTextField.CENTER);
        pvRBValText.setHorizontalAlignment(JTextField.CENTER);

        markerPosText.removeInnerFocusListener();
        pvSetValText.removeInnerFocusListener();
        pvRBValText.removeInnerFocusListener();

        findIntersectionPanel.setLayout(new BorderLayout());
        Border etchedBorder = BorderFactory.createEtchedBorder();
        findIntersectionPanel.setBorder(etchedBorder);

        JPanel temp_0 = new JPanel();
        temp_0.setLayout(new GridLayout(1, 2, 1, 1));
        temp_0.add(markerPosLabel);
        temp_0.add(markerPosText);

        JPanel temp_1 = new JPanel();
        temp_1.setLayout(new BorderLayout());
        temp_1.add(findButton, BorderLayout.NORTH);
        temp_1.add(temp_0, BorderLayout.CENTER);
        temp_1.add(setValButton, BorderLayout.SOUTH);

        JPanel temp_2 = new JPanel();
        temp_2.setLayout(new GridLayout(2, 2, 1, 1));
        temp_2.add(pvSetLabel);
        temp_2.add(pvSetValText);
        temp_2.add(pvRBLabel);
        temp_2.add(pvRBValText);

        JPanel temp_3 = new JPanel();
        temp_3.setLayout(new BorderLayout());
        temp_3.add(temp_1, BorderLayout.NORTH);
        temp_3.add(temp_2, BorderLayout.CENTER);
        temp_3.add(readValButton, BorderLayout.SOUTH);

        findIntersectionPanel.add(temp_3, BorderLayout.NORTH);

        dragVerLineListener
                = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int ind = graphAnalysis.getDraggedLineIndex();
                double phase = graphAnalysis.getVerticalValue(ind);
                double shift = MainAnalysisController.getPhaseShift(graphAnalysis.getAllGraphData());
                markerPos = phase - shift;
                if (shift != 0.) {
                    markerPos += 180.;
                    while (markerPos < 0.) {
                        markerPos += 360.;
                    }
                    markerPos = markerPos % 360.;
                    markerPos -= 180.;
                }
                markerPosText.setValueQuietly(markerPos);
            }
        };

        markerPosText.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphAnalysis.addDraggedVerLinesListener(null);
                markerPos = markerPosText.getValue();
                double shift = MainAnalysisController.getPhaseShift(graphAnalysis.getAllGraphData());
                double phase = markerPos + shift;
                if (shift != 0.) {
                    phase += 180.;
                    while (phase < 0.) {
                        phase += 360.;
                    }
                    phase = phase % 360.;
                    phase -= 180.;
                }
                graphAnalysis.setVerticalLineValue(phase, 0);
                graphAnalysis.addDraggedVerLinesListener(dragVerLineListener);
            }
        });

        findIntersectionListener
                = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double xMin = graphAnalysis.getCurrentMinX();
                double xMax = graphAnalysis.getCurrentMaxX();
                double yMin = graphAnalysis.getCurrentMinY();
                double yMax = graphAnalysis.getCurrentMaxY();
                Vector<BasicGraphData> interpGDV = graphAnalysis.getAllGraphData();
                interpGDV.remove(graphDataLocal);
                if (interpGDV.size() > 1) {
                    Double[] intersectV = GraphDataOperations.findIntersection(interpGDV,
                            xMin, xMax, yMin, yMax, 0.001);
                    if (intersectV[0] != null) {
                        graphAnalysis.addDraggedVerLinesListener(null);
                        double phase = intersectV[0];
                        double shift = MainAnalysisController.getPhaseShift(interpGDV);
                        markerPos = phase - shift;
                        if (shift != 0.) {
                            markerPos += 180.;
                            while (markerPos < 0.) {
                                markerPos += 360.;
                            }
                            markerPos = markerPos % 360.;
                            markerPos -= 180.;
                        }
                        markerPosText.setValue(markerPos);
                        graphAnalysis.addDraggedVerLinesListener(dragVerLineListener);
                        messageTextLocal.setText(null);
                        messageTextLocal.setText("The intersection point x="
                                + valFormat.format(intersectV[0])
                                + " +- "
                                + valFormat.format(intersectV[2])
                                + "   shift = "
                                + valFormat.format(shift)
                        );
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                        messageTextLocal.setText(null);
                        messageTextLocal.setText("Cannot find intersection.");
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                    messageTextLocal.setText(null);
                    messageTextLocal.setText("Cannot find intersection. Do not have enough data.");
                }

            }
        };

        setValButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double val = markerPosText.getValue();
                if (scanVariable.getChannel() != null) {
                    scanVariable.setValue(val);
                } else {
                    messageTextLocal.setText(null);
                    messageTextLocal.setText("The scan PV channel does not exist.");
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        readValButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        findButton.addActionListener(findIntersectionListener);

        findButton.setForeground(Color.blue);
        setValButton.setForeground(Color.blue);
        readValButton.setForeground(Color.blue);
    }

}
