package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;

public class XCorWidget extends CartoonWidget {

	public static final Shape SHAPE = new Polygon(new int[] { 0, REF_HEIGHT/4,
			-REF_HEIGHT/4 }, new int[] { -REF_HEIGHT/2, 0, 0 }, 3);

	public XCorWidget() {
		super(Color.YELLOW);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}
}
