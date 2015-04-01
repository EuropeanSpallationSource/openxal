package edu.stanford.slac.util.zplot.model;

import java.text.DecimalFormat;


/**
 * Device
 * 
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public class Device implements Comparable<Device> {
	private String name;
	private String area;
	private double z;
	private double y;
	private Widget widget;

	// other parameters will be added on demand

	/**
	 * @param name
	 * @param z
	 * @param y
	 */
	public Device(String name, double z, double y, Widget widget) {
		init(name, null, z, y, widget);
	}
	
	public Device(String name, String area, double z, double y, Widget widget) {
		init(name, area, z, y, widget);
	}
	
	private void init(String name, String area, double z, double y, Widget widget) {
		this.name = name;
		this.area = area;
		this.z = z;
		this.y = y;
		this.widget = widget;
	}

	public final int compareTo(Device o) {
		if (o == null) {
			// greater than null
			return 1;
		}
		return Double.compare(this.z, o.z);
	}

	public String toString() {
		return "Device name = " + getName() + "\t Z = " + getZ() + "\t Y = "
				+ getY();
	}
	
	public String getDisplayLabel() {
		return this.name;
	}

	public String getArea() {
		return this.area;
	}

	public final String getName() {
		return name;
	}

	public final double getZ() {
		DecimalFormat decForm = new DecimalFormat("#.##");
		try {
			return Double.valueOf(decForm.format(z));
		} catch (NumberFormatException e) {
			//System.out.println("ERROR: BAD Z for " + getName());
			return 0;
		}
//		return z;
	}

	public final double getY() {
		DecimalFormat decForm = new DecimalFormat("#.###");
		try {
			return Double.valueOf(decForm.format(y));
		} catch (NumberFormatException e) {
			//System.out.println("ERROR: BAD Y for " + getName());
			return 0;
		}
//		return y;
	}
	

	public Widget getWidget() {
		return this.widget;
	}	

	public String getTooltip() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><p>");
		sb.append(String.format("%s (%s : %s)", getName(), getZ(), getY()));
		sb.append("</p>");
		sb.append("</html>");
		return sb.toString();
	}
	
}
