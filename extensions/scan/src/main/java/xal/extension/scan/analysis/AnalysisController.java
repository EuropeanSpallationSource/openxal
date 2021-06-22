package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 * This class is a base class for different analysis of the scan data.
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class AnalysisController{

    protected String name = "EMPTY";
    protected String typeName = "EMPTY_TYPE";

    //variables from upper level application
    protected boolean scanPVShowState    = false;
    protected boolean scanPVRBShowState = false;

    protected MainAnalysisController mainController = null;

    protected JPanel parentAnalysisPanel  = null;
    protected JPanel customControlPanel   = null;
    protected JPanel customGraphPanel     = null;
    protected JPanel globalButtonsPanel   = null;

    protected ScanVariable scanVariableParameter = null;
    protected ScanVariable scanVariable          = null;
    protected Vector<MeasuredValue> measuredValuesV             = null;
    protected FunctionGraphsJPanel graphAnalysis = null;
    protected JTextField messageTextLocal        = null;

    //data reader panel
    protected JPanel dataReaderPanel = null;

    //local temporary draph data 
    protected BasicGraphData graphDataLocal = null;

    /**  The constructor.*/   
    public AnalysisController( MainAnalysisController mainControllerIn,
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
			       BasicGraphData graphDataLocalIn){


	mainController = mainControllerIn;
	parentAnalysisPanel = parentAnalysisPanelIn;
	customControlPanel = customControlPanelIn;
	customGraphPanel = customGraphPanelIn;
	globalButtonsPanel = globalButtonsPanelIn;
	scanVariableParameter = scanVariableParameterIn;
	scanVariable = scanVariableIn;
	measuredValuesV = measuredValuesVIn;
	graphAnalysis = graphAnalysisIn;
	messageTextLocal = messageTextLocalIn;
	graphDataLocal = graphDataLocalIn;
        dataReaderPanel = mainController.getDataReaderPanel();

    }

    /**  Sets the name of the analysis.*/  
    public void setName(String name){
	this.name = name;
    }

    /**  Sets the type name of the analysis.*/  
    public void setTypeName(String typeName){
	this.typeName = typeName;
    }

    /**  Returns the name of the analysis.*/  
    public String getName(){
	return name;
    }

    /**  Returns the type name of the analysis.*/  
    public String getTypeName(){
	return typeName;
    }

    /**
     * Sets mask specifying if the data for scan PV  scan read back PV should be shown.
     */
    public void setScanPVandScanPV_RB_State(boolean scanPVShowState,boolean scanPVRBShowState) {
        this.scanPVShowState = scanPVShowState;
        this.scanPVRBShowState = scanPVRBShowState;
    }

    /**
     * Sets the configuration of the analysis.
     * The subclasses should call the super-class method in this method.
     */
    public void dumpAnalysisConfig(DataAdaptor analysisConfig){
       DataAdaptor nameDA = analysisConfig.createChild("ANALYSIS_NAME");
       nameDA.setValue("name",getName());
    }

    /**  Sets fonts for all GUI elements.
     *   The subclasses should call the super-class method in this method.
     */  
    public void setFontsForAll(Font fnt){

    }

    /**  Does what necessary for close this analysis window. 
     *   This method could be overridden, because it is empty here.
     */  
    public void ShutUp(){
    }

    /**  Does what necessary for open this analysis window. 
     *   This method could be overridden, because it is empty here.
     */  
    public void ShowUp(){
    }

    /**
     * Updates data on the analysis graph panel. 
     * This method will be called outside of this analysis controller,
     * and should update data only related to the inner business for 
     * this analysis.
     * This method could be overridden, because it is empty here. 
     */
    public void updateDataSetOnGraphPanel(){
    }

    /**
     * Sets local message text field.
     */  
    public void setMessageTextField(JTextField messageTextLocal) {
	this.messageTextLocal = messageTextLocal;
    }


}
