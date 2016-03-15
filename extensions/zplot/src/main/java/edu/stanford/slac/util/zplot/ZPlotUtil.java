package edu.stanford.slac.util.zplot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.JLabel;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import edu.stanford.slac.util.zplot.cartoon.CartoonWidgetIcon;
import edu.stanford.slac.util.zplot.cartoon.model.widget.CartoonWidget;

public class ZPlotUtil {

	public static int getSelectedSubplotIndex(ZPlot zPlot) {
		if (zPlot.getRangeAxis() == null) {
			return -1;
		}
		XYPlot selectedSubplot = (XYPlot) zPlot.getRangeAxis().getPlot();
		for (int i = 0; i < zPlot.getSubplots().size(); i++) {
			if (selectedSubplot == zPlot.getSubplots().get(i)) {
				return i;
			}
		}
		return -1;
	}

	public static void drawCartoonWidgetOnLabel(CartoonWidget widget,
			JLabel label) {
		label.setOpaque(true);
		label.setBackground(Color.BLACK);
		Dimension preferredSize = label.getPreferredSize();
		CartoonWidgetIcon widgetIcon = new CartoonWidgetIcon(widget,
				preferredSize.width, preferredSize.height);
		label.setIcon(widgetIcon);
	}

	public static Rectangle2D extendClip(final Rectangle2D clip, int hMargin,
			int vMargin) {
		return new Rectangle2D.Double(clip.getX() - hMargin, clip.getY()
				- vMargin, clip.getWidth() + 2 * hMargin, clip.getHeight() + 2
				* vMargin);

	}
	
	public static final void setZPlotAxisLook(ValueAxis axis, Paint paint){
		if(axis == null || paint == null){
			return;
		}
		axis.setAxisLinePaint(paint);
		axis.setLabelPaint(paint);
		axis.setTickLabelPaint(paint);
		axis.setTickMarkPaint(paint);
	}
	
	private static final int DARK_LIMIT = 0x70;
	private static final int BRIGHT_LIMIT = 0xFF - DARK_LIMIT; 
	
	public static void invertPixelIntensity(BufferedImage srcImage, BufferedImage destImage, int x, int y){
		
		int rgb = srcImage.getRGB(x, y);
		
		int red = (rgb & 0xFF0000) >> 16;
		int green = (rgb & 0x00FF00) >> 8;
		int blue = (rgb & 0x0000FF);
		
		boolean isPixelDarkEnough = red < DARK_LIMIT && green < DARK_LIMIT && blue < DARK_LIMIT;
		if(!isPixelDarkEnough){
			boolean isPixelBrightEnough = red > BRIGHT_LIMIT && green > BRIGHT_LIMIT && blue > BRIGHT_LIMIT;
			if(!isPixelBrightEnough){
				destImage.setRGB(x, y, rgb);
				return;
			}
		}
		destImage.setRGB(x, y, 0xFFFFFF - rgb);
	}
	
	public static class RangeChecker{
		
		private final LinkedList<Range> ranges;
		
		
		public RangeChecker(){
			this.ranges = new LinkedList<Range>();
		}
		
		public boolean overlapsRange(double upper, double lower){
			for(Range r : this.ranges){
				if(r.intersects(upper, lower)){
					return true;
				}
			}
			return false;
		}
		
		public void addRange(double upper, double lower){
			this.ranges.add(new Range(upper, lower));
		}
	}
}
