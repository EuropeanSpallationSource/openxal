package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 * This class is a analysis class for general data reading and management.
 *
 * @version 1.0
 * @author A. Shishlo
 */
public final class AnalysisCntrlManagement extends AnalysisController {

    /**
     * The constructor.
     */
    public AnalysisCntrlManagement(MainAnalysisController mainControllerIn,
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

        String nameIn = "MANAGEMENT";
        DataAdaptor nameDA = analysisConf.childAdaptor("ANALYSIS_NAME");
        if (nameDA != null) {
            nameIn = nameDA.stringValue("name");
        }
        setName(nameIn);

    }

    /**
     * Does what necessary for close this analysis window.
     */
    @Override
    public void shutUp() {
        super.shutUp();
        customControlPanel.removeAll();
        customGraphPanel.removeAll();
    }

    /**
     * Does what necessary for open this analysis window. This method could be
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
