package edu.stanford.slac.util.zplot;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.editor.ChartEditor;
import org.jfree.chart.editor.ChartEditorManager;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.ResourceBundleWrapper;

import edu.stanford.slac.util.zplot.cartoon.BeamlineCartoon;
import edu.stanford.slac.util.zplot.cartoon.model.widget.BPMWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.CartoonWidget;
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
import edu.stanford.slac.util.zplot.model.Device;
import edu.stanford.slac.util.zplot.ui.DevicePlotsPanel;
import edu.stanford.slac.util.zplot.ui.DevicesPanel;
import edu.stanford.slac.util.zplot.ui.PropertiesDialog;

class ZPlotController {
	private final ZPlotPanel zPlotPanel;

	protected DevicesPanel devicesPanel;

	// not to confuse with DevicePlotsPanelParent
	protected DevicePlotsPanel devicePlotsPanel;

	private JCheckBox getCartoonWidgetCheckBox(Class<? extends CartoonWidget> c) {
		if (c == BPMWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.bpmCheckBox;
		}
		if (c == CavityWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.cavityCheckBox;
		}
		if (c == DipoleWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.dipoleCheckBox;
		}
		if (c == DQuadWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.dQuadCheckBox;
		}
		if (c == FQuadWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.fQuadCheckBox;
		}
		if (c == LossMonitorWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.lossMonitorCheckBox;
		}
		if (c == MarkerWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.markerCheckBox;
		}
		if (c == ProfileMonitorWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.profileMonitorCheckBox;
		}
		if (c == SolenoidWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.solenoidCheckBox;
		}
		if (c == ToroidWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.toroidCheckBox;
		}
		if (c == UndulatorWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.undulatorCheckBox;
		}
		if (c == WireScannerWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.wireScannerCheckBox;
		}
		if (c == XCollimatorWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.xCollimatorCheckBox;
		}
		if (c == XCorWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.xCorCheckBox;
		}
		if (c == YCollimatorWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.yCollimatorCheckBox;
		}
		if (c == YCorWidget.class) {
			return this.devicesPanel.cartoonFilterPanel.yCorCheckBox;
		}
		throw new RuntimeException("Unsupported cartoon widget " + c);
	}

	private JPanel createPlotPanel(JTabbedPane chartTabbedPane,
			JTabbedPane plotTabbedPane) {
		JPanel titlePanel = (JPanel) chartTabbedPane
				.getComponentAt(chartTabbedPane.indexOfTab("Title"));

		{
			JPanel general = (JPanel) titlePanel.getComponent(0);
			JPanel interior = (JPanel) general.getComponent(0);

			for (int i = 2; i >= 0; i--) {
				interior.remove(i);
			}

			TitledBorder titledBorder = (TitledBorder) general.getBorder();
			titledBorder.setTitle("Title:");
		}
		// ///
		JPanel subplotPanel = (JPanel) plotTabbedPane
				.getComponentAt(plotTabbedPane.indexOfTab("Appearance"));
		{
			JPanel general = (JPanel) subplotPanel.getComponent(0);
			JPanel interior = (JPanel) general.getComponent(0);

			for (int i = 11; i >= 0; i--) {
				if (i >= 6 && i <= 8) {
					continue;
				}
				interior.remove(i);
			}

			TitledBorder titledBorder = (TitledBorder) general.getBorder();
			titledBorder.setBorder(BorderFactory.createEtchedBorder());
			titledBorder.setTitle("Subplot:");
		}

		JPanel generalPanel = (JPanel) chartTabbedPane
				.getComponentAt(chartTabbedPane.indexOfTab("Other"));
		{
			JPanel general = (JPanel) generalPanel.getComponent(0);
			JPanel interior = (JPanel) general.getComponent(0);

			for (int i = 17; i >= 6; i--) {
				interior.remove(i);
			}

			TitledBorder titledBorder = (TitledBorder) general.getBorder();
			titledBorder.setTitle("General:");
		}

		JPanel p = new JPanel(new GridBagLayout());
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 0;
			p.add(generalPanel, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 1;
			c.weighty = 0;
			p.add(subplotPanel, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 2;
			c.weightx = 1;
			c.weighty = 0;
			p.add(titlePanel, c);
		}

		JPanel plotPanel = new JPanel(new BorderLayout());
		plotPanel.add(p, BorderLayout.NORTH);

		return plotPanel;
	}

	protected ZPlotPanel getZPlotPanel() {
		return this.zPlotPanel;
	}

	public ZPlotController(ZPlotPanel zPlotPanel) {
		this.zPlotPanel = zPlotPanel;
	}

	protected void processDeviceSelection() {

		String selectedFirstName = (String) this.devicesPanel.deviceRangePanel.firstComboBox
				.getSelectedItem();
		if (selectedFirstName == null || selectedFirstName.length() == 0) {
			return;
		}
		String selectedLastName = (String) this.devicesPanel.deviceRangePanel.lastComboBox
				.getSelectedItem();
		if (selectedLastName == null || selectedLastName.length() == 0) {
			return;
		}

		ZPlot zPlot = (ZPlot) this.zPlotPanel.getChart().getPlot();
		Device[] labeledDevices = zPlot.getLabeledDevices();
		double startPos = 0, endPos = 0;
		for (Device d : labeledDevices) {
			if (d.getName().equals(selectedFirstName)) {
				startPos = d.getZ();
			}
			if (d.getName().equals(selectedLastName)) {
				endPos = d.getZ();
			}
		}
		if (startPos == endPos)
			return;
		if (endPos > startPos) {
			zPlot.getDomainAxis().setRange(startPos, endPos);
		} else {
			// switch
			zPlot.getDomainAxis().setRange(endPos, startPos);
		}

	}

	protected void createDevicesPanel() {
		this.devicesPanel = new DevicesPanel();

		ZPlot zPlot = (ZPlot) this.zPlotPanel.getChart().getPlot();

		boolean devicesLabeled = zPlot.getLabeledDevices() != null;
		{
			this.devicesPanel.deviceRangePanel.firstLabel
					.setVisible(devicesLabeled);
			this.devicesPanel.deviceRangePanel.firstComboBox
					.setVisible(devicesLabeled);
			this.devicesPanel.deviceRangePanel.lastLabel
					.setVisible(devicesLabeled);
			this.devicesPanel.deviceRangePanel.lastComboBox
					.setVisible(devicesLabeled);
		}

		if (devicesLabeled) {
			this.devicesPanel.deviceRangePanel.firstComboBox
					.addItem(new String());
			for (Device d : zPlot.getLabeledDevices()) {
				this.devicesPanel.deviceRangePanel.firstComboBox.addItem(d
						.getName());
				this.devicesPanel.deviceRangePanel.lastComboBox.addItem(d
						.getName());
			}
			this.devicesPanel.deviceRangePanel.lastComboBox
					.addItem(new String());
			this.devicesPanel.deviceRangePanel.lastComboBox
					.setSelectedIndex(this.devicesPanel.deviceRangePanel.lastComboBox
							.getItemCount() - 1);
		}

		String[] labels = zPlot.getSubplotLabels();

		{
			String[] dataPlotLabels = new String[labels.length - 1];
			System.arraycopy(labels, 0, dataPlotLabels, 0,
					dataPlotLabels.length);
			this.devicePlotsPanel = new DevicePlotsPanel(dataPlotLabels);
			this.devicesPanel.devicePlotsPanelParent
					.setLayout(new BorderLayout());
			this.devicesPanel.devicePlotsPanelParent.add(this.devicePlotsPanel,
					BorderLayout.CENTER);
		}
		int i = 0;
		for (i = 0; i < labels.length - 1; i++) {
			this.devicePlotsPanel.dataPlotLabelCheckBoxes[i].setSelected(!zPlot
					.getSkippedSubplotIndices().contains(i));
		}

		boolean showBeamlineCartoon = !zPlot.getSkippedSubplotIndices()
				.contains(i);

		this.devicePlotsPanel.showCartoonCheckBox
				.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						JCheckBox source = (JCheckBox) e.getSource();
						devicesPanel.cartoonFilterPanel.setVisible(source
								.isSelected());
						devicesPanel.validate();
					}

				});
		{
			this.devicePlotsPanel.showCartoonCheckBox
					.setSelected(showBeamlineCartoon);
			this.devicesPanel.cartoonFilterPanel
					.setVisible(showBeamlineCartoon);

			ZPlotUtil.drawCartoonWidgetOnLabel(new BPMWidget(),
					this.devicesPanel.cartoonFilterPanel.bpmIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new CavityWidget(),
					this.devicesPanel.cartoonFilterPanel.cavityIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new DipoleWidget(),
					this.devicesPanel.cartoonFilterPanel.dipoleIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new DQuadWidget(),
					this.devicesPanel.cartoonFilterPanel.dQuadIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new FQuadWidget(),
					this.devicesPanel.cartoonFilterPanel.fQuadIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new LossMonitorWidget(),
					this.devicesPanel.cartoonFilterPanel.lossMonitorIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new MarkerWidget(),
					this.devicesPanel.cartoonFilterPanel.markerIconLabel);

			ZPlotUtil
					.drawCartoonWidgetOnLabel(
							new ProfileMonitorWidget(),
							this.devicesPanel.cartoonFilterPanel.profileMonitorIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new SolenoidWidget(),
					this.devicesPanel.cartoonFilterPanel.solenoidIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new ToroidWidget(),
					this.devicesPanel.cartoonFilterPanel.toroidIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new UndulatorWidget(),
					this.devicesPanel.cartoonFilterPanel.undulatorIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new WireScannerWidget(),
					this.devicesPanel.cartoonFilterPanel.wireScannerIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new XCollimatorWidget(),
					this.devicesPanel.cartoonFilterPanel.xCollimatorIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new XCorWidget(),
					this.devicesPanel.cartoonFilterPanel.xCorIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new YCollimatorWidget(),
					this.devicesPanel.cartoonFilterPanel.yCollimatorIconLabel);

			ZPlotUtil.drawCartoonWidgetOnLabel(new YCorWidget(),
					this.devicesPanel.cartoonFilterPanel.yCorIconLabel);
		}

		DevicesDataset devicesDataset = (DevicesDataset) zPlot
				.getBeamlineCartoon().getDataset();
		
		if(devicesDataset != null){
			for (i = 0; i < devicesDataset.getItemCount(0); i++) {
				Class<? extends CartoonWidget> c = ((CartoonWidget) devicesDataset
						.getDevice(i).getWidget()).getClass();
	
				JCheckBox checkBox = getCartoonWidgetCheckBox(c);
				if (checkBox.isEnabled() == false) {
					checkBox.setEnabled(true);
					checkBox.setSelected(true);
				}
			}
		}

		for (Class<? extends CartoonWidget> c : zPlot.getBeamlineCartoon()
				.getFilteredWidgets()) {
			JCheckBox checkBox = getCartoonWidgetCheckBox(c);
			checkBox.setSelected(false);
		}
	}

	protected void processSubplotsSelection() {
		ZPlot zPlot = (ZPlot) this.zPlotPanel.getChart().getPlot();

		zPlot.getSkippedSubplotIndices().clear();

		JCheckBox checkBox = null;
		int i = 0;
		for (i = 0; i < this.devicePlotsPanel.dataPlotLabelCheckBoxes.length; i++) {
			checkBox = this.devicePlotsPanel.dataPlotLabelCheckBoxes[i];
			if (!checkBox.isSelected()) {
				zPlot.getSkippedSubplotIndices().add(i);
			}
		}

		if (!this.devicePlotsPanel.showCartoonCheckBox.isSelected()) {
			zPlot.getSkippedSubplotIndices().add(i);
		}

	}

	protected void processCartoonFilter() {
		ZPlot zPlot = (ZPlot) this.zPlotPanel.getChart().getPlot();
		BeamlineCartoon beamlineCartoon = zPlot.getBeamlineCartoon();

		beamlineCartoon.getFilteredWidgets().clear();
		if (this.devicesPanel.cartoonFilterPanel.bpmCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(BPMWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.cavityCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(CavityWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.dipoleCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(DipoleWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.dQuadCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(DQuadWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.fQuadCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(FQuadWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.lossMonitorCheckBox
				.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(LossMonitorWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.markerCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(MarkerWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.profileMonitorCheckBox
				.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets()
					.add(ProfileMonitorWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.solenoidCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(SolenoidWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.toroidCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(ToroidWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.undulatorCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(UndulatorWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.wireScannerCheckBox
				.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(WireScannerWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.xCollimatorCheckBox
				.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(XCollimatorWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.xCorCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(XCorWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.yCollimatorCheckBox
				.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(YCollimatorWidget.class);
		}
		if (this.devicesPanel.cartoonFilterPanel.yCorCheckBox.isSelected() == false) {
			beamlineCartoon.getFilteredWidgets().add(YCorWidget.class);
		}
	}

	public void showPropertiesDialog() {

		JPanel p = null;
		final ChartEditor editor = ChartEditorManager
				.getChartEditor(this.zPlotPanel.getChart());

		JPanel chartEditorPanel = (JPanel) editor;

		p = (JPanel) chartEditorPanel.getComponent(0);
		JTabbedPane chartTabbedPane = (JTabbedPane) p.getComponent(0);

		p = (JPanel) chartTabbedPane.getComponentAt(chartTabbedPane
				.indexOfTab("Plot"));
		p = (JPanel) p.getComponent(0);
		JTabbedPane plotTabbedPane = (JTabbedPane) p.getComponent(0);

		JPanel plotPanel = createPlotPanel(chartTabbedPane, plotTabbedPane);

		createDevicesPanel();

		// reorganize tabs
		plotTabbedPane.insertTab("Devices", null, this.devicesPanel, null, 0);
		plotTabbedPane.insertTab("Plot", null, plotPanel, null, 0);

		String rangeAxisTabTitle = ResourceBundle.getBundle("org.jfree.chart.editor.LocalizationBundle")
				.getString("Range_Axis");
		int rangeAxisTabIndex = plotTabbedPane.indexOfTab(rangeAxisTabTitle);

		if (rangeAxisTabIndex >= 0) {
			plotTabbedPane.setSelectedIndex(rangeAxisTabIndex);

			// add subplot title to the tab title
			ZPlot zPlot = (ZPlot) this.zPlotPanel.getChart().getPlot();
			XYPlot selectedSubplot = (XYPlot) zPlot.getRangeAxis().getPlot();
			String[] subplotLabels = zPlot.getSubplotLabels();
			for (int i = 0; i < zPlot.getSubplots().size(); i++) {
				if (selectedSubplot == zPlot.getSubplot(i)) {
					plotTabbedPane.setTitleAt(rangeAxisTabIndex, String.format(
							"%s (%s)", rangeAxisTabTitle, subplotLabels[i]));
					break;
				}
			}
		}

		// clear chart editor panel
		chartEditorPanel.removeAll();

		chartEditorPanel.setLayout(new BorderLayout());
		chartEditorPanel.add(plotTabbedPane, BorderLayout.CENTER);

		Runnable applyRunnable = new Runnable() {

			public void run() {
				JFreeChart chart = getZPlotPanel().getChart();
				editor.updateChart(chart);

				ZPlot zPlot = (ZPlot) chart.getPlot();

				ValueAxis domainAxis = zPlot.getDomainAxis();
				ZPlotUtil.setZPlotAxisLook(domainAxis, domainAxis
						.getLabelPaint());
				
				ValueAxis subplotDomainAxis = null;
				for (int i = 0; i < zPlot.getSubplots().size(); i++) {
					subplotDomainAxis = zPlot.getSubplotDomainAxis(i);
					if (subplotDomainAxis != null) {
						ZPlotUtil.setZPlotAxisLook(subplotDomainAxis,
								domainAxis.getLabelPaint());
					}
				}

				ValueAxis rangeAxis = zPlot.getRangeAxis();
				if (rangeAxis != null) {
					ZPlotUtil.setZPlotAxisLook(rangeAxis, rangeAxis
							.getLabelPaint());
				}

				processDeviceSelection();
				processSubplotsSelection();
				processCartoonFilter();
			}

		};

		new PropertiesDialog(this.zPlotPanel, 
				ResourceBundleWrapper.getBundle("org.jfree.chart.LocalizationBundle").getString("Chart_Properties"),
				(JPanel) editor, applyRunnable);
	}

}
