package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MachineModel {
	static final private List<String> PROPERTY_NAME;
	static final private int PROPERTY_SIZE;
	static final private List<String> propertyType;
	private List<Object> propertyValue;
	
	/**
	 * Static initializer 
	 */
	static {
		PROPERTY_NAME = Arrays.asList(new String[] {
				"ID", "RUN_ELEMENT_DATE", "RUN_SOURCE_CHK", "MODEL_MODES_ID", "COMMENTS", "DATE_CREATED", "GOLD", "REF", "SEL"});		
		propertyType = Arrays.asList(new String[]{"String", "Date", "String", "Long", "String", "Date", "String", "Boolean", "Boolean"});
		PROPERTY_SIZE = PROPERTY_NAME.size();
	}
	
	/**
	 * Primary constructor
	 */	
	
	public MachineModel(List<Object> propertyValue) {
		this.propertyValue = propertyValue;
	}

	public MachineModel() {
		this(Arrays.asList(new Object[PROPERTY_SIZE]));
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

	public static String getPropertyType(String propertyName) {
		return getPropertyType(PROPERTY_NAME.indexOf(propertyName));
	}

	public static List<String> getAllPropertyType() {
		return propertyType;
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
