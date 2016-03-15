package edu.stanford.slac.util.zplot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;

import edu.stanford.slac.util.zplot.model.Device;
import edu.stanford.slac.util.zplot.model.Obstruction;
import edu.stanford.slac.util.zplot.model.WidgetsRepository;

/**
 * ZPlotLineAndShapeRenderer
 * 
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public class ZPlotFeatherRenderer extends XYBarRenderer {

	/**
	 * My Field (please, document me!)
	 */
	private static final long serialVersionUID = -5114426691791064320L;

	public static final Shape DOWN_ARROW = WidgetsRepository.DEFAULT_SHAPES[5];
	public static final Shape UP_ARROW = WidgetsRepository.DEFAULT_SHAPES[2];
	public static final Paint OUT_OF_RANGE_PAINT = Color.RED;
	public static double yVal = 0;
	public static boolean showOffScale = true;
	public static Rectangle2D clipsave = null;
	public static double xpointPos = 0;
	public static double xpointNeg = 0;
	public static final Shape SHAPE = WidgetsRepository.DEFAULT_SHAPES[4];

	private DevicesDataset devicesDataset;

	private boolean baseShapesVisible;

	private void addEntity(Shape area, PlotRenderingInfo info,
			XYDataset dataset, int series, int item) {
		// tooltip area
		EntityCollection entities = null;
		if (info != null) {
			entities = info.getOwner().getEntityCollection();
		}

		if (entities != null) {
			addEntity(entities, area, dataset, series, item, -1, -1);
		}
	}

	private void markOutOfRangeValues(Graphics2D g2, Shape s, double x, double y, PlotRenderingInfo info, XYDataset dataset, int series, int item) {

		s = AffineTransform.getTranslateInstance(x, y)
				.createTransformedShape(s);

		// save clip and paint
		Rectangle2D clip = g2.getClipBounds();
		Paint p = g2.getPaint();

		g2.setClip(ZPlotUtil.extendClip(clip, 20, 1000));
		g2.setPaint(OUT_OF_RANGE_PAINT);
		g2.fill(s);
		
//		System.out.println(g2.getClipBounds().toString());
		// draw off scale values
		if (showOffScale) {
			//top left corner placement of the PV values
			int posX = (int)x-12;
			int posY = (int)y-1;
			int negX = (int)x-17;
			int negY = (int)y+11;
			
			if (yVal > 0) {
				String str = String.valueOf(yVal);
				if (xpointPos+22 > posX && xpointPos!=posX && xpointPos-posX < 22) {
					//do not draw the PV value, overlapping will occur
				}
				else {
					Font font = new Font("Serif", Font.PLAIN, 10);
					g2.setFont(font);
					g2.drawString(str, posX, posY);
					xpointPos = posX;
				}
			}
			else if (yVal < 0) {
				String str = String.valueOf(yVal);
				if (xpointNeg+22 > negX && xpointNeg!=negX && xpointNeg-negX < 22) {
					//do not draw the PV value, overlapping will occur
				}
				else {
					Font font = new Font("Serif", Font.PLAIN, 10);
					g2.setFont(font);
					g2.drawString(str, negX, negY);
					xpointNeg = negX;
				}
			}
		}

		// reset clip and paint
		g2.setClip(clip);
		g2.setPaint(p);
		
		addEntity(s, info, dataset, series, item);
	}

	private void drawItemShape(Graphics2D g2, double x, double y,
			PlotRenderingInfo info, XYDataset dataset, int series, int item) {
		Shape s = getItemShape(series, item);

		s = AffineTransform.getTranslateInstance(x, y)
				.createTransformedShape(s);
		g2.fill(s);

		addEntity(s, info, dataset, series, item);

	}

	public ZPlotFeatherRenderer() {
		this.baseShapesVisible = false;
		setBaseToolTipGenerator(new ZPlotToolTipGenerator());
	}

	public boolean getBaseShapesVisible() {
		return this.baseShapesVisible;
	}

	public void setBaseShapesVisible(boolean baseShapesVisible) {
		this.baseShapesVisible = baseShapesVisible;
	}

	@Override
	public Paint getItemPaint(int row, int column) {
		if (this.devicesDataset != null) {
			Device d = this.devicesDataset.getDevice(column);
			Paint p = d.getWidget().getColor();
			if (p != null) {
				return p;
			}
		}
		return lookupSeriesPaint(row);
	}

	@Override
	public Shape getItemShape(int row, int column) {
		if (this.devicesDataset != null) {
			Device d = this.devicesDataset.getDevice(column);
			Shape s = d.getWidget().getShape();
			if (s != null) {
				return s;
			}
		}
		return lookupSeriesShape(row);
	}

	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state,
			Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
			int series, int item, CrosshairState crosshairState, int pass) {

		double xValue = dataset.getXValue(series, item);
		if (domainAxis.getRange().contains(xValue) == false) {
			return;
		}
		double yValue = dataset.getYValue(series, item);

		// needed for getItemPaint
		if (dataset instanceof DevicesDataset) {
			this.devicesDataset = (DevicesDataset) dataset;
		} else {
			this.devicesDataset = null;
		}

		super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
				dataset, series, item, crosshairState, pass);

		// the feather is 1 pixel wide
		double x = domainAxis.valueToJava2D(xValue, dataArea, plot
				.getDomainAxisEdge()) + 0.5;
		double y = -1;

		if (getBaseShapesVisible() && rangeAxis.getRange().contains(yValue)) {
			y = rangeAxis.valueToJava2D(yValue, dataArea, plot
					.getRangeAxisEdge());
			//might not have been set in super class
			Paint itemPaint = getItemPaint(series, item);
			g2.setPaint(itemPaint);
			drawItemShape(g2, x, y, info, dataset, series, item);
			return;
		}

		if (this.devicesDataset == null
				|| this.devicesDataset.getDevice(item) instanceof Obstruction) {
			return;
		}
		// mark out of range values
		boolean markPositiveOutOfRange = rangeAxis.getUpperBound() >= 0
				&& yValue > rangeAxis.getUpperBound();
		boolean markNegativeOutOfRange = rangeAxis.getLowerBound() <= 0
				&& yValue < rangeAxis.getLowerBound();

		Shape s = null;
		if (markNegativeOutOfRange) {
			s = DOWN_ARROW;
			y = dataArea.getMaxY();
		}
		else if (markPositiveOutOfRange) {
			s = UP_ARROW;
			y = dataArea.getMinY();
		}
		else{
			//do nothing
			return;
		}
		yVal = Math.floor(yValue * 100. + 0.5) / 100.;
		markOutOfRangeValues(g2, s, x, y, info, dataset, series, item);
	}
}
