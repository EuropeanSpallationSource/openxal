package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class MachineModelDetail {
	private List<Object> propertyValue;

	static final private LinkedHashMap<String, String> PROPERTY_TYPES;
	static {
		LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
		properties.put("RUNS_ID", "String");
		properties.put("ELEMENT_NAME", "String");
		properties.put("INDEX_SLICE_CHK", "Double");
		properties.put("ZPOS", "Double");
		properties.put("E", "Double");
		properties.put("ALPHA_X", "Double");
		properties.put("ALPHA_Y", "Double");
		properties.put("BETA_X", "Double");
		properties.put("BETA_Y", "Double");
		properties.put("PSI_X", "Double");
		properties.put("PSI_Y", "Double");
		properties.put("ETA_X", "Double");
		properties.put("ETA_Y", "Double");
		properties.put("ETAP_X", "Double");
		properties.put("ETAP_Y", "Double");
		// Matrix
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				properties.put("R" + Integer.toString(i+1) + Integer.toString(j+1), "Double");
			}
		}
		properties.put("LEFF", "Double");
		properties.put("SLEFF", "Double");
		properties.put("ORDINAL", "Double");
		properties.put("SUML", "Double");
		properties.put("DEVICE_TYPE", "String");
		properties.put("EPICS_NAME", "String");
		properties.put("P", "Double");
		properties.put("Bmag_X", "Double");
		properties.put("Bmag_Y", "Double");
		PROPERTY_TYPES = properties;
	}
	static final private List<String> PROPERTY_NAMES = new ArrayList<String>(PROPERTY_TYPES.keySet());
	static final int PROPERTY_SIZE = PROPERTY_NAMES.size();

	// Construction
	public MachineModelDetail() {
		this.propertyValue = Arrays.asList(new Object[PROPERTY_SIZE]);
	}

	// About propertyName
	public static String getPropertyName(int index) {
		return PROPERTY_NAMES.get(index);
	}

	public static List<String> getAllPropertyName() {
		return PROPERTY_NAMES;
	}

	// About propertyType
	public static String getPropertyType(int index) {
		return getPropertyType(getPropertyName(index));
	}

	public static String getPropertyType(String propertyName) {
		return PROPERTY_TYPES.get(propertyName);
	}

	// About PropertyValue
	public Object getPropertyValue(int index) {
		return propertyValue.get(index);
	}

	public Object getPropertyValue(String propertyName) {
		return getPropertyValue(PROPERTY_NAMES.indexOf(propertyName));
	}

	public List<Object> getAllPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(int index, Object propertyValue) {
		this.propertyValue.set(index, propertyValue);
	}

	public void setPropertyValue(String propertyName, Object propertyValue) {
		this.propertyValue.set(PROPERTY_NAMES.indexOf(propertyName),
				propertyValue);
	}

	public static int getPropertySize() {
		return PROPERTY_SIZE;
	}
}
