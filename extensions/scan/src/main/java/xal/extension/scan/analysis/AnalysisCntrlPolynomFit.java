package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 * This class is a analysis class for polynomial fitting.
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public final class AnalysisCntrlPolynomFit extends AnalysisController{

    //DEFINITION  "POLYNOMIAL FITTING" PANEL
    private JPanel polynomFitMaxPanel = new JPanel();
    private JButton fittingPanel2Button = new JButton("START FITTING");
    private JSpinner rankPanel2Spinner  = new JSpinner(new SpinnerNumberModel(0,0,3,1)); 
    private JLabel spinnerPanel2Label   = new JLabel(" Order of Fitting :",JLabel.LEFT);
    private DecimalFormat coeffPanel2Format = new DecimalFormat("0.000E0");

    /**  The constructor.*/   
    public AnalysisCntrlPolynomFit(MainAnalysisController mainControllerIn,
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


	String nameIn = "POLYNOMIAL FITTING";
	DataAdaptor nameDA =   analysisConf.childAdaptor("ANALYSIS_NAME");
	if(nameDA != null){
	    nameIn = nameDA.stringValue("name");
	}
        setName(nameIn);

	//create fitting panel
	makePolynomFittingPanel();
    }

    /**
     * Sets the configurations of the analysis.
     */
    @Override
    public void dumpAnalysisConfig(DataAdaptor analysisConfig){
	super.dumpAnalysisConfig(analysisConfig);
    }

    /**  Sets fonts for all GUI elements.
     */  
    @Override
    public void setFontsForAll(Font fnt){
	super.setFontsForAll(fnt);

	//FITTING PANEL ELEMENTS
	fittingPanel2Button.setFont(fnt);
	rankPanel2Spinner.setFont(fnt);
	((JSpinner.DefaultEditor) rankPanel2Spinner.getEditor()).getTextField().setFont(fnt);
	spinnerPanel2Label.setFont(fnt);
    }

    /**  Does what necessary for close this analysis window. 
     */  
    @Override
    public void ShutUp(){
	super.ShutUp();
	customControlPanel.removeAll();
    }

    /**  Does what necessary for open this analysis window. 
     *   This method could be overridden, because it is empty here.
     */  
    @Override
    public void ShowUp(){
	super.ShowUp();
	customControlPanel.add(dataReaderPanel,BorderLayout.NORTH);
	customControlPanel.add(polynomFitMaxPanel,BorderLayout.CENTER);
        customGraphPanel.add(graphAnalysis,BorderLayout.CENTER);
        customGraphPanel.add(globalButtonsPanel,BorderLayout.SOUTH);
    }

    /**
     * Updates data on the analysis graph panel.  
     */
    @Override
    public void updateDataSetOnGraphPanel(){
	super.updateDataSetOnGraphPanel();
    }

    //-----------------------------------------------------
    //PANEL DEFINITION
    //-----------------------------------------------------
    private void makePolynomFittingPanel(){

	rankPanel2Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

	JPanel tmp0 = new JPanel();
        tmp0.setLayout(new GridLayout(1,2,1,1));
        tmp0.add(spinnerPanel2Label);
        tmp0.add(rankPanel2Spinner);
        

	JPanel tmp1 = new JPanel();
        tmp1.setLayout(new GridLayout(2,1,1,1));
        tmp1.add(tmp0);
        tmp1.add(fittingPanel2Button);

        polynomFitMaxPanel.setLayout(new BorderLayout());
        polynomFitMaxPanel.add(tmp1,BorderLayout.NORTH);

	fittingPanel2Button.addActionListener(new ActionListener(){
                @Override
		public void actionPerformed(ActionEvent e){
		    BasicGraphData gd = mainController.getChoosenDraphData();
                    if(gd != null){
			graphAnalysis.removeGraphData(graphDataLocal);
			graphDataLocal.removeAllPoints();
			double xMin = graphAnalysis.getCurrentMinX();
			double xMax = graphAnalysis.getCurrentMaxX();
			double yMin = graphAnalysis.getCurrentMinY();
			double yMax = graphAnalysis.getCurrentMaxY();
                        int order = ((Integer) rankPanel2Spinner.getValue());
			GraphDataOperations.polynomialFit(gd,graphDataLocal,xMin,xMax,order,10);

			double[][] coeff =  GraphDataOperations.polynomialFit(gd,xMin,xMax,order);
                        if(coeff != null && coeff[0].length > 0){
                            String formula = "Fitting: y = ";
			    for(int i = 0, n=coeff[0].length; i < n; i++){
				formula = formula + "("+coeffPanel2Format.format(coeff[0][i]) +")*X^"+i;
                                if(i != (n-1)){
				    formula = formula +" + ";
				}
			    }
			    messageTextLocal.setText(null);
			    messageTextLocal.setText(formula);
			}
			else{
			    messageTextLocal.setText(null);
			    messageTextLocal.setText("Cannot do fitting.");
			    Toolkit.getDefaultToolkit().beep();
			}
			graphAnalysis.addGraphData(graphDataLocal);
		    }
		    else{
			messageTextLocal.setText(null);
			messageTextLocal.setText("Please choose graph and point first. Use S-button on the graph panel.");
			Toolkit.getDefaultToolkit().beep();
		    }
		}
	    }); 

	fittingPanel2Button.setForeground(Color.blue);
    }

}
