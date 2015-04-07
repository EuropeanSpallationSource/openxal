package edu.stanford.slac.util.zplot;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import edu.stanford.slac.util.zplot.ZPlotUtil.RangeChecker;
import edu.stanford.slac.util.zplot.cartoon.BeamlineCartoon;
import edu.stanford.slac.util.zplot.cartoon.model.CartoonDevice;
import edu.stanford.slac.util.zplot.model.Beamline;
import edu.stanford.slac.util.zplot.model.Device;

/**
 * Might be used without ZPlotPanel, but JFreeChart should not be subclassed.
 * 
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public class ZPlot extends CombinedDomainXYPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2394511170239308711L;

	public static enum RendererType {
		FEATHER, LINE
	}

	public static final String Z_AXIS_LABEL_IN_M = "Z (m)";

	public static final int BEAMLINE_LABELS_SPACE = 20;
	public static final int DEVICE_LABELS_SPACE = 60;
	public static final RectangleInsets DEFAULT_DEVICE_LABELS_INSETS = new RectangleInsets(
			DEFAULT_INSETS.getTop(), DEFAULT_INSETS.getLeft(),
			BEAMLINE_LABELS_SPACE + DEVICE_LABELS_SPACE, DEFAULT_INSETS
					.getRight());

	public static final int CARTOON_HEIGHT = 80;
	public static final int DEFAULT_GAP = 20;

	private final Range[] defaultVerticalRanges;
	private final String[] subplotLabels;
	private final ValueAxis[] subplotDomainAxes;
	private final HashSet<Integer> skippedSubplotIndices;

	private Device[] labeledDevices;
	private Beamline[] beamlines;
	private ValueAxis selectedRangeAxis;

	private void drawBeamlines(Graphics2D g2, Rectangle2D dataArea) {
		if (this.beamlines == null || this.beamlines.length < 1) {
			return;
		}
		ValueAxis domainAxis = getDomainAxis();
		ArrayList<String> beamlineLabelsToDraw = new ArrayList<String>();
		ArrayList<Float> beamlineLabelXCoords = new ArrayList<Float>();

		double minZ = domainAxis.getRange().getLowerBound();
		double maxZ = domainAxis.getRange().getUpperBound();

		// figure out which beamlines to draw where, and how much horizontal
		// space they need
		g2.setFont(domainAxis.getTickLabelFont());
		g2.setPaint(BeamlineCartoon.BEAMLINE_COLOR);

		float y = (float) (dataArea.getMaxY() - getAxisOffset()
				.calculateBottomOutset(dataArea.getHeight()));

		RangeChecker rangeChecker = new RangeChecker();
		for (Beamline b : this.beamlines) {

			if (b.getEndZ() < minZ || b.getStartZ() > maxZ) {
				// beamline completely out of range
				continue;
			}

			final int TICK_HEIGHT = 10;

			float startX = 0f;
			if (b.getStartZ() < minZ) {
				startX = (float) domainAxis.valueToJava2D(minZ, dataArea,
						RectangleEdge.BOTTOM);
			} else {
				startX = (float) domainAxis.valueToJava2D(b.getStartZ(),
						dataArea, RectangleEdge.BOTTOM);
				g2.drawLine((int) startX, (int) y, (int) startX, (int) y
						+ TICK_HEIGHT);
			}

			float endX = 0f;
			if (b.getEndZ() > maxZ) {
				endX = (float) domainAxis.valueToJava2D(maxZ, dataArea,
						RectangleEdge.BOTTOM);
			} else {
				// draw tick
				endX = (float) domainAxis.valueToJava2D(b.getEndZ(), dataArea,
						RectangleEdge.BOTTOM);
				g2.drawLine((int) endX, (int) y, (int) endX, (int) y
						+ TICK_HEIGHT);

			}

			// x here is the center of the beamline label
			String label = b.getName();
			Rectangle2D bounds = TextUtilities.getTextBounds(label, g2, g2
					.getFontMetrics());
			double labelWidth = bounds.getWidth();
			if (labelWidth > endX - startX) {
				// text doesn't fit
				continue;
			}

			// x coordinate of beamline label
			double upper = (startX + endX) / 2.0 - labelWidth / 2.0;
			double lower = upper + labelWidth;
			// check overlapping labels
			if(rangeChecker.overlapsRange(upper, lower)){
				continue;
			}
			rangeChecker.addRange(upper, lower);	

			beamlineLabelsToDraw.add(label);
			beamlineLabelXCoords.add((float)upper);
		}

		g2.setPaint(domainAxis.getTickLabelPaint());

		TextAnchor textAnchor = TextAnchor.TOP_LEFT;
		TextAnchor rotationAnchor = textAnchor;

		// + domainAxis.getTickLabelInsets().getTop());
		for (int i = 0; i < beamlineLabelsToDraw.size(); i++) {
			TextUtilities.drawRotatedString(beamlineLabelsToDraw.get(i), g2,
					beamlineLabelXCoords.get(i), y, textAnchor, 0, rotationAnchor);
		}
	}

	private void drawDeviceLabels(Graphics2D g2, Rectangle2D dataArea) {
		if (this.labeledDevices == null || this.labeledDevices.length < 1) {
			return;
		}
		if (this.beamlines == null || this.beamlines.length > 0) {
			drawBeamlines(g2, dataArea);
		}
		ValueAxis domainAxis = getDomainAxis();
		double angle = -Math.PI / 4;
		ArrayList<String> deviceLabelsToDraw = new ArrayList<String>();
		ArrayList<Float> deviceLabelXCoords = new ArrayList<Float>();

		double minZ = domainAxis.getRange().getLowerBound();
		double maxZ = domainAxis.getRange().getUpperBound();

		// figure out which labels to draw where, and how much horizontal
		// space they need
		g2.setFont(domainAxis.getTickLabelFont());
		g2.setPaint(domainAxis.getTickLabelPaint());

		RangeChecker rangeChecker = new RangeChecker();
		for (Device d : this.labeledDevices) {
			double z = d.getZ();
			if (z < minZ || z > maxZ) {
				continue;
			}

			String label = d.getDisplayLabel();
			// avoid to draw overlapping labels
			Rectangle2D bounds = TextUtilities.getTextBounds(label, g2, g2
					.getFontMetrics());
			// draw label bottom-up
			double labelWidth = bounds.getHeight();

			// x at the center of the label
			double upper = domainAxis.valueToJava2D(z, dataArea,
					RectangleEdge.BOTTOM) - labelWidth / 2.0;
			double lower = upper + labelWidth;

			// check overlapping
			if(rangeChecker.overlapsRange(upper, lower)){
				//System.out.println(String.format("u: %f l: %f", upper, lower));
				continue;
			}
			rangeChecker.addRange(upper, lower);

			deviceLabelsToDraw.add(label);
			deviceLabelXCoords.add((float)upper);
		}

		// draw labels bottom-up
		TextAnchor textAnchor = TextAnchor.TOP_RIGHT;
		TextAnchor rotationAnchor = textAnchor;

		float y = (float) (dataArea.getMaxY() - getAxisOffset()
				.calculateBottomOutset(dataArea.getHeight()))
				+ BEAMLINE_LABELS_SPACE;
		// + domainAxis.getTickLabelInsets().getTop());
		for (int i = 0; i < deviceLabelsToDraw.size(); i++) {
			TextUtilities.drawRotatedString(deviceLabelsToDraw.get(i), g2,
					deviceLabelXCoords.get(i), y, textAnchor, angle, rotationAnchor);
		}
	}

	private int getBeamlineCartoonIndex() {
		return getSubplots().size() - 1;
	}
	
	private void setSubplotDomainAxes(){
		for (int i = 0; i < getSubplots().size(); i++) {
			if (this.skippedSubplotIndices.contains(i)) {
				continue;
			}
			ValueAxis domainAxis = this.subplotDomainAxes[i];
			if (domainAxis == null) {
				continue;
			}
			domainAxis.setRange(getDomainAxis().getRange(), true, false);
			getSubplot(i).setDomainAxis(0, domainAxis, false);
		}
	}

	private void unsetSubplotDomainAxes(){
		//needs to be unset for zooming
		for (int i = 0; i < getSubplots().size(); i++) {
			getSubplot(i).setDomainAxis(0, null, false);
		}
	}
		
	@Override
	protected AxisSpace calculateAxisSpace(Graphics2D g2, Rectangle2D plotArea) {
		@SuppressWarnings("unchecked")
		List<XYPlot> subplots = (List<XYPlot>)getSubplots();
		
		boolean skipBeamlineCartoon = this.skippedSubplotIndices
				.contains(getBeamlineCartoonIndex());
		
		if (skipBeamlineCartoon) {
			for (int i = 0; i<subplots.size(); i++)
				subplots.get(i).setWeight(this.skippedSubplotIndices.contains(i) ? 0 : 1);
		} else {
			// first let's calculate the space we have
			AxisSpace space = super.calculateAxisSpace(g2, plotArea);
			Rectangle2D adjustedPlotArea = space.shrink(plotArea, null);
			
			// divide it into equal parts for visible plots without the cartoon
	        int n = subplots.size() - this.skippedSubplotIndices.size() - 1;
	        double usableSize = adjustedPlotArea.getHeight() - getGap() * n - CARTOON_HEIGHT;
	        double plotSize = usableSize / n;
	        
	        // set weights so we get the right size of the cartoon
	        for (int i = 0; i<subplots.size(); i++) {
	        	XYPlot plot = subplots.get(i);
				if (plot instanceof BeamlineCartoon)
					plot.setWeight((int)(CARTOON_HEIGHT + getGap()/2)); 
				else
					plot.setWeight(this.skippedSubplotIndices.contains(i) ? 0 : (int)plotSize);
			}
		}
		
		// (rerun) the axis space computation with new weight
		return super.calculateAxisSpace(g2, plotArea);
	}

	public ZPlot(final int nrDataPlots) {
		super(new NumberAxis(Z_AXIS_LABEL_IN_M));
		// and beamline cartoon
		int nrSubplots = nrDataPlots + 1;
		this.defaultVerticalRanges = new Range[nrSubplots];
		this.subplotLabels = new String[nrSubplots];
		this.subplotDomainAxes = new ValueAxis[nrSubplots];
		this.skippedSubplotIndices = new HashSet<Integer>();
		this.labeledDevices = null;
		this.selectedRangeAxis = null;

		for (int i = 0; i < nrDataPlots; i++) {
			XYPlot xyPlot = new XYPlot(null, null, new NumberAxis(),
					new StandardXYItemRenderer());
			add(xyPlot, 2);
			this.subplotLabels[i] = "Plot" + i;
		}

		// last, no label
		add(new BeamlineCartoon(), 1);

		AxisSpace axisSpace = new AxisSpace();
		axisSpace.setLeft(70);

		setGap(DEFAULT_GAP);
		setFixedRangeAxisSpace(axisSpace);

		NumberAxis domainAxis = (NumberAxis) getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
		domainAxis.setAxisLineStroke(new BasicStroke(2f));
		
		for (Object o : getSubplots()) {
			XYPlot xyPlot = (XYPlot) o;
			xyPlot.setDomainGridlinesVisible(false);
			xyPlot.setRangeGridlinesVisible(false);
			xyPlot.setRangeZeroBaselineVisible(true);	
			xyPlot.setOutlineVisible(false);

			NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
			rangeAxis.setAutoRangeIncludesZero(false);
		}
		
	}
	

	public void setDefaultVerticalRange(int subplotIndex, Range range,
			boolean apply) {
		this.defaultVerticalRanges[subplotIndex] = range;
		if (apply) {
			getSubplot(subplotIndex).getRangeAxis().setRange(range);
		}
	}
	
	public void setVerticalRange(int subplotIndex, Range range,
			boolean apply) {
		if (apply) {
			getSubplot(subplotIndex).getRangeAxis().setRange(range);
		}
	}
	
	public void setSubplotDomainAxis(int subplotIndex, ValueAxis domainAxis){
		this.subplotDomainAxes[subplotIndex] = domainAxis;
	}
	
	public ValueAxis getSubplotDomainAxis(int subplotIndex){
		return this.subplotDomainAxes[subplotIndex];
	}

	public Range getDefaultVerticalRange(int subplotIndex) {
		return this.defaultVerticalRanges[subplotIndex];
	}

	@Override
	public ValueAxis getRangeAxis() {
		// super.getRangeAxis() returns NULL
		return this.selectedRangeAxis;
	}

	@Override
	public String getPlotType() {
		return "ZPlot";
	}

	@Override
	public void handleClick(int x, int y, PlotRenderingInfo info) {
		super.handleClick(x, y, info);

		XYPlot plot = findSubplot(info, new Point2D.Double(x, y));
		if (plot == null || plot == getBeamlineCartoon()) {
			this.selectedRangeAxis = null;
		} else {
			this.selectedRangeAxis = plot.getRangeAxis();
		}
	}

	@Override
	public void setBackgroundPaint(Paint paint) {
		// TODO Auto-generated method stub
		super.setBackgroundPaint(paint);
		for (Object o : getSubplots()) {
			((XYPlot) o).setBackgroundPaint(paint);
		}
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
			PlotState parentState, PlotRenderingInfo info) {
		setSubplotDomainAxes();
		
		super.draw(g2, area, anchor, parentState, info);
		
		unsetSubplotDomainAxes();

		Rectangle2D dataArea = null;
		if (info == null) {
			// calculate again
			AxisSpace space = calculateAxisSpace(g2, area);
			dataArea = space.shrink(area, null);
		} else {
			dataArea = info.getDataArea();
		}
		drawDeviceLabels(g2, dataArea);
	}

	/**
	 * @param devices
	 *            may be null
	 * @param beamlines
	 *            may be null
	 */
	public void labelDevices(Device[] devices, Beamline[] beamlines) {
		this.labeledDevices = devices;
		this.beamlines = beamlines;

		if (this.labeledDevices == null) {
			setInsets(DEFAULT_INSETS);
			getDomainAxis().setVisible(true);
		} else {
			setInsets(DEFAULT_DEVICE_LABELS_INSETS);
			getDomainAxis().setVisible(false);
		}
	}

	public Device[] getLabeledDevices() {
		return this.labeledDevices;
	}

	/**
	 * @param subplot
	 * @param devices
	 * @param datasetIndex
	 *            may be -1
	 * @param rendererType
	 * @return the index of the dataset
	 */
	public int setDevices(XYPlot subplot, Device[] devices, int datasetIndex,
			RendererType rendererType) {
		DevicesDataset dataset = new DevicesDataset(devices);

		XYItemRenderer renderer = null;
		if (RendererType.FEATHER == rendererType) {
			renderer = new ZPlotFeatherRenderer();
		} else {
			renderer = new ZPlotLineAndShapeRenderer();
		}

		if (datasetIndex == -1) {
			int datasetCount = subplot.getDatasetCount();
			if (datasetCount == 1 && subplot.getDataset(0) == null) {
				datasetCount = 0;
			}
			datasetIndex = datasetCount;
		}

		synchronized (this) {
			subplot.setDataset(datasetIndex, dataset);
		}
		subplot.setRenderer(datasetIndex, renderer);
		return datasetIndex;
	}

	public void setCartoonDevices(CartoonDevice[] cartoonDevices) {
		getBeamlineCartoon().setDataset(0, new DevicesDataset(cartoonDevices));
	}

	public XYPlot getSubplot(int index) {
		return (XYPlot) getSubplots().get(index);
	}

	public void setSubplotLabel(int dataPlotIndex, String label) {
		this.subplotLabels[dataPlotIndex] = label;
	}

	public String[] getSubplotLabels() {
		return this.subplotLabels;
	}

	public HashSet<Integer> getSkippedSubplotIndices() {
		return this.skippedSubplotIndices;
	}

	public BeamlineCartoon getBeamlineCartoon() {
		return (BeamlineCartoon) getSubplot(getBeamlineCartoonIndex());
	}

}
