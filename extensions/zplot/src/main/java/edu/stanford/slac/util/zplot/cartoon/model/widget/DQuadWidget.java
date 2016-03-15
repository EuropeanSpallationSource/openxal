package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class DQuadWidget extends CartoonWidget {

	public DQuadWidget() {
		super(Color.RED);
	}

	@Override
	public Shape getShape(int length) {
		if(length <= 0){
			length = 1;
		}
		return new Rectangle2D.Double(0, 0, length,
				REF_HEIGHT * 2);
	}
}
