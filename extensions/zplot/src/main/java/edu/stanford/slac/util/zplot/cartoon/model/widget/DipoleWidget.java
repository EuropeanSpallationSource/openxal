package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;

public class DipoleWidget extends CartoonWidget {

	public DipoleWidget() {
		super(Color.BLUE);
	}

	@Override
	public Shape getShape(int length) {
		return createSymmetricalRectangle(length, REF_HEIGHT * 2);
	}
}
