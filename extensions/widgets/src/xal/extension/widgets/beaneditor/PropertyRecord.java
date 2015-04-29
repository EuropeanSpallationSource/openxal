package xal.extension.widgets.beaneditor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Wraps a property for display as a record in a table */
public class PropertyRecord {
	/** wrapped property */
	final private EditableProperty PROPERTY;

	/** current value which may be pending */
	private Object _value;

	/** indicates that this record has unsaved changes */
	private boolean _hasChanges;


	/** Constructor */
	public PropertyRecord( final EditableProperty property ) {
		PROPERTY = property;

		// initialize the value and status from the underlying property
		revert();
	}


	/** name of the property */
	public String getName() {
		return PROPERTY.getName();
	}


	/** Get the path to this property */
	public String getPath() {
		return PROPERTY.getPath();
	}


	/**  Get the label for display. */
	public String getDisplayLabel() {
		return isEditable() ? getName() : getPath();
	}


	/** Get the property type */
	public Class<?> getPropertyType() {
		return PROPERTY.getPropertyType();
	}


	/** Get the value for this property */
	public Object getValue() {
		return _value;
	}


	/** set the pending value */
	public void setValueAsObject( final Object value ) {
		if ( isEditable() ) {
			_value = value;

			// get the property's current value
			final Object propertyValue = PROPERTY.getValue();

			// if the value is really different from the property's current value then mark it as having changes
			// if the value is null then look for strict equality otherwise compare using equals
			if ( ( value == null && value != propertyValue ) || ( value != null && !value.equals( propertyValue ) )  ) {
				_hasChanges = true;
			}
			else {
				_hasChanges = false;
			}
		}
	}


	/** set the pending value */
	public void setValue( final boolean value ) {
		setValueAsObject( Boolean.valueOf( value ) );
	}


	/** Set the pending string value. Most values (except for boolean) are set as string since the table cell editor does so. */
	public void setValue( final String value ) {
		final Class<?> rawType = getPropertyType();
		if ( rawType == String.class ) {
			setValueAsObject( value );
		}
		else {
			try {
				final Class<?> type = rawType.isPrimitive() ? _value.getClass() : rawType;	// convert to wrapper type (e.g. double.class to Double.class) if necessary
				final Object objectValue = toObjectOfType( value, type );
				setValueAsObject( objectValue );
			}
			catch( Exception exception ) {
				System.err.println( "Exception: " + exception );
				System.err.println( "Error parsing the value: " + value + " as " + rawType );
			}
		}
	}


	/** Convert the string to an Object of the specified type */
	static private Object toObjectOfType( final String stringValue, final Class<?> type ) {
		try {
			// every wrapper class has a static method named "valueOf" that takes a String and returns a corresponding instance of the wrapper
			final Method converter = type.getMethod( "valueOf", String.class );
			return converter.invoke( null, stringValue );
		}
		catch ( Exception exception ) {
			throw new RuntimeException( "No match to parse string: " + stringValue + " as " + type );
		}
	}


	/** synonym for isEditable so the table model will work */
	public boolean getEditable() {
		return isEditable();
	}


	/** only primitive properties are editable */
	public boolean isEditable() {
		return PROPERTY.isPrimitive();
	}


	/** indicates whether this record has unpublished changes */
	public boolean hasChanges() {
		return _hasChanges;
	}


	/** revert to the property value if this record is editable and has unpublished changes */
	public void revertIfNeeded() {
		if ( isEditable() && hasChanges() ) {
			revert();
		}
	}


	/** revert back to the current value of the underlying property */
	public void revert() {
		// the value is only meaningful for primitive properties (only thing we want to display)
		_value = PROPERTY.isPrimitive() ? PROPERTY.getValue() : null;
		_hasChanges = false;
	}


	/** publish the pending value to the underlying property if editable and marked with unpublished changes */
	public void publishIfNeeded() {
		if ( isEditable() && hasChanges() ) {
			publish();
		}
	}


	/** publish the pending value to the underlying property */
	public void publish() {
		PROPERTY.setValue( _value );
		_hasChanges = false;
	}


	/** Get the units */
	public String getUnits() {
		return PROPERTY.getUnits();
	}


	/** Generate a flat list of records from the given property tree */
	static public List<PropertyRecord> toRecords( final EditablePropertyContainer propertyTree ) {
		final List<PropertyRecord> records = new ArrayList<>();
		appendPropertiesToRecords( propertyTree, records );
		return records;
	}


	/** append the properties in the given tree to the records nesting deeply */
	static private void appendPropertiesToRecords( final EditablePropertyContainer propertyTree, final List<PropertyRecord> records ) {
		records.add( new PropertyRecord( propertyTree ) );		// add the container itself

		// add all the primitive properties
		final List<EditablePrimitiveProperty> properties = propertyTree.getChildPrimitiveProperties();
		for ( final EditablePrimitiveProperty property : properties ) {
			records.add( new PropertyRecord( property ) );
		}

		// navigate down through each container and append their sub trees
		final List<EditablePropertyContainer> containers = propertyTree.getChildPropertyContainers();
		for ( final EditablePropertyContainer container : containers ) {
			appendPropertiesToRecords( container, records );	// add the containers descendents
		}
	}
}