package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class ToroidWidget extends CartoonWidget {

	private final static int DIAMETER = 2;

	private static Shape createShape() {
		Shape bpm = BPMWidget.SHAPE;
		Shape hole = new Ellipse2D.Double(-DIAMETER/2, -DIAMETER/2,
				DIAMETER, DIAMETER);

		Area result = new Area(bpm);
		result.subtract(new Area(hole));
		return result;

	}

	public static final Shape SHAPE = createShape();

	public ToroidWidget() {
		super(Color.CYAN);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}
}
