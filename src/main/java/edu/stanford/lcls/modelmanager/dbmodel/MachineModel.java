package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MachineModel {
	static final private List<String> PROPERTY_NAME;
	static final private int PROPERTY_SIZE = 7;
	static private List<String> propertyType;
	private List<Object> propertyValue;
	
	/**
	 * Static initializer 
	 */
	static {
		PROPERTY_NAME = new ArrayList<String>(Arrays.asList(new String[] {
				"ID", "RUN_ELEMENT_DATE", "RUN_SOURCE_CHK", "MODEL_MODES_ID", "COMMENTS", "DATE_CREATED", "GOLD", "REF", "SEL" }));
		propertyType = new ArrayList<String>(PROPERTY_SIZE);
	}
	
	/**
	 * Primary constructor
	 */	
	
	public MachineModel(List<Object> propertyValue) {
		this.propertyValue = propertyValue;
	}

	public MachineModel() {
		this(new ArrayList<Object>(PROPERTY_SIZE));
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
	
	public static void addPropertyType(int index, String propertyDBType, int propertyDBSize) {		
		if(propertyDBType.equals("NUMBER"))
			propertyType.add(index, "Double");
		else if(propertyDBType.equals("DATE"))
			propertyType.add(index, "Date");
		else if(propertyDBType.equals("VARCHAR2"))
			propertyType.add(index, "String");
		else
			propertyType.add(index, "Other");
	}

	public static void setPropertyType(int index, String propertyType) {
		MachineModel.propertyType.set(index, propertyType);
	}

	public static void setPropertyType(String propertyName, String propertyType) {
		MachineModel.propertyType.set(PROPERTY_NAME.indexOf(propertyName),
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

	public void addPropertyValue(int index, Object propertyValue) {
		this.propertyValue.add(index, propertyValue);
	}
	
	public void addPropertyValue(String propertyName, Object propertyValue) {
		this.propertyValue.add(PROPERTY_NAME.indexOf(propertyName), propertyValue);
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
