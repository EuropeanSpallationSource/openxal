package edu.stanford.slac.util.zplot.cartoon.model;

import edu.stanford.slac.util.zplot.cartoon.model.widget.CartoonWidget;
import edu.stanford.slac.util.zplot.model.Device;
import edu.stanford.slac.util.zplot.model.Widget;

public class CartoonDevice extends Device {

	private final double length;

	/**
	 * @param name the name of this device
	 * @param zMiddle the z position of the middle of this device
	 * @param length the length of this device
	 * @param widget the appropriate widget for this device
	 */
	public CartoonDevice(String name, double zMiddle, double length, Widget widget) {
		super(name, zMiddle - length/2, 0, widget);
		this.length = length;
	}

	
	public CartoonDevice(String name, double z, Widget widget) {
		this(name, z, 0, widget);
	}

	public final double getLength() {
		return this.length;
	}
	
	

	@Override
	public CartoonWidget getWidget() {
		// TODO Auto-generated method stub
		return (CartoonWidget) super.getWidget();
	}

	public String toString() {
		return "Device name = " + getName() + "\t Z = " + getZ()
				+ "\t length = " + getLength();
	}

	public String getTooltip() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><p>");
		sb.append(String.format("%s", getName()));
		sb.append("</p>");
		sb.append("</html>");
		return sb.toString();
	}

}
