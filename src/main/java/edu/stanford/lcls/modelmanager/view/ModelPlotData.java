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

public class ModelPlotData {
	private static ZPlotPanel zPlotPanel;
	private static ZPlot zPlot;
	final private static int X_PLOT_INDEX = 0;
	final private static int Y_PLOT_INDEX = 1;
	private static XYPlot xPlot;
	private static XYPlot yPlot;
	private static Range zPlotDomainAxis;

	private static double getZPos(MachineModelDetail mmd) {
		return getDouble(mmd, "ZPOS");
	}
	
	private static double getDouble(MachineModelDetail mmd, String column) {
		String valueString = (String) mmd.getPropertyValue(column);
		return valueString == null ? 0 : Double.valueOf(valueString);
	}

	private static String getName(MachineModelDetail mmd) {
		return getString(mmd, "ELEMENT_NAME");
	}

	private static String getString(MachineModelDetail mmd, String column) {
		return (String) mmd.getPropertyValue(column);
	}

	// plot original
	public static ZPlotPanel plotOriginal(
		MachineModelDetail[] designMachineModelDetails,
		MachineModelDetail[] selectMachineModelDetails,
		String plotFunctionID1, String plotFunctionID2,
		int plotSignMethod, boolean plotNodeMethod, JPanel parent,  boolean isGold) {

		Color goldColor;
		if (isGold)
			goldColor = Color.ORANGE;
		else
			goldColor = Color.CYAN;
		
		// construct devices
		final int DESIGN_DEVICES_NUMBER = designMachineModelDetails.length;
		final int SELECT_DEVICES_NUMBER = selectMachineModelDetails.length;
		Device[] devices1 = new Device[DESIGN_DEVICES_NUMBER];
		Device[] devices2 = new Device[SELECT_DEVICES_NUMBER];

		// -----------------------Design X--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++) {
			String deviceName = getName(designMachineModelDetails[i]);
			double z = getZPos(designMachineModelDetails[i]);
			double value = getDouble(designMachineModelDetails[i], plotFunctionID1);
			devices1[i] = new Device(deviceName, z, value, new ReferenceWidget(goldColor));
		}
		Arrays.sort(devices1);
		final Device[] designXmagnets = new Device[devices1.length];
		System.arraycopy(devices1, 0, designXmagnets, 0, devices1.length);

		// -----------------------Select X--------------
		for (int i = 0; i < SELECT_DEVICES_NUMBER; i++) {
			String deviceName = getName(selectMachineModelDetails[i]);
			double z = getZPos(selectMachineModelDetails[i]);
			double value = getDouble(selectMachineModelDetails[i], plotFunctionID1);
			devices2[i] = new Device(deviceName, z, value, new SelectedWidget());
		}
		Arrays.sort(devices2);
		final Device[] selectXmagnets = new Device[devices2.length];
		System.arraycopy(devices2, 0, selectXmagnets, 0, devices2.length);

		// -----------------------Design Y--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++) {
			String deviceName = getName(designMachineModelDetails[i]);
			double z = getZPos(designMachineModelDetails[i]);
			double value = getDouble(designMachineModelDetails[i], plotFunctionID2);
			devices1[i] = new Device(deviceName, z, value, new ReferenceWidget(goldColor));
		}
		Arrays.sort(devices1);
		final Device[] designYmagnets = new Device[devices1.length];
		System.arraycopy(devices1, 0, designYmagnets, 0, devices1.length);

		// -----------------------Select Y--------------
		for (int i = 0; i < SELECT_DEVICES_NUMBER; i++) {
			String deviceName = getName(selectMachineModelDetails[i]);
			double z = getZPos(selectMachineModelDetails[i]);
			double value = getDouble(selectMachineModelDetails[i], plotFunctionID2);
			devices2[i] = new Device(deviceName, z, value, new SelectedWidget());
		}
		Arrays.sort(devices2);
		final Device[] selectYmagnets = new Device[devices2.length];
		System.arraycopy(devices2, 0, selectYmagnets, 0, devices2.length);

		// -----------------CartoonDevice--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++) {
			String deviceName = null;
			if(plotSignMethod == 2)
				deviceName = getString(designMachineModelDetails[i], "EPICS_NAME");
			else
				deviceName = getName(designMachineModelDetails[i]);
				
			double length = getDouble(designMachineModelDetails[i], "SLEFF");
			double Startz = getDouble(designMachineModelDetails[i], "ZPOS") - length / 2;
			String deviceType = getString(designMachineModelDetails[i], "DEVICE_TYPE");

			devices1[i] = getCartoonDevice(deviceName, Startz, length, deviceType);
		}
		Arrays.sort(devices1);
		final CartoonDevice[] cartoonDevices = new CartoonDevice[devices1.length];
		System.arraycopy(devices1, 0, cartoonDevices, 0, devices1.length);

		final Beamline[] beamlines = new Beamline[DESIGN_DEVICES_NUMBER];
		double startZ;
		double length;
		for (int i = 0; i < beamlines.length; i++) {
			length = getDouble(designMachineModelDetails[i], "SLEFF");
			startZ = getZPos(designMachineModelDetails[i]) - length;
			beamlines[i] = new Beamline(getName(designMachineModelDetails[i]),
					startZ, startZ + length);
		}

		// create z plot listener
		final ZPlotListener zPlotListener = new ZPlotListener() {
			public void tooltipShown(ZPlotEvent event) {
			}

			public void zoomCompleted(ZPlotEvent event) {
			}
		};

		// create z plot
		zPlot = new ZPlot(2);
		
		if(plotSignMethod != 0)
			zPlot.labelDevices(devices1, beamlines);
		
		zPlot.setGap(30);

		xPlot = zPlot.getSubplot(X_PLOT_INDEX);
		zPlot.setDevices(xPlot, designXmagnets, 1, RendererType.LINE);
		zPlot.setDevices(xPlot, selectXmagnets, 0, RendererType.LINE);
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xPlot.getRenderer(1);
			renderer.setBaseShapesVisible(plotNodeMethod);
			renderer = (XYLineAndShapeRenderer) xPlot.getRenderer(0);
			renderer.setBaseShapesVisible(plotNodeMethod);
		}
		xPlot.getRangeAxis().setLabel(plotFunctionID1);
		xPlot.getRangeAxis().setAutoRange(true);

		yPlot = zPlot.getSubplot(Y_PLOT_INDEX);
		zPlot.setDevices(yPlot, designYmagnets, 1, RendererType.LINE);
		zPlot.setDevices(yPlot, selectYmagnets, 0, RendererType.LINE);
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) yPlot.getRenderer(1);
			renderer.setBaseShapesVisible(plotNodeMethod);
			renderer = (XYLineAndShapeRenderer) yPlot.getRenderer(0);
			renderer.setBaseShapesVisible(plotNodeMethod);
		}
		yPlot.getRangeAxis().setLabel(plotFunctionID2);
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

	// plot different
	public static ZPlotPanel plotDifferent(
			MachineModelDetail[] designMachineModelDetails,
			MachineModelDetail[] selectMachineModelDetails,
			String plotFunctionID1, String plotFunctionID2,
			int plotSignMethod, boolean plotNodeMethod, JPanel parent) {

		// construct devices
		final int DESIGN_DEVICES_NUMBER = designMachineModelDetails.length;
		final int SELECT_DEVICES_NUMBER = selectMachineModelDetails.length;
		Device[] devices1 = new Device[DESIGN_DEVICES_NUMBER];
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

		// -----------------CartoonDevice--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++) {
			String deviceName = null;
			if(plotSignMethod == 2)
				deviceName = getString(designMachineModelDetails[i], "EPICS_NAME");
			else
				deviceName = getName(designMachineModelDetails[i]);

			double length = getDouble(designMachineModelDetails[i], "SLEFF");
			double Startz = getZPos(designMachineModelDetails[i]) - length / 2;
			String deviceType = getString(designMachineModelDetails[i], "DEVICE_TYPE");

			devices1[i] = getCartoonDevice(deviceName, Startz, length, deviceType);
		}
		Arrays.sort(devices1);
		final CartoonDevice[] cartoonDevices = new CartoonDevice[devices1.length];
		System.arraycopy(devices1, 0, cartoonDevices, 0, devices1.length);

		final Beamline[] beamlines = new Beamline[DESIGN_DEVICES_NUMBER];
		double startZ;
		double length;
		for (int i = 0; i < beamlines.length; i++) {
			length = getDouble(designMachineModelDetails[i], "SLEFF");
			startZ = getZPos(designMachineModelDetails[i]) - length;
			beamlines[i] = new Beamline(getName(designMachineModelDetails[i]),
					startZ, startZ + length);
		}

		// create z plot listener
		final ZPlotListener zPlotListener = new ZPlotListener() {
			public void tooltipShown(ZPlotEvent event) {
			}

			public void zoomCompleted(ZPlotEvent event) {
			}
		};

		// create z plot
		zPlot = new ZPlot(2);
		
		if(plotSignMethod != 0)
			zPlot.labelDevices(devices1, beamlines);
		zPlot.setGap(30);

		xPlot = zPlot.getSubplot(X_PLOT_INDEX);
		zPlot.setDevices(xPlot, differentXmagnets, 0, RendererType.LINE);
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xPlot.getRenderer(0);
			renderer.setBaseShapesVisible(plotNodeMethod);
		}
		xPlot.getRangeAxis().setLabel("d_" + plotFunctionID1);
		xPlot.getRangeAxis().setAutoRange(true);

		yPlot = zPlot.getSubplot(Y_PLOT_INDEX);
		zPlot.setDevices(yPlot, differentYmagnets, 0, RendererType.LINE);
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) yPlot.getRenderer(0);
			renderer.setBaseShapesVisible(plotNodeMethod);
		}
		yPlot.getRangeAxis().setLabel("d_" + plotFunctionID2);
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

	// Only plot one machine mode

	public static ZPlotPanel plotData(MachineModelDetail[] machineModelDetails,
			String plotFunctionID1, String plotFunctionID2,
			int plotSignMethod, boolean plotNodeMethod, JPanel parent, boolean isGold) {
		// construct devices
		final int DEVICES_NUMBER = machineModelDetails.length;
		Color goldColor;
		if (isGold)
			goldColor = Color.ORANGE;
		else
			goldColor = Color.CYAN;

		Device[] devices = new Device[DEVICES_NUMBER];

		// -----------------------X--------------
		for (int i = 0; i < DEVICES_NUMBER; i++) {
			String deviceName = getName(machineModelDetails[i]);
			double z = getZPos(machineModelDetails[i]);
			double value = getDouble(machineModelDetails[i], plotFunctionID1);
			devices[i] = new Device(deviceName, z, value, new ReferenceWidget(goldColor));
		}
		Arrays.sort(devices);
		final Device[] xmagnets = new Device[devices.length];
		System.arraycopy(devices, 0, xmagnets, 0, devices.length);

		// -----------------------Y--------------
		for (int i = 0; i < DEVICES_NUMBER; i++) {
			String deviceName = getName(machineModelDetails[i]);
			double z = getZPos(machineModelDetails[i]);
			double value = getDouble(machineModelDetails[i], plotFunctionID2);
			devices[i] = new Device(deviceName, z, value, new ReferenceWidget(goldColor));
		}
		Arrays.sort(devices);
		final Device[] ymagnets = new Device[devices.length];
		System.arraycopy(devices, 0, ymagnets, 0, devices.length);

		// -----------------CartoonDevice--------------
		for (int i = 0; i < DEVICES_NUMBER; i++) {
			String deviceName = null;
			if(plotSignMethod == 2)
				deviceName = getString(machineModelDetails[i], "EPICS_NAME");	
			else
				deviceName = getName(machineModelDetails[i]);

			double length = getDouble(machineModelDetails[i], "SLEFF");
			double Startz = getZPos(machineModelDetails[i]) - length / 2;
			String deviceType = getString(machineModelDetails[i], "DEVICE_TYPE");

			devices[i] = getCartoonDevice(deviceName, Startz, length, deviceType);
		}
		Arrays.sort(devices);
		final CartoonDevice[] cartoonDevices = new CartoonDevice[devices.length];
		System.arraycopy(devices, 0, cartoonDevices, 0, devices.length);

		final Beamline[] beamlines = new Beamline[DEVICES_NUMBER];
		double startZ;
		double length;
		for (int i = 0; i < beamlines.length; i++) {
			length = getDouble(machineModelDetails[i], "SLEFF");
			startZ = getZPos(machineModelDetails[i]) - length;
			beamlines[i] = new Beamline(getName(machineModelDetails[i]),
					startZ, startZ + length);				
		}

		// create z plot
		zPlot = new ZPlot(2);

		if(plotSignMethod != 0)
			zPlot.labelDevices(devices, beamlines);
		zPlot.setGap(30);

		xPlot = zPlot.getSubplot(X_PLOT_INDEX);
		zPlot.setDevices(xPlot, xmagnets, 0, RendererType.LINE);		
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xPlot.getRenderer(0);
			renderer.setBaseShapesVisible(plotNodeMethod);
		}	
		xPlot.getRangeAxis().setLabel(plotFunctionID1);
		xPlot.getRangeAxis().setAutoRange(true);

		yPlot = zPlot.getSubplot(Y_PLOT_INDEX);
		zPlot.setDevices(yPlot, ymagnets, 0, RendererType.LINE);
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) yPlot.getRenderer(0);
			renderer.setBaseShapesVisible(plotNodeMethod);
		}
		yPlot.getRangeAxis().setLabel(plotFunctionID2);
		yPlot.getRangeAxis().setAutoRange(true);

		zPlot.setCartoonDevices(cartoonDevices);

		// create plot panel
		zPlotPanel = new ZPlotPanel(parent, zPlot);
		if (zPlotDomainAxis != null)
			setRange(zPlotDomainAxis);
		return zPlotPanel;
	}
	
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

	public static void setRange(Range zPlotDomainAxis){
		zPlot.getDomainAxis().setRange(zPlotDomainAxis);
	}
	
	public static void clearRange(){
		zPlot = null;
	}
	
	public static void getRange(){
		zPlotDomainAxis = getZPlotDomainAxis();
	}
	
	public static Range getZPlotDomainAxis(){
		if (zPlot != null)
			return new Range(zPlot.getDomainAxis().getRange().getLowerBound(),
					zPlot.getDomainAxis().getRange().getUpperBound());
		else
			return null;
	}

}

