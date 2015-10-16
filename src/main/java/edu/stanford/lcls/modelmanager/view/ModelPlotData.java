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

	// plot original
	public static ZPlotPanel plotOriginal(
			MachineModelDetail[] designMachineModelDetails,
			MachineModelDetail[] selectMachineModelDetails,
			int plotFunctionID1, int plotFunctionID2,
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
			String deviceName = (String) designMachineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			double z = Double.valueOf((String) designMachineModelDetails[i]
					.getPropertyValue("ZPOS"));// - Double.valueOf((String) designMachineModelDetails[0].getPropertyValue("ZPOS"));
			double value = Double.valueOf((String) designMachineModelDetails[i]
					.getPropertyValue(plotFunctionID1));
			devices1[i] = new Device(deviceName, z, value, new ReferenceWidget(goldColor));
		}
		Arrays.sort(devices1);
		final Device[] designXmagnets = new Device[devices1.length];
		System.arraycopy(devices1, 0, designXmagnets, 0, devices1.length);

		// -----------------------Select X--------------
		for (int i = 0; i < SELECT_DEVICES_NUMBER; i++) {
			String deviceName = (String) selectMachineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			double z = Double.valueOf((String) selectMachineModelDetails[i]
					.getPropertyValue("ZPOS"));
			double value = Double.valueOf((String) selectMachineModelDetails[i]
					.getPropertyValue(plotFunctionID1));
			devices2[i] = new Device(deviceName, z, value, new SelectedWidget());
		}
		Arrays.sort(devices2);
		final Device[] selectXmagnets = new Device[devices2.length];
		System.arraycopy(devices2, 0, selectXmagnets, 0, devices2.length);

		// -----------------------Design Y--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++) {
			String deviceName = (String) designMachineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			double z = Double.valueOf((String) designMachineModelDetails[i]
					.getPropertyValue("ZPOS"));
			double value = Double.valueOf((String) designMachineModelDetails[i]
					.getPropertyValue(plotFunctionID2));
			devices1[i] = new Device(deviceName, z, value, new ReferenceWidget(goldColor));
		}
		Arrays.sort(devices1);
		final Device[] designYmagnets = new Device[devices1.length];
		System.arraycopy(devices1, 0, designYmagnets, 0, devices1.length);

		// -----------------------Select Y--------------
		for (int i = 0; i < SELECT_DEVICES_NUMBER; i++) {
			String deviceName = (String) selectMachineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			double z = Double.valueOf((String) selectMachineModelDetails[i]
					.getPropertyValue("ZPOS"));
			double value = Double.valueOf((String) selectMachineModelDetails[i]
					.getPropertyValue(plotFunctionID2));
			devices2[i] = new Device(deviceName, z, value, new SelectedWidget());
		}
		Arrays.sort(devices2);
		final Device[] selectYmagnets = new Device[devices2.length];
		System.arraycopy(devices2, 0, selectYmagnets, 0, devices2.length);

		// -----------------CartoonDevice--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++) {
			String deviceName = null;
			if(plotSignMethod == 1)
				deviceName = (String) designMachineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			else if(plotSignMethod == 2)
				deviceName = (String) designMachineModelDetails[i]
				     .getPropertyValue("EPICS_NAME");
			else
				deviceName = (String) designMachineModelDetails[i]
				     .getPropertyValue("ELEMENT_NAME");
				
			double length = Double
					.valueOf((String) designMachineModelDetails[i]
							.getPropertyValue("SLEFF"));
			double Startz = Double
					.valueOf((String) designMachineModelDetails[i]
							.getPropertyValue("ZPOS"))
					- length / 2;
			String deviceType = (String) designMachineModelDetails[i]
					.getPropertyValue("DEVICE_TYPE");

			devices1[i] = getCartoonDevice(deviceName, Startz, length, deviceType);
		}
		Arrays.sort(devices1);
		final CartoonDevice[] cartoonDevices = new CartoonDevice[devices1.length];
		System.arraycopy(devices1, 0, cartoonDevices, 0, devices1.length);

		final Beamline[] beamlines = new Beamline[DESIGN_DEVICES_NUMBER];
		double startZ;
		double length;
		for (int i = 0; i < beamlines.length; i++) {
			length = Double.valueOf((String) designMachineModelDetails[i]
					.getPropertyValue("SLEFF"));
			startZ = Double.valueOf((String) designMachineModelDetails[i]
					.getPropertyValue("ZPOS"))
					- length;
			beamlines[i] = new Beamline((String) designMachineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME"), startZ, startZ + length);
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
		xPlot.getRangeAxis().setLabel(
				MachineModelDetail.getPropertyName(plotFunctionID1));
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
		yPlot.getRangeAxis().setLabel(
				MachineModelDetail.getPropertyName(plotFunctionID2));
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
			int plotFunctionID1, int plotFunctionID2,
			int plotSignMethod, boolean plotNodeMethod, JPanel parent) {

		// construct devices
		final int DESIGN_DEVICES_NUMBER = designMachineModelDetails.length;
		final int SELECT_DEVICES_NUMBER = selectMachineModelDetails.length;
		Device[] devices1 = new Device[DESIGN_DEVICES_NUMBER];
		ArrayList<Double> allDeveiceZPos = new ArrayList<Double>(
				DESIGN_DEVICES_NUMBER);
		ArrayList<Integer> indexInDesignDevice = new ArrayList<Integer>();
		ArrayList<Integer> indexInSelectDevice = new ArrayList<Integer>();

		// -----------------------Get match devices in design--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++)
			allDeveiceZPos.add(Double
					.valueOf((String) designMachineModelDetails[i]
							.getPropertyValue("ZPOS")));

		for (int i = 0; i < SELECT_DEVICES_NUMBER; i++) {
			Double z = Double.valueOf((String) selectMachineModelDetails[i]
					.getPropertyValue("ZPOS"));
			int indexInDesign = -1;
			for (int j = 0; j < allDeveiceZPos.size(); j++) {
				if (Math.abs(allDeveiceZPos.get(j) - z) < 0.0001){
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
			String selectDeviceName = (String) selectMachineModelDetails[indexInSelectDevice
					.get(i)].getPropertyValue("ELEMENT_NAME");
			double z = Double
					.valueOf((String) selectMachineModelDetails[indexInSelectDevice
							.get(i)].getPropertyValue("ZPOS"));

			double value = Double
					.valueOf((String) selectMachineModelDetails[indexInSelectDevice
							.get(i)].getPropertyValue(plotFunctionID1))
					- Double
							.valueOf((String) designMachineModelDetails[indexInDesignDevice
									.get(i)].getPropertyValue(plotFunctionID1));
			devices2[i] = new Device(selectDeviceName, z, value, new DifferenceWidget());

		}
		Arrays.sort(devices2);
		final Device[] differentXmagnets = new Device[devices2.length];
		System.arraycopy(devices2, 0, differentXmagnets, 0, devices2.length);

		// -----------------------Different in Y--------------
		for (int i = 0; i < indexInSelectDevice.size(); i++) {
			String selectDeviceName = (String) selectMachineModelDetails[indexInSelectDevice
					.get(i)].getPropertyValue("ELEMENT_NAME");
			double z = Double
					.valueOf((String) selectMachineModelDetails[indexInSelectDevice
							.get(i)].getPropertyValue("ZPOS"));

			double value = Double
					.valueOf((String) selectMachineModelDetails[indexInSelectDevice
							.get(i)].getPropertyValue(plotFunctionID2))
					- Double
							.valueOf((String) designMachineModelDetails[indexInDesignDevice
									.get(i)].getPropertyValue(plotFunctionID2));
			devices2[i] = new Device(selectDeviceName, z, value, new DifferenceWidget());

		}
		Arrays.sort(devices2);
		final Device[] differentYmagnets = new Device[devices2.length];
		System.arraycopy(devices2, 0, differentYmagnets, 0, devices2.length);

		// -----------------CartoonDevice--------------
		for (int i = 0; i < DESIGN_DEVICES_NUMBER; i++) {
			String deviceName = null;
			if(plotSignMethod == 1)
				deviceName = (String) designMachineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			else if(plotSignMethod == 2)
				deviceName = (String) designMachineModelDetails[i]
				     .getPropertyValue("EPICS_NAME");
			else
				deviceName = (String) designMachineModelDetails[i]
				                            					.getPropertyValue("ELEMENT_NAME");

			double length = Double
					.valueOf((String) designMachineModelDetails[i]
							.getPropertyValue("SLEFF"));
			double Startz = Double
					.valueOf((String) designMachineModelDetails[i]
							.getPropertyValue("ZPOS"))
					- length / 2;
			String deviceType = (String) designMachineModelDetails[i]
					.getPropertyValue("DEVICE_TYPE");

			devices1[i] = getCartoonDevice(deviceName, Startz, length, deviceType);
		}
		Arrays.sort(devices1);
		final CartoonDevice[] cartoonDevices = new CartoonDevice[devices1.length];
		System.arraycopy(devices1, 0, cartoonDevices, 0, devices1.length);

		final Beamline[] beamlines = new Beamline[DESIGN_DEVICES_NUMBER];
		double startZ;
		double length;
		for (int i = 0; i < beamlines.length; i++) {
			length = Double.valueOf((String) designMachineModelDetails[i]
					.getPropertyValue("SLEFF"));
			startZ = Double.valueOf((String) designMachineModelDetails[i]
					.getPropertyValue("ZPOS"))
					- length;
			beamlines[i] = new Beamline((String) designMachineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME"), startZ, startZ + length);
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
		xPlot.getRangeAxis().setLabel("d_" +
				MachineModelDetail.getPropertyName(plotFunctionID1));
		xPlot.getRangeAxis().setAutoRange(true);

		yPlot = zPlot.getSubplot(Y_PLOT_INDEX);
		zPlot.setDevices(yPlot, differentYmagnets, 0, RendererType.LINE);
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) yPlot.getRenderer(0);
			renderer.setBaseShapesVisible(plotNodeMethod);
		}
		yPlot.getRangeAxis().setLabel("d_" +
				MachineModelDetail.getPropertyName(plotFunctionID2));
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
			int plotFunctionID1, int plotFunctionID2,
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
			String deviceName = (String) machineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			double z = Double.valueOf((String) machineModelDetails[i]
					.getPropertyValue("ZPOS"));
			double value = Double.valueOf((String) machineModelDetails[i]
					.getPropertyValue(plotFunctionID1));
			devices[i] = new Device(deviceName, z, value, new ReferenceWidget(goldColor));
		}
		Arrays.sort(devices);
		final Device[] xmagnets = new Device[devices.length];
		System.arraycopy(devices, 0, xmagnets, 0, devices.length);

		// -----------------------Y--------------
		for (int i = 0; i < DEVICES_NUMBER; i++) {
			String deviceName = (String) machineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			double z = Double.valueOf((String) machineModelDetails[i]
					.getPropertyValue("ZPOS"));
			double value = Double.valueOf((String) machineModelDetails[i]
					.getPropertyValue(plotFunctionID2));
			devices[i] = new Device(deviceName, z, value, new ReferenceWidget(goldColor));
		}
		Arrays.sort(devices);
		final Device[] ymagnets = new Device[devices.length];
		System.arraycopy(devices, 0, ymagnets, 0, devices.length);

		// -----------------CartoonDevice--------------
		for (int i = 0; i < DEVICES_NUMBER; i++) {
			String deviceName = null;
			if(plotSignMethod == 1)
				deviceName = (String) machineModelDetails[i]
					.getPropertyValue("ELEMENT_NAME");
			else if(plotSignMethod == 2)
				deviceName = (String) machineModelDetails[i]
				     .getPropertyValue("EPICS_NAME");	
			else
				deviceName = (String) machineModelDetails[i]
				                            					.getPropertyValue("ELEMENT_NAME");

			double length = Double.valueOf((String) machineModelDetails[i]
					.getPropertyValue("SLEFF"));
			double Startz = Double.valueOf((String) machineModelDetails[i]
					.getPropertyValue("ZPOS"))

					- length / 2;
			String deviceType = (String) machineModelDetails[i]
					.getPropertyValue("DEVICE_TYPE");

			devices[i] = getCartoonDevice(deviceName, Startz, length, deviceType);
		}
		Arrays.sort(devices);
		final CartoonDevice[] cartoonDevices = new CartoonDevice[devices.length];
		System.arraycopy(devices, 0, cartoonDevices, 0, devices.length);

		final Beamline[] beamlines = new Beamline[DEVICES_NUMBER];
		double startZ;
		double length;
		for (int i = 0; i < beamlines.length; i++) {
			length = Double.valueOf((String) machineModelDetails[i]
						.getPropertyValue("SLEFF"));
			startZ = Double.valueOf((String) machineModelDetails[i]
						.getPropertyValue("ZPOS"))

						- length;				
			beamlines[i] = new Beamline((String) machineModelDetails[i]
						.getPropertyValue("ELEMENT_NAME"), startZ, startZ + length);				
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
		xPlot.getRangeAxis().setLabel(
				MachineModelDetail.getPropertyName(plotFunctionID1));
		xPlot.getRangeAxis().setAutoRange(true);

		yPlot = zPlot.getSubplot(Y_PLOT_INDEX);
		zPlot.setDevices(yPlot, ymagnets, 0, RendererType.LINE);
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) yPlot.getRenderer(0);
			renderer.setBaseShapesVisible(plotNodeMethod);
		}
		yPlot.getRangeAxis().setLabel(
				MachineModelDetail.getPropertyName(plotFunctionID2));
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

