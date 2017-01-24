package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;

public class UndulatorWidget extends CartoonWidget {

	public UndulatorWidget() {
		super(Color.BLUE);
	}

	@Override
	public Shape getShape(int length) {
		Shape r1 = createSymmetricalRectangle(length, REF_HEIGHT);
		Shape r2 = createSymmetricalRectangle(length, REF_HEIGHT/3);
		Area result = new Area(r1);
		result.subtract(new Area(r2));
		return result;
		
	}

}
