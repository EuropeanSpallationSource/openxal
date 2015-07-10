package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.Arrays;
import java.util.List;

public class MachineModelDetail {
	static final private List<String> PROPERTY_NAME;
	static final private int PROPERTY_SIZE;
	static private List<String> propertyType;
	private List<Object> propertyValue;

	static {
		PROPERTY_NAME = Arrays.asList(new String[] {
				"RUNS_ID", "ELEMENT_NAME", "INDEX_SLICE_CHK", "ZPOS", "E", 
				"ALPHA_X", "ALPHA_Y", "BETA_X", "BETA_Y", "PSI_X", "PSI_Y",
				"ETA_X", "ETA_Y", "ETAP_X", "ETAP_Y",
				"R11", "R12", "R13", "R14", "R15", "R16", "R21", "R22", "R23",
				"R24", "R25", "R26", "R31", "R32", "R33", "R34", "R35", "R36",
				"R41", "R42", "R43", "R44", "R45", "R46", "R51", "R52", "R53",
				"R54", "R55", "R56", "R61", "R62", "R63", "R64", "R65", "R66",
				"LEFF", "SLEFF", "ORDINAL", "SUML", "DEVICE_TYPE", "EPICS_NAME",
				"P" , "Bmag_X", "Bmag_Y"});
		propertyType = Arrays.asList(new String[] {"Double", "String", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", 
				"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double",
				"Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "Double", "String", "String", "Double", "Double", "Double"});
		PROPERTY_SIZE = PROPERTY_NAME.size(); // 58
	}

	// Construction
	public MachineModelDetail() {
		this.propertyValue = Arrays.asList(new Object[PROPERTY_SIZE]);
	}

	// About propertyName
	public static String getPropertyName(int index) {
		return PROPERTY_NAME.get(index);
	}

	public static List<String> getAllPropertyName() {
		return PROPERTY_NAME;
	}

	// About propertyType
	public static String getPropertyType(int index) {
		return propertyType.get(index);
	}

	public static String getPropertyType(String propertyName) {
		return getPropertyType(PROPERTY_NAME.indexOf(propertyName));
	}

	public static List<String> getAllPropertyType() {
		return propertyType;
	}

	// About PropertyValue
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

	// About propertySize
	public static int getPropertySize() {
		return PROPERTY_SIZE;
	}
}
