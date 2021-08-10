package xal.extension.widgets.beaneditor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps a property for display as a record in a table
 */
public class PropertyRecord {

    private static final Logger LOGGER = Logger.getLogger(PropertyRecord.class.getName());
    /**
     * wrapped property
     */
    private final EditableProperty property;

    /**
     * current value which may be pending
     */
    private Object value;

    /**
     * indicates that this record has unsaved changes
     */
    private boolean hasChanges;

    /**
     * Constructor
     */
    public PropertyRecord(final EditableProperty property) {
        this.property = property;

        // initialize the value and status from the underlying property
        revert();
    }

    /**
     * name of the property
     */
    public String getName() {
        return property.getName();
    }

    /**
     * Get the path to this property
     */
    public String getPath() {
        return property.getPath();
    }

    /**
     * Get the label for display.
     */
    public String getDisplayLabel() {
        return isEditable() ? getName() : getPath();
    }

    /**
     * Get the property type
     */
    public Class<?> getPropertyType() {
        return property.getPropertyType();
    }

    /**
     * Get the value for this property
     */
    public Object getValue() {
        return value;
    }

    /**
     * set the pending value
     */
    public void setValue(final Object value) {
        if (isEditable()) {
            this.value = value;

            // get the property's current value
            final Object propertyValue = property.getValue();

            // if the value is really different from the property's current value then mark it as having changes
            // if the value is null then look for strict equality otherwise compare using equals
            if ((value == null && value != propertyValue) || (value != null && !value.equals(propertyValue))) {
                hasChanges = true;
            } else {
                hasChanges = false;
            }
        }
    }

    /**
     * set the pending value
     */
    public void setValue(final boolean value) {
        setValue(Boolean.valueOf(value));
    }

    /**
     * Set the pending string value. Most values (except for boolean) are set as
     * string since the table cell editor does so.
     */
    public void setValue(final String value) {
        final Class<?> rawType = getPropertyType();
        if (rawType == String.class) {
            setValue((Object) value);
        } else {
            try {
                // convert to wrapper type (e.g. double.class to Double.class) if necessary
                final Class<?> type = rawType.isPrimitive() ? this.value.getClass() : rawType;
                final Object objectValue = toObjectOfType(value, type);
                setValue(objectValue);
            } catch (Exception exception) {
                LOGGER.log(Level.WARNING, "Exception: ", exception);
                LOGGER.log(Level.WARNING, "Error parsing the value: {0} as {1}", new Object[]{value, rawType});
            }
        }
    }

    /**
     * Convert the string to an Object of the specified type
     */
    private static Object toObjectOfType(final String stringValue, final Class<?> type) {
        try {
            // every wrapper class has a static method named "valueOf" that takes a String and returns a corresponding instance of the wrapper
            final Method converter = type.getMethod("valueOf", String.class);
            return converter.invoke(null, stringValue);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException exception) {
            throw new RuntimeException("No match to parse string: " + stringValue + " as " + type, exception);
        }
    }

    /**
     * synonym for isEditable so the table model will work
     */
    public boolean getEditable() {
        return isEditable();
    }

    /**
     * only primitive properties are editable
     */
    public boolean isEditable() {
        return property.isPrimitive();
    }

    /**
     * indicates whether this record has unpublished changes
     */
    public boolean hasChanges() {
        return hasChanges;
    }

    /**
     * revert to the property value if this record is editable and has
     * unpublished changes
     */
    public void revertIfNeeded() {
        if (isEditable() && hasChanges()) {
            revert();
        }
    }

    /**
     * revert back to the current value of the underlying property
     */
    public void revert() {
        // the value is only meaningful for primitive properties (only thing we want to display)
        value = property.isPrimitive() ? property.getValue() : null;
        hasChanges = false;
    }

    /**
     * publish the pending value to the underlying property if editable and
     * marked with unpublished changes
     */
    public void publishIfNeeded() {
        if (isEditable() && hasChanges()) {
            publish();
        }
    }

    /**
     * publish the pending value to the underlying property
     */
    public void publish() {
        property.setValue(value);
        hasChanges = false;
    }

    /**
     * Get the units
     */
    public String getUnits() {
        return property.getUnits();
    }

    /**
     * Generate a flat list of records from the given property tree
     */
    public static List<PropertyRecord> toRecords(final EditablePropertyContainer propertyTree) {
        final List<PropertyRecord> records = new ArrayList<>();
        appendPropertiesToRecords(propertyTree, records);
        return records;
    }

    /**
     * append the properties in the given tree to the records nesting deeply
     */
    private static void appendPropertiesToRecords(final EditablePropertyContainer propertyTree, final List<PropertyRecord> records) {
        // add the container itself
        records.add(new PropertyRecord(propertyTree));

        // add all the primitive properties
        final List<EditablePrimitiveProperty> properties = propertyTree.getChildPrimitiveProperties();
        for (final EditablePrimitiveProperty property : properties) {
            records.add(new PropertyRecord(property));
        }

        // navigate down through each container and append their sub trees
        final List<EditablePropertyContainer> containers = propertyTree.getChildPropertyContainers();
        for (final EditablePropertyContainer container : containers) {
            // add the containers descendents
            appendPropertiesToRecords(container, records);
        }
    }
}
