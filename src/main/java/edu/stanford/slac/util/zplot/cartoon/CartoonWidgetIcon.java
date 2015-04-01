package edu.stanford.slac.util.zplot.cartoon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;

import edu.stanford.slac.util.zplot.cartoon.model.widget.CartoonWidget;
import edu.stanford.slac.util.zplot.model.WidgetsRepository;

public class CartoonWidgetIcon implements Icon {

	public static int BORDER = 2;

	private final Paint paint;
	private final Shape scaledShape;
	private final int width;
	private final int height;
	private final int beamlineY;

	public CartoonWidgetIcon(CartoonWidget widget, int width, int height) {
		this.paint = widget.getColor();
		this.width = width;
		this.height = height;

		Shape originalShape = widget.getShape(width);
		Rectangle originalBounds = originalShape.getBounds();

		// scale shape
		double scaleX = (this.width - 2 * BORDER) / originalBounds.getWidth();
		// moved to the middle
		double scaleY = (this.height - 2 * BORDER) / originalBounds.getHeight();

		double scaleFactor = Math.min(scaleX, scaleY);

		// determine where to place beamline
		int scaledMinY = (int) (originalBounds.getMinY() * scaleFactor);
		int scaledMaxY = (int) (originalBounds.getMaxY() * scaleFactor);

		int iconCenterY = height / 2;
		if (Math.abs(scaledMaxY) < iconCenterY
				&& Math.abs(scaledMinY) < iconCenterY) {
			// center horizontally
			this.beamlineY = iconCenterY;
		} else {
			this.beamlineY = height - BORDER - scaledMaxY;
		}

		// move shape to x= 0, y=0
		AffineTransform transform = AffineTransform.getTranslateInstance(
				-originalBounds.x, -originalBounds.y);
		Shape shapeAt00 = transform.createTransformedShape(originalShape);

		// scale
		transform = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
		Shape scaledShapeAt00 = transform.createTransformedShape(shapeAt00);

		// move shape back
		transform = AffineTransform.getTranslateInstance(originalBounds.x
				* scaleFactor, originalBounds.y * scaleFactor);
		this.scaledShape = transform.createTransformedShape(scaledShapeAt00);

	}

	public int getIconHeight() {
		return this.height;
	}

	public int getIconWidth() {
		return this.width;
	}

	public void paintIcon(Component c, Graphics g, final int x, final int y) {
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		Rectangle r = this.scaledShape.getBounds();

		// center horizontally
		int cCenterX = c.getWidth() / 2 + x;
		int tx = cCenterX - (int) r.getCenterX();

		g2.setPaint(this.paint);

		Shape s = AffineTransform.getTranslateInstance(tx, this.beamlineY)
				.createTransformedShape(this.scaledShape);
		g2.fill(s);

		// draw beamline
		g2.setStroke(WidgetsRepository.DEFAULT_STROKES[1]);
		g2.setPaint(BeamlineCartoon.BEAMLINE_COLOR);
		g2.drawLine(x, this.beamlineY, x + this.width, this.beamlineY);
	}

}
