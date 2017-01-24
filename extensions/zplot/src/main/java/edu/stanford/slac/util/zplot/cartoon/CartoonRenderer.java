package edu.stanford.slac.util.zplot.cartoon;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

import edu.stanford.slac.util.zplot.DevicesDataset;
import edu.stanford.slac.util.zplot.ZPlot;
import edu.stanford.slac.util.zplot.ZPlotToolTipGenerator;
import edu.stanford.slac.util.zplot.cartoon.model.CartoonDevice;
import edu.stanford.slac.util.zplot.model.Device;

/**
 * ZPlotLineAndShapeRenderer
 * 
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
class CartoonRenderer extends XYLineAndShapeRenderer {

	/**
	 * My Field (please, document me!)
	 */
	private static final long serialVersionUID = -5114426691791064320L;
	// private ZPlot zPlot;
	private DevicesDataset devicesDataset;
	private double pixelPerM;
	
	protected void drawSecondaryPass(Graphics2D g2, XYPlot plot,
			XYDataset dataset, int pass, int series, int item,
			ValueAxis domainAxis, Rectangle2D dataArea, ValueAxis rangeAxis,
			CrosshairState crosshairState, EntityCollection entities) {

		Shape entityArea = null;

		// get the data point...
		double x1 = dataset.getXValue(series, item);
		double y1 = dataset.getYValue(series, item);
		if (Double.isNaN(y1) || Double.isNaN(x1)) {
			return;
		}

		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
		double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

		if (getItemShapeVisible(series, item)) {
			Shape shape = getItemShape(series, item);
			if (orientation == PlotOrientation.HORIZONTAL) {
				shape = ShapeUtilities.createTranslatedShape(shape, transY1,
						transX1);
			} else if (orientation == PlotOrientation.VERTICAL) {
				shape = ShapeUtilities.createTranslatedShape(shape, transX1,
						transY1);
			}
			entityArea = shape;
			if (shape.intersects(dataArea) == false){
				return;
			}
			
			if (getItemShapeFilled(series, item)) {
				if (getUseFillPaint()) {
					g2.setPaint(getItemFillPaint(series, item));
				} else {
					g2.setPaint(getItemPaint(series, item));
				}
				g2.fill(shape);
			}
			if (getDrawOutlines()) {
				if (getUseOutlinePaint()) {
					g2.setPaint(getItemOutlinePaint(series, item));
				} else {
					g2.setPaint(getItemPaint(series, item));
				}
				g2.setStroke(getItemOutlineStroke(series, item));
				g2.draw(shape);
			}
		}

		double xx = transX1;
		double yy = transY1;
		if (orientation == PlotOrientation.HORIZONTAL) {
			xx = transY1;
			yy = transX1;
		}

		// draw the item label if there is one...
		if (isItemLabelVisible(series, item)) {
			drawItemLabel(g2, orientation, dataset, series, item, xx, yy,
					(y1 < 0.0));
		}

		int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
		int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
		updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex,
				rangeAxisIndex, transX1, transY1, orientation);

		// add an entity for the item, but only if it falls within the data
		// area...
		if (entities != null) {
			addEntity(entities, entityArea, dataset, series, item, xx, yy);
		}
	}

	public CartoonRenderer() {
		setBaseToolTipGenerator(new ZPlotToolTipGenerator());
		setDrawOutlines(false);
	}

	public double getPixelPerM() {
		return this.pixelPerM;
	}

	public void setPixelPerM(double pixelPerM) {
		this.pixelPerM = pixelPerM;
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
			Shape s = null;
			if (d instanceof CartoonDevice) {
				CartoonDevice cd = (CartoonDevice) d;
				// Length is rounded up to fill the virtual gaps between magnets.
				int length = (int) Math.ceil(cd.getLength() * getPixelPerM() + 0.5);
				s = cd.getWidget().getShape(length);
			} else {
				s = d.getWidget().getShape();
			}
			if (s != null) {
				return s;
			}
		}
		return lookupSeriesShape(row);
	}

	@Override
	public boolean getItemVisible(int row, int column) {
		if (this.devicesDataset != null) {
			Device d = this.devicesDataset.getDevice(column);
			ZPlot zPlot = (ZPlot) ((BeamlineCartoon) getPlot()).getParent();
			if (zPlot.getBeamlineCartoon().getFilteredWidgets().contains(
					d.getWidget().getClass())) {
				return false;
			}
		}
		return super.getItemVisible(row, column);

	}

	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state,
			Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
			int series, int item, CrosshairState crosshairState, int pass) {
		if (dataset instanceof DevicesDataset) {

			this.devicesDataset = (DevicesDataset) dataset;
		}
		super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
				dataset, series, item, crosshairState, pass);
	}

	public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea,
			XYPlot plot, XYDataset data, PlotRenderingInfo info) {

		XYItemRendererState state = super.initialise(g2, dataArea, plot, data,
				info);
		state.setProcessVisibleItemsOnly(false);
		return state;

	}
}
