package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class FQuadWidget extends CartoonWidget {

	public FQuadWidget() {
		super(Color.RED);
	}

	@Override
	public Shape getShape(int length) {
		if(length <= 0){
			length = 1;
		}
		return new Rectangle2D.Double(0, -REF_HEIGHT * 2, length,
				REF_HEIGHT * 2);
	}

}