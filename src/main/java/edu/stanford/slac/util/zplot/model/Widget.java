package edu.stanford.slac.util.zplot.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;

public class Widget {
	private Color color;
	private final Shape shape;
	private final Stroke stroke;
	
	/**
	 * The visual representation
	 * @param color
	 * @param shape
	 * @param stroke
	 */
	public Widget(Color color, Shape shape, Stroke stroke) {
		super();
		this.color = color;
		this.shape = shape;
		this.stroke = stroke;
	}
	
	public Color getColor() {
		return color;
	}
	public void setColor(Color c) {
		color = c;
		return;
	}
	public Shape getShape() {
		return shape;
	}
	public Stroke getStroke() {
		return stroke;
	}
	
	
}
