package xal.extension.wirescan.apputils;


import xal.extension.widgets.plot.BasicGraphData;
import java.awt.Color;


/*
 * The  WireScanData class keeps the WS raw waveforms and related data such as
 * the parent WS file, PV Log Id, Fitting curves, logarithmic representations of
 * the waveforms.
 */
public class WireScanData{
	
	//the raw waveform for X direction 
	private BasicGraphData wfX = new BasicGraphData();
	//the raw waveform for Y direction 		
	private BasicGraphData wfY = new BasicGraphData();
	
	//Wire scanner Id
	private String wsId = "none";
	
	//PV logger Id
	private int pvlogId = -1;
	
	//name of WS file 
	private String wsFilename = "none";
	
	//fitting parameters	
	private double sigmaX = 0.;
	private double sigmaY = 0.;
	
	private double centerX = 0.;
	private double centerY = 0.;	
	
	private double baseX = 0.;
	private double baseY = 0.;
	
	private double ampX = 0.;
	private double ampY = 0.;	
	
	private double sigmaRmsX = 0.;
	private double sigmaRmsY = 0.;
	
	private double centerRmsX = 0.;
	private double centerRmsY = 0.;
	
	//the log(wf) logarithms of the raw waveform
	private BasicGraphData logWfX = new BasicGraphData();
	private BasicGraphData logWfY = new BasicGraphData();
	
	//fitted curve
	private BasicGraphData fitWfX = new BasicGraphData();
	private BasicGraphData fitWfY = new BasicGraphData();
	
	//logarithm of fitted curve
	private BasicGraphData logFitWfX = new BasicGraphData();
	private BasicGraphData logFitWfY = new BasicGraphData();
	
	/** Constructor of an empty wire scanner data object */
	public WireScanData(){		
		setLegendToGraphs();
	}
	
	private void setLegendToGraphs(){
		String str = " file=" + wsFilename + "ws=" + wsId + " pvLog= " + pvlogId + " dir= ";
		wfX.setGraphProperty("Legend",str + "X  raw");
		wfY.setGraphProperty("Legend",str + "Y  raw");
		logWfX.setGraphProperty("Legend",str + "X  log(raw)");
		logWfY.setGraphProperty("Legend",str + "Y  log(raw)");
		fitWfX.setGraphProperty("Legend",str + "X  Gauss Fit");
		fitWfY.setGraphProperty("Legend",str + "Y  Gauss Fit");
		logFitWfX.setGraphProperty("Legend",str + "X  log(Gauss Fit)");
		logFitWfY.setGraphProperty("Legend",str + "Y  log(Gauss Fit)");
		
		wfX.setGraphColor(Color.black);
		wfY.setGraphColor(Color.black);
		logWfX.setGraphColor(Color.black);
		logWfY.setGraphColor(Color.black);
		fitWfX.setGraphColor(Color.red);
		fitWfY.setGraphColor(Color.red);
		logFitWfX.setGraphColor(Color.red);
		logFitWfY.setGraphColor(Color.red);
		
		
		wfX.setDrawLinesOn(false);
		wfY.setDrawLinesOn(false);
		logWfX.setDrawLinesOn(false);
		logWfY.setDrawLinesOn(false);
		fitWfX.setDrawPointsOn(false);
		fitWfY.setDrawPointsOn(false);
		logFitWfX.setDrawPointsOn(false);
		logFitWfY.setDrawPointsOn(false);	
		
		wfX.setGraphPointSize(4);
		wfY.setGraphPointSize(4);
		logWfX.setGraphPointSize(4);
		logWfY.setGraphPointSize(4);
		fitWfX.setLineThick(2);
		fitWfY.setLineThick(2);
		logFitWfX.setLineThick(2);
		logFitWfY.setLineThick(2);	
	}
	
	/** Returns the Id of the Wire Scanner */
	public String getId(){
		return wsId;
	}
	
	/** Sets the Id of the Wire Scanner */
	public void setId(String wsId){
		this.wsId = wsId;
		setLegendToGraphs();
	}
	
	/** Returns the PV Logger Id of the scan */
	public int getPVLogId(){
		return pvlogId;
	}
	
	/** Sets the PV Logger Id of the scan */
	public void setPVLogId(int pvlogId){
		this.pvlogId = pvlogId;
		setLegendToGraphs();		
	}
	
	/** Returns the name of WS data file */	
	public String getWSFileName(){
		return wsFilename;
	}
	
	/** Sets the name of WS data file */	
	public void setWSFileName(String ws_filename){
		this.wsFilename = ws_filename;
		setLegendToGraphs();		
	}
	
	/** Returns a reference to the BasicGraphData instance with the raw waveform for X-direction */		
	public BasicGraphData getRawWFX(){
		return wfX;
	}
	
	/** Returns a reference to the BasicGraphData instance with the raw waveform for Y-direction */		
	public BasicGraphData getRawWFY(){
		return wfY;
	}	
	
	/** Returns a reference to the BasicGraphData instance with the log of raw waveform for X-direction */		
	public BasicGraphData getLogRawWFX(){
		return logWfX;
	}
	
	/** Returns a reference to the BasicGraphData instance with the log of raw waveform for Y-direction */		
	public BasicGraphData getLogRawWFY(){
		return logWfY;
	}	

	/** Returns a reference to the BasicGraphData instance with the fitting waveform for X-direction */	
	public BasicGraphData getFitWFX(){
		return fitWfX;
	}
	
	/** Returns a reference to the BasicGraphData instance with the fitting waveform for Y-direction */		
	public BasicGraphData getFitWFY(){
		return fitWfY;
	}	
	
	/** Returns a reference to the BasicGraphData instance with the log of fitting waveform for X-direction */	
	public BasicGraphData getLogFitWFX(){
		return logFitWfX;
	}
	
	/** Returns a reference to the BasicGraphData instance with the log of fitting waveform for Y-direction */		
	public BasicGraphData getLogFitWFY(){
		return logFitWfY;
	}	
	
	
	/** Returns the sigma parameter of the Gaussian fit for X-direction */
	public double getSigmaX(){
		return sigmaX;
	}
	
	/** Returns the sigma parameter of the Gaussian fit for Y-direction */	
	public double getSigmaY(){
		return sigmaY;
	}
	
	/** Returns the base parameter of the Gaussian fit for X-direction */
	public double getBaseX(){
		return baseX;
	}
	
	/** Returns the base parameter of the Gaussian fit for Y-direction */	
	public double getBaseY(){
		return baseY;
	}

	/** Returns the amp parameter of the Gaussian fit for X-direction */
	public double getAmpX(){
		return ampX;
	}
	
	/** Returns the amp parameter of the Gaussian fit for Y-direction */	
	public double getAmpY(){
		return ampY;
	}	
	
	/** Returns the position of the center  parameter of the Gaussian fit for X-direction */		
	public double getCenterX(){
		return centerX;
	}
	
	/** Returns the position of the center  parameter of the Gaussian fit for Y-direction */		
	public double getCenterY(){
		return centerY;
	}
	
	/** Sets the sigma parameter of the Gaussian fit for X-direction */
	public void setSigmaX(double sigmaX){
		this.sigmaX = sigmaX;
	}
	
	/** Sets the sigma parameter of the Gaussian fit for Y-direction */	
	public void setSigmaY(double sigmaY){
		this.sigmaY = sigmaY;
	}
		
	/** Sets the base parameter of the Gaussian fit for X-direction */
	public void setBaseX(double baseX){
		this.baseX = baseX;
	}
	
	/** Sets the base parameter of the Gaussian fit for Y-direction */	
	public void setBaseY(double baseY){
		this.baseY = baseY;
	}

	/** Sets the amp parameter of the Gaussian fit for X-direction */
	public void setAmpX(double ampX){
		this.ampX = ampX;
	}
	
	/** Sets the amp parameter of the Gaussian fit for Y-direction */	
	public void setAmpY(double ampY){
		this.ampY = ampY;
	}	
		
	/** Sets the position of the center  parameter of the Gaussian fit for Y-direction */	
	public void setCenterX(double centerX){
		this.centerX = centerX;
	}
	
	/** Sets the position of the center  parameter of the Gaussian fit for Y-direction */	
	public void setCenterY(double centerY){
		this.centerY = centerY;
	}
	
	/** Returns the rms sigma parameter for X-direction */
	public double getSigmaRmsX(){
		return sigmaRmsX;
	}
	
	/** Returns the rms sigma parameter for X-direction */	
	public double getSigmaRmsY(){
		return sigmaRmsY;
	}	
	
	/** Sets the sigma rms parameter for X-direction */
	public void setSigmaRmsX(double sigmaRmsX){
		this.sigmaRmsX = sigmaRmsX;
	}
	
	/** Sets the sigma rms parameter for Y-direction */	
	public void setSigmaRmsY(double sigmaRmsY){
		this.sigmaRmsY = sigmaRmsY;
	}	
		
	/** Returns the rms center parameter for X-direction */
	public double getCenterRmsX(){
		return centerRmsX;
	}
	
	/** Returns the rms center parameter for X-direction */	
	public double getCenterRmsY(){
		return centerRmsY;
	}	
	
	/** Sets the center rms parameter for X-direction */
	public void setCenterRmsX(double centerRmsX){
		this.centerRmsX = centerRmsX;
	}
	
	/** Sets the center rms parameter for Y-direction */	
	public void setCenterRmsY(double centerRmsY){
		this.centerRmsY = centerRmsY;
	}		
	
}

