package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;

public class SolenoidWidget extends CartoonWidget {

	public SolenoidWidget() {
		super(Color.GREEN);
	}

	@Override
	public Shape getShape(int length) {
		return createSymmetricalRectangle(length, REF_HEIGHT);
	}
}
