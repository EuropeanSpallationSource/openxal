package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class WireScannerWidget extends CartoonWidget {

	private static Shape createShape() {
		Shape rect = new Rectangle2D.Double(-REF_WIDTH/2, -2 * REF_HEIGHT, REF_WIDTH,
				REF_HEIGHT);

		// clockwise starting on the left, then at the top
		Shape topTriangle = new Polygon(new int[] { -REF_WIDTH/2 + 1,
				REF_WIDTH/2 - 1, -REF_WIDTH/2 + 1 }, new int[] {
				-2 * REF_HEIGHT - 1,
				-2 * REF_HEIGHT - 1,
				-3 * REF_HEIGHT / 2 - 1 }, 3);

		Shape middleTriangle = new Polygon(
				new int[] { -REF_WIDTH/2 + 1, REF_WIDTH/2 - 1,
						REF_WIDTH/2 - 1 },
				new int[] { -3 * REF_HEIGHT / 2, -2 * REF_HEIGHT, -REF_HEIGHT },
				3);

		Shape bottomTriangle = new Polygon(new int[] { -REF_WIDTH/2  + 1,
				REF_WIDTH/2 - 1, -REF_WIDTH/2 + 1 }, new int[] {
				-3 * REF_HEIGHT / 2 + 1,
				-REF_HEIGHT + 1, -REF_HEIGHT + 1 }, 3);
		
		Shape base = new Rectangle2D.Double(-REF_WIDTH/2, -REF_HEIGHT, REF_WIDTH, 1);

		Shape stick = new Rectangle2D.Double( - 1, -REF_HEIGHT, 2,
				REF_HEIGHT);

		Area result = new Area(rect);
		result.add(new Area(stick));
		result.subtract(new Area(topTriangle));
		result.subtract(new Area(middleTriangle));
		result.subtract(new Area(bottomTriangle));
		result.add(new Area(base));
		return result;

	}

	private static final Shape SHAPE = createShape();

	public WireScannerWidget() {
		super(Color.LIGHT_GRAY);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}

}
