package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;

public class CavityWidget extends CartoonWidget {

	public static final Color COPPER = new Color(0xC96333);

	public CavityWidget() {
		super(COPPER);
	}

	@Override
	public Shape getShape(int length) {
		return createSymmetricalRectangle(length, REF_HEIGHT);
	}

}
