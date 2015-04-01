package edu.stanford.slac.util.zplot.cartoon;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;

import edu.stanford.slac.util.zplot.cartoon.model.widget.CartoonWidget;

public class BeamlineCartoon extends XYPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4875357169059083657L;

	public static final Color BEAMLINE_COLOR = new Color(0xFFCC00);
	
	private final HashSet<Class<? extends CartoonWidget>> filteredWidgets;

	public BeamlineCartoon() {
		super(null, new NumberAxis(), new NumberAxis(), new CartoonRenderer());
		this.filteredWidgets = new HashSet<Class<? extends CartoonWidget>>();

		getRangeAxis().setVisible(false);
		getRangeAxis().setRange(-1, 1);
		CartoonRenderer cartoonRenderer = (CartoonRenderer) getRenderer();
		cartoonRenderer.setBaseLinesVisible(false);
		
		setRangeZeroBaselinePaint(BEAMLINE_COLOR);
		
	}
	
	

	@Override
	public boolean isRangeZoomable() {
		return false;
	}
	
	



	@Override
	public void zoomRangeAxes(double lowerPercent, double upperPercent,
			PlotRenderingInfo info, Point2D source) {
		//do nothing
		return;
	}



	@Override
	public void zoomRangeAxes(double factor, PlotRenderingInfo info,
			Point2D source, boolean useAnchor) {
		//do nothing
		return;
	}


	@Override
	public boolean render(Graphics2D g2, Rectangle2D dataArea, int index,
			PlotRenderingInfo info, CrosshairState crosshairState) {
		XYItemRenderer renderer = getRenderer(index);
		if (renderer instanceof CartoonRenderer) {
			CartoonRenderer cartoonRenderer = (CartoonRenderer) renderer;
			Range range = getDomainAxis().getRange();
			double deltaPix = dataArea.getMaxX() - dataArea.getX();
			double deltaValue = range.getUpperBound() - range.getLowerBound();

			cartoonRenderer.setPixelPerM(deltaPix / deltaValue);
		}

		return super.render(g2, dataArea, index, info, crosshairState);
	}

	public HashSet<Class<? extends CartoonWidget>> getFilteredWidgets() {
		return this.filteredWidgets;
	}

}
