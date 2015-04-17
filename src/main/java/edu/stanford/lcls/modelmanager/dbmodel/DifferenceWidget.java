package edu.stanford.lcls.modelmanager.dbmodel;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import edu.stanford.slac.util.zplot.cartoon.model.widget.CartoonWidget;

public class DifferenceWidget extends CartoonWidget{
	public static final Shape SHAPE = new Ellipse2D.Double(-REF_HEIGHT / 4, -REF_HEIGHT / 4,
			REF_HEIGHT/2, REF_HEIGHT/2);

	public DifferenceWidget() {
		super(Color.RED);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}
}
