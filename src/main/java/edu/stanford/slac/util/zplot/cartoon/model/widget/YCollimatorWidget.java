package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class YCollimatorWidget extends CartoonWidget {

	private static Shape createShape() {
		Shape rect = new Rectangle2D.Double(-REF_WIDTH / 8, -REF_HEIGHT,
				REF_WIDTH / 4, 2 * REF_HEIGHT);

		// clockwise starting on the left, then at the top
		Shape middleTrapezoid = new Polygon(new int[] { -REF_WIDTH / 8,
				REF_WIDTH / 8 + 1, REF_WIDTH / 8 + 1, -REF_WIDTH / 8 }, new int[] {
				-REF_HEIGHT / 2, -REF_HEIGHT / 4, REF_HEIGHT / 4,
				REF_HEIGHT / 2 }, 4);

		Area result = new Area(rect);
		result.subtract(new Area(middleTrapezoid));
		return result;

	}

	private static final Shape SHAPE = createShape();

	public YCollimatorWidget() {
		super(Color.LIGHT_GRAY);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}

}
