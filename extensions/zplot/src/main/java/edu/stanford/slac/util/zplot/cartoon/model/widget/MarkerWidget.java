package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class MarkerWidget extends CartoonWidget {

	private static final int WIDTH = 2;
	private static final int NR_DASHES = 6;
	
	private static Shape createShape() {
		Area result = new Area();
		
		Shape rect = null;
		// - - - - -
		int dashHeight = REF_HEIGHT * 2 / (2 * NR_DASHES  - 1);
		for(int i=0; i<NR_DASHES; i++){
			rect = new Rectangle2D.Double(WIDTH/2, 2 * dashHeight * i - REF_HEIGHT, WIDTH, dashHeight);
			result.add(new Area(rect));
		}
		return result;

	}

	private static final Shape SHAPE = createShape();

	public MarkerWidget() {
		super(Color.LIGHT_GRAY);
	}

	@Override
	public Shape getShape(int length) {
		return SHAPE;
	}

}
