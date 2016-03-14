package edu.stanford.lcls.modelmanager.view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JPanel;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;

import edu.stanford.lcls.modelmanager.dbmodel.DifferenceWidget;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDetail;
import edu.stanford.lcls.modelmanager.dbmodel.ReferenceWidget;
import edu.stanford.lcls.modelmanager.dbmodel.SelectedWidget;
import edu.stanford.slac.util.zplot.ZPlot;
import edu.stanford.slac.util.zplot.ZPlotEvent;
import edu.stanford.slac.util.zplot.ZPlotListener;
import edu.stanford.slac.util.zplot.ZPlotPanel;
import edu.stanford.slac.util.zplot.ZPlot.RendererType;
import edu.stanford.slac.util.zplot.cartoon.model.CartoonDevice;
import edu.stanford.slac.util.zplot.cartoon.model.widget.BPMWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.CavityWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.DQuadWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.DipoleWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.FQuadWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.LossMonitorWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.MarkerWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.ProfileMonitorWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.SolenoidWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.ToroidWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.UndulatorWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.WireScannerWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.XCollimatorWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.XCorWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.YCollimatorWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.YCorWidget;
import edu.stanford.slac.util.zplot.model.Beamline;
import edu.stanford.slac.util.zplot.model.Device;

/**
 * Plots for ModelPlotView
 * 
 * @author unknown
 * @author Blaz Kranjc
 */
public class ModelPlotData {
	private static ZPlotPanel zPlotPanel;
	private static ZPlot zPlot;
	final private static int X_PLOT_INDEX = 0;
	final private static int Y_PLOT_INDEX = 1;
	private static XYPlot xPlot;
	private static XYPlot yPlot;
	private static Range zPlotDomainAxis;

	/**
	 * Return the Z position of element.
	 * @param mmd Details of element.
	 * @return Z position of element.
	 */
	private static double getZPos(MachineModelDetail mmd) {
		return getDouble(mmd, "ZPOS");
	}
	
	/**
	 * Get double property from element details.
	 * @param mmd Details of element.
	 * @param property Name of the property.
	 * @return Value of property for element.
	 */
	private static double getDouble(MachineModelDetail mmd, String property) {
		String valueString = (String) mmd.getPropertyValue(property);
		return valueString == null ? 0 : Double.valueOf(valueString);
	}

	/**
	 * Get the name of the element.
	 * @param mmd Details of element.
	 * @return Name of the element.
	 */
	private static String getName(MachineModelDetail mmd) {
		return getString(mmd, "ELEMENT_NAME");
	}

	/**
	 * Get the string value of property for element.
	 * @param mmd Details of element.
	 * @param property Name of the property.
	 * @return String value of property for element.
	 */
	private static String getString(MachineModelDetail mmd, String property) {
		return (String) mmd.getPropertyValue(property);
	}
	
	/**
	 * Construct array used in plots.
	 * @param mmds Details for all elements in accelerator.
	 * @param property Property to plot.
	 * @return Array of devices which is to be plotted.
	 */
	private static Device[] constructPlotDevices(MachineModelDetail[] mmds, 
			String property) {
		return constructPlotDevices(mmds, property, null);
	}

	/**
	 * Construct array used in plots.
	 * @param mmds Details for all elements in accelerator.
	 * @param property Property to plot.
	 * @param color Color used for the plot.
	 * @return Array of devices which is to be plotted.
	 */
	private static Device[] constructPlotDevices(MachineModelDetail[] mmds, 
			String property, Color color) {

		int deviceNumber = mmds.length;
		Device[] devices = new Device[deviceNumber];
		for (int i = 0; i < deviceNumber; i++) {
			String deviceName = getName(mmds[i]);
			double z = getZPos(mmds[i]);
			double value = getDouble(mmds[i], property);
			if (color != null) {
				devices[i] = new Device(deviceName, z, value, new ReferenceWidget(color));
			}
			else {
				devices[i] = new Device(deviceName, z, value, new SelectedWidget());
			}
		}
		Arrays.sort(devices);

		return devices;
	}
	
	/**
	 * Constructs cartoon device from machine model details.
	 * @param mmds Array of machine model details.
	 * @param plotSignMethod Flag to note what names to use for devices.
	 * @return
	 */
	private static CartoonDevice[] constructCartoonDevices(MachineModelDetail[] mmds, int plotSignMethod) {
		int deviceNumber = mmds.length;
		CartoonDevice[] devices = new CartoonDevice[deviceNumber];
		for (int i = 0; i < deviceNumber; i++) {
			String deviceName = null;
			if(plotSignMethod == 2)
				deviceName = getString(mmds[i], "EPICS_NAME");
			else
				deviceName = getName(mmds[i]);
				
			double length = getDouble(mmds[i], "SLEFF");
			double Startz = getDouble(mmds[i], "ZPOS") - length / 2;
			String deviceType = getString(mmds[i], "DEVICE_TYPE");

			devices[i] = getCartoonDevice(deviceName, Startz, length, deviceType);
		}
		
		return devices;
	}

	/**
	 * Construct beamlines used in the cartoon plot.
	 * @param mmds Array of machine model details.
	 * @return beamlines
	 */
	private static Beamline[] constructBeamlines(MachineModelDetail[] mmds) {
		final Beamline[] beamlines = new Beamline[mmds.length];
		double startZ;
		double length;
		for (int i = 0; i < beamlines.length; i++) {
			length = getDouble(mmds[i], "SLEFF");
			startZ = getZPos(mmds[i]) - length;
			beamlines[i] = new Beamline(getName(mmds[i]), startZ, startZ + length);
		}
		return beamlines;
	}
	
	/**
	 * Generates plots.
	 * @param devicesX Data for first plot.
	 * @param devicesY Data for second plot.
	 * @param cartoonDevices Data for cartoon device plot.
	 * @param beamlines Beamlines.
	 * @param plotXLabel Label for first plot.
	 * @param plotYLabel Label for second plot.
	 * @param plotSignMethod
	 * @param plotNodeMethod
	 * @param parent
	 * @return Panel containing the plots.
	 */
	private static ZPlotPanel generatePlot(Device[] devicesX, Device[] devicesY, 
			CartoonDevice[] cartoonDevices, Beamline[] beamlines, String plotXLabel,
			String plotYLabel, int plotSignMethod, boolean plotNodeMethod, JPanel parent) {
		
		return generatePlot(devicesX, devicesY, null, null, cartoonDevices, 
			beamlines, plotXLabel, plotYLabel, plotSignMethod, plotNodeMethod, parent);

	}

	/**
	 * Generates plots.
	 * @param designX Reference model data for first plot.
	 * @param designY Reference model data for second plot.
	 * @param selectX Selected model data for first plot.
	 * @param selectY Selected model data for second plot.
	 * @param cartoonDevices Data for cartoon device plot.
	 * @param beamlines Beamlines.
	 * @param plotXLabel Label for first plot.
	 * @param plotYLabel Label for second plot.
	 * @param plotSignMethod
	 * @param plotNodeMethod
	 * @param parent
	 * @return Panel containing the plots.
	 */
	private static ZPlotPanel generatePlot(Device[] designX, Device[] designY, 
			Device[] selectX, Device[] selectY, CartoonDevice[] cartoonDevices, 
			Beamline[] beamlines, String plotXLabel, String plotYLabel, 
			int plotSignMethod, boolean plotNodeMethod, JPanel parent) {

		final ZPlotListener zPlotListener = new ZPlotListener() {
			public void tooltipShown(ZPlotEvent event) {
			}

			public void zoomCompleted(ZPlotEvent event) {
			}
		};

		zPlot = new ZPlot(2);
		
		if(plotSignMethod != 0)
			zPlot.labelDevices(cartoonDevices, beamlines);
		
		zPlot.setGap(30);

		xPlot = zPlot.getSubplot(X_PLOT_INDEX);
		zPlot.setDevices(xPlot, designX, 1, RendererType.LINE);
		if (selectX != null) {
			zPlot.setDevices(xPlot, selectX, 0, RendererType.LINE);
		}
		{
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xPlot.getRenderer(1);
            renderer.setBaseShapesVisible(plotNodeMethod);
            if (selectX != null) {
                renderer = (XYLineAndShapeRenderer) xPlot.getRenderer(0);
                renderer.setBaseShapesVisible(plotNodeMethod);
            }
		}
		xPlot.getRangeAxis().setLabel(plotXLabel);
		xPlot.getRangeAxis().setAutoRange(true);

		yPlot = zPlot.getSubplot(Y_PLOT_INDEX);
		zPlot.setDevices(yPlot, designY, 1, RendererType.LINE);
		if (selectY != null) {
			zPlot.setDevices(yPlot, selectY, 0, RendererType.LINE);
		}
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) yPlot.getRenderer(1);
			renderer.setBaseShapesVisible(plotNodeMethod);
			if (selectY != null) {
				renderer = (XYLineAndShapeRenderer) yPlot.getRenderer(0);
				renderer.setBaseShapesVisible(plotNodeMethod);
			}
		}
		yPlot.getRangeAxis().setLabel(plotYLabel);
		yPlot.getRangeAxis().setAutoRange(true);

		zPlot.setCartoonDevices(cartoonDevices);

		// label subplots
		zPlot.setSubplotLabel(X_PLOT_INDEX, "X Plot");
		zPlot.setSubplotLabel(Y_PLOT_INDEX, "Y Plot");

		// create plot panel
		zPlotPanel = new ZPlotPanel(parent, zPlot);
		zPlotPanel.addZPlotListener(zPlotListener);
		if (zPlotDomainAxis != null)
			setRange(zPlotDomainAxis);
		zPlotPanel.repaint();
		return zPlotPanel;
	}

	/**
	 * Plot model details properties.
	 * @param machineModelDetails Element details for model.
	 * @param plotFunctionID1 Property value to plot on first graph.
	 * @param plotFunctionID2 Property value to plot on second graph.
	 * @param plotSignMethod Switch for names to use in plots.
	 * @param plotNodeMethod 
	 * @param parent Panel that contains the plot.
	 * @param isGold Flag to note if the model is marked as gold.
	 * @return The panel with graphs.
	 */
	public static ZPlotPanel plotData(MachineModelDetail[] machineModelDetails,
			String plotFunctionID1, String plotFunctionID2,
			int plotSignMethod, boolean plotNodeMethod, JPanel parent, boolean isGold) {

		return plotData(machineModelDetails, null,  plotFunctionID1, plotFunctionID2,
			plotSignMethod, plotNodeMethod, parent,  isGold);
	}
	
	/**
	 * Plot the reference and selected models properties on the same graph.
	 * @param designMachineModelDetails Element details for reference model.
	 * @param selectMachineModelDetails Element details for selected model.
	 * @param plotFunctionID1 Property value to plot on first graph.
	 * @param plotFunctionID2 Property value to plot on second graph.
	 * @param plotSignMethod Switch for names to use in plots.
	 * @param plotNodeMethod 
	 * @param parent Panel that contains the plot.
	 * @param isGold Flag to note if reference model marked as gold.
	 * @return The panel with graphs.
	 */
	public static ZPlotPanel plotData(
			MachineModelDetail[] designMachineModelDetails,
			MachineModelDetail[] selectMachineModelDetails,
			String plotFunctionID1, String plotFunctionID2,
			int plotSignMethod, boolean plotNodeMethod, JPanel parent,  boolean isGold) {

		Color plotColor;
		if (isGold)
			plotColor = Color.ORANGE;
		else
			plotColor = Color.CYAN;
		
		final Device[] designXmagnets = constructPlotDevices(designMachineModelDetails, plotFunctionID1, plotColor);
		final Device[] designYmagnets = constructPlotDevices(designMachineModelDetails, plotFunctionID2, plotColor);
		final CartoonDevice[] cartoonDevices = constructCartoonDevices(designMachineModelDetails, plotSignMethod);
		final Beamline[] beamlines = constructBeamlines(designMachineModelDetails);
		if (selectMachineModelDetails == null) {
			return generatePlot(designXmagnets, designYmagnets, cartoonDevices, beamlines, plotFunctionID1, plotFunctionID2,
					plotSignMethod, plotNodeMethod, parent);
		}

		final Device[] selectXmagnets = constructPlotDevices(selectMachineModelDetails, plotFunctionID1);
		final Device[] selectYmagnets = constructPlotDevices(selectMachineModelDetails, plotFunctionID2);
		return generatePlot(designXmagnets, designYmagnets, selectXmagnets, selectYmagnets,
				cartoonDevices, beamlines, plotFunctionID1, plotFunctionID2, plotSignMethod, plotNodeMethod, parent);
	}

	/**
	 * Plot the reference and selected models properties differences.
	 * @param designMachineModelDetails Element details for reference model.
	 * @param selectMachineModelDetails Element details for selected model.
	 * @param plotFunctionID1 Property value to plot on first graph.
	 * @param plotFunctionID2 Property value to plot on second graph.
	 * @param plotSignMethod Switch for names to use in plots.
	 * @param plotNodeMethod 
	 * @param parent Panel that contains the plot.
	 * @return The panel with graphs.
	 */
	public static ZPlotPanel plotDifferent(
			MachineModelDetail[] designMachineModelDetails,
			MachineModelDetail[] selectMachineModelDetails,
			String plotFunctionID1, String plotFunctionID2,
			int plotSignMethod, boolean plotNodeMethod, JPanel parent) {

		// construct devices
		final int DESIGN_DEVICES_NUMBER = designMachineModelDetails.length;
		final int SELECT_DEVICES_NUMBER = selectMachineModelDetails.length;
		ArrayList<Double> allDeviceZPos = new ArrayList<Double>(
				DESIGN_DEVICES_NUMBER);
		ArrayList<Integer> indexInDesignDevice = new ArrayList<Integer>();
		ArrayList<Integer> indexInSelectDevice = new ArrayList<Integer>();

		// -----------------------Get match devices in design--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++)
			allDeviceZPos.add(getZPos(designMachineModelDetails[i]));

		for (int i = 0; i < SELECT_DEVICES_NUMBER; i++) {
			Double z = getZPos(selectMachineModelDetails[i]);
			int indexInDesign = -1;
			for (int j = 0; j < allDeviceZPos.size(); j++) {
				if (Math.abs(allDeviceZPos.get(j) - z) < 0.0001){
					indexInDesign = j;
				}
			}
			
			if (indexInDesign > 0) {
				indexInDesignDevice.add(indexInDesign);
				indexInSelectDevice.add(i);
			}
		}
		Device[] devices2 = new Device[indexInSelectDevice.size()];

		// -----------------------Different in X--------------

		for (int i = 0; i < indexInSelectDevice.size(); i++) {
			String selectDeviceName = getName(selectMachineModelDetails[indexInSelectDevice.get(i)]);
			double z = getZPos(selectMachineModelDetails[indexInSelectDevice.get(i)]);

			double value = getDouble(selectMachineModelDetails[indexInSelectDevice.get(i)], plotFunctionID1) -
					getDouble(designMachineModelDetails[indexInDesignDevice.get(i)], plotFunctionID1);
			devices2[i] = new Device(selectDeviceName, z, value, new DifferenceWidget());

		}
		Arrays.sort(devices2);
		final Device[] differentXmagnets = new Device[devices2.length];
		System.arraycopy(devices2, 0, differentXmagnets, 0, devices2.length);

		// -----------------------Different in Y--------------
		for (int i = 0; i < indexInSelectDevice.size(); i++) {
			String selectDeviceName = getName(selectMachineModelDetails[indexInSelectDevice.get(i)]);
			double z = getZPos(selectMachineModelDetails[indexInSelectDevice.get(i)]);
			double value = getDouble(selectMachineModelDetails[indexInSelectDevice.get(i)], plotFunctionID2) -
					getDouble(designMachineModelDetails[indexInDesignDevice.get(i)], plotFunctionID2);
			devices2[i] = new Device(selectDeviceName, z, value, new DifferenceWidget());

		}
		Arrays.sort(devices2);
		final Device[] differentYmagnets = new Device[devices2.length];
		System.arraycopy(devices2, 0, differentYmagnets, 0, devices2.length);

		final CartoonDevice[] cartoonDevices = constructCartoonDevices(designMachineModelDetails, plotSignMethod);
		final Beamline[] beamlines = constructBeamlines(designMachineModelDetails);

		return generatePlot(differentXmagnets, differentYmagnets, cartoonDevices, beamlines, 
				"d_"+plotFunctionID1,  "d_"+plotFunctionID2, plotSignMethod, plotNodeMethod, parent);
	}

	
	/**
	 * Factory method for CartoonDevice object.
	 * @param deviceName Name of the device.
	 * @param startZ Center position of the device.
	 * @param length Length of the device.
	 * @param deviceType Type of the device.
	 * @return CartoonDevice widget corresponding to deviceType.
	 */
	private static CartoonDevice getCartoonDevice(String deviceName, double startZ, double length, String deviceType) {
		if (deviceType.equals("BPM")) {
			return new CartoonDevice(deviceName, startZ,
					new BPMWidget());
		} else if (deviceType.equals("LRG") || deviceType.equals("Bnch") || deviceType.equals("RG")) {
			return new CartoonDevice(deviceName, startZ,
					length, new CavityWidget());
		} else if (deviceType.equals("YBEND") || deviceType.equals("XBEND") || deviceType.equals("BEND")) {
			return new CartoonDevice(deviceName, startZ, length,
					new DipoleWidget());
		} else if (deviceType.equals("DQUAD")) {
			return new CartoonDevice(deviceName, startZ, length,
					new DQuadWidget());
		} else if (deviceType.equals("FQUAD")) {
			return new CartoonDevice(deviceName, startZ, length,
					new FQuadWidget());
		} else if (deviceType.equals("INST")) {
			return new CartoonDevice(deviceName, startZ,
					new LossMonitorWidget());
		} else if (deviceType.equals("PROF")) {
			return new CartoonDevice(deviceName, startZ,
					new ProfileMonitorWidget());
		} else if (deviceType.equals("SOLE")) {
			return new CartoonDevice(deviceName, startZ, length,
					new SolenoidWidget());
		} else if (deviceType.equals("TORO")) {
			return new CartoonDevice(deviceName, startZ, length,
					new ToroidWidget());
		} else if (deviceType.equals("USEG")) {
			return new CartoonDevice(deviceName, startZ, length,
					new UndulatorWidget());
		} else if (deviceType.equals("WIRE")) {
			return new CartoonDevice(deviceName, startZ,
					new WireScannerWidget());
		} else if (deviceType.equals("COLL")) {
			return new CartoonDevice(deviceName, startZ,
					new XCollimatorWidget());
		} else if (deviceType.equals("COLL")) {
			return new CartoonDevice(deviceName, startZ,
					new YCollimatorWidget());
		} else if (deviceType.equals("XCOR") || deviceType.equals("DCH")) {
			return new CartoonDevice(deviceName, startZ,
					new XCorWidget());
		} else if (deviceType.equals("YCOR") || deviceType.equals("DCV")) {
			return new CartoonDevice(deviceName, startZ,
					new YCorWidget());
		} else if (deviceType.equals("marker") || deviceType.equals("MARK")) {
			return new CartoonDevice(deviceName, startZ,
					new MarkerWidget());
		} else {
			return new CartoonDevice(deviceName, startZ,
					new MarkerWidget());
		}
	}

	/**
	 * Set range of the plot.
	 * @param zPlotDomainAxis
	 */
	public static void setRange(Range zPlotDomainAxis){
		zPlot.getDomainAxis().setRange(zPlotDomainAxis);
	}
	
	/**
	 * Clear the ranges on the plot.
	 */
	public static void clearRange(){
		zPlot = null;
	}
	
	/**
	 * Get Range of the plot.
	 */
	public static void getRange(){
		zPlotDomainAxis = getZPlotDomainAxis();
	}
	
	/**
	 * Get Z plot domain range.
	 * @return plot range
	 */
	public static Range getZPlotDomainAxis(){
		if (zPlot != null)
			return new Range(zPlot.getDomainAxis().getRange().getLowerBound(),
					zPlot.getDomainAxis().getRange().getUpperBound());
		else
			return null;
	}

}

