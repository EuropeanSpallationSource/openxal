package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 * This class is an empty analysis class. It analyzes the scan data.
 *
 * @author A. Shishlo
 * @version 1.0
 */
public final class AnalysisCntrlEmpty extends AnalysisController {

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
    public AnalysisCntrlEmpty(MainAnalysisController mainControllerIn,
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

        String nameIn = "DATA READER";
        DataAdaptor nameDA = analysisConf.childAdaptor("ANALYSIS_NAME");
        if (nameDA != null) {
            nameIn = nameDA.stringValue("name");
        }
        setName(nameIn);

    }

    /**
     * Does what necessary to open this analysis window. This method could be
     * overridden, because it is empty here.
     */
    @Override
    public void showUp() {
        super.showUp();
        customControlPanel.add(dataReaderPanel, BorderLayout.NORTH);
        customGraphPanel.add(graphAnalysis, BorderLayout.CENTER);
        customGraphPanel.add(globalButtonsPanel, BorderLayout.SOUTH);
    }
}
