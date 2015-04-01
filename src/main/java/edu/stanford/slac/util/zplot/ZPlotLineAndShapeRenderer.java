package edu.stanford.slac.util.zplot;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import edu.stanford.slac.util.zplot.model.Device;
import edu.stanford.slac.util.zplot.model.Obstruction;

/**
 * ZPlotLineAndShapeRenderer
 * 
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public class ZPlotLineAndShapeRenderer extends XYLineAndShapeRenderer {

	/**
	 * My Field (please, document me!)
	 */
	private static final long serialVersionUID = -5114426691791064320L;
	private DevicesDataset devicesDataset;

	public ZPlotLineAndShapeRenderer() {
		setBaseToolTipGenerator(new ZPlotToolTipGenerator());
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
	public Stroke getItemStroke(int row, int column) {
		if (this.devicesDataset != null) {
			Device d = this.devicesDataset.getDevice(column);
			Stroke s = d.getWidget().getStroke();
			if (s != null) {
				return s;
			}
		}
		return lookupSeriesStroke(row);
	}

	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state,
			Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
			int series, int item, CrosshairState crosshairState, int pass) {
		if (dataset instanceof DevicesDataset) {
			this.devicesDataset = (DevicesDataset) dataset;
			Device d = this.devicesDataset.getDevice(item);
			if (d instanceof Obstruction) {
				if (pass == 0) {
					int x = (int) domainAxis.valueToJava2D(d.getZ(), dataArea,
							plot.getDomainAxisEdge());
					Shape area = new Rectangle2D.Double(x, dataArea.getMinY(),
							1, dataArea.getHeight());
					g2.setStroke(getItemStroke(series, item));
					g2.setPaint(getItemPaint(series, item));
					g2.fill(area);
					// tooltip area
					EntityCollection entities = null;
					if (info != null) {
						entities = info.getOwner().getEntityCollection();
					}

					if (entities != null) {
						addEntity(entities, area, dataset, series, item, -1, -1);
					}
				}
				return;
			}
		}
		super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
				dataset, series, item, crosshairState, pass);
	}

}
