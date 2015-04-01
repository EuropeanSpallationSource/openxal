package edu.stanford.slac.util.zplot.cartoon.model.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import edu.stanford.slac.util.zplot.model.Widget;

public abstract class CartoonWidget extends Widget {

	public static final int REF_HEIGHT = 12;
	
	public static final int REF_WIDTH = 12;

	public static Rectangle2D.Double createSymmetricalRectangle(int width,
			int height) {
		if(width <= 0){
			width = 1;
		}
		return new Rectangle2D.Double(0, -height / 2, width, height);
	}

	public CartoonWidget(Color color) {
		super(color, null, null);
		// TODO Auto-generated constructor stub
	}

	public abstract Shape getShape(int length);
	
	public Shape getShape(){
		return getShape(1);
	}

}
