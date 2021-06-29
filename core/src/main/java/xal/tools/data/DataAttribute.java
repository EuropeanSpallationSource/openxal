/*
 * DataAttribute.java
 *
 * Created on May 22, 2002, 2:35 PM
 */

package xal.tools.data;

import java.util.logging.*;


/**
 * DataAttribute class
 * @author  tap
 */
public class DataAttribute {
    private static final Logger LOGGER = Logger.getLogger(DataAttribute.class.getName());
	
    private String defaultValueStr;
    private String name;
    private Class<?> type;
    private boolean isPrimaryKey;
    private Object defaultValue;

	
    /** Creates new DataAttribute */
    public DataAttribute( String aName, Class<?> aType, boolean primaryState, String defaultValue ) {
        name = aName;
        type = aType;
        isPrimaryKey = primaryState;
        defaultValueStr = defaultValue;
    }

	
    /** Creates new DataAttribute */
    public DataAttribute( String aName, Class<?> aType, boolean primaryState ) {
		this( aName, aType, primaryState, null );
    }

    
    public DataAttribute( DataAdaptor adaptor ) {
        DataListener reader = readerWriter();
        reader.update(adaptor);
    }
    
    
    public DataListener readerWriter() {
        return new ReaderWriter();
    }
    
    
    public String name() {
        return name;
    }
    
    
    public Class<?> type() {
        return type;
    }
    
    
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
	
	
	/**
	 * Get the serialized default value to assign for this attribute if a value is not specified.
	 * @return the default value for this attribute
	 */
	public String getDefaultStringValue() {
		return defaultValueStr;
	}
    
	/**
     * Get the deserialized default value to assign for this attribute if a value is not specified.
	 * @return the default value for this attribute
	 */
	public Object getDefaultValue()
	{
		if (defaultValueStr == null) return null;
		if (defaultValue == null) {
			defaultValue = GenericRecord.valueOfTypeFromString( type, defaultValueStr );
		}
		return defaultValue;
	}
    
    
    /*
     * ReaderWriter is responsible for reading and writing a DataAttribute 
     * object based on a DataAdaptor adaptor.
     */
    private class ReaderWriter implements DataListener {
        @Override
        public String dataLabel() {
            return "attribute";
        }
        
        
        @Override
        public void update( DataAdaptor adaptor ) {
            name = adaptor.stringValue("name");
            try {
                String typeName = adaptor.stringValue("type");
                type = Class.forName(typeName);
            }
            catch( ClassNotFoundException exception ) {
                LOGGER.log(Level.SEVERE, "Error during update.", exception);
            }
            
            if ( adaptor.hasAttribute("isPrimaryKey") ) {
                isPrimaryKey = adaptor.booleanValue("isPrimaryKey");
            }
            else {
                isPrimaryKey = false;
            }
			
			if ( adaptor.hasAttribute( "defaultValue" ) ) {
				defaultValueStr = adaptor.stringValue( "defaultValue" );
			}
        }
        
        
        @Override
        public void write( DataAdaptor adaptor ) {
            adaptor.setValue("name", name);
            
            String typeName = type.getName();
            adaptor.setValue("type", typeName);
            
            adaptor.setValue("isPrimaryKey", isPrimaryKey);
			
			if ( defaultValueStr != null ) {
				adaptor.setValue( "defaultValue", defaultValueStr );
			}
        }
    }
}
