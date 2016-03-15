package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class LossMonitorWidget extends CartoonWidget {

	private final static int DIAMETER = REF_WIDTH / 2;

	private static Shape createShape() {
		Shape circle = new Ellipse2D.Double(-DIAMETER / 2, -REF_HEIGHT
				- DIAMETER / 2, DIAMETER, DIAMETER);
		Shape stick = new Rectangle2D.Double(-1, -REF_HEIGHT, 2, REF_HEIGHT);

		Area result = new Area(circle);
		result.add(new Area(stick));
		return result;

	}

	public static final Shape SHAPE = createShape();

	public LossMonitorWidget() {
		super(Color.LIGHT_GRAY);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}
}
