package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
				"ELEMENT_NAME", "DEVICE_PROPERTY", "DEVICE_VALUE", "ZPOS" });
		propertyType = Arrays.asList(new String[] { "String", "String", "Object", "Double" });//TODO check and set apropirately
		PROPERTY_SIZE = PROPERTY_NAME.size();
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
	public MachineModelDevice(String elementName, String deviceProperty, String deviceValue, String zpos){
	    this();
	    this.setPropertyValue("ELEMENT_NAME", elementName );
	    this.setPropertyValue("DEVICE_PROPERTY", deviceProperty);
	    this.setPropertyValue("DEVICE_VALUE", deviceValue);
	    this.setPropertyValue("ZPOS", zpos);
	}

	//About propertyName
	public static String getPropertyName(int index) {
		return PROPERTY_NAME.get(index);
	}

	public static List<String> getAllPropertyName() {
		return PROPERTY_NAME;
	}

	//About propertyType
	public static String getPropertyType(int index) {
		return propertyType.get(index);
	}
	
	public String getPropertyClass(int index){//TODO check and set apropirately
        if("ENBL".equals(getPropertyValue(1)) && index == 2){
            return "Boolean";
        }
	    return getPropertyType(index);
	    
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

	//About PropertyValue
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
