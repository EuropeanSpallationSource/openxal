package edu.stanford.slac.util.zplot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.editor.ChartEditor;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import edu.stanford.slac.util.zplot.cartoon.BeamlineCartoon;

/**
 * ZPlotChartPanel
 * 
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public class ZPlotPanel extends ChartPanel {

	private class ZoomHistory {
		private final LinkedList<Range> beamlineHistory;
		private final LinkedList<Range[]> dataRangeHistory;

		public ZoomHistory() {
			this.beamlineHistory = new LinkedList<Range>();
			this.dataRangeHistory = new LinkedList<Range[]>();
		}

		public void addRanges() {
			if (getChart() == null)
				return;
			if (getChart().getPlot() instanceof ZPlot == false) {
				return;
			}

			ZPlot zPlot = (ZPlot) getChart().getPlot();
			Range r = zPlot.getDomainAxis().getRange();
			this.beamlineHistory.add(new Range(r.getLowerBound(), r
					.getUpperBound()));

			Range[] dataRanges = new Range[zPlot.getSubplots().size()];
			for (int i = 0; i < dataRanges.length; i++) {
				r = zPlot.getSubplot(i).getRangeAxis().getRange();
				dataRanges[i] = new Range(r.getLowerBound(), r.getUpperBound());
			}
			this.dataRangeHistory.add(dataRanges);
		}

		public void goBack() {
			if (getChart() == null)
				return;
			if (getChart().getPlot() instanceof ZPlot == false) {
				return;
			}

			ZPlot zPlot = (ZPlot) getChart().getPlot();
			Range domainRange = this.beamlineHistory.removeLast();
			zPlot.getDomainAxis().setRange(domainRange);

			Range[] dataRanges = this.dataRangeHistory.removeLast();
			for (int i = 0; i < dataRanges.length; i++) {
				zPlot.getSubplot(i).getRangeAxis().setRange(dataRanges[i]);
			}
		}

		public boolean isEmpty() {
			return this.beamlineHistory.isEmpty();
		}
	}

	/**
	 * My Field (please, document me!)
	 */
	private static final long serialVersionUID = 4628767939941936938L;

	public static final String AUTO_COMMAND = "AUTO";
	public static final String BG_COLOR_COMMAND = "BG_COLOR";
	public static final String DEFAULT_VIEW_COMMAND = "DEFAULT_VIEW";
	public static final String DOUBLE_RANGES_COMMAND = "DOUBLE_RANGES";
	public static final String FILL_RANGES_COMMAND = "FILL_RANGES";
	public static final String HALVE_RANGES_COMMAND = "HALVE_RANGES";
	public static final String UNDO_ZOOM_COMMAND = "UNDO_ZOOM";

	private static final int TOOLTIP_SHOWN = 0;
	private static final int ZOOM_COMPLETED = TOOLTIP_SHOWN + 1;

	public static final int TOOLTIP_MARGIN_WIDTH = 2;
	public static final int TOOLTIP_MARGIN_HEIGHT = 2;

	public static boolean USE_BUFFER = true;

	public static Font TITLE_FONT = JFreeChart.DEFAULT_TITLE_FONT.deriveFont(
			Font.PLAIN).deriveFont(14f);

	private final ArrayList<ZPlotListener> zPlotListeners;
	private final ZPlotController zPlotController;
	private final ZoomHistory zoomHistory;

	private String lastToolTip;
	private boolean canUserChangeTitle;

	private ChartEditor chartEditor;
	private JCheckBoxMenuItem autoRangeModeCheckBoxMenuItem;
	private JMenuItem bgColorMenuItem;
	private JMenuItem undoZoomMenuItem;

	private void selectSubplot(int x, int y) {
		if (getChart() == null
				|| getChart().getPlot() instanceof ZPlot == false) {
			return;
		}
		getChart().getPlot().handleClick(x, y,
				getChartRenderingInfo().getPlotInfo());
	}

	private void notifyListeners(int eventType) {
		ZPlotEvent event = new ZPlotEvent(this);
		for (ZPlotListener zpl : this.zPlotListeners) {
			switch (eventType) {
			case TOOLTIP_SHOWN:
				zpl.tooltipShown(event);
				break;
			case ZOOM_COMPLETED:
				zpl.zoomCompleted(event);
				break;
			}
		}
	}

	private void applyCommandToRangeAxis(ValueAxis axis, String command) {
		Range oldRange = axis.getRange();
		if (command.equals(AUTO_COMMAND)) {
			axis.setAutoRange(this.autoRangeModeCheckBoxMenuItem.isSelected());
		} else if (command.equals(DOUBLE_RANGES_COMMAND)) {
			double lower = oldRange.getCentralValue() - oldRange.getLength();
			double upper = 0;
			upper = oldRange.getCentralValue() + oldRange.getLength();
			axis.setRange(lower, upper);
		} else if (command.equals(FILL_RANGES_COMMAND)) {
			axis.setAutoRange(true);
			axis.setAutoRange(false);
		} else if (command.equals(HALVE_RANGES_COMMAND)) {
			axis.setRange(
					oldRange.getCentralValue() - oldRange.getLength() / 4,
					oldRange.getCentralValue() + oldRange.getLength() / 4);
		}
		axisRangeModified(axis);
	}

	private ChartEntity findEntity(int x, int y) {
		EntityCollection entities = getChartRenderingInfo()
				.getEntityCollection();
		if (entities == null)
			return null;
		ChartEntity entity = null;
		for (int i = -TOOLTIP_MARGIN_WIDTH; i <= TOOLTIP_MARGIN_WIDTH; i++) {
			entity = entities.getEntity(x + i, y);
			if (entity != null) {
				return entity;
			}
		}
		for (int i = -TOOLTIP_MARGIN_HEIGHT; i <= TOOLTIP_MARGIN_HEIGHT; i++) {
			entity = entities.getEntity(x, y + i);
			if (entity != null) {
				return entity;
			}
		}
		return null;
	}

	protected void axisRangeModified(ValueAxis axis) {
		if (getChart().getPlot() instanceof ZPlot == false)
			return;
		ZPlot zPlot = (ZPlot) getChart().getPlot();
		if (axis == zPlot.getBeamlineCartoon().getRangeAxis()) {
			// anything symmetrical
			axis.setRange(-10, 10);
		}

	}

	protected ChartEditor getChartEditor() {
		return this.chartEditor;
	}

	@Override
    protected JPopupMenu createPopupMenu(boolean properties,
            boolean copy, boolean save, boolean print, boolean zoom) {
		JPopupMenu result = new JPopupMenu("ZPlot:");
		{
			JMenuItem propertiesItem = new JMenuItem("Properties...");
			propertiesItem.setActionCommand(PROPERTIES_COMMAND);
			propertiesItem.addActionListener(this);
			result.add(propertiesItem);
		}

		result.addSeparator();
		{
			JMenuItem saveItem = new JMenuItem("Save Image...");
			saveItem.setActionCommand(SAVE_COMMAND);
			saveItem.addActionListener(this);
			result.add(saveItem);
		}
		{
			JMenuItem printItem = new JMenuItem("Print...");
			printItem.setActionCommand(PRINT_COMMAND);
			printItem.addActionListener(this);
			result.add(printItem);
		}

		result.addSeparator();
		{
			JMenuItem defaultAxesItem = new JMenuItem("Default View");
			defaultAxesItem.setActionCommand(DEFAULT_VIEW_COMMAND);
			defaultAxesItem.addActionListener(this);
			result.add(defaultAxesItem);
		}

		JMenu dataRangesMenu = new JMenu("Data Ranges");
		{
			JMenuItem doubleRangeItem = new JMenuItem("Double Ranges");
			doubleRangeItem.setActionCommand(DOUBLE_RANGES_COMMAND);
			doubleRangeItem.addActionListener(this);
			dataRangesMenu.add(doubleRangeItem);
		}
		{
			// The FILL_RANGES_COMMAND menu item should say "Auto Scale" as per
			// Paul Emma
			JMenuItem fillRangeItem = new JMenuItem("Auto Scale");
			fillRangeItem.setActionCommand(FILL_RANGES_COMMAND);
			fillRangeItem.addActionListener(this);
			dataRangesMenu.add(fillRangeItem);
		}
		{
			JMenuItem halveRangeItem = new JMenuItem("Halve Ranges");
			halveRangeItem.setActionCommand(HALVE_RANGES_COMMAND);
			halveRangeItem.addActionListener(this);
			dataRangesMenu.add(halveRangeItem);
		}
		result.add(dataRangesMenu);
		{
			this.undoZoomMenuItem = new JMenuItem("Undo Zoom");
			this.undoZoomMenuItem.setActionCommand(UNDO_ZOOM_COMMAND);
			this.undoZoomMenuItem.addActionListener(this);
			result.add(this.undoZoomMenuItem);
		}
		result.addSeparator();
		{

			this.autoRangeModeCheckBoxMenuItem = new JCheckBoxMenuItem(
					"Auto-Range Mode", false);
			autoRangeModeCheckBoxMenuItem.setActionCommand(AUTO_COMMAND);
			autoRangeModeCheckBoxMenuItem.addActionListener(this);
			result.add(autoRangeModeCheckBoxMenuItem);
		}
		{
			this.bgColorMenuItem = new JMenuItem("White Background");
			this.bgColorMenuItem.setActionCommand(BG_COLOR_COMMAND);
			this.bgColorMenuItem.addActionListener(this);
			result.add(this.bgColorMenuItem);
		}
		return result;
	}

	protected void displayPopupMenu(int x, int y) {
		if (getChart() == null)
			return;
		if (getChart().getPlot() instanceof ZPlot == false) {
			return;
		}
		this.undoZoomMenuItem.setEnabled(!this.zoomHistory.isEmpty());

		super.displayPopupMenu(x, y);

		// adjust for scale
		x = (int) ((x - getInsets().left) / getScaleX());
		y = (int) ((y - getInsets().top) / getScaleY());
		selectSubplot(x, y);

	}

	/**
	 * My Constructor (please, document me!)
	 * 
	 */
	public ZPlotPanel(Container parent, ZPlot zPlot) {
		super(null, USE_BUFFER);

		this.canUserChangeTitle = true;
		this.zPlotListeners = new ArrayList<ZPlotListener>();
		this.zPlotController = new ZPlotController(this);
		this.zoomHistory = new ZoomHistory();

		setPreferredSize(parent.getPreferredSize());

		parent.removeAll();
		parent.setLayout(new BorderLayout());
		parent.add(this, BorderLayout.CENTER);

		// Title reserves space
		JFreeChart chart = new JFreeChart("Beamline Z Plot", TITLE_FONT, zPlot,
				false);

		setChart(chart);

		setZPlotColors(Color.BLACK, Color.WHITE);

	}

	public void setZPlotColors(Color bgColor, Color fgColor) {
		JFreeChart chart = getChart();

		chart.setBackgroundPaint(bgColor);
		chart.setBorderPaint(fgColor);
		chart.getTitle().setPaint(fgColor);

		ZPlot zPlot = (ZPlot) chart.getPlot();
		zPlot.setBackgroundPaint(bgColor);
		ZPlotUtil.setZPlotAxisLook(zPlot.getDomainAxis(), fgColor);
		for (int i = 0; i < zPlot.getSubplots().size(); i++) {
			XYPlot xyPlot = (XYPlot) zPlot.getSubplot(i);
			xyPlot.setNoDataMessagePaint(fgColor);
			if (xyPlot instanceof BeamlineCartoon == false) {
				xyPlot.setRangeZeroBaselinePaint(fgColor);
			}
			if (zPlot.getSubplotDomainAxis(i) != null) {
				ZPlotUtil.setZPlotAxisLook(zPlot.getSubplotDomainAxis(i),
						fgColor);
			}

			ZPlotUtil.setZPlotAxisLook(xyPlot.getRangeAxis(), fgColor);
		}

	}

	public Rectangle2D getScreenDataArea(int x, int y) {
		Rectangle2D result = super.getScreenDataArea(x, y);
		int limit = x + getSize().width;
		while (result == null && x < limit) {
			result = super.getScreenDataArea(++x, y);
		}
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		super.actionPerformed(event);
		if (getChart() == null
				|| getChart().getPlot() instanceof ZPlot == false)
			return;
		ZPlot zPlot = (ZPlot) getChart().getPlot();

		String command = event.getActionCommand();

		// handled as special case to increase performance
		if (command.equals(DEFAULT_VIEW_COMMAND)) {
			zPlot.getDomainAxis().setAutoRange(true);
			ValueAxis rangeAxis = null;
			int i = 0;
			for (Object o : zPlot.getSubplots()) {
				rangeAxis = ((XYPlot) o).getRangeAxis();
				Range r = zPlot.getDefaultVerticalRange(i++);
				if (r != null) {
					rangeAxis.setRange(r);
				}
			}
			return;
		}

		if (command.equals(BG_COLOR_COMMAND)) {
			if (this.bgColorMenuItem.getText().startsWith("Black")) {
				setZPlotColors(Color.BLACK, Color.WHITE);
				this.bgColorMenuItem.setText("White Background");
			} else {
				setZPlotColors(Color.WHITE, Color.BLACK);
				this.bgColorMenuItem.setText("Black Background");
			}
		}

		if (command.equals(UNDO_ZOOM_COMMAND)) {
			this.zoomHistory.goBack();
			return;
		}

		// range axes
		ValueAxis rangeAxis = null;
		for (Object o : zPlot.getSubplots()) {
			rangeAxis = ((XYPlot) o).getRangeAxis();
			applyCommandToRangeAxis(rangeAxis, command);
		}
	}

	@Override
	public void doEditChartProperties() {
		this.zPlotController.showPropertiesDialog();
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		if (getChartRenderingInfo() == null)
			return null;
		EntityCollection entities = getChartRenderingInfo()
				.getEntityCollection();
		if (entities == null)
			return null;
		Insets insets = getInsets();
		double x = (e.getX() - insets.left) / getScaleX();
		double y = (e.getY() - insets.top) / getScaleY();
		ChartEntity entity = findEntity((int) x, (int) y);
		if (entity == null) {
			return null;
		}

		this.lastToolTip = entity.getToolTipText();

		if (entity instanceof XYItemEntity) {
			XYItemEntity xyItemEntity = (XYItemEntity) entity;
			if (xyItemEntity.getDataset() instanceof DevicesDataset) {
				DevicesDataset devicesDataset = (DevicesDataset) xyItemEntity
						.getDataset();
				this.lastToolTip = devicesDataset.getDevice(
						xyItemEntity.getItem()).getTooltip();
			}
		}
		selectSubplot(e.getX(), e.getY());
		notifyListeners(TOOLTIP_SHOWN);
		return this.lastToolTip;
	}

	@Override
	public void zoom(Rectangle2D selection) {
		this.zoomHistory.addRanges();

		if (getChart() == null)
			return;
		if (getChart().getPlot() instanceof ZPlot == false) {
			return;
		}

		ZPlot zPlot = (ZPlot) getChart().getPlot();
		zPlot.getDomainAxis().setAutoRange(false);
		this.zoomHistory.addRanges();
		

		synchronized(zPlot){
			super.zoom(selection);
		}
		Point2D selectOrigin = translateScreenToJava2D(new Point((int) Math
				.ceil(selection.getX()), (int) Math.ceil(selection.getY())));

		XYPlot subPlot = zPlot.findSubplot(getChartRenderingInfo()
				.getPlotInfo(), selectOrigin);
		
		axisRangeModified(subPlot.getDomainAxis());
		axisRangeModified(subPlot.getRangeAxis());
		
		notifyListeners(ZOOM_COMPLETED);

	}

	public String getLastToolTip() {
		return this.lastToolTip;
	}

	/**
	 * Creates a print job for the chart.
	 */
	public void createChartPrintJob() {

		PrinterJob job = PrinterJob.getPrinterJob();
		PageFormat pf = job.defaultPage();
		pf.setOrientation(PageFormat.LANDSCAPE);
		job.setPrintable(this, pf);
		if (job.printDialog()) {
			try {
				job.print();
			} catch (PrinterException e) {
				JOptionPane.showMessageDialog(this, e);
			}
		}
	}

	public void addZPlotListener(ZPlotListener zpl) {
		this.zPlotListeners.add(zpl);
	}

	public void removeZPlotListener(ZPlotListener zpl) {
		this.zPlotListeners.remove(zpl);
	}

	public void setTitle(String text, Paint p) {
		setTitle(text);
		getChart().getTitle().setPaint(p);
	}

	public void setTitle(String text) {
		if (text == null) {
			text = new String();
		}
		getChart().setTitle(text);
		getChart().getTitle().setFont(TITLE_FONT);
	}

	public String getTitle() {
		return getChart().getTitle().getText();
	}

	public void setCanUserChangeTitle(boolean flag) {
		this.canUserChangeTitle = flag;
	}

	public boolean canUserChangeTitle() {
		return this.canUserChangeTitle;
	}

}
