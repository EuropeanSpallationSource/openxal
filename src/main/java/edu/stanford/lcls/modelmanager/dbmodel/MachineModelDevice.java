package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * Machine model device storing parameters for device.
 * 
 * @version 1.0 18 Avg 2015
 * 
 * @author unknown
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class MachineModelDevice {
	static final private List<String> PROPERTY_NAME;
	static final private int PROPERTY_SIZE;
	static final private List<String> propertyType;
	private List<Object> propertyValue;
	
	/**
	 * Static initializer 
	 */
	static {
		PROPERTY_NAME = Arrays.asList(new String[] {
				"ELEMENT_NAME", "DEVICE_PROPERTY", "DEVICE_VALUE","INITIAL_VALUE", "UNITS", "ZPOS" });
		propertyType = Arrays.asList(new String[] { "String", "String", "Double","Double","String", "Double" });
		PROPERTY_SIZE = PROPERTY_NAME.size();//6
	}
	
	/**
	 * Primary constructor
	 */	
	public MachineModelDevice(ArrayList<Object> propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	public MachineModelDevice(List<Object> propertyValue) {
		this.propertyValue = propertyValue;
	}

	public MachineModelDevice() {
		this(Arrays.asList(new Object[PROPERTY_SIZE]));
	}
	
	/**
	 * Convenience constructor setting all parameters.
	 * @param elementName String name of the element
	 * @param deviceProperty String name of device property
	 * @param deviceValue String of a double device value
	 * @param zpos String of a double device position
	 */
	public MachineModelDevice(String elementName, String deviceProperty, String deviceValue,String initialValue,String units, String zpos){
	    this();
	    this.setPropertyValue("ELEMENT_NAME", elementName );
	    this.setPropertyValue("DEVICE_PROPERTY", deviceProperty);
	    this.setPropertyValue("DEVICE_VALUE", deviceValue);
	    this.setPropertyValue("INITIAL_VALUE", initialValue);
	    this.setPropertyValue("UNITS", units);
	    this.setPropertyValue("ZPOS", zpos);
	}

	public static String getPropertyName(int index) {
		return PROPERTY_NAME.get(index);
	}

	public static List<String> getAllPropertyName() {
		return PROPERTY_NAME;
	}

	public static String getPropertyType(int index) {
		return propertyType.get(index);
	}

	public static String getPropertyType(String propertyName) {
		return getPropertyType(PROPERTY_NAME.indexOf(propertyName));
	}

	public static List<String> getAllPropertyType() {
		return propertyType;
	}
	
	public static void setPropertyType(String propertyName, String propertyType) {
		MachineModelDevice.propertyType.set(PROPERTY_NAME.indexOf(propertyName),
				propertyType);
	}

	public Object getPropertyValue(int index) {
		return propertyValue.get(index);
	}

	public Object getPropertyValue(String propertyName) {
		return getPropertyValue(PROPERTY_NAME.indexOf(propertyName));
	}

	public List<Object> getAllPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(int index, Object propertyValue) {
		this.propertyValue.set(index, propertyValue);
	}

	public void setPropertyValue(String propertyName, Object propertyValue) {
		this.propertyValue.set(PROPERTY_NAME.indexOf(propertyName),
				propertyValue);
	}

	//About propertySize
	public static int getPropertySize() {
		return PROPERTY_SIZE;
	}

}
