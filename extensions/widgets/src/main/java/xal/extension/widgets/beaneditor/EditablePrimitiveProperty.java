package xal.extension.widgets.beaneditor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.annotation.AProperty.Units;

/**
 * editable property representing a primitive that is directly editable
 */
class EditablePrimitiveProperty extends EditableProperty {

    private static final Logger LOGGER = Logger.getLogger(EditablePrimitiveProperty.class.getName());

    /**
     * property's units
     */
    private final String UNITS;

    /**
     * Constructor
     */
    protected EditablePrimitiveProperty(final String pathPrefix, final Object target, final PropertyDescriptor descriptor) {
        super(pathPrefix, target, descriptor);

        UNITS = fetchUnits();
    }

    /**
     * fetch the units
     */
    private String fetchUnits() {
        // first check to see if there is a Units annotation (ideal when known at compile time) on the accessor method and use it otherwise fallback to fetching by unit property methods
        final Method readMethod = PROPERTY_DESCRIPTOR.getReadMethod();
        final Units units = readMethod != null ? readMethod.getAnnotation(Units.class) : null;
        if (units != null) {
            return units.value();
        } else {		// unit property methods allow for dynamic units (i.e. units not known at runtime)
            // form the accessor as get<PropertyName>Units() replacing <PropertyName> with the property's name whose first character is upper case
            final char[] nameChars = getName().toCharArray();
            nameChars[0] = Character.toUpperCase(nameChars[0]);		// capitalize the first character of the name
            final String propertyName = String.valueOf(nameChars);	// property name whose first character is upper case

            // first look for a method of the form get<PropertyName>Units() taking no arguments and returning a String
            final String unitsAccessorName = "get" + propertyName + "Units";
            try {
                final Method unitsAccessor = TARGET.getClass().getMethod(unitsAccessorName);
                if (unitsAccessor.getReturnType() == String.class) {
                    return (String) unitsAccessor.invoke(TARGET);
                }
            } catch (NoSuchMethodException exception) {
                // fallback look for a method of the form getUnitsForProperty( String name ) returning a String
                try {
                    final Method unitsAccessor = TARGET.getClass().getMethod("getUnitsForProperty", String.class);
                    if (unitsAccessor.getReturnType() == String.class) {
                        return (String) unitsAccessor.invoke(TARGET, getName());
                    }
                    return "";
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException fallbackException) {
                    return "";
                }
            } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException exception) {
                LOGGER.log(Level.INFO, null, exception);
                return "";
            }

            return "";
        }
    }

    /**
     * determine whether the property is a container
     */
    @Override
    public boolean isContainer() {
        return false;
    }

    /**
     * determine whether the property is a primitive
     */
    @Override
    public boolean isPrimitive() {
        return true;
    }

    /**
     * Set the value for this property
     */
    @Override
    public void setValue(final Object value) {
        if (TARGET != null && PROPERTY_DESCRIPTOR != null) {
            final Method setter = PROPERTY_DESCRIPTOR.getWriteMethod();
            try {
                setter.invoke(TARGET, value);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot set value " + value + " on target: " + TARGET + " with descriptor: " + PROPERTY_DESCRIPTOR.getName(), exception);
            }
        } else {
            if (TARGET == null && PROPERTY_DESCRIPTOR == null) {
                throw new RuntimeException("Cannot set value " + value + " on target because both the target and descriptor are null.");
            } else if (TARGET == null) {
                throw new RuntimeException("Cannot set value " + value + " on target with descriptor: " + PROPERTY_DESCRIPTOR.getName() + " because the target is null.");
            } else if (PROPERTY_DESCRIPTOR == null) {
                throw new RuntimeException("Cannot set value " + value + " on target: " + TARGET + " because the property descriptor is null.");
            }
        }
    }

    /**
     * Get the units
     */
    @Override
    public String getUnits() {
        return UNITS;
    }

    /**
     * Get a string representation of this property
     */
    @Override
    public String toString() {
        return getPath() + ": " + getValue() + " " + getUnits();
    }
}
