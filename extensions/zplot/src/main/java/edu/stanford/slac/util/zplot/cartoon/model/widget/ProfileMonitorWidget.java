package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class ProfileMonitorWidget extends CartoonWidget {

	private final static int HOLE_DIAMETER = REF_HEIGHT / 5;

	private static Shape createShape() {
		Shape screen = new Rectangle2D.Double(-REF_WIDTH/2, -2 * REF_HEIGHT, REF_WIDTH,
				REF_HEIGHT);
		Shape hole = new Ellipse2D.Double(- HOLE_DIAMETER / 2, -3
				* REF_HEIGHT / 2 - HOLE_DIAMETER / 2, HOLE_DIAMETER,
				HOLE_DIAMETER);
		Shape stick = new Rectangle2D.Double(- 1, -REF_HEIGHT, 2,
				REF_HEIGHT);
		
		Area result =  new Area(screen);
		result.add(new Area(stick));
		result.subtract(new Area(hole));
		return result;
		
	}

	public static final Shape SHAPE = createShape();

	public ProfileMonitorWidget() {
		super(Color.LIGHT_GRAY);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}
}
