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
    private final String units;

    /**
     * Constructor
     */
    protected EditablePrimitiveProperty(final String pathPrefix, final Object target, final PropertyDescriptor descriptor) {
        super(pathPrefix, target, descriptor);

        units = fetchUnits();
    }

    /**
     * fetch the units
     */
    private String fetchUnits() {
        // first check to see if there is a Units annotation (ideal when known at compile time) on the accessor method and use it otherwise fallback to fetching by unit property methods
        final Method readMethod = propertyDescriptor.getReadMethod();
        final Units units = readMethod != null ? readMethod.getAnnotation(Units.class) : null;
        if (units != null) {
            return units.value();
            // unit property methods allow for dynamic units (i.e. units not known at runtime)
        } else {
            // form the accessor as get<PropertyName>Units() replacing <PropertyName> with the property's name whose first character is upper case
            final char[] nameChars = getName().toCharArray();
            // capitalize the first character of the name
            nameChars[0] = Character.toUpperCase(nameChars[0]);
            // property name whose first character is upper case
            final String propertyName = String.valueOf(nameChars);

            // first look for a method of the form get<PropertyName>Units() taking no arguments and returning a String
            final String unitsAccessorName = "get" + propertyName + "Units";
            try {
                final Method unitsAccessor = target.getClass().getMethod(unitsAccessorName);
                if (unitsAccessor.getReturnType() == String.class) {
                    return (String) unitsAccessor.invoke(target);
                }
            } catch (NoSuchMethodException exception) {
                LOGGER.log(Level.INFO, null, exception);
                // fallback look for a method of the form getUnitsForProperty( String name ) returning a String
                try {
                    final Method unitsAccessor = target.getClass().getMethod("getUnitsForProperty", String.class);
                    if (unitsAccessor.getReturnType() == String.class) {
                        return (String) unitsAccessor.invoke(target, getName());
                    }
                    return "";
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException fallbackException) {
                    LOGGER.log(Level.INFO, null, fallbackException);
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
        if (target != null && propertyDescriptor != null) {
            final Method setter = propertyDescriptor.getWriteMethod();
            try {
                setter.invoke(target, value);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot set value " + value + " on target: " + target + " with descriptor: " + propertyDescriptor.getName(), exception);
            }
        } else {
            if (target == null && propertyDescriptor == null) {
                throw new RuntimeException("Cannot set value " + value + " on target because both the target and descriptor are null.");
            } else if (target == null) {
                throw new RuntimeException("Cannot set value " + value + " on target with descriptor: " + propertyDescriptor.getName() + " because the target is null.");
            } else if (propertyDescriptor == null) {
                throw new RuntimeException("Cannot set value " + value + " on target: " + target + " because the property descriptor is null.");
            }
        }
    }

    /**
     * Get the units
     */
    @Override
    public String getUnits() {
        return units;
    }

    /**
     * Get a string representation of this property
     */
    @Override
    public String toString() {
        return getPath() + ": " + getValue() + " " + getUnits();
    }
}
