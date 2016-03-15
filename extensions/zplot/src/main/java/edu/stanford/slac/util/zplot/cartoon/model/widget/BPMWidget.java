package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class BPMWidget extends CartoonWidget {
	
	public static final Shape SHAPE = new Ellipse2D.Double(-REF_HEIGHT / 4, -REF_HEIGHT / 4,
			REF_HEIGHT/2, REF_HEIGHT/2);

	public BPMWidget() {
		super(Color.CYAN);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}

}
